package com.KnaIvER.polymer.matrix;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

/**
 * Created by Martin on 2016/7/19 0019.
 */
public class CustomView extends View {

    private Matrix matrix;
    private Paint paint1, paint2;


    public CustomView(Context context) {
        super(context);
    }

    public CustomView(Context context, AttributeSet attrs) {
        super(context, attrs);
        viewInit();
    }

    private void viewInit() {

        paint1 = new Paint();
        paint1.setColor(Color.BLACK);
        paint1.setStyle(Paint.Style.FILL);
        paint1.setTextSize(30);

        paint2 = new Paint();
        paint2.setColor(Color.BLUE);
        paint2.setStyle(Paint.Style.FILL);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
//        matrix = new Matrix();
//        matrix.postScale(2, 2);
//        canvas.save();
//        canvas.setMatrix(matrix);
//        canvas.concat(matrix);
        canvas.drawRect(100, 100, 300, 300, paint1);
//        canvas.restore();
        canvas.drawRect(300, 300, 500, 500, paint2);

        canvas.drawText("original", 50, 600, paint1);
    }


}
