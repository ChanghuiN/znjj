package com.hchstudio.znjj.EMB;

import android.content.Context;
import android.util.Log;

/**
 * Created by admin on 16-4-30.
 */
public abstract class NodePresent {
    private static final String TAG = "NodePresent";

    public Node mNode;

    /**
     * 工厂设计模式 根据结点类型创建实例,协调器，结点
     *
     * @param n
     * @return
     */
    public static NodePresent FactoryCreateInstance(Node n) {
        Log.i(TAG, "FactoryCreateInstance---type---" + n.mDevType);
        if (n.mDevType == Node.DEV_InforTemp) {
            Log.i(TAG, "DEV_InforTemp");
            return new InfoPresent(n);
        } else if (n.mDevType == Node.DEV_LIGHT) {
            Log.i(TAG, "DEV_LIGHT");
            return new LightPresent(n);
        } else if (n.mDevType == Node.DEV_InfrarCon) {
            Log.i(TAG, "DEV_InfrarCon");
            return new InfrarConPresent(n);
        } else if (n.mDevType == Node.DEV_InFRARED) {
            Log.i(TAG, "DEV_InFRARED");
            return new InfraredPresent(n);
        } else if (n.mDevType == Node.DEV_InforFire) {
            Log.i(TAG, "DEV_InforFire");
            return new InforFirePresent(n);
        }else {
            return null;
        }
    }

    public NodePresent(Node n) {
        mNode = n;
    }

    /**
     * 发送命令请求
     *
     * @param cmd
     * @param dat
     */
    void sendRequest(int cmd, byte[] dat) {
        byte[] data = new byte[dat.length + 4]; // 在dat前面再加上4个字节
        data[0] = (byte) (mNode.mNetAddr >> 8); // data[0]保存该节点网络地址的前24位
        data[1] = (byte) mNode.mNetAddr; // data[1]保存该结点的网络地址
        data[2] = (byte) (cmd >> 8); // data[2]保存该节点命令的前24位
        data[3] = (byte) cmd; // data[3]保存该节点命令

        for (int i = 0; i < dat.length; i++)
            data[4 + i] = dat[i]; // 保存dat的信息
        ZigBeeTool.getInstance().mZbThread.requestAppMessage(2, data);
    }

    void sendRequest(int addr, int cmd, byte[] dat) {
        byte[] data = new byte[dat.length + 4]; // 在dat前面再加上4个字节
        data[0] = (byte) (addr >> 8); // data[0]保存该节点网络地址的前24位
        data[1] = (byte) addr; // data[1]保存该结点的网络地址
        data[2] = (byte) (cmd >> 8); // data[2]保存该节点命令的前24位
        data[3] = (byte) cmd; // data[3]保存该节点命令

        for (int i = 0; i < dat.length; i++)
            data[4 + i] = dat[i]; // 保存dat的信息
        ZigBeeTool.getInstance().mZbThread.requestAppMessage(2, data);
    }

    // 抽象方法，在子类中实现访问，交互
    public abstract void setup();

    public abstract void setdown();

    public abstract void procData(int req, int data);

    //返回数据
    public abstract void procAppMsgData(Context context, int addr, int cmd, byte[] dat);
}
