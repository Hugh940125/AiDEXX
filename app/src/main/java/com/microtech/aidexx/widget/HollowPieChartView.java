package com.microtech.aidexx.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Region;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import com.microtech.aidexx.R;
import com.microtech.aidexx.utils.LogUtil;


/**
 * 饼状统计图，带有标注线，都可以自行设定其多种参数选项
 * <p/>
 * Created By: Seal.Wu
 */
public class HollowPieChartView extends View {

    private TextPaint mTextPaint;
    private float mTextWidth;
    private float mTextHeight;
    float centerX = 0;
    float centerY = 0;

    /**
     * 饼图半径
     */
    private float pieChartCircleRadius = 100;

    private float textBottom;
    /**
     * 记录文字大小
     */
    private float mTextSize = 14;

    /**
     * 饼图所占矩形区域（不包括文字）
     */
    private final RectF pieChartCircleRectF = new RectF();
    private final Path path = new Path();
    private final Paint paint = new Paint();

    /**
     * 饼状图信息列表
     */
    private PieceDataHolder pieceDataHolder;


    /**
     * 标记线长度
     */
    private float markerLineLength = 10f;

    public HollowPieChartView(Context context) {
        super(context);
        init(null, 0);
    }

    public HollowPieChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public HollowPieChartView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.HollowPieChartView, defStyle, 0);

        pieChartCircleRadius = a.getDimension(
                R.styleable.HollowPieChartView_pieRadius,
                pieChartCircleRadius);
        LogUtil.d("pieChartCircleRadius: " + pieChartCircleRadius, "");

        mTextSize = a.getDimension(R.styleable.HollowPieChartView_pieTextSize, mTextSize) / getResources().getDisplayMetrics().density;

        a.recycle();

        // Set up a default TextPaint object
        mTextPaint = new TextPaint();
        mTextPaint.setFlags(Paint.ANTI_ALIAS_FLAG);


        // Update TextPaint and text measurements from attributes
        invalidateTextPaintAndMeasurements();
    }

    private void invalidateTextPaintAndMeasurements() {

        mTextPaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, mTextSize, getContext().getResources().getDisplayMetrics()));

        Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();
        mTextHeight = fontMetrics.descent - fontMetrics.ascent;
        textBottom = fontMetrics.bottom;
    }

    /**
     * 设置饼状图的半径
     *
     * @param pieChartCircleRadius 饼状图的半径（px）
     */
    public void setPieChartCircleRadius(int pieChartCircleRadius) {

        this.pieChartCircleRadius = pieChartCircleRadius;

        invalidate();
    }

    /**
     * 设置标记线的长度
     *
     * @param markerLineLength 标记线的长度（px）
     */
    public void setMarkerLineLength(int markerLineLength) {
        this.markerLineLength = markerLineLength;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (null == pieceDataHolder) {
            return;
        }
        initPieChartCircleRectF();
        drawAllSectors(canvas);
        path.reset();
        path.addCircle(centerX, centerY, pieChartCircleRadius * 0.7f, Path.Direction.CCW);
        canvas.clipPath(path, Region.Op.INTERSECT); // 2.
        canvas.drawColor(Color.WHITE);  // 3.

        drawCenterText(canvas);

        paint.setColor(Color.RED);
        paint.setStrokeWidth(3);
//        canvas.drawPoint(centerX, centerY, paint);
    }

    private void drawCenterText(Canvas canvas) {
        mTextPaint.setColor(pieceDataHolder.color);
        mTextPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();
        float distance = (fontMetrics.bottom - fontMetrics.top) / 2 - fontMetrics.bottom;
        float baseline = centerY + distance;
        canvas.drawText(pieceDataHolder.marker, centerX, baseline, mTextPaint);
    }

    private void drawAllSectors(Canvas canvas) {
        float startAngel = 0;
        float sweepAngel = pieceDataHolder.value / 100 * 360;
        drawSector(canvas, pieceDataHolder.color, startAngel, sweepAngel);

        startAngel = startAngel + sweepAngel;
        sweepAngel = (100 - pieceDataHolder.value) / 100 * 360;
        drawSector(canvas, pieceDataHolder.backGroundColor, startAngel, sweepAngel);
    }

    private void initPieChartCircleRectF() {
        pieChartCircleRectF.left = getWidth() / 2f - pieChartCircleRadius;
        pieChartCircleRectF.top = getHeight() / 2f - pieChartCircleRadius;
        pieChartCircleRectF.right = pieChartCircleRectF.left + pieChartCircleRadius * 2;
        pieChartCircleRectF.bottom = pieChartCircleRectF.top + pieChartCircleRadius * 2;
        centerX = (pieChartCircleRectF.left + pieChartCircleRectF.right) / 2;
        centerY = (pieChartCircleRectF.top + pieChartCircleRectF.bottom) / 2;
    }

    /**
     * Gets the example dimension attribute value.
     *
     * @return The example dimension attribute value.(sp)
     */
    public float getTextSize() {
        return mTextSize;
    }

    /**
     * Sets the view's text dimension attribute value. In the PieChartView view, this dimension
     * is the font size.
     *
     * @param textSize The text dimension attribute value to use.(sp)
     */
    public void setTextSize(float textSize) {
        mTextSize = textSize;
        invalidateTextPaintAndMeasurements();
    }

    /**
     * 设置饼状图要显示的数据
     *
     * @param data 列表数据
     */
    public void setData(PieceDataHolder data) {

        if (data != null) {
            pieceDataHolder = data;
        }

        invalidate();
    }

    /**
     * 绘制扇形
     *
     * @param canvas     画布
     * @param color      要绘制扇形的颜色
     * @param startAngle 起始角度
     * @param sweepAngle 结束角度
     */
    protected void drawSector(Canvas canvas, int color, float startAngle, float sweepAngle) {

        Paint paint = new Paint();
        paint.setFlags(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(color);

        canvas.drawArc(pieChartCircleRectF, startAngle, sweepAngle, true, paint);

    }

    /**
     * 绘制标注线和标记文字
     *
     * @param canvas      画布
     * @param color       标记的颜色
     * @param rotateAngel 标记线和水平相差旋转的角度
     */
    protected void drawMarkerLineAndText(Canvas canvas, int color, float rotateAngel, String text) {
        Paint paint = new Paint();
        paint.setFlags(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(color);

        Path path = new Path();
        path.close();
        path.moveTo(getWidth() >> 1, getHeight() >> 1);
        final float x = (float) (getWidth() / 2 + (markerLineLength + pieChartCircleRadius) * Math.cos(Math.toRadians(rotateAngel)));
        final float y = (float) (getHeight() / 2 + (markerLineLength + pieChartCircleRadius) * Math.sin(Math.toRadians(rotateAngel)));
        path.lineTo(x, y);
        float landLineX;
        if (270f > rotateAngel && rotateAngel > 90f) {
            landLineX = x - 20;
        } else {
            landLineX = x + 20;
        }
        path.lineTo(landLineX, y);
        canvas.drawPath(path, paint);

        mTextPaint.setColor(color);

        if (270f > rotateAngel && rotateAngel > 90f) {
            float textWidth = mTextPaint.measureText(text);
            canvas.drawText(text, landLineX - textWidth, y + mTextHeight / 2 - textBottom, mTextPaint);

        } else {
            canvas.drawText(text, landLineX, y + mTextHeight / 2 - textBottom, mTextPaint);
        }
    }

    /**
     * 饼状图每块的信息持有者
     */
    public static final class PieceDataHolder {

        /**
         * 每块扇形的值的大小
         */
        private float value;

        /**
         * 扇形的颜色
         */
        private int color;


        /**
         * 扇形背景色
         */
        private int backGroundColor;

        /**
         * 每块的标记
         */
        private String marker;


        public PieceDataHolder(float value, int color, int backGroundColor, String marker) {
            this.value = value;
            this.color = color;
            this.backGroundColor = backGroundColor;
            this.marker = marker;
        }
    }

}
