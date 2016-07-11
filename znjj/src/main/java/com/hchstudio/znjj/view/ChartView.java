package com.hchstudio.znjj.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.Log;
import android.view.View;

import com.hchstudio.znjj.utils.SPUtils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by hch on 16-5-16.
 * 400 * 300
 */
public class ChartView extends View {

    private static final String TAG = "ChartView";
    private String[] times = new String[7];
    private int[] temps = new int[7];
    private int[] hums = new int[7];
    private int max = 0;
    private int min = 100;

    public ChartView(Context context) {
        super(context);
        String hisStr = (String) SPUtils.get(context,"dev_humiture","");
//        Log.i(TAG, "ChartView: "+hisStr);
        try {
            JSONObject jo = new JSONObject(hisStr);
            hisStr = jo.optString("history");
            Log.i(TAG,"hisStr---" + hisStr);
            String[] strs = hisStr.split("\\.");
            for (int i=0;i<strs.length;i++){
                String[] strs1 = strs[i].split("_");
                Log.i(TAG, "ChartView: "+strs1[0]);
                times[i] = strs1[0]+":00";
                temps[i] = Integer.parseInt(strs1[1]);
                hums[i] = Integer.parseInt(strs1[2]);
                max = getMax(max,temps[i]);
                max = getMax(max,hums[i]);
                min = getMin(min,temps[i]);
                min = getMin(min,hums[i]);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    protected void onDraw(Canvas canvas){
        super.onDraw(canvas);
//        canvas.drawColor(Color.YELLOW);
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);

        paint.setColor(Color.parseColor("#666666"));
        paint.setStrokeWidth(2);
        canvas.drawLine(30,290,390,290,paint);      //画X轴
        canvas.drawLine(30,290,30,30,paint);        //画Y轴

        //坐标参数
        paint.setStrokeWidth(1);
        paint.setTextSize(15);
        for (int i=0;i<times.length;i++){
            canvas.drawText(times[i],16+60*i,305,paint);
        }
        int mid = (max+min)/20*10;
        for (int i=0;i<5;i++){
            canvas.drawText(String.valueOf(mid-20+10*i),11,295-65*i,paint);
        }

        //画虚线
        paint.setColor(Color.parseColor("#CCCCCC"));
        for (int i=1; i<=4;i++){                            //X轴
            canvas.drawLine(30,290-65*i,390,290-65*i,paint);
        }
        for (int i=1; i<=6;i++){                            //Y轴
            canvas.drawLine(30+60*i,290,30+60*i,30,paint);
        }

        //画路径
        paint.setStrokeWidth(2);
        paint.setColor(Color.RED);              //绘制温度折线
        int startX = 30;
        int startY = (int)(290-(temps[0]-mid+20)*6.5);
        for (int i=1;i<temps.length;i++){
            canvas.drawLine(startX,startY,30+60*i,(int)(290-(temps[i]-mid+20)*6.5),paint);
            startX = 30+60*i;
            startY = (int)(290-(temps[i]-mid+20)*6.5);
        }
        canvas.drawLine(343,44,366,44,paint);
        paint.setColor(Color.BLUE);             //绘制湿度折线
        startX = 30;
        startY = (int)(290-(hums[0]-mid+20)*6.5);
        for (int i=1;i<hums.length;i++){
            canvas.drawLine(startX,startY,30+60*i,(int)(290-(hums[i]-mid+20)*6.5),paint);
            startX = 30+60*i;
            startY = (int)(290-(hums[i]-mid+20)*6.5);
        }
        canvas.drawLine(343,64,366,64,paint);

        paint.setColor(Color.parseColor("#191919"));
        canvas.drawText("温度",370,50,paint);
        canvas.drawText("湿度",370,70,paint);
        //温湿度记录
        paint.setTextSize(24);
        canvas.drawText("温湿度记录",150,30,paint);
    }

    private int getMax(int a,int b){
        if (a >=b )
            return a;
        else
            return b;
    }

    private int getMin(int a,int b){
        if (a <= b)
            return a;
        else
            return b;
    }
}
