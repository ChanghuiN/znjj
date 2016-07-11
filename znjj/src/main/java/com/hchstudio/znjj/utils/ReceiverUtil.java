package com.hchstudio.znjj.utils;

import android.app.ActivityManager;
import android.content.Context;

/**
 * Created by 奔向阳光 on 2016/4/21.
 */
public class ReceiverUtil {

    public static boolean isServiceRunning(Context context,String serviceName){
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo serviceInfo : manager.getRunningServices(Integer.MAX_VALUE)){
            if(serviceName.equals(serviceInfo.service.getClassName())){
                return true;
            }
        }
        return false;
    }
}
