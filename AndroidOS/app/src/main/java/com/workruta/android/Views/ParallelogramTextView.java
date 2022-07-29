package com.workruta.android.Views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.util.AttributeSet;

public class ParallelogramTextView extends androidx.appcompat.widget.AppCompatTextView {

    public ParallelogramTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    @Override
    public void draw(Canvas canvas) {
        Path path = new Path();
        path.moveTo(getWidth(), 0);
        path.lineTo(0, 0);
        path.lineTo(0, getHeight());
        path.lineTo(getWidth() - 50, getHeight());
        path.lineTo(getWidth(), 0);
        canvas.save();
        canvas.clipPath(path);
        super.draw(canvas);
        canvas.restore();
    }

}
