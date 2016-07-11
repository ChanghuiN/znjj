package com.hchstudio.znjj.activity;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hchstudio.znjj.App;
import com.hchstudio.znjj.EMB.InfrarConPresent;
import com.hchstudio.znjj.EMB.LightPresent;
import com.hchstudio.znjj.EMB.Node;
import com.hchstudio.znjj.EMB.NodePresent;
import com.hchstudio.znjj.R;
import com.hchstudio.znjj.utils.SPUtils;
import com.hchstudio.znjj.view.ChartView;
import com.hchstudio.znjj.view.InfraConView;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.lang.reflect.Field;
import java.util.ArrayList;

/**
 * Created by hch on 16-5-15.
 */
public class ConDialog extends Dialog implements View.OnClickListener {

    private static final String TAG = "ConDialog";

    private ArrayList<NodePresent> nodePresents;
    private String equip_str;
    private JSONObject equip_jo;
    private NodePresent mNodePresent;

    private Context mContext;
    private RelativeLayout mView;

    //电灯
    private ImageView iv_light;         //电灯
    private ImageView iv_bar;           //进度条
    private ImageView iv_addL;          //亮度加
    private ImageView iv_redL;          //亮度减
    private int stateLi;                  //电灯状态（第几个灯亮）

    //空调
    private InfraConView iv_contr;         //温度控制
    private ImageView iv_switch;        //开关按钮
    private TextView tv_auto;          //自动模式
    private TextView tv_refri;         //制冷模式
    private TextView tv_dehum;         //除湿模式
    private TextView tv_blow;          //送风模式
    private TextView tv_heat;          //制暖模式
    private ImageView iv_addT;          //温度加
    private ImageView iv_redT;          //温度减

    private int mTemp;                  //温度值
    private boolean swi;                //空调开关

