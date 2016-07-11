package com.hchstudio.znjj.EMB;

import android.content.Context;
import android.util.Log;

import com.hchstudio.znjj.App;
import com.hchstudio.znjj.AppInterface;
import com.hchstudio.znjj.net.HttpClient;
import com.hchstudio.znjj.utils.SPUtils;

import java.util.ArrayList;

/**
 * Created by admin on 16-5-2.
 * 继电器
 */
public class LightPresent extends NodePresent {

    private static final String TAG = "LightPresent";

    public static final int LINGHT_ZERO = 0x00;
    public static final int LINGHT_ADD = 0x02;
    public static final int LINGHT_SUB = 0x01;

    private int[] linghts = new int[4]; // 标识这盏灯的状态，0断开，1开启
    private int state = 1;

    public LightPresent(Node n) {
        super(n);
    }

    @Override
    public void setup() {
        int cmd = 0x0001; // 改成你的指令
        super.sendRequest(cmd, new byte[] { 0x03, 0x02, 0x03, 0x03, 0x03, 0x04, 0x03, 0x05 });
    }

    @Override
    public void setdown() {

    }

    @Override
    public void procData(int req,int data) {
        int cmd = 0x0002;
        switch (req){
            case LINGHT_ZERO:
                super.sendRequest(cmd, new byte[] { 0x03, 0x02, 0x00, 0x03, 0x03, 0x00, 0x03, 0x04, 0x00, 0x03, 0x05, 0x00});
                break;
            case LINGHT_ADD:
                super.sendRequest(cmd, new byte[] { 0x03, (byte) (state+1), 0x01});
                break;
            case LINGHT_SUB:
                super.sendRequest(cmd, new byte[] { 0x03, (byte) state, 0x00});
        }
    }

    @Override
    public void procAppMsgData(Context context, int addr, int cmd, byte[] dat) {
        Log.i(TAG,"linght---procAppMsgData---");
        int pid;
        if (cmd == 0x8001 && dat[0] == 0) {// 读参数响应
            for (int i = 1; i < dat.length; /* i+=2 */) {
                pid = ETool.builduInt(dat[i], dat[i + 1]);
                i += 2;
                if (pid == 0x0302) {
                    linghts[0] = dat[i];
                } else if (pid == 0x0303) {
                    linghts[1] = dat[i];
                } else if (pid == 0x0304) {
                    linghts[2] = dat[i];
                } else if (pid == 0x0305) {
                    linghts[3] = dat[i];
                } else {
                    i++;
                    continue;
                }
                Log.i(TAG,"dat0001---" + dat[i]);
                i++;
            }
        }
        if (cmd == 0x0003) {
            for (int i = 0; i < dat.length-1; /* i+=2 */) {
                pid = ETool.builduInt(dat[i], dat[i + 1]);
                i += 2;
                Log.i(TAG,"dat0003---" + dat[i]);
                if (pid == 0x0302) {
                    linghts[0] = dat[i];
                } else if (pid == 0x0303) {
                    linghts[1] = dat[i];
                } else if (pid == 0x0304) {
                    linghts[2] = dat[i];
                } else if (pid == 0x0305) {
                    linghts[3] = dat[i];
                } else {
                    i++;
                    continue;
                }
                i++;
            }
        }
        for (int i=3;i>=0;i--){
            Log.i(TAG,"linght---" + linghts[i]);
            if(linghts[i] == 1) {
                state = i+2;
                Log.i(TAG,"linght---state---"+state);
                break;
            } else {
                state = 1;
            }
        }
        String res = "";
        for (int i=0;i<linghts.length;i++){
            res += linghts[i];
        }
        Log.i(TAG,"linght---state-----"+res);
        super.mNode.state = res;
        ArrayList<NodePresent> nodePresents = App.nodePresents;
        ArrayList<Node> nodes = new ArrayList<Node>();
        for (int i=0;i<nodePresents.size();i++){
            nodes.add(nodePresents.get(i).mNode);
        }
        String ndChildStr = Node.toChildStr(nodes);
        SPUtils.put(context, "ndChild", ndChildStr);
        String dev_id = (String) SPUtils.get(context,"dev_id","");
        new HttpClient.Builder<String>()
                .url(AppInterface.setSECENE)
                .post()
                .addParams("znjj_id",dev_id)
                .addParams("type","initnode")
                .addParams("data", ndChildStr)
                .builder()
                .execute();
    }
}
