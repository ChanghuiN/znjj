package com.hchstudio.znjj.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.hchstudio.znjj.utils.SPUtils;

import org.json.JSONException;
import org.json.JSONObject;

import cn.jpush.android.api.JPushInterface;

/**
 * Created by admin on 16-5-8.
 * type     search 搜索节点
 * equip 控制命令
 * room 刷新界面，房间
 * context  内容
 */
public class JPushReceiver extends BroadcastReceiver {

    private static final String TAG = "JPushReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        String message = bundle.getString(JPushInterface.EXTRA_MESSAGE);
        if (message != null) {
            try {
                JSONObject messJO = new JSONObject(message);
                String type = messJO.getString("type");
                if ("search".equals(type)) {                 //节点搜索
                    Intent intent1 = new Intent("com.hchstudio.znjj.service.MainServiceReceiver");
                    intent1.putExtra("tag", 0x01);
                    context.sendBroadcast(intent1);
                    Toast.makeText(context, "正在搜索节点...", Toast.LENGTH_SHORT).show();
                } else if ("control".equals(type)) {           //远程控制
                    Intent intent1 = new Intent("com.hchstudio.znjj.service.MainServiceReceiver");
                    intent1.putExtra("tag", 0x02);
                    intent1.putExtra("content", messJO.optString("content"));
                    context.sendBroadcast(intent1);
                } else if ("room".equals(type)) {            //刷新界面
                    SPUtils.put(context, "room", messJO.optString("content"));
                    Intent intent1 = new Intent("com.hchstudio.znjj.activity.ViewReceiver");
                    intent1.putExtra("tag", 0x01);
                    context.sendBroadcast(intent1);
                } else if ("equip".equals(type)) {
                    SPUtils.put(context, "equip", messJO.optString("content"));
                    Intent intent1 = new Intent("com.hchstudio.znjj.activity.ViewReceiver");
                    intent1.putExtra("tag", 0x02);
                    context.sendBroadcast(intent1);
                }
            } catch (JSONException e) {
                Log.e(TAG, "json解析错误");
                e.printStackTrace();
            }
            Log.i(TAG, bundle.getString(JPushInterface.EXTRA_MESSAGE));
        }
    }
}