    public ConDialog(Context context, String equipStr) {
        super(context, R.style.Condialog);
        this.mContext = context;
        this.equip_str = equipStr;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mView = new RelativeLayout(mContext);
        mView.setBackgroundColor(Color.parseColor("#ddffffff"));
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(500, 350);
        this.setCanceledOnTouchOutside(true);
        this.setContentView(mView, lp);
        Log.i(TAG, "width---" + mView.getLayoutParams().width + "---height---" + mView.getLayoutParams().height);

        nodePresents = App.nodePresents;
        mTemp = 16;

        if (equip_str == null || equip_str.equals("")) {      //温湿度
            Log.i(TAG, "显示温湿度框框---");
            RelativeLayout.LayoutParams cvlp = new RelativeLayout.LayoutParams(420, 320);
            cvlp.addRule(RelativeLayout.CENTER_IN_PARENT);
            ChartView chartView = new ChartView(mContext);
            chartView.setLayoutParams(cvlp);
            mView.addView(chartView);
        } else {
            Log.i(TAG, "onCreate: node---" + equip_str);
            try {
                equip_jo = new JSONObject(equip_str);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (equip_jo != null) {
                for (NodePresent np : nodePresents) {
                    Log.i(TAG, "onCreate: np---" + np.mNode.mNetAddr);
                    if (np.mNode.mNetAddr == equip_jo.optInt("equip_netaddr")) {
                        Log.i(TAG, "onCreate: node---OK");
                        mNodePresent = np;
                        break;
                    }
                }
                if (mNodePresent != null) {
                    switch (mNodePresent.mNode.mDevType) {
                        case Node.DEV_LIGHT:        //电灯
                            Log.i(TAG, "onCreate: stateLi" + mNodePresent.mNode.state);
                            //判断灯的状态
                            if (mNodePresent.mNode.state.equals("0000")) {
                                stateLi = 5;
                            } else if (mNodePresent.mNode.state.equals("1000")) {
                                stateLi = 4;
                            } else if (mNodePresent.mNode.state.equals("1100")) {
                                stateLi = 3;
                            } else if (mNodePresent.mNode.state.equals("1110")) {
                                stateLi = 2;
                            } else if (mNodePresent.mNode.state.equals("1111")) {
                                stateLi = 1;
                            }
                            iv_light = new ImageView(mContext);
                            iv_light.setImageResource(R.drawable.deng_1);
                            RelativeLayout.LayoutParams lp_iv_light = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, 150);
                            lp_iv_light.addRule(RelativeLayout.CENTER_IN_PARENT);
                            iv_light.setLayoutParams(lp_iv_light);
                            iv_light.setId(R.id.iv_light);
                            mView.addView(iv_light);
                            iv_bar = new ImageView(mContext);     //进度条
                            iv_bar.setId(R.id.iv_bar);
                            RelativeLayout.LayoutParams lp_iv_bar = new RelativeLayout.LayoutParams(280, RelativeLayout.LayoutParams.WRAP_CONTENT);
                            lp_iv_bar.addRule(RelativeLayout.BELOW, iv_light.getId());
                            lp_iv_bar.addRule(RelativeLayout.CENTER_HORIZONTAL);
                            lp_iv_bar.topMargin = 10;
                            mView.addView(iv_bar, lp_iv_bar);
                            setLinght();
                            TextView tv_title = new TextView(mContext);     //标题
                            tv_title.setText(equip_jo.optString("equip_name"));
                            RelativeLayout.LayoutParams lp_tv_title = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                            lp_tv_title.addRule(RelativeLayout.ABOVE, iv_light.getId());
                            lp_tv_title.addRule(RelativeLayout.CENTER_HORIZONTAL);
                            lp_tv_title.bottomMargin = 20;
                            tv_title.setLayoutParams(lp_tv_title);
                            tv_title.setTextSize(TypedValue.COMPLEX_UNIT_PX, 24);
                            mView.addView(tv_title);
                            TextView tv_OFF = new TextView(mContext);       //关
                            tv_OFF.setTextSize(TypedValue.COMPLEX_UNIT_PX, 20);
                            tv_OFF.setTextColor(Color.parseColor("#7c7c7c"));
                            tv_OFF.setText("OFF");
                            RelativeLayout.LayoutParams lp_tv_OFF = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                            lp_tv_OFF.addRule(RelativeLayout.BELOW, iv_bar.getId());
                            lp_tv_OFF.addRule(RelativeLayout.ALIGN_LEFT, iv_bar.getId());
                            tv_OFF.setLayoutParams(lp_tv_OFF);
                            mView.addView(tv_OFF);
                            TextView tv_ON = new TextView(mContext);        //开
                            tv_ON.setText("ON");
                            tv_ON.setTextSize(TypedValue.COMPLEX_UNIT_PX, 20);
                            tv_ON.setTextColor(Color.parseColor("#7c7c7c"));
                            RelativeLayout.LayoutParams lp_tv_ON = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                            lp_tv_ON.addRule(RelativeLayout.BELOW, iv_bar.getId());
                            lp_tv_ON.addRule(RelativeLayout.ALIGN_RIGHT, iv_bar.getId());
                            tv_ON.setLayoutParams(lp_tv_ON);
                            mView.addView(tv_ON);
                            iv_addL = new ImageView(mContext);        //增加亮度
                            iv_addL.setImageResource(R.drawable.linght_add);
                            iv_addL.setId(R.id.iv_addL);
                            RelativeLayout.LayoutParams lp_iv_addl = new RelativeLayout.LayoutParams(40, ViewGroup.LayoutParams.WRAP_CONTENT);
                            lp_iv_addl.addRule(RelativeLayout.RIGHT_OF, iv_bar.getId());
                            lp_iv_addl.addRule(RelativeLayout.ALIGN_TOP, iv_bar.getId());
                            lp_iv_addl.leftMargin = 15;
                            lp_iv_addl.topMargin = -9;
                            iv_addL.setLayoutParams(lp_iv_addl);
                            iv_addL.setOnClickListener(this);
                            mView.addView(iv_addL);
                            iv_redL = new ImageView(mContext);        //降低亮度
                            iv_redL.setImageResource(R.drawable.linght_rel);
                            iv_redL.setId(R.id.iv_redL);
                            RelativeLayout.LayoutParams lp_iv_redl = new RelativeLayout.LayoutParams(40, ViewGroup.LayoutParams.WRAP_CONTENT);
                            lp_iv_redl.addRule(RelativeLayout.LEFT_OF, iv_bar.getId());
                            lp_iv_redl.addRule(RelativeLayout.ALIGN_TOP, iv_bar.getId());
                            lp_iv_redl.rightMargin = 15;
                            lp_iv_redl.topMargin = -9;
                            iv_redL.setOnClickListener(this);
                            iv_redL.setLayoutParams(lp_iv_redl);
                            mView.addView(iv_redL);
                            break;
                        case Node.DEV_InfrarCon:    //红外遥控
                            Log.i(TAG, "onCreate: 空调");
                            String humiture = (String) SPUtils.get(mContext, "dev_humiture", "");
                            try {
                                JSONObject jo = new JSONObject(humiture);
                                String temp = jo.optString("temp");
                                mTemp = Integer.parseInt(temp);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            swi = true;
                            mNodePresent.procData(InfrarConPresent.SETING_NUM,27);
                            iv_contr = new InfraConView(mContext,mTemp,InfrarConPresent.MODEL_HEAT);
                            iv_contr.setId(R.id.iv_con);
                            RelativeLayout.LayoutParams lp_iv_con = new RelativeLayout.LayoutParams(330,250);
                            lp_iv_con.addRule(RelativeLayout.CENTER_IN_PARENT);
//                            lp_iv_con.rightMargin = 10;
                            iv_contr.setLayoutParams(lp_iv_con);
                            mView.addView(iv_contr);
                            iv_switch = new ImageView(mContext);
                            iv_switch.setId(R.id.iv_switch);
                            iv_switch.setOnClickListener(this);
                            RelativeLayout.LayoutParams lp_iv_swi = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
                            lp_iv_swi.addRule(RelativeLayout.LEFT_OF,iv_contr.getId());
                            lp_iv_swi.addRule(RelativeLayout.ALIGN_TOP,iv_contr.getId());
                            iv_switch.setLayoutParams(lp_iv_swi);
                            iv_switch.setImageResource(R.drawable.infra_no);
                            mView.addView(iv_switch);
                            iv_addT = new ImageView(mContext);
                            iv_addT.setId(R.id.iv_addT);
                            RelativeLayout.LayoutParams lp_iv_add = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
                            lp_iv_add.addRule(RelativeLayout.ALIGN_BOTTOM,iv_contr.getId());
                            lp_iv_add.addRule(RelativeLayout.ALIGN_RIGHT,iv_contr.getId());
                            lp_iv_add.rightMargin = 150;
                            lp_iv_add.bottomMargin = 20;
                            iv_addT.setLayoutParams(lp_iv_add);
                            iv_addT.setImageResource(R.drawable.infra_add);
                            iv_addT.setOnClickListener(this);
                            mView.addView(iv_addT);
                            iv_redT = new ImageView(mContext);
                            iv_redT.setId(R.id.iv_redT);
                            RelativeLayout.LayoutParams lp_iv_red = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
                            lp_iv_red.addRule(RelativeLayout.ALIGN_BOTTOM,iv_contr.getId());
                            lp_iv_red.addRule(RelativeLayout.ALIGN_LEFT,iv_contr.getId());
                            lp_iv_red.leftMargin = 60;
                            lp_iv_red.bottomMargin = 20;
                            iv_redT.setLayoutParams(lp_iv_red);
                            iv_redT.setImageResource(R.drawable.infra_red);
                            iv_redT.setOnClickListener(this);
                            mView.addView(iv_redT);

                            LinearLayout ll = new LinearLayout(mContext);
                            ll.setOrientation(LinearLayout.VERTICAL);
                            ll.setBackgroundResource(R.drawable.con_ll);
                            RelativeLayout.LayoutParams lp_ll = new RelativeLayout.LayoutParams(120,RelativeLayout.LayoutParams.WRAP_CONTENT);
                            lp_ll.addRule(RelativeLayout.ALIGN_TOP,iv_contr.getId());
                            lp_ll.addRule(RelativeLayout.RIGHT_OF,iv_contr.getId());
                            lp_ll.leftMargin = -65;
                            ll.setLayoutParams(lp_ll);
                            //模式选择
                            LinearLayout.LayoutParams lp_tv_1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
                            tv_auto = new TextView(mContext);
                            tv_auto.setId(R.id.tv_auto);
                            tv_auto.setOnClickListener(this);
                            tv_auto.setText("自动");
                            tv_auto.setHeight(45);
                            tv_auto.setWidth(120);
                            tv_auto.setGravity(Gravity.CENTER);
                            tv_auto.setTextSize(TypedValue.COMPLEX_UNIT_PX,24);
                            tv_auto.setTextColor(Color.parseColor("#f7f7f7"));
//                            tv_auto.setLayoutParams(lp_tv_1);
                            ll.addView(tv_auto);
                            //分割线
                            LinearLayout.LayoutParams lp_1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,1);
                            View view_1 = new View(mContext);
                            view_1.setLayoutParams(lp_1);
                            view_1.setBackgroundColor(Color.parseColor("#2BB7EB"));
                            ll.addView(view_1);
                            tv_refri = new TextView(mContext);
                            tv_refri.setId(R.id.tv_refri);
                            tv_refri.setOnClickListener(this);
                            tv_refri.setText("制冷");
                            tv_refri.setHeight(45);
                            tv_refri.setWidth(120);
                            tv_refri.setGravity(Gravity.CENTER);
                            tv_refri.setTextSize(TypedValue.COMPLEX_UNIT_PX,24);
                            tv_refri.setTextColor(Color.parseColor("#f7f7f7"));
                            ll.addView(tv_refri);
                            //分割线
                            LinearLayout.LayoutParams lp_2 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,1);
                            View view_2 = new View(mContext);
                            view_2.setLayoutParams(lp_2);
                            view_2.setBackgroundColor(Color.parseColor("#2BB7EB"));
                            ll.addView(view_2);
                            tv_dehum = new TextView(mContext);
                            tv_dehum.setId(R.id.tv_dehum);
                            tv_dehum.setOnClickListener(this);
                            tv_dehum.setText("除湿");
                            tv_dehum.setHeight(45);
                            tv_dehum.setWidth(120);
                            tv_dehum.setGravity(Gravity.CENTER);
                            tv_dehum.setTextSize(TypedValue.COMPLEX_UNIT_PX,24);
                            tv_dehum.setTextColor(Color.parseColor("#f7f7f7"));
                            ll.addView(tv_dehum);
                            //分割线
                            LinearLayout.LayoutParams lp_3 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,1);
                            View view_3 = new View(mContext);
                            view_3.setLayoutParams(lp_3);
                            view_3.setBackgroundColor(Color.parseColor("#2BB7EB"));
                            ll.addView(view_3);
                            tv_blow = new TextView(mContext);
                            tv_blow.setId(R.id.tv_blow);
                            tv_blow.setOnClickListener(this);
                            tv_blow.setText("送风");
                            tv_blow.setHeight(45);
                            tv_blow.setWidth(120);
                            tv_blow.setGravity(Gravity.CENTER);
                            tv_blow.setTextSize(TypedValue.COMPLEX_UNIT_PX,24);
                            tv_blow.setTextColor(Color.parseColor("#f7f7f7"));
                            ll.addView(tv_blow);
                            //分割线
                            LinearLayout.LayoutParams lp_4 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,1);
                            View view_4 = new View(mContext);
                            view_4.setLayoutParams(lp_4);
                            view_4.setBackgroundColor(Color.parseColor("#2BB7EB"));
                            ll.addView(view_4);
                            tv_heat = new TextView(mContext);
                            tv_heat.setId(R.id.tv_heat);
                            tv_heat.setOnClickListener(this);
                            tv_heat.setText("制暖");
                            tv_heat.setHeight(45);
                            tv_heat.setWidth(120);
                            tv_heat.setGravity(Gravity.CENTER);
                            tv_heat.setTextSize(TypedValue.COMPLEX_UNIT_PX,24);
                            tv_heat.setTextColor(Color.parseColor("#f7f7f7"));
                            ll.addView(tv_heat);
                            mView.addView(ll);
                            break;
                    }
                } else {
                    TextView tv_null = new TextView(mContext);
                    tv_null.setText("家电已断开连接");
                    tv_null.setTextSize(TypedValue.COMPLEX_UNIT_PX, 30);
                    RelativeLayout.LayoutParams lp_tv_null = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    lp_tv_null.addRule(RelativeLayout.CENTER_IN_PARENT);
                    tv_null.setLayoutParams(lp_tv_null);
                    mView.addView(tv_null);
                }
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_addL:      //增加亮度
                if (stateLi == 5) {
                    Toast.makeText(mContext, "已达到最高亮度", Toast.LENGTH_SHORT).show();
                    break;
                }
                stateLi++;
                mNodePresent.procData(LightPresent.LINGHT_SUB,0);
                setLinght();
                Toast.makeText(mContext, "增加亮度", Toast.LENGTH_SHORT).show();
                break;
            case R.id.iv_redL:      //减少亮度
                if (stateLi == 1) {
                    Toast.makeText(mContext, "已达到最低亮度", Toast.LENGTH_SHORT).show();
                    break;
                }
                stateLi--;
                mNodePresent.procData(LightPresent.LINGHT_ADD,0);
                setLinght();
                Toast.makeText(mContext, "降低亮度", Toast.LENGTH_SHORT).show();
                break;
            case R.id.iv_addT:          //空调加
                mTemp++;
                if (mTemp>30){
                    mTemp--;
                    break;
                }
//                Toast.makeText(mContext, "增加温度", Toast.LENGTH_SHORT).show();
                iv_contr.setTemp(mTemp);
                mNodePresent.procData(InfrarConPresent.SETTING_TEMP,mTemp);
                break;
            case R.id.iv_redT:          //空调减
                mTemp--;
                if (mTemp <16){
                    mTemp++;
                    break;
                }
//                Toast.makeText(mContext, "降低温度", Toast.LENGTH_SHORT).show();
                mNodePresent.procData(InfrarConPresent.SETTING_TEMP,mTemp);
                iv_contr.setTemp(mTemp);
                break;
            case R.id.iv_switch:        //空调开关
                if (swi){
                    mNodePresent.procData(InfrarConPresent.POWER_OFF,mTemp);
                    swi = false;
                    iv_switch.setImageResource(R.drawable.infra_off);
                } else {
                    mNodePresent.procData(InfrarConPresent.POWER_ON,mTemp);
                    swi = true;
                    iv_switch.setImageResource(R.drawable.infra_no);
                }
                break;
            case R.id.tv_auto:          //自动模式
                mNodePresent.procData(InfrarConPresent.MODEL_AUTO,mTemp);
                iv_contr.setModel(InfrarConPresent.MODEL_AUTO);
                break;
            case R.id.tv_refri:         //制冷模式
                mNodePresent.procData(InfrarConPresent.MODEL_REFRI,mTemp);
                iv_contr.setModel(InfrarConPresent.MODEL_REFRI);
                break;
            case R.id.tv_dehum:         //除湿模式
                mNodePresent.procData(InfrarConPresent.MODEL_COOL,mTemp);
                iv_contr.setModel(InfrarConPresent.MODEL_COOL);
                break;
            case R.id.tv_blow:          //送风模式
                mNodePresent.procData(InfrarConPresent.MODEL_BLOW,mTemp);
                iv_contr.setModel(InfrarConPresent.MODEL_BLOW);
                break;
            case R.id.tv_heat:          //制暖模式
                mNodePresent.procData(InfrarConPresent.MODEL_HEAT,mTemp);
                iv_contr.setModel(InfrarConPresent.MODEL_HEAT);
                break;
        }
    }

    private void setLinght(){
        try {
            Field sf = R.drawable.class.getField("switch_"+stateLi);
            int sid = sf.getInt(new R.drawable());
            iv_bar.setImageResource(sid);
            Field df = R.drawable.class.getField("deng_"+stateLi);
            int did = df.getInt(new R.drawable());
            iv_light.setImageResource(did);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}