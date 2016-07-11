package com.hchstudio.znjj.activity;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hchstudio.znjj.AppInterface;
import com.hchstudio.znjj.R;
import com.hchstudio.znjj.adapter.MyFPAdapter;
import com.hchstudio.znjj.net.HttpClient;
import com.hchstudio.znjj.service.MainService;
import com.hchstudio.znjj.utils.ReceiverUtil;
import com.hchstudio.znjj.utils.SPUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import cn.jpush.android.api.JPushInterface;

/**
 * Created by 奔向阳光 on 2016/3/25.
 */
public class MainActivity extends FragmentActivity implements View.OnClickListener, ViewPager.OnPageChangeListener {

    private static final String TAG = "MainActivity";

    private View mView;
    private LinearLayout main_ll_wel;
    private ImageView wel_iv;

    private TextView main_tv_date;      //显示日期
    private TextView main_tv_week;      //显示星期
    private TextView main_tv_temp;      //显示温度
    private TextView main_tv_hum;       //显示湿度

    private ViewPager main_vp;
    private ArrayList<Fragment> fragments;
    private ArrayList<TextView> textViews;
    private MyFPAdapter myAdapter;
    private LinearLayout main_ll_tab;

    private ViewReceiver mReceiver;

    private Button bt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mView = View.inflate(this, R.layout.activity_main, null);
        setContentView(mView);

        Log.i(TAG, "MainActivity---1onCreate---" + Thread.currentThread().getName());
        initData();

