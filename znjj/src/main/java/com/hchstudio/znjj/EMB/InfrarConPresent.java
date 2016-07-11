package com.hchstudio.znjj.EMB;

import android.content.Context;

/**
 * Created by admin on 16-5-1.
 * 红外遥控 节点  0x10
 * 设定遥控号    0x1002 (0-999)
 * 开关         0x1003 (0x00/0x01)
 * 空调模式     0x1004 (0x00.自动 0x01.制冷 0x02.降温 0x03.送风 0x04.制暖)
 * 设定温度     0x1005 (16-31度)(0x10-0x1e)
 */
public class InfrarConPresent extends NodePresent {

    private static final String TAG = "InfrarConPresent";

    public static final int SETING_NUM = 0x100200;      //设置遥控号
    public static final int POWER_ON = 0x100301;        //开
    public static final int POWER_OFF = 0x100300;       //关
    public static final int MODEL_AUTO = 0x100400;      //自动模式 no
    public static final int MODEL_REFRI = 0x100401;     //制冷模式 yes
    public static final int MODEL_COOL = 0x100402;      //除湿模式 no
    public static final int MODEL_BLOW = 0x100403;      //送风模式 no
    public static final int MODEL_HEAT = 0x100404;      //制暖模式 yes
    public static final int SETTING_TEMP = 0x100500;         //设定温度

    public InfrarConPresent(Node n) {
        super(n);
    }

    @Override
    public void setup() {
//        int cmd = 0x0001; // 改成你的指令
//        super.sendRequest(cmd, new byte[] { 0x10, 0x02, 0x10, 0x03, 0x10, 0x04, 0x10, 0x05 });
    }

    @Override
    public void setdown() {

    }

    @Override
    public void procData(int req, int data) {
        int cmd = 0x0002;
        switch (req) {
            case SETING_NUM:
                super.sendRequest(cmd, new byte[]{0x10, 0x02, (byte)data});
                break;
            case POWER_ON:
                super.sendRequest(cmd, new byte[]{0x10, 0x03, 0x01});
                break;
            case POWER_OFF:
                super.sendRequest(cmd, new byte[]{0x10, 0x03, 0x00});
                break;
            case MODEL_AUTO:
                super.sendRequest(cmd, new byte[]{0x10, 0x04, 0x00});
                break;
            case MODEL_REFRI:
                super.sendRequest(cmd, new byte[]{0x10, 0x04, 0x01});
                break;
            case MODEL_COOL:
                super.sendRequest(cmd, new byte[]{0x10, 0x04, 0x02});
                break;
            case MODEL_BLOW:
                super.sendRequest(cmd, new byte[]{0x10, 0x04, 0x03});
                break;
            case MODEL_HEAT:
                super.sendRequest(cmd, new byte[]{0x10, 0x04, 0x04});
                break;
            case SETTING_TEMP:
                super.sendRequest(cmd, new byte[]{0x10, 0x05, (byte) data});
                break;
        }
    }

    @Override
    public void procAppMsgData(Context context, int addr, int cmd, byte[] dat) {

    }
}
