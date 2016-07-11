package com.hchstudio.znjj.EMB;

/**
 * Created by admin on 16-5-1.
 */
public interface ZbCallback {

    int MSG_CONNECT_STATUS = 0x01;
    int MSG_NEW_NETWORK = 0x02;
    int MSG_GET_APP_MSG = 0x03;

    void zbcallback(int tag, Object b);
}
