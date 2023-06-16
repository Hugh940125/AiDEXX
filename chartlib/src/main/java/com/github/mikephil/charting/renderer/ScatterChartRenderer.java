package com.github.mikephil.charting.renderer;

import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.github.mikephil.charting.animation.ChartAnimator;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.ScatterData;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.dataprovider.ScatterDataProvider;
import com.github.mikephil.charting.interfaces.datasets.IScatterDataSet;
import com.github.mikephil.charting.renderer.scatter.IShapeRenderer;
import com.github.mikephil.charting.utils.MPPointD;
import com.github.mikephil.charting.utils.MPPointF;
import com.github.mikephil.charting.utils.Transformer;
import com.github.mikephil.charting.utils.Utils;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.util.List;

public class ScatterChartRenderer extends LineScatterCandleRadarRenderer {

    protected ScatterDataProvider mChart;

    public ScatterChartRenderer(ScatterDataProvider chart, ChartAnimator animator, ViewPortHandler viewPortHandler) {
        super(animator, viewPortHandler);
        mChart = chart;
    }

    @Override
    public void initBuffers() {
    }

    @Override
    public void drawData(Canvas c) {

        ScatterData scatterData = mChart.getScatterData();

        for (IScatterDataSet set : scatterData.getDataSets()) {

            if (set.isVisible())
                drawDataSet(c, set);
        }
    }

    float[] mPixelBuffer = new float[2];

    protected void drawDataSet(Canvas c, IScatterDataSet dataSet) {

        if (dataSet.getEntryCount() < 1)
            return;

        ViewPortHandler viewPortHandler = mViewPortHandler;

        Transformer trans = mChart.getTransformer(dataSet.getAxisDependency());

        float phaseY = mAnimator.getPhaseY();

        IShapeRenderer renderer = dataSet.getShapeRenderer();
        if (renderer == null) {
            Log.i("MISSING", "There's no IShapeRenderer specified for ScatterDataSet");
            return;
        }

        int max = (int)(Math.min(
                Math.ceil((float)dataSet.getEntryCount() * mAnimator.getPhaseX()),
                (float)dataSet.getEntryCount()));

        for (int i = 0; i < max; i++) {

            Entry e = dataSet.getEntryForIndex(i);

            mPixelBuffer[0] = e.getX();
            mPixelBuffer[1] = e.getY() * phaseY;

            trans.pointValuesToPixel(mPixelBuffer);

            if (!viewPortHandler.isInBoundsRight(mPixelBuffer[0]))
                break;

            if (!viewPortHandler.isInBoundsLeft(mPixelBuffer[0])
                    || !viewPortHandler.isInBoundsY(mPixelBuffer[1]))
                continue;

            mRenderPaint.setColor(dataSet.getColor(i / 2));
            renderer.renderShape(
                    c, dataSet, mViewPortHandler,
                    mPixelBuffer[0], mPixelBuffer[1],
                    mRenderPaint);
        }
    }

    @Override
    public void drawValues(Canvas c) {

        // if values are drawn
        if (isDrawingValuesAllowed(mChart)) {

            List<IScatterDataSet> dataSets = mChart.getScatterData().getDataSets();

            for (int i = 0; i < mChart.getScatterData().getDataSetCount(); i++) {

                IScatterDataSet dataSet = dataSets.get(i);
                boolean attached =  dataSet.isAttachedToLineDataSet();

                if (!shouldDrawValues(dataSet) || dataSet.getEntryCount() < 1)
                    continue;

                // apply the text-styling defined by the DataSet
                applyValueTextStyle(dataSet);

                mXBounds.set(mChart, dataSet);

                float[] positions = mChart.getTransformer(dataSet.getAxisDependency())
                        .generateTransformedValuesScatter(dataSet,
                                mAnimator.getPhaseX(), mAnimator.getPhaseY(), mXBounds.min, mXBounds.max);

                float shapeSize = Utils.convertDpToPixel(dataSet.getScatterShapeSize());

                ValueFormatter formatter = dataSet.getValueFormatter();

                MPPointF iconsOffset = MPPointF.getInstance(dataSet.getIconsOffset());
                iconsOffset.x = Utils.convertDpToPixel(iconsOffset.x);
                iconsOffset.y = Utils.convertDpToPixel(iconsOffset.y);

                for (int j = 0; j < positions.length; j += 2) {

                    if (!mViewPortHandler.isInBoundsRight(positions[j]))
                        break;

                    // make sure the lines don't do shitty things outside bounds
                    if ((!mViewPortHandler.isInBoundsLeft(positions[j])
                            || !mViewPortHandler.isInBoundsY(positions[j + 1])))
                        continue;

                    Entry entry = dataSet.getEntryForIndex(j / 2 + mXBounds.min);

                    if (dataSet.isDrawValuesEnabled()) {
                        drawValue(c, formatter.getPointLabel(entry), positions[j], positions[j + 1] - shapeSize, dataSet.getValueTextColor(j / 2 + mXBounds.min));
                    }

                    if (entry.getIcon() != null && dataSet.isDrawIconsEnabled()) {

                        Drawable icon = entry.getIcon();

                        if (attached) {
                            for (int k = 0; k < j; k += 2) {
                                Drawable existingIcon = dataSet.getEntryForIndex(k / 2 + mXBounds.min).getIcon();
                                if (existingIcon == null) {
                                    continue;
                                }
                                int width = (icon.getIntrinsicWidth() + existingIcon.getIntrinsicWidth()) / 2;
                                int height = (icon.getIntrinsicHeight() + existingIcon.getIntrinsicHeight()) / 2;
                                if (Math.abs(positions[j] - positions[k]) < width && Math.abs(positions[j + 1] - positions[k + 1]) < height) {
                                    positions[j + 1] = positions[k + 1] - height;
                                }
                            }
                        }

                        Utils.drawImage(
                                c,
                                icon,
                                (int)(positions[j] + iconsOffset.x),
                                (int)(positions[j + 1] + iconsOffset.y),
                                icon.getIntrinsicWidth(),
                                icon.getIntrinsicHeight());
                    }
                }

                MPPointF.recycleInstance(iconsOffset);
            }
        }
    }

