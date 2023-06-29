package com.microtech.aidexx.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.util.AttributeSet;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

@SuppressLint("AppCompatCustomView")
public class DashView extends TextView {

    public DashView(@NonNull Context context) {
        this(context, null);
    }

    public DashView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, android.R.attr.textViewStyle);
    }

    public DashView(
            @NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private final Paint linePaint = new Paint();
    private final Path linePath = new Path();
    private final PathEffect pathEffect = new DashPathEffect(new float[]{30, 10, 30, 10}, 2);


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        linePaint.reset();
        linePaint.setAntiAlias(true);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(10);
        linePaint.setColor(getTextColors().getDefaultColor());
        linePaint.setPathEffect(pathEffect);
        linePath.reset();

        linePath.moveTo(0, 0);
        linePath.lineTo(0, getHeight());
        canvas.drawPath(linePath, linePaint);
    }
}