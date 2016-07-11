package com.hchstudio.znjj.EMB;

import android.content.Context;
import android.util.Log;

import com.hchstudio.znjj.AppInterface;
import com.hchstudio.znjj.net.HttpClient;
import com.hchstudio.znjj.utils.SPUtils;

/**
 * Created by hch on 16-6-7.
 * 可燃气体传感器
 */
public class InforFirePresent extends NodePresent {

    private static final String TAG = "InforFirePresent";

    boolean mSensorEnable = true;

    public InforFirePresent(Node n) {
        super(n);
    }

    @Override
    public void setup() {
        super.sendRequest(0x0001, new byte[]{0x05, 0x01, 0x05, 0x02,});
    }

    @Override
    public void setdown() {

    }

    @Override
    public void procData(int req, int data) {

    }

    @Override
    public void procAppMsgData(Context context, int addr, int cmd, byte[] dat) {
        int i = -1;
        if (addr != super.mNode.mNetAddr)
            return;

        if (cmd == 0x0003)
            i = 0;// 主 动 上 报 传 感 器 值
        if (cmd == 0x8001 && dat[0] == 0)
            i = 1;// 读 参 数 响 应
        if (cmd == 0x8002 && dat[0] == 0) {// 写 参 数 响 应
            if (mSensorEnable) {
                // mCo2SensorImageView.setImageBitmap(Resource.imageCo2SensorBlue);
                mSensorEnable = false;
            } else {
                // mCo2SensorImageView.setImageBitmap(Resource.imageCo2SensorEnable);
                mSensorEnable = true;
            }
            return;
        }

        while (i >= 0 && i < dat.length) {
            int pid = ETool.builduInt(dat[i], dat[i + 1]);
            if (pid == 0x0501) {
                if (dat[i + 2] == 0) {
                    /* 传感结点被禁止 */
                    mSensorEnable = false;
                } else {
                    mSensorEnable = true;
                }
                i += 3;

            } else if (pid == 0x0502) {
                if (dat[i + 2] != 0) {
					/* 报警 */
//                    mSensorAlarm = true;
//                    if (mAlarmCheckBox.isChecked()) {
                    String msg = this.mNode.mNetAddr + ":检测到可燃气体";
                    Log.i(TAG, "procAppMsgData: " + msg);
                    String dev_id = (String) SPUtils.get(context,"dev_id","");
                    new HttpClient.Builder<String>()
                            .url(AppInterface.setSECENE)
                            .post()
                            .addParams("znjj_id",dev_id)
                            .addParams("type","sendWarn")
                            .addParams("data", "可燃气体超出标准浓度")
                            .builder()
                            .execute();
//                        Tool.notify("可燃气体告警", msg);
//                        Tool.playAlarm(3);
                    // mAlarmNumber =
                    // (mAlarmNumberEditText.getText().toString());
                    // if (mAlarmNumber != null && mAlarmNumber.length()>0)
                    // {
                    // Tool.sendShortMessage(mAlarmNumber, msg);
                    // }
//                    }
//                } else {
//                    mSensorAlarm = false;
//                }
                    i += 3;
                } else {
                    return;
                }
            }
        }
    }
}
