package com.hchstudio.znjj.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;
import android.view.View;

import com.hchstudio.znjj.EMB.InfrarConPresent;
import com.hchstudio.znjj.R;

/**
 * Created by hch on 16-6-4.
 */
public class InfraConView extends View {

    private static final String TAG = "InfraConView";

    private int infraNum;           //温度值
    private int infraModel;         //模式

    private int width;
    private int height;

    public InfraConView(Context context, int temp, int model) {
        super(context);
        this.infraNum = temp;
        this.infraModel = model;
        if (temp > 30)
            infraNum = 30;
        if (temp < 16)
            infraNum = 16;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        width = getWidth();
        height = getHeight();
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
//        canvas.drawColor(Color.YELLOW);
        Log.i(TAG, "onDraw: draw--" + infraNum);

        //画笔
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);
        //边缘的数字显示
        Bitmap bm2 = BitmapFactory.decodeResource(getResources(), R.drawable.infra_out);
        bm2 = Bitmap.createScaledBitmap(bm2, getHeight(), getHeight(), true);
        canvas.drawBitmap(bm2, height / 2 - bm2.getWidth() / 2, height / 2 - bm2.getHeight() / 2, paint);
        if (infraModel == InfrarConPresent.MODEL_AUTO || infraModel == InfrarConPresent.MODEL_COOL || infraModel == InfrarConPresent.MODEL_BLOW) {
            //中间变色的
            Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.infra_center1);
            bm = Bitmap.createScaledBitmap(bm, getHeight() * 65 / 115, getHeight() * 65 / 115, true);
            canvas.drawBitmap(bm, height / 2 - bm.getWidth() / 2, height / 2 - bm.getHeight() / 2, paint);
        } else {
            //中间变色的
            Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.infra_center);
            bm = Bitmap.createScaledBitmap(bm, getHeight() * 65 / 115, getHeight() * 65 / 115, true);
            canvas.drawBitmap(bm, height / 2 - bm.getWidth() / 2, height / 2 - bm.getHeight() / 2, paint);
        }
        //显示指针
        Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.infra_p);
        bmp = Bitmap.createScaledBitmap(bmp, getHeight() * 10 / 115, getHeight() * 10 / 115, true);
        Bitmap bm1;
        switch (infraModel) {
            case InfrarConPresent.MODEL_AUTO:
                //中间显示的
                bm1 = BitmapFactory.decodeResource(getResources(), R.drawable.infra_mauto);
                bm1 = Bitmap.createScaledBitmap(bm1, getHeight() * 45 / 115, getHeight() * 45 / 115, true);
                canvas.drawBitmap(bm1, height / 2 - bm1.getWidth() / 2, height / 2 - bm1.getHeight() / 2, paint);
                break;
            case InfrarConPresent.MODEL_BLOW:
                //中间显示的
                bm1 = BitmapFactory.decodeResource(getResources(), R.drawable.infra_mblow);
                bm1 = Bitmap.createScaledBitmap(bm1, getHeight() * 45 / 115, getHeight() * 45 / 115, true);
                canvas.drawBitmap(bm1, height / 2 - bm1.getWidth() / 2, height / 2 - bm1.getHeight() / 2, paint);
                break;
            case InfrarConPresent.MODEL_COOL:
                //中间显示的
                bm1 = BitmapFactory.decodeResource(getResources(), R.drawable.infra_mcool);
                bm1 = Bitmap.createScaledBitmap(bm1, getHeight() * 45 / 115, getHeight() * 45 / 115, true);
                canvas.drawBitmap(bm1, height / 2 - bm1.getWidth() / 2, height / 2 - bm1.getHeight() / 2, paint);
                break;
            case InfrarConPresent.MODEL_HEAT:
                //中间显示的
                bm1 = BitmapFactory.decodeResource(getResources(), R.drawable.infra_mheat);
                bm1 = Bitmap.createScaledBitmap(bm1, getHeight() * 45 / 115, getHeight() * 45 / 115, true);
                canvas.drawBitmap(bm1, height / 2 - bm1.getWidth() / 2, height / 2 - bm1.getHeight() / 2, paint);
                break;
            case InfrarConPresent.MODEL_REFRI:
                //中间显示的
                bm1 = BitmapFactory.decodeResource(getResources(), R.drawable.infra_mrefri);
                bm1 = Bitmap.createScaledBitmap(bm1, getHeight() * 45 / 115, getHeight() * 45 / 115, true);
                canvas.drawBitmap(bm1, height / 2 - bm1.getWidth() / 2, height / 2 - bm1.getHeight() / 2, paint);
                break;
        }
        if (infraModel == InfrarConPresent.MODEL_HEAT || infraModel == InfrarConPresent.MODEL_REFRI) {
            //显示文字
            int deg = (29 - infraNum) * 15;
            int mx = (int) (height * 55 / 115 / 2 * Math.cos(Math.PI * deg / 180));
            int my = (int) (height * 55 / 115 / 2 * Math.sin(Math.PI * deg / 180));
//        Log.i(TAG, "onDraw: x" + mx + "y" + my+"q"+Math.sin(Math.PI*30/180));
            canvas.drawBitmap(bmp, height / 2 - bmp.getWidth() / 2 + mx, height / 2 - bmp.getHeight() / 2 - my, paint);
            paint.setTextSize(35);
            Paint.FontMetricsInt fontMetrics = paint.getFontMetricsInt();
            int baseline = (height - fontMetrics.bottom - fontMetrics.top) / 2;
            // 下面这行是实现水平居中，drawText对应改为传入targetRect.centerX()
            paint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText(String.valueOf(infraNum), height / 2, baseline, paint);
        }
    }

    public void setTemp(int temp) {
        infraNum = temp;
        invalidate();
    }

    public void setModel(int model) {
        infraModel = model;
        invalidate();
    }
}
