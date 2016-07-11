package com.hchstudio.znjj.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import com.hchstudio.znjj.App;
import com.hchstudio.znjj.AppInterface;
import com.hchstudio.znjj.EMB.ETool;
import com.hchstudio.znjj.EMB.Node;
import com.hchstudio.znjj.EMB.NodePresent;
import com.hchstudio.znjj.EMB.ZbCallback;
import com.hchstudio.znjj.EMB.ZbLink;
import com.hchstudio.znjj.EMB.ZigBeeTool;
import com.hchstudio.znjj.net.HttpClient;
import com.hchstudio.znjj.utils.SPUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cn.jpush.android.api.JPushInterface;

/**
 * Created by 奔向阳光 on 2016/4/21.
 */
public class MainService extends Service implements ZbCallback {

    public static final String TAG = "MainService";

    private ZigBeeTool mZigBeeTool;
    private MainServiceReceiver mServiceReceiver;
    private ArrayList<NodePresent> nodePresents;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Log.i(TAG, "onCreate---");
        super.onCreate();
        new Thread() {
            @Override
            public void run() {
                Looper.prepare();
                nodePresents = ((App)getApplication()).nodePresents;
                JPushInterface.setDebugMode(true);
                JPushInterface.init(MainService.this);
                String jpushID = JPushInterface.getRegistrationID(MainService.this);
                new HttpClient.Builder<String>()
                        .url(AppInterface.setSECENE)
                        .post()
                        .addParams("type","add_dev")
                        .addParams("data", jpushID)
                        .builder()
                        .execute(new HttpClient.HttpCallback<String>() {
                            @Override
                            public void onResponse(String response) {
                                SPUtils.put(MainService.this, "dev_id", response);
                            }

                            @Override
                            public void onFailure(IOException e) {

                            }
                        });
                mZigBeeTool = ZigBeeTool.getInstance(Looper.myLooper(), MainService.this);
                connectTo();
                IntentFilter intentFilter = new IntentFilter();
                intentFilter.addAction("com.hchstudio.znjj.service.MainServiceReceiver");
                mServiceReceiver = new MainServiceReceiver();
                registerReceiver(mServiceReceiver, intentFilter);
                initNode();
                Looper.loop();
            }
        }.start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand---");
        return super.onStartCommand(intent, flags, startId);
    }

    //连接zigbee
    private void connectTo() {
        if (mZigBeeTool.getConnectStatus() != ZbLink.CONNECT_CONNECTED) {
            mZigBeeTool.connectToserver();
        }
    }

    //初始化节点
    private void initNode() {
        String ndChildStr = (String) SPUtils.get(this, "ndChild", "");
        Log.i(TAG, "ndChildStr---" + ndChildStr);
        List<Node> ndChilds = Node.fromChildStr(ndChildStr);
        for (NodePresent np : nodePresents) {
            np.setdown();
        }
        nodePresents.clear();
        if (ndChilds != null) {
            for (Node nd : ndChilds) {
                Log.i(TAG, "mNodeType---" + nd.mDevType + "---" + nd.mNetAddr);
                NodePresent np = NodePresent.FactoryCreateInstance(nd);
                if (np != null) {
                    nodePresents.add(np);
                    np.setup();
                }
            }
        } else {
            Log.i(TAG, "ndChilds---null");
        }
    }

    //zigbee回调函数---并更新数据，更新界面
    @Override
    public void zbcallback(int tag, Object b) {
        switch (tag) {
            case ZbCallback.MSG_CONNECT_STATUS:
                break;
            case ZbCallback.MSG_NEW_NETWORK:
                if (b != null) {
                    Node nd = (Node) b;
                    Log.i(TAG, "_childNode---" + nd._childNode.size());
                    String ndChildStr = nd.toChildStr();
                    SPUtils.put(this, "ndChild", ndChildStr);
                    String dev_id = (String) SPUtils.get(MainService.this,"dev_id","");
                    new HttpClient.Builder<String>()
                            .url(AppInterface.setSECENE)
                            .post()
                            .addParams("znjj_id",dev_id)
                            .addParams("type","initnode")
                            .addParams("data", ndChildStr)
                            .builder()
                            .execute();
                    initNode();
                    //succeed
                } else {
                    connectTo();
                    //failure
                }
                break;
            case ZbCallback.MSG_GET_APP_MSG:        //zigbee接受数据处理
                byte[] dat = (byte[]) b;
                if (dat == null)
                    return;
                Log.d(TAG, "APP MSG :" + ETool.byteTostring(dat));
                if (dat == null || dat.length <= 4) {
                    Log.d(TAG, "APP MSG timeout or package error.");
                    return;
                }
                // int addr = 0xffff & ((dat[0]<<8) | (0xff&dat[1]));
                int addr = ETool.builduInt(dat[0], dat[1]); // 从dat中提取addr信息
                // int cmd = 0xffff & ((dat[2]<<8) | (0xff&dat[3]));
                int cmd = ETool.builduInt(dat[2], dat[3]); // 从dat中提取cmd信息

                byte[] data = new byte[dat.length - 4];
                for (int i = 0; i < data.length; i++)
                    data[i] = dat[4 + i]; // 新字节数组，将原来的数据还原
                for (NodePresent np : nodePresents) {
                    if (np.mNode.mNetAddr == addr) {
                        np.procAppMsgData(MainService.this, addr, cmd, data);
                    }
                }
                break;
        }
    }

    int i = 1;

    //接受者---被主界面控制的
    class MainServiceReceiver extends BroadcastReceiver {

        /**
         * Default                0x00
         * 控制家电                0x01
         *
         * @param context
         * @param intent
         */
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i("TAG", "onReceive---" + intent.getIntExtra("tag", 0x00));
            switch (intent.getIntExtra("tag", 0x00)) {
                case 0x00:
                    break;
                case 0x01:
                    mZigBeeTool.requestSerachNetWrok();
                    break;
                case 0x02:
                    String content = intent.getStringExtra("content");
                    try {
                        JSONObject jo = new JSONObject(content);
                        for (NodePresent np:nodePresents){
                            if (np.mNode.mNetAddr == jo.optInt("equip_netaddr")){
                                np.procData(jo.optInt("equip_cmd"),jo.optInt("equip_data",0));
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
//                    Log.i(TAG, "i---" + i);
//                    for (NodePresent np : nodePresents) {
//                        np.procData(0x01);
//                        i++;
//                    }
//                    if (i == 5) i = 0;
                    break;
            }
        }
    }

    ;

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy---");
        super.onDestroy();
        for (NodePresent np : nodePresents) {
            np.setdown();
        }
        unregisterReceiver(mServiceReceiver);
    }
}