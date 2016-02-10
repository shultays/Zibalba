package com.goodchild.zibalba.widgets;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.Button;
import android.widget.TextView;

public class ZTextView extends TextView {

    public static Typeface font;
    public ZTextView(Context context) {
        super(context);
        initialize();
    }

    public ZTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public ZTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initialize();
    }


    private void initialize() {
        if(isInEditMode()) return;
        if(font == null){
            font =  Typeface.createFromAsset(getContext().getAssets(), "aztec.ttf");
        }
        setTypeface(font);
    }

}
