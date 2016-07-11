package com.hchstudio.znjj.activity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hchstudio.znjj.EMB.Node;
import com.hchstudio.znjj.R;
import com.hchstudio.znjj.utils.SPUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

/**
 * Created by hch on 16-5-13.
 */
public class MyFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = "MyFragment";

    private Context mContext;
    private View mView;
    private JSONObject jo;
    private LinearLayout frag_ll;
    private LinearLayout frag_ll_list;

    private TextView frag_tv_3;

    public MyFragment(JSONObject jo){
        this.jo = jo;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragm,container,false);
        mContext = getContext();
        frag_ll = (LinearLayout) mView.findViewById(R.id.frag_ll);
        frag_ll_list = (LinearLayout) mView.findViewById(R.id.frag_ll_list);

        Bitmap bm = BitmapFactory.decodeStream(this.getContext().getClass().getClassLoader().getResourceAsStream("assets/room_" + jo.optString("room_image") + ".png"));
        Drawable da = new BitmapDrawable(bm);
        frag_ll.setBackgroundDrawable(da);

        String room_equip_netaddr = jo.optString("room_equip_netaddr");
        String[] room_equip_netaddrs = room_equip_netaddr.split(",");
        String equipStr = (String) SPUtils.get(mContext,"equip","");
        Log.i(TAG, "onCreateView: equipStr---" + equipStr);
        JSONArray equipJA = null;
        try {
            equipJA = new JSONArray(equipStr);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        for (int i=0; i<room_equip_netaddrs.length; i++){
            if (equipJA != null){
                for (int j=0; j<equipJA.length(); j++){
                    try {
                        JSONObject jo = (JSONObject) equipJA.get(j);
                        Log.i(TAG, "onCreateView: ---" + room_equip_netaddrs[i] + "---" + jo.optString("equip_netaddr"));
                        if (room_equip_netaddrs[i].equals(jo.optString("equip_netaddr"))){
                            View view = new View(mContext);
                            LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,1);
                            lp1.leftMargin = 10;
                            lp1.rightMargin = 10;
                            view.setLayoutParams(lp1);
                            view.setBackgroundColor(Color.parseColor("#77000000"));
                            frag_ll_list.addView(view);
                            TextView tv = new TextView(mContext);
                            LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,62);
                            tv.setLayoutParams(lp2);
                            tv.setText(jo.optString("equip_name"));
                            tv.setTextSize(TypedValue.COMPLEX_UNIT_PX,30);
                            tv.setGravity(Gravity.CENTER);
                            tv.setTextColor(Color.parseColor("#6f6f6f"));
                            tv.setTag(jo.toString());
                            tv.setOnClickListener(this);
                            frag_ll_list.addView(tv);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        frag_tv_3 = (TextView) mView.findViewById(R.id.frag_tv_3);
        frag_tv_3.setOnClickListener(this);

        return mView;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onClick(View view) {
//        int tag = Integer.parseInt(((String)view.getTag()).substring(2),16);//Integer.parseInt("8C",16); //(String)view.getTag();
//        switch (tag) {
//            case Node.DEV_InforTemp:
//                Log.i(TAG,"点击里温湿度---");
//                break;
//            case Node.DEV_LIGHT:
//                Log.i(TAG,"点击里继电器---");
//                break;
//            case Node.DEV_InfrarCon:
//                Log.i(TAG,"点击里空调---");
//                break;
//        }
        String tag = (String) view.getTag();
        ConDialog dialog = new ConDialog(mContext,tag);
        dialog.show();
    }
}
