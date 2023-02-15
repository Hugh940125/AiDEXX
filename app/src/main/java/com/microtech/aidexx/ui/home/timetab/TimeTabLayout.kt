package com.microtech.aidexx.ui.home.timetab

import android.animation.Animator
import android.animation.AnimatorSet
import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.microtech.aidexx.R
import com.microtech.aidexx.utils.DensityUtils
import com.microtech.aidexx.utils.ThemeManager

private const val UP_LIFT_DP: Float = 15F
private const val ANIM_DURATION: Long = 200

class TimeTabLayout : LinearLayoutCompat, View.OnClickListener {
    private lateinit var mContext: Context
    var currentSelect = 0
    var lastSelect = 0
    var layoutSix: ConstraintLayout? = null
    var layoutTwelve: ConstraintLayout? = null
    var layoutTwentyFour: ConstraintLayout? = null
    var tvTwentyFour: TextView? = null
    var tvTwelve: TextView? = null
    var tvSix: TextView? = null
    var bgTwentyFour: View? = null
    var bgTwelve: View? = null
    var bgSix: View? = null
    var upLiftPx: Float = 0F
    var onTabChange: ((pos: Int) -> Unit)? = null

    constructor(context: Context) : this(context, null)

    constructor(
        context: Context,
        attrs: AttributeSet?,
    ) : this(context, attrs, 0)

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
    ) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(context)
    }

    fun init(context: Context) {
        mContext = context
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.layout_time_tab, this, true)
        layoutSix = view.findViewById(R.id.layout_six)
        layoutTwelve = view.findViewById(R.id.layout_twelve)
        layoutTwentyFour = view.findViewById(R.id.layout_twenty_four)
        bgSix = view.findViewById(R.id.bg_six)
        bgTwelve = view.findViewById(R.id.bg_twelve)
        bgTwentyFour = view.findViewById(R.id.bg_twenty_four)
        tvSix = view.findViewById(R.id.tv_six)
        tvTwelve = view.findViewById(R.id.tv_twelve)
        tvTwentyFour = view.findViewById(R.id.tv_twenty_four)
        upLiftPx = DensityUtils.dp2pix(context, UP_LIFT_DP)
        layoutSix?.setOnClickListener(this)
        layoutTwelve?.setOnClickListener(this)
        layoutTwentyFour?.setOnClickListener(this)
        val animatorSet = AnimatorSet()
        val transAnimator =
            AnimatorManager.instance().createTransAnimator(layoutSix, -upLiftPx, 50)
        val alphaAnimator =
            AnimatorManager.instance().createAlphaAnimator(bgSix, 0F, 1F, 50)
        animatorSet.playTogether(transAnimator, alphaAnimator)
        animatorSet.start()
        tvSix?.typeface = Typeface.DEFAULT_BOLD
        if (ThemeManager.isLight()) {
            tvSix?.setTextColor(ContextCompat.getColor(mContext, R.color.green_65))
        } else {
            tvSix?.setTextColor(ContextCompat.getColor(mContext, R.color.gray_e6))
        }
    }

    override fun onClick(v: View?) {
        lastSelect = currentSelect
        when (v?.id) {
            R.id.layout_six -> {
                select(0)
                currentSelect = 0
            }
            R.id.layout_twelve -> {
                select(1)
                currentSelect = 1
            }
            R.id.layout_twenty_four -> {
                select(2)
                currentSelect = 2
            }
        }
        unselect(lastSelect)
        if (lastSelect == currentSelect) {
            return
        }
        onTabChange?.invoke(currentSelect)
    }

    fun select(pos: Int) {
        if (pos == currentSelect) {
            return
        }
        var layoutTarget: ConstraintLayout? = null
        var bgTarget: View? = null
        var tvTarget: TextView? = null
        when (pos) {
            0 -> {
                layoutTarget = layoutSix
                bgTarget = bgSix
                tvTarget = tvSix
            }
            1 -> {
                layoutTarget = layoutTwelve
                bgTarget = bgTwelve
                tvTarget = tvTwelve
            }
            2 -> {
                layoutTarget = layoutTwentyFour
                bgTarget = bgTwentyFour
                tvTarget = tvTwentyFour
            }
        }
        val animatorSet = AnimatorSet()
        val transAnimator =
            AnimatorManager.instance().createTransAnimator(layoutTarget, -upLiftPx, ANIM_DURATION)
        val alphaAnimator =
            AnimatorManager.instance().createAlphaAnimator(bgTarget, 0F, 1F, ANIM_DURATION)
        val argbAnimator = AnimatorManager.instance()
            .createArgbAnimator(ContextCompat.getColor(mContext, R.color.gray_d8),
                if (ThemeManager.isLight())
                    ContextCompat.getColor(mContext,
                        R.color.green_65) else ContextCompat.getColor(mContext, R.color.gray_e6),
                ANIM_DURATION)
        argbAnimator.addUpdateListener {
            tvTarget?.setTextColor(it.animatedValue as Int)
        }
        animatorSet.playTogether(transAnimator, alphaAnimator, argbAnimator)
        animatorSet.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator?) {
                tvTarget?.typeface = Typeface.DEFAULT_BOLD
            }

            override fun onAnimationEnd(animation: Animator?) {
                if (ThemeManager.isLight()) {
                    tvTarget?.setTextColor(ContextCompat.getColor(mContext, R.color.green_65))
                } else {
                    tvTarget?.setTextColor(ContextCompat.getColor(mContext, R.color.gray_e6))
                }
            }

            override fun onAnimationCancel(animation: Animator?) {
            }

            override fun onAnimationRepeat(animation: Animator?) {
            }
        })
        animatorSet.start()
    }

    fun unselect(pos: Int) {
        if (pos == currentSelect) {
            return
        }
        var layoutTarget: ConstraintLayout? = null
        var bgTarget: View? = null
        var tvTarget: TextView? = null
        when (pos) {
            0 -> {
                layoutTarget = layoutSix
                bgTarget = bgSix
                tvTarget = tvSix
            }
            1 -> {
                layoutTarget = layoutTwelve
                bgTarget = bgTwelve
                tvTarget = tvTwelve
            }
            2 -> {
                layoutTarget = layoutTwentyFour
                bgTarget = bgTwentyFour
                tvTarget = tvTwentyFour
            }
        }
        val animatorSet = AnimatorSet()
        val transAnimator =
            AnimatorManager.instance().createTransAnimator(layoutTarget, 0F, ANIM_DURATION)
        val alphaAnimator =
            AnimatorManager.instance().createAlphaAnimator(bgTarget, 1F, 0F, ANIM_DURATION)
        val argbAnimator = AnimatorManager.instance()
            .createArgbAnimator(if (ThemeManager.isLight())
                ContextCompat.getColor(mContext,
                    R.color.green_65) else ContextCompat.getColor(mContext, R.color.gray_e6),
                ContextCompat.getColor(mContext, R.color.gray_d8), ANIM_DURATION)
        argbAnimator.addUpdateListener {
            tvTarget?.setTextColor(it.animatedValue as Int)
        }
        animatorSet.playTogether(transAnimator, alphaAnimator, argbAnimator)
        animatorSet.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator?) {
                tvTarget?.typeface = Typeface.DEFAULT
            }

            override fun onAnimationEnd(animation: Animator?) {
                tvTarget?.setTextColor(ContextCompat.getColor(mContext, R.color.gray_d8))
                tvTarget?.setTextColor(ContextCompat.getColor(mContext, R.color.gray_d8))
            }

            override fun onAnimationCancel(animation: Animator?) {
            }

            override fun onAnimationRepeat(animation: Animator?) {
            }
        })
        animatorSet.start()
    }

}