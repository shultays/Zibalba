package com.goodchild.zibalba.widgets;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;

/**
 * Created by Engin Mercan on 22.01.2014.
 */
public class FunkyText extends View {
    Typeface font;
    String text = "ZIBALBA";


    int maxW = -1, maxH = -1;

    Paint p = new Paint();
    Rect rc = new Rect();

    float heightLimit, widthLimit;
    float textSize = 80.0f;

    float ang = 80;
    OvershootInterpolator interpolator = new OvershootInterpolator();
    DecelerateInterpolator scaleInterpolator = new DecelerateInterpolator();


    int w=-1, h=-1;
    float scale;
    long startTime;

    Animation.AnimationListener listener;
    boolean eventSent = false;

    public FunkyText(Context context) {
        super(context);
        initialize();
    }

    public FunkyText(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public FunkyText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize();
    }

    private void initialize() {
        if(!isInEditMode()){
            font = Typeface.createFromAsset(getContext().getAssets(), "aztec.ttf");
            p.setTypeface(font);
        }
        p.setTextAlign(Paint.Align.CENTER);
        p.setTextSize(textSize);
        p.setColor(Color.RED);
        for(int i = 0; i<text.length(); i++){
            p.getTextBounds(text, i, i + 1, rc);
            maxW = Math.max(maxW, rc.width());
            maxH = Math.max(maxH, rc.height());

        }
        heightLimit = 200+maxH;
        widthLimit = (float) (Math.cos((180 - ang) / 2)*Math.PI/2)*(heightLimit);
    }

    public void setAnimationListener(Animation.AnimationListener listener){
        this.listener = listener;
    }

    public void onDraw(Canvas canvas){
        if(startTime == 0) startTime = System.currentTimeMillis();
        if(System.currentTimeMillis() > startTime+3000 && listener != null && eventSent == false){
            eventSent = true;
            listener.onAnimationEnd(null);
        }
        if(isInEditMode()) startTime = System.currentTimeMillis()-5000;
        float t = (System.currentTimeMillis()-startTime)/1800.0f;

        if(w != getWidth() || h != getHeight()){
            w = getWidth();
            h = getHeight();

            float heightScale = (h/2)/(heightLimit);
            float widthScale = (w)/(widthLimit);
            scale = Math.min(heightScale, widthScale);
        }

        canvas.save();
        float moveT = (t-1.5f)*3;
        moveT = 1-interpolator.getInterpolation(Math.min(Math.max(moveT, 0.0f), 1.0f));
        canvas.translate(w/2, h/2+heightLimit*(moveT)*scale*0.5f);

        canvas.scale(scale, scale, 0, 0);

        for(int i=0; i<text.length(); i++){
            float letterT = (t-0.11f*i)/(1-0.11f*(text.length()-1));
            float animateT = Math.min(letterT, 1.0f);
            animateT = Math.max(animateT, 0.0f);
            float scaleT = letterT-animateT+0.05f;
            scaleT = Math.max(scaleT, 0.0f);
            scaleT = 1-Math.abs(scaleT-0.5f)*2;
            if(scaleT<0) scaleT = 0.0f;
            scaleT = scaleInterpolator.getInterpolation(scaleT);
            animateT = interpolator.getInterpolation(animateT);
            int alpha = (int) (Math.min(Math.max(letterT * 10, 0.0f), 1.0f)*255);
            
            float a = (ang*i)/(text.length()-1);
            canvas.save();
            canvas.rotate(a - ang / 2, 0, 0);
            canvas.scale(1.2f, 1, 0, 0);
            canvas.translate(0, -200 * (2.5f - animateT * 1.5f));
            canvas.scale(1 + scaleT, 1 + scaleT, 0, 0);
            p.setColor(Color.argb(alpha, 51, 34, 16));
            p.setTextSize(textSize);
            canvas.drawText(text.charAt(i) + "", 0, 0, p);
            p.getTextBounds(text, i, i+1, rc);

            canvas.translate(0, rc.height()*0.1f);

            p.setTextSize(textSize*0.9f);
            p.setColor(Color.argb(alpha, 206, 136, 66));
            canvas.drawText(text.charAt(i)+"", 0, 0, p);

            canvas.restore();
        }

        canvas.restore();

        invalidate();
    }
}
