package com.microtech.aidexx.ui.home.chart

import android.animation.Animator
import android.view.View
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.jobs.AnimatedZoomJob
import com.github.mikephil.charting.utils.ObjectPool
import com.github.mikephil.charting.utils.Transformer
import com.github.mikephil.charting.utils.ViewPortHandler

class MyAnimatedZoomJob(viewPortHandler: ViewPortHandler?,
                        v: View?,
                        trans: Transformer?,
                        axis: YAxis?,
                        xAxisRange: Float,
                        scaleX: Float,
                        scaleY: Float,
                        xOrigin: Float,
                        yOrigin: Float,
                        zoomCenterX: Float,
                        zoomCenterY: Float,
                        zoomOriginX: Float,
                        zoomOriginY: Float,
                        duration: Long
) : AnimatedZoomJob(viewPortHandler, v, trans, axis, xAxisRange, scaleX, scaleY, xOrigin, yOrigin,
    zoomCenterX, zoomCenterY, zoomOriginX, zoomOriginY, duration) {

    private var inAnimation = false

    companion object {
        private var pool: ObjectPool<MyAnimatedZoomJob>? = null
        var animators = 0
            private set

        init {
            pool = ObjectPool.create(8,
                MyAnimatedZoomJob(null, null, null, null,
                    0f, 0f, 0f, 0f, 0f,
                    0f, 0f, 0f, 0f,
                    0)) as ObjectPool<MyAnimatedZoomJob>
        }

        fun getInstance(
            viewPortHandler: ViewPortHandler?,
            v: View?,
            trans: Transformer?,
            axis: YAxis?,
            xAxisRange: Float,
            scaleX: Float,
            scaleY: Float,
            xOrigin: Float,
            yOrigin: Float,
            zoomCenterX: Float,
            zoomCenterY: Float,
            zoomOriginX: Float,
            zoomOriginY: Float,
            duration: Long
        ): MyAnimatedZoomJob? {
            val result = pool?.get()
            result?.mViewPortHandler = viewPortHandler
            result?.xValue = scaleX
            result?.yValue = scaleY
            result?.mTrans = trans
            result?.view = v
            result?.xOrigin = xOrigin
            result?.yOrigin = yOrigin
            result?.zoomCenterX = zoomCenterX
            result?.zoomCenterY = zoomCenterY
            result?.zoomOriginX = zoomOriginX
            result?.zoomOriginY = zoomOriginY
            result?.yAxis = axis
            result?.xAxisRange = xAxisRange
            result?.resetAnimator()
            result?.animator?.duration = duration
            return result
        }
    }

    override fun onAnimationStart(animation: Animator?) {
        if (!inAnimation) {
            inAnimation = true
            animators++
        }
        super.onAnimationStart(animation)
    }

    override fun onAnimationEnd(animation: Animator?) {
        super.onAnimationEnd(animation)
        if (inAnimation) {
            inAnimation = false
            animators--
        }
    }

    override fun instantiate(): ObjectPool.Poolable {
        return MyAnimatedZoomJob(null, null, null, null,
            0f, 0f, 0f, 0f, 0f,
            0f, 0f, 0f, 0f,
            0)
    }
}