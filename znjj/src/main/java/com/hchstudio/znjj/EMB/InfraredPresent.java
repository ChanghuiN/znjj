package com.hchstudio.znjj.EMB;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.Log;

import com.hchstudio.znjj.AppInterface;
import com.hchstudio.znjj.R;
import com.hchstudio.znjj.net.HttpClient;
import com.hchstudio.znjj.utils.SPUtils;

/**
 * Created by hch on 16-6-7.
 * 人体红外传感器
 */
public class InfraredPresent extends NodePresent {

    private static final String TAG = "InfraredPresent";

    private SoundPool mSoundPool;

    boolean mSensorEnable = true;
//    boolean mSensorAlarm = false;

    public InfraredPresent(Node n) {
        super(n);
        mSoundPool = new SoundPool(5, AudioManager.STREAM_MUSIC, 5);
    }

    @Override
    public void setup() {
//        super.sendRequest(0x0002, new byte[] { 0x04, 0x01, 0x02 });
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
            i = 0;
        if (cmd == 0x8001 && dat[0] == 0)
            i = 1;
        if (cmd == 0x8002 && dat[0] == 0) {

            if (mSensorEnable) {
                // mInfraredImageView.setImageBitmap(Resource.imageInfraredDisable);
                mSensorEnable = false;
            } else {
                // mInfraredImageView.setImageBitmap(Resource.imageInfraredEnable);
                mSensorEnable = true;
            }
            return;
        }
        while (i >= 0 && i < dat.length) {
            int pid = ETool.builduInt(dat[i], dat[i + 1]);
            switch (pid) {
                case 0x0401:
                    if (dat[i + 2] == 0) { // 结点被禁止
                        // mInfraredImageView.setImageBitmap(Resource.imageInfraredDisable);
                        mSensorEnable = false;
                    } else {
                        // mInfraredImageView.setImageBitmap(Resource.imageInfraredEnable);
                        mSensorEnable = true;
                    }
                    i += 3;
                    break;
                case 0x0402:
                    if (dat[i + 2] != 0) {
                    /* 报警 */
//                        mSensorAlarm = true;
                        // String msg = this.mNode.mNetAddr + ":检测到入侵";
//                        if (mAlarmCheckBox.isChecked()) {
						/* 发送告警通知和短信 */
                        String msg = this.mNode.mNetAddr + ":检测到入侵";
                        Log.i(TAG, "procAppMsgData: " + msg);
                        String dev_id = (String) SPUtils.get(context, "dev_id", "");
                        new HttpClient.Builder<String>()
                                .url(AppInterface.setSECENE)
                                .post()
                                .addParams("znjj_id", dev_id)
                                .addParams("type", "sendWarn")
                                .addParams("data", "检测到非法入侵")
                                .builder()
                                .execute();
                        mSoundPool.load(context, R.raw.bj, 1);
                        mSoundPool.pause(1);
                        mSoundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
                            @Override
                            public void onLoadComplete(SoundPool soundPool, int i, int i1) {
                                mSoundPool.play(1, 1, 1, 0, 0, 2);
                            }
                        });
//                            Tool.notify("入侵告警", msg);
//                            Tool.playAlarm(3);
                        // mAlarmNumber =
                        // (mAlarmNumberEditText.getText().toString());
                        // if (mAlarmNumber != null && mAlarmNumber.length()>0)
                        // {
                        // Tool.sendShortMessage(mAlarmNumber, msg);
                        // }
                    }
                    // mAlarmNumber =
                    // (mAlarmNumberEditText.getText().toString());
                    // if (mAlarmNumber != null && mAlarmNumber.length()>0) {
                    // Tool.sendShortMessage(mAlarmNumber, msg);
                    // }
//                    } else {
//                        mSensorAlarm = false;
//                    }
                    i += 3;
                    break;
                default:
                    return;
            }
        }
    }
}
