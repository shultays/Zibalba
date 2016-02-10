package com.goodchild.zibalba.widgets;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.*;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import com.goodchild.zibalba.ClientConstants;
import com.goodchild.zibalba.R;
import com.goodchild.zibalba.models.BoardPosModel;
import com.goodchild.zibalba.models.TableModel;
import com.goodchild.zibalba.packets.Packet;
import com.goodchild.zibalba.packets.TableCommitPacket;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.Vector;

/**
 * Created by Engin on 1/16/14.
 */
public class CountDown extends View implements View.OnClickListener {

    Bitmap num0, num1, num5;

    int num = 103;
    boolean isMayan;
    private Typeface font;

    public CountDown(Context context) {
        super(context);
        initialize();
    }

    public CountDown(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public CountDown(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initialize();
    }

    private void initialize() {
        num0 = BitmapFactory.decodeResource(getResources(),
                R.drawable.num0);
        num1 = BitmapFactory.decodeResource(getResources(),
                R.drawable.num1);
        num5 = BitmapFactory.decodeResource(getResources(),
                R.drawable.num5);

        setBackgroundResource(R.drawable.tabbackground);

        if(!isInEditMode()){
            font =  Typeface.createFromAsset(getContext().getAssets(), "aztec.ttf");
            p.setTypeface(font);

            SharedPreferences sharedPreferences = getContext().getSharedPreferences("SHARED_PREFERENCES", Context.MODE_PRIVATE);
            isMayan = sharedPreferences.getBoolean(ClientConstants.MAYAN_COUNTDOWN_ENABLED, true);
        }
        p.setTextSize(24.0f);
        p.setTextAlign(Paint.Align.CENTER);
        p.setColor(0xFF4F3F00);


        setOnClickListener(this);
    }

    Paint p = new Paint();

    public void onDraw(Canvas canvas){
        int w = getWidth();
        int h = getHeight();
        if(!isMayan){
            canvas.save();

            canvas.scale(w / 50.0f, h / 50.0f, w / 2, h / 2);
            canvas.translate(w / 2, h / 2 + 7);
            canvas.scale(1.2f, 1, 0, 0);
            canvas.drawText(num+"", 0, 0, p);
            canvas.restore();

        }else{

            float hScale = h/400.0f;
            float wScale = w/400.0f;

            canvas.save();

            canvas.scale(w / 400.0f, h / 400.0f, w / 2, h / 2);
            canvas.translate(w / 2, h + 60);
            drawNum(canvas, num % 20);

            canvas.restore();

            canvas.save();
            canvas.scale(w/400.0f, h/400.0f, w / 2, h / 2);
            canvas.translate(w / 2, h - 140);
            if(num/20>0)drawNum(canvas, num / 20);

            canvas.restore();
        }

    }

    public void setNum(int num) {
        this.num = num;
        invalidate();
    }

    private void drawNum(Canvas canvas, int i) {
        if(i==0){
            canvas.translate(0, -60);
            canvas.drawBitmap(num0, -num0.getWidth()/2, -num0.getHeight()/2, null);
            return;
        }
        while(i>=5){
            canvas.drawBitmap(num5, -num5.getWidth()/2, -num5.getHeight()/2, null);
            canvas.translate(0, -30);
            i-=5;
        }
        if(i>0){
            int w = num1.getWidth()*i + 10*(i-1);
            int x = w/2;

            while(i-->0){
                canvas.drawBitmap(num1, x-num1.getWidth()/2 - num1.getWidth()/2, -num1.getHeight()/2 - num1.getHeight()/2, null);
                x -= num1.getWidth()+10;
            }
            canvas.translate(0, -30);
        }
    }

    @Override
    public void onClick(View view) {
        isMayan = !isMayan;

        SharedPreferences sharedPreferences = getContext().getSharedPreferences("SHARED_PREFERENCES", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(ClientConstants.MAYAN_COUNTDOWN_ENABLED, isMayan);
        editor.commit();
        invalidate();
    }
}