    public void drawValues(Canvas c, List<Float> attachPositions) {

        // if values are drawn
        if (isDrawingValuesAllowed(mChart)) {

            List<IScatterDataSet> dataSets = mChart.getScatterData().getDataSets();

            for (int i = 0; i < mChart.getScatterData().getDataSetCount(); i++) {

                IScatterDataSet dataSet = dataSets.get(i);
                boolean attached =  dataSet.isAttachedToLineDataSet();

                if (!shouldDrawValues(dataSet) || dataSet.getEntryCount() < 1)
                    continue;

                // apply the text-styling defined by the DataSet
                applyValueTextStyle(dataSet);

                mXBounds.set(mChart, dataSet);

                float[] positions = mChart.getTransformer(dataSet.getAxisDependency())
                        .generateTransformedValuesScatter(dataSet,
                                mAnimator.getPhaseX(), mAnimator.getPhaseY(), mXBounds.min, mXBounds.max);

                if (attached) {
                    for (int j = 0; j < positions.length; j += 2) {
                        float minDx = 20;
                        int minDxI = 0;
                        for (int k = 0; k < attachPositions.size(); k += 2) {
                            if (attachPositions.get(k) != null){
                                float dx = Math.abs(positions[j] - attachPositions.get(k));
                                if (dx < minDx) {
                                    minDx = dx;
                                    minDxI = k;
                                }
                            }
                        }
                        if (minDx < 20) {
                            positions[j + 1] = attachPositions.get(minDxI + 1);
                        }
                    }
                }

                float shapeSize = Utils.convertDpToPixel(dataSet.getScatterShapeSize());

                ValueFormatter formatter = dataSet.getValueFormatter();

                MPPointF iconsOffset = MPPointF.getInstance(dataSet.getIconsOffset());
                iconsOffset.x = Utils.convertDpToPixel(iconsOffset.x);
                iconsOffset.y = Utils.convertDpToPixel(iconsOffset.y);

                for (int j = 0; j < positions.length; j += 2) {

                    if (!mViewPortHandler.isInBoundsRight(positions[j]))
                        break;

                    // make sure the lines don't do shitty things outside bounds
                    if ((!mViewPortHandler.isInBoundsLeft(positions[j])
                            || !mViewPortHandler.isInBoundsY(positions[j + 1])))
                        continue;

                    Entry entry = dataSet.getEntryForIndex(j / 2 + mXBounds.min);

                    if (dataSet.isDrawValuesEnabled()) {
                        drawValue(c, formatter.getPointLabel(entry), positions[j], positions[j + 1] - shapeSize, dataSet.getValueTextColor(j / 2 + mXBounds.min));
                    }

                    if (entry.getIcon() != null && dataSet.isDrawIconsEnabled()) {

                        Drawable icon = entry.getIcon();

                        if (attached) {
                            for (int k = 0; k < j; k += 2) {
                                Drawable existingIcon = dataSet.getEntryForIndex(k / 2 + mXBounds.min).getIcon();
                                if (existingIcon == null) {
                                    continue;
                                }
                                int width = (icon.getIntrinsicWidth() + existingIcon.getIntrinsicWidth()) / 2;
                                int height = (icon.getIntrinsicHeight() + existingIcon.getIntrinsicHeight()) / 2;
                                if (Math.abs(positions[j] - positions[k]) < width && Math.abs(positions[j + 1] - positions[k + 1]) < height) {
                                    positions[j + 1] = positions[k + 1] - height;
                                }
                            }
                        }

                        if (attached) {
                            for (int k = 0; k < j; k += 2) {
                                Drawable existingIcon = dataSet.getEntryForIndex(k / 2 + mXBounds.min).getIcon();
                                if (existingIcon == null) {
                                    continue;
                                }
                                int width = (icon.getIntrinsicWidth() + existingIcon.getIntrinsicWidth()) / 2;
                                int height = (icon.getIntrinsicHeight() + existingIcon.getIntrinsicHeight()) / 2;
                                if (Math.abs(positions[j] - positions[k]) < width && Math.abs(positions[j + 1] - positions[k + 1]) < height) {
                                    positions[j + 1] = positions[k + 1] - height;
                                }
                            }
                        }

                        Utils.drawImage(
                                c,
                                icon,
                                (int)(positions[j] + iconsOffset.x),
                                (int)(positions[j + 1] + iconsOffset.y),
                                icon.getIntrinsicWidth(),
                                icon.getIntrinsicHeight());
                    }
                }

                MPPointF.recycleInstance(iconsOffset);
            }
        }
    }

    @Override
    public void drawValue(Canvas c, String valueText, float x, float y, int color) {
        mValuePaint.setColor(color);
        c.drawText(valueText, x, y, mValuePaint);
    }

    @Override
    public void drawExtras(Canvas c) {
    }

    @Override
    public void drawHighlighted(Canvas c, Highlight[] indices) {

        ScatterData scatterData = mChart.getScatterData();

        for (Highlight high : indices) {

            IScatterDataSet set = scatterData.getDataSetByIndex(high.getDataSetIndex());

            if (set == null || !set.isHighlightEnabled())
                continue;

            final Entry e = set.getEntryForXValue(high.getX(), high.getY());

            if (!isInBoundsX(e, set))
                continue;

            MPPointD pix = mChart.getTransformer(set.getAxisDependency()).getPixelForValues(e.getX(), e.getY() * mAnimator
                    .getPhaseY());

            high.setDraw((float) pix.x, (float) pix.y);

            // draw the lines
            drawHighlightLines(c, (float) pix.x, (float) pix.y, set);
        }
    }
}
