package com.goodchild.zibalba.widgets;

import android.content.Context;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.util.AttributeSet;
import android.widget.ImageView;
import com.androidquery.AQuery;

/**
 * Created by Engin Mercan on 21.01.2014.
 */
public class ZSepiaImageView extends ImageView {

    private static ColorMatrixColorFilter sepiaFilter;
    static{
        ColorMatrix matrixA = new ColorMatrix();
       /* // making image B&W
        matrixA.setSaturation(0.02f);

        float translate = 0.2f;
        float i_translate = 1.2f-translate;

        ColorMatrix matrixC = new ColorMatrix();
        matrixC.set(

                new float[]{
                        i_translate, 0.0f, 0.0f, 0.0f, translate,
                        0.0f, i_translate, 0.0f, 0.0f, translate,
                        0.0f, 0.0f, i_translate, 0.0f, translate,
                        0.0f, 0.0f, 0.0f, 1.0f, 0.0f});

        matrixA.setConcat(matrixC, matrixA);

        ColorMatrix matrixB = new ColorMatrix();
        // applying scales for RGB color values
        matrixB.setScale(0.62f, .50f, .0f, 1.0f);

        matrixA.setConcat(matrixB, matrixA);*/

        float translate = 0.2f;
        float i_translate = 1.0f-translate;
        matrixA.set(
                new float[]{
                        0.288f, 0.160f, 0.0f, 0.0f, 0.0f,
                        0.186f, 0.200f, 0.0f, 0.0f, 0.0f,
                        0.186f, 0.150f, 0.0f, 0.0f, 0.0f,
                        0.0f, 0.0f, 0.0f, 1.0f, 0.0f});

        ColorMatrix matrixC = new ColorMatrix();
        matrixC.set(

                new float[]{
                        i_translate, 0.0f, 0.0f, 0.0f, translate*255,
                        0.0f, i_translate, 0.0f, 0.0f, translate*255,
                        0.0f, 0.0f, i_translate, 0.0f, translate*255,
                        0.0f, 0.0f, 0.0f, 1.0f, 0.0f});

        matrixA.setConcat(matrixA, matrixC);

        sepiaFilter = new ColorMatrixColorFilter(matrixA);

    }


    public ZSepiaImageView(Context context) {
        super(context);
        initialize();
    }

    public ZSepiaImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public ZSepiaImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initialize();
    }

    private void initialize() {
        if(isInEditMode()) return;
        setColorFilter(sepiaFilter);
    }

    public void setImagePath(String imagePath) {
        AQuery aq = new AQuery(this);
        aq.image(imagePath, true, true);

    }

}
