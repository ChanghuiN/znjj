package com.hchstudio.znjj.EMB;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

/**
 * Created by 奔向阳光 on 2016/3/30.
 */
public class ZigBeeTool {

    private static final String TAG = "ZigBeeTool";

    public ZbThread mZbThread;
    private static ZigBeeTool mInstance;
    private ZbCallback mZbCallback;

    private ZigBeeTool(Looper looper,ZbCallback zbCallback){
        UiHandler handler = new UiHandler(looper);
        mZbThread = new ZbThread(handler);
        this.mZbCallback = zbCallback;
    }

    public static ZigBeeTool getInstance(Looper looper,ZbCallback zbCallback){
        if(mInstance == null){
            synchronized(ZigBeeTool.class){
                if(mInstance == null){
                    mInstance = new ZigBeeTool(looper,zbCallback);
                }
            }
        }
        return mInstance;
    }

    public static ZigBeeTool getInstance(){
        return mInstance;
    }

    public void connectToserver(){
        mZbThread.requestConnect("127.0.0.1", 8320);
    }

    public void requestSerachNetWrok(){
        mZbThread.requestSerachNetWrok();
    }

    public int getConnectStatus(){
        return mZbThread.getConnectStatus();
    }

    /**
     * 处理消息
     *
     * @author Administrator
     *
     */
    class UiHandler extends Handler {

        public UiHandler(Looper looper){
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ZbThread.MSG_CONNECT_STATUS:
                    Log.i(TAG, "UiHandler---MSG_CONNECT_STATUS---");
//                    onConnectChange(msg.arg1);
                    break;
                case ZbThread.MSG_NEW_NETWORK:
                    Log.i(TAG, "UiHandler---MSG_NEW_NETWORK---");
//                    onMsgNetwork(msg.arg1);
                    int arg1 = msg.arg1;
                    Object nd = msg.obj;
                    if(arg1 == 0){
                        mZbCallback.zbcallback(ZbCallback.MSG_NEW_NETWORK,nd);
                    }else{
                        mZbCallback.zbcallback(ZbCallback.MSG_NEW_NETWORK,null);
                    }
                    break;
                case ZbThread.MSG_CONNECT_DATA:
                    Log.i(TAG, "UiHandler---MSG_CONNECT_DATA---");
//                    byte[] dat = (byte[]) msg.obj;
                    if (msg.arg1 == 0x6980) { /* app msg */
//                    onResponseMSG_GET_APP_MSG(dat);
                        mZbCallback.zbcallback(ZbCallback.MSG_GET_APP_MSG,msg.obj);
                    }
                    break;
                case ZbThread.MSG_GET_APP_MSG:
                    Log.i(TAG, "UiHandler---MSG_GET_APP_MSG---");
//                    byte[] dat2 = (byte[]) msg.obj;
//                    onResponseMSG_GET_APP_MSG(dat2);
                    mZbCallback.zbcallback(ZbCallback.MSG_GET_APP_MSG,msg.obj);
                    break;
            }
        }
    }
}
