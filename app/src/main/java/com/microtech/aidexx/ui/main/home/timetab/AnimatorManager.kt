package com.microtech.aidexx.ui.main.home.timetab

import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.constraintlayout.widget.ConstraintLayout

class AnimatorManager private constructor() {
    companion object {
        private val INSTANCE = AnimatorManager()

        fun instance(): AnimatorManager {
            return INSTANCE
        }
    }

    fun createTransAnimator(target: ConstraintLayout?, value: Float, duration: Long): ObjectAnimator {
        val transAnimator = ObjectAnimator.ofFloat(target, "translationY", value)
        transAnimator?.duration = duration
        transAnimator.interpolator = DecelerateInterpolator()
        return transAnimator
    }

    fun createAlphaAnimator(target: View?, from: Float, to: Float): ObjectAnimator {
        val alphaAnimator = ObjectAnimator.ofFloat(target, "alpha", from, to)
        alphaAnimator?.duration = 50
        return alphaAnimator
    }

    fun createArgbAnimator(from: Int, to: Int, duration: Long): ValueAnimator {
        val valueAnimator = ValueAnimator.ofInt(from, to)
        valueAnimator.duration = duration
        valueAnimator.setEvaluator(ArgbEvaluator())
        return valueAnimator
    }
}