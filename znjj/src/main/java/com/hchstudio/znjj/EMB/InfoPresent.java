package com.hchstudio.znjj.EMB;

import android.content.Context;
import android.util.Log;

import com.hchstudio.znjj.AppInterface;
import com.hchstudio.znjj.net.HttpClient;
import com.hchstudio.znjj.utils.SPUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Calendar;

/**
 * Created by admin on 16-5-1.
 * 温湿度传感器
 */
public class InfoPresent extends NodePresent {

    private static final String TAG = "InfoPresent";

    private int temp = 0;       //温度
    private int hum = 0;        //湿度

    public InfoPresent(Node n) {
        super(n);
    }

    @Override
    public void setup() {
//        int addr = mNode.mNetAddr;
        int cmd = 0x0002; // 改成你的指令
        super.sendRequest(cmd, new byte[]{0x01, 0x02, 0x05});
    }

    @Override
    public void setdown() {
//        int addr = mNode.mNetAddr;
        int cmd = 0x0002; // 改成你的指令
        super.sendRequest(cmd, new byte[]{0x01, 0x02, 0x00});
    }

    @Override
    public void procData(int req,int data) {
    }

    @Override
    public void procAppMsgData(final Context context, int addr, int cmd, byte[] dat) {
        int param;
        int THIValue1 = 0;
        int THIValue2 = 0;
        int hour;
        int minute;
        int second;
        if (cmd != 0x0003) return;
        if (dat.length < 3) return;

        param = ETool.builduInt(dat[0], dat[1]); // dat[0]<<8 | dat[1];
        THIValue1 = ETool.builduInt(dat[2]); // 取温度值
        THIValue2 = ETool.builduInt(dat[3]); // 取湿度值
        Log.d(TAG, "current temp : " + dat[2]);
        Log.d(TAG, "current humid : " + dat[3]);

        //温湿度超标报警
        String dev_id = (String) SPUtils.get(context,"dev_id","");
        if (THIValue1 > 30) {
            String msg = this.mNode.mNetAddr + ":温度超出正常值";
            Log.i(TAG, "procAppMsgData: " + msg);
            new HttpClient.Builder<String>()
                    .url(AppInterface.setSECENE)
                    .post()
                    .addParams("znjj_id", dev_id)
                    .addParams("type", "sendWarn")
                    .addParams("data", "当前温度为"+ THIValue1 +"℃,已超出正常值")
                    .builder()
                    .execute();
        }

        String humiture = (String) SPUtils.get(context, "dev_humiture", "");
        Log.i(TAG, "humiture---" + humiture + "id---" + SPUtils.get(context, "dev_id", "").toString());
        JSONObject jo = null;
        if (humiture != "") {
            try {
                jo = new JSONObject(humiture);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            jo = new JSONObject();
        }
        if (temp != THIValue1 || hum != THIValue2) {
            temp = THIValue1;
            hum = THIValue2;
            try {
                jo.put("temp", temp);
                jo.put("hum", hum);
                Log.i(TAG, "temphum---put");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        Calendar c = Calendar.getInstance();
        hour = c.get(Calendar.HOUR_OF_DAY);
        minute = c.get(Calendar.MINUTE);
        second = c.get(Calendar.SECOND);
        Log.i(TAG, hour + "h---" + minute);
        if ((hour == 3) && minute == 0 && second < 5 ||
                (hour == 6 && minute == 0 && second < 5) ||
                (hour == 9 && minute == 0 && second < 5) ||
                (hour == 12 && minute == 0 && second < 5) ||
                (hour == 15 && minute == 0 && second < 5) ||
                (hour == 18 && minute == 0 && second < 5) ||
                (hour == 21 && minute == 0 && second < 5)) {
            String history = jo.optString("history");
            Log.i(TAG, "temphum---history---" + history);
            if (history == "") {
                history = hour + "_" + temp + "_" + hum;
            } else {
                history = history + "." +hour + "_" + temp + "_" + hum;
                String[] his = history.split("\\.");
                Log.i(TAG, "procAppMsgData: length---" + his.length);
                if(his.length>7){
                    for (int i=0;i<7;i++){
                        if (i == 0){
                            history = his[his.length-7+i];
                        } else {
                            history = history + "." + his[his.length-7+i];
                        }
                    }
                }
            }
            try {
                Log.i(TAG, "temphum---history---put");
                jo.put("history", history);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        Log.i(TAG, "dev_humiture---" + jo.toString());
        if (!(humiture.equals(jo.toString()))) {
            Log.i(TAG, "dev_humiture---put" + jo.toString());
//            SPUtils.put(context, "dev_humiture", jo.toString());
            SPUtils.put(context, "dev_humiture", "{\"history\":\"3_30_42.6_32_44.9_35_40.12_36_39.15_34_40.18_34_41.21_30_41\",\"temp\":31,\"hum\":43}");
//            String dev_id = (String) SPUtils.get(context,"dev_id","");
            new HttpClient.Builder<String>()
                    .url(AppInterface.setSECENE)
                    .post()
                    .addParams("znjj_id",dev_id)
                    .addParams("type","dev_humiture")
                    .addParams("data", jo.toString())
                    .builder()
                    .execute();
        }
    }
}