        bt = (Button) this.findViewById(R.id.bt);
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent("com.hchstudio.znjj.service.MainServiceReceiver");
                intent.putExtra("tag", 0x02);
                sendBroadcast(intent);
            }
        });
        //启动内部监听
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.hchstudio.znjj.activity.ViewReceiver");
        mReceiver = new ViewReceiver();
        registerReceiver(mReceiver, intentFilter);
        initView();
    }

    private void initData() {
        if (ReceiverUtil.isServiceRunning(this, "com.hchstudio.znjj.service.MainService")) {
            Log.i(TAG, "已开启");
        } else {
            Intent intent = new Intent(this, MainService.class);
            startService(intent);
            Log.i(TAG, "开启服务");
        }
        fragments = new ArrayList<Fragment>();
        textViews = new ArrayList<TextView>();
    }

    private void initView() {
        main_ll_wel = (LinearLayout) mView.findViewById(R.id.main_ll_wel);
        wel_iv = (ImageView) mView.findViewById(R.id.wel_iv);
        main_vp = (ViewPager) mView.findViewById(R.id.mian_vp);
        main_ll_tab = (LinearLayout) mView.findViewById(R.id.main_ll_tab);
        main_tv_date = (TextView) mView.findViewById(R.id.main_tv_date);
        main_tv_week = (TextView) mView.findViewById(R.id.main_tv_week);
        main_tv_temp = (TextView) mView.findViewById(R.id.main_tv_temp);
        main_tv_hum = (TextView) mView.findViewById(R.id.main_tv_hum);

        //设置时间
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int week = calendar.get(Calendar.DAY_OF_WEEK);
        main_tv_date.setText(year + "年" + month + "月" + day + "日");
        String weekStr = "";
        switch (week) {
            case 1:
                weekStr = "一";
                break;
            case 2:
                weekStr = "二";
                break;
            case 3:
                weekStr = "三";
                break;
            case 4:
                weekStr = "四";
                break;
            case 5:
                weekStr = "五";
                break;
            case 6:
                weekStr = "六";
                break;
            case 7:
                weekStr = "日";
                break;
        }
        main_tv_week.setText("星期" + weekStr);

        //设置温湿度
        String humiture = (String) SPUtils.get(this, "dev_humiture", "");
        try {
            JSONObject jo = new JSONObject(humiture);
            String temp = jo.optString("temp");
            String hum = jo.optString("hum");
            main_tv_temp.setText("室内温度:" + temp + "°C");
            main_tv_hum.setText("室内湿度:" + hum + "%");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        myAdapter = new MyFPAdapter(getSupportFragmentManager(), fragments);
        main_vp.setAdapter(myAdapter);
        main_vp.setOnPageChangeListener(this);
        refreshM();
    }

    @Override
    public void onClick(View view) {
        for (int i = 0; i < textViews.size(); i++) {
            if (textViews.get(i).equals(view)) {
                main_vp.setCurrentItem(i);
                break;
            }
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        for (int i = 0; i < textViews.size(); i++) {
            if (i == position) {
                textViews.get(i).setBackgroundResource(R.drawable.tab_sele);
                continue;
            }
            textViews.get(i).setBackgroundResource(R.drawable.tab_disele);
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    /**
     * 0x00     缺省值
     * 0x01     刷新界面，有房间增减变动
     */
    class ViewReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "ViewReceiver---onReceive: ---");
            switch (intent.getIntExtra("tag", 0x00)) {
                case 0x00:
                    Log.i(TAG, "onReceive: tag缺省");
                    break;
                case 0x01:
                    Log.i(TAG, "onReceive: 刷新界面");
                    refreshM();
                    break;
                case 0x02:
                    Log.i(TAG, "onReceive: 刷新家电");
                    break;
            }
        }
    }

    public void refreshM(){
        String rooms = (String) SPUtils.get(this, "room", "");
        Log.i(TAG, "initView: room---" + rooms);
        if ("".equals(rooms)) {
            main_ll_wel.setVisibility(View.VISIBLE);
            new HttpClient.Builder()
                    .url(AppInterface.getCodePicture)
                    .post()
                    .addParams("scene_str", JPushInterface.getRegistrationID(this))
                    .type(Bitmap.class)
                    .builder()
                    .execute(new HttpClient.HttpCallback<Bitmap>() {
                        @Override
                        public void onResponse(Bitmap response) {
                            wel_iv.setImageBitmap(response);
                        }

                        @Override
                        public void onFailure(IOException e) {

                        }
                    });
        } else {
            main_ll_wel.setVisibility(View.GONE);
            textViews.clear();
            main_ll_tab.removeAllViews();
            try {
                JSONArray ja = new JSONArray(rooms);
                for (int i = 0; i < ja.length(); i++) {
                    JSONObject jo = (JSONObject) ja.get(i);
                    Log.i(TAG, "jo---" + jo.optString("room_name"));

                    TextView tv = new TextView(this);       //新建textView
                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, 46);
                    lp.setMargins(30, 0, 30, 0);
                    tv.setLayoutParams(lp);
                    tv.setText(jo.optString("room_name"));
                    tv.setGravity(Gravity.CENTER);
                    tv.setPadding(20, 0, 20, 0);
                    tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, 25);
                    tv.setBackgroundResource(R.drawable.tab_disele);
                    tv.setOnClickListener(this);
                    textViews.add(tv);
                    main_ll_tab.addView(tv);

                    fragments.add(new MyFragment(jo));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            myAdapter.notifyDataSetChanged();
            main_vp.setCurrentItem(0);
            for (int i = 0; i < textViews.size(); i++) {
                if (i == 0) {
                    textViews.get(i).setBackgroundResource(R.drawable.tab_sele);
                    continue;
                }
                textViews.get(i).setBackgroundResource(R.drawable.tab_disele);
            }
        }
    }


    /**
     * 菜单处理
     */
    private static final int _COMMAND_BOUND_ID = Menu.FIRST + 1;
    private static final int _COMMAND_SEARCH_ID = Menu.FIRST + 2;
    private static final int _COMMAND_ABOUT_ID = Menu.FIRST + 3;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE, _COMMAND_BOUND_ID, 1, "微信绑定");
        menu.add(Menu.NONE, _COMMAND_SEARCH_ID, 2, "搜索网络");
        menu.add(Menu.NONE, _COMMAND_ABOUT_ID, 3, "关于我们");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case _COMMAND_BOUND_ID:     //微信绑定
                final Dialog dialog = new Dialog(this);
                LinearLayout ll = new LinearLayout(this);
                ll.setOrientation(LinearLayout.VERTICAL);
                TextView tv = new TextView(this);
                tv.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                tv.setPadding(0, 5, 0, 5);
                tv.setText("用微信扫描绑定");
                tv.setGravity(Gravity.CENTER);
                tv.setTextSize(20);
                tv.setTextColor(Color.GRAY);
                ll.addView(tv);
                final ImageView iv = new ImageView(this);
                ll.addView(iv);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setCanceledOnTouchOutside(true);
                dialog.setCancelable(true);
                dialog.setContentView(ll);
//                dialog.show();
                Map<String, String> map = new HashMap<String, String>();
                map.put("scene_str", JPushInterface.getRegistrationID(MainActivity.this));
                HttpClient httpClient = new HttpClient.Builder()
                        .url(AppInterface.getCodePicture)
                        .post(map)
                        .type(Bitmap.class)
                        .builder();
                httpClient.execute(new HttpClient.HttpCallback<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap response) {
                        iv.setImageBitmap(Bitmap.createScaledBitmap(response, 300, 300, true));
                        dialog.show();
                    }

                    @Override
                    public void onFailure(IOException e) {

                    }
                });
                break;
            case _COMMAND_SEARCH_ID:
                Intent intent = new Intent("com.hchstudio.znjj.service.MainServiceReceiver");
                intent.putExtra("tag", 0x01);
                sendBroadcast(intent);
                Toast.makeText(this, "正在搜索节点...", Toast.LENGTH_SHORT).show();
                break;
            case _COMMAND_ABOUT_ID:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(mReceiver);
        super.onDestroy();
    }

    //获取手机唯一标识
//    private String getPhoneID() {
//        TelephonyManager TelephonyMgr = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
//        String m_szImei = TelephonyMgr.getDeviceId();
//
//        String m_szDevIDShort = "35" +              //we make this look like a valid IMEI
//                Build.BOARD.length() % 10 +
//                Build.BRAND.length() % 10 +
//                Build.CPU_ABI.length() % 10 +
//                Build.DEVICE.length() % 10 +
//                Build.DISPLAY.length() % 10 +
//                Build.HOST.length() % 10 +
//                Build.ID.length() % 10 +
//                Build.MANUFACTURER.length() % 10 +
//                Build.MODEL.length() % 10 +
//                Build.PRODUCT.length() % 10 +
//                Build.TAGS.length() % 10 +
//                Build.TYPE.length() % 10 +
//                Build.USER.length() % 10; //13 digits
//
//        String m_szAndroidID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
//
//        WifiManager wm = (WifiManager) getSystemService(Context.WIFI_SERVICE);
//        String m_szWLANMAC = wm.getConnectionInfo().getMacAddress();
//
//        BluetoothAdapter m_BluetoothAdapter = null; // Local Bluetooth adapter
//        m_BluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//        String m_szBTMAC = m_BluetoothAdapter.getAddress();
//
//        String m_szLongID = m_szImei + m_szDevIDShort + m_szAndroidID + m_szWLANMAC + m_szBTMAC;
//        // compute md5
//        MessageDigest m = null;
//        try {
//            m = MessageDigest.getInstance("MD5");
//        } catch (NoSuchAlgorithmException e) {
//            e.printStackTrace();
//        }
//        m.update(m_szLongID.getBytes(), 0, m_szLongID.length());
//        // get md5 bytes
//        byte p_md5Data[] = m.digest();
//        // create a hex string
//        String m_szUniqueID = new String();
//        for (int i = 0; i < p_md5Data.length; i++) {
//            int b = (0xFF & p_md5Data[i]);
//            // if it is a single digit, make sure it have 0 in front (proper padding)
//            if (b <= 0xF)
//                m_szUniqueID += "0";
//            // add number to string
//            m_szUniqueID += Integer.toHexString(b);
//        }   // hex string to uppercase
//        m_szUniqueID = m_szUniqueID.toUpperCase();
//        return m_szUniqueID;
//    }
}
