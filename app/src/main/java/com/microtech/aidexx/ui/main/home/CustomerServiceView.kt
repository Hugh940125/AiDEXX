package com.microtech.aidexx.ui.main.home

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Rect
import android.os.*
import android.os.VibrationEffect.DEFAULT_AMPLITUDE
import android.util.AttributeSet
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import com.microtech.aidexx.R
import com.microtech.aidexx.utils.DensityUtils
import com.microtech.aidexx.utils.ThemeManager
import com.microtech.aidexx.utils.mmkv.MmkvManager
import com.microtech.aidexx.widget.dialog.customerservice.MessageManager
import java.util.*
import kotlin.math.abs

private const val MIN_DELAY_TIME = 1000

class CustomerServiceView : LinearLayout {
    private lateinit var mFrameRect: Rect
    private var isMoving: Boolean = false
    private lateinit var mContext: Context
    private var isLongClick: Boolean = false
    private var isInMotion: Boolean = false
    private lateinit var timer: Timer
    private var isConsume: Boolean = false
    private lateinit var childRect: Rect
    private lateinit var child: RelativeLayout
    private var lastY: Int = 0
    private var lastX: Int = 0
    private var downY: Int = 0
    private var downX: Int = 0
    private var moveY: Int = 0
    private var moveX: Int = 0
    private var lastClickTime: Long = 0
    var onClick: (() -> Unit)? = null
    lateinit var serviceImageView: ImageView
    lateinit var tvMessageCount: TextView
    lateinit var mHandler: Handler
    private val layoutRunnable = {
        child.requestLayout()
    }

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(context)
    }

    fun init(context: Context) {
        mHandler = Handler(Looper.getMainLooper())
        this.mContext = context
        if (childCount > 0) {
            removeAllViews()
        }
        child = RelativeLayout(context)
        child.background = ContextCompat.getDrawable(context, R.drawable.bg_customer_service)
        child.layoutParams = LayoutParams(DensityUtils.dp2px(50f), DensityUtils.dp2px(50f))
        serviceImageView = ImageView(context)
        val layoutParams =
            RelativeLayout.LayoutParams(DensityUtils.dp2px(30f), DensityUtils.dp2px(30f))
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT)
        serviceImageView.setImageDrawable(
            if (ThemeManager.isLight())
                ContextCompat.getDrawable(context, R.drawable.ic_customer_service_light)
            else ContextCompat.getDrawable(context, R.drawable.ic_customer_service_dark)
        )
        serviceImageView.layoutParams = layoutParams
        child.addView(serviceImageView)
        val linearLayout = LinearLayout(context)
        val linearLayoutParams =
            RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
            )
        linearLayoutParams.marginEnd = DensityUtils.dp2px(10f)
        linearLayoutParams.topMargin = DensityUtils.dp2px(10f)
        linearLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_END)
        linearLayout.layoutParams = linearLayoutParams
        child.addView(linearLayout)
        tvMessageCount = TextView(context)
        tvMessageCount.gravity = Gravity.CENTER
        val tvParams =
            RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
            )
        val padding = DensityUtils.dp2px(1f)
        tvMessageCount.setPadding(padding, 0, padding, 0)
        tvMessageCount.layoutParams = tvParams
        tvMessageCount.setTextColor(ContextCompat.getColor(context, R.color.white))
        tvMessageCount.textSize = DensityUtils.sp2px(4f)
        tvMessageCount.background = ContextCompat.getDrawable(context, R.drawable.red_dot)
        tvMessageCount.text = "0"
        tvMessageCount.visibility = INVISIBLE
        linearLayout.addView(tvMessageCount)
        addView(child, 0)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        measureChildren(widthMeasureSpec, heightMeasureSpec)
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)
        setMeasuredDimension(width, height)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mFrameRect = Rect(left, top, right, bottom)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        if (getSavedLeft() != 0 || getSavedTop() != 0 || getSavedRight() != 0 || getSavedBottom() != 0) {
            child.layout(
                getSavedLeft(),
                getSavedTop(),
                getSavedRight(),
                getSavedBottom()
            )
        } else {
            child.layout(
                right - DensityUtils.dp2px(50f),
                DensityUtils.dp2px(100f),
                right,
                DensityUtils.dp2px(100f) + DensityUtils.dp2px(50f)
            )
        }
    }

    private fun isFastClick(): Boolean {
        var flag = true
        val currentClickTime = System.currentTimeMillis()
        if (currentClickTime - lastClickTime >= MIN_DELAY_TIME) {
            flag = false
            lastClickTime = currentClickTime
        }
        return !flag
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        moveX = 0
        moveY = 0
        val actionMasked = event.actionMasked
        val rawX = event.rawX.toInt()
        val rawY = event.rawY.toInt()
        when (actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                downX = event.x.toInt()
                downY = event.y.toInt()
                lastX = rawX
                lastY = rawY
                val left: Int = this.child.left
                val top: Int = this.child.top
                val right: Int = this.child.right
                val bottom: Int = this.child.bottom
                childRect = Rect(left, top, right, bottom)
                if (childRect.contains(downX, downY)) {
                    isConsume = true
                    timer = Timer()
                    timer.schedule(object : TimerTask() {
                        override fun run() {
                            if (!isInMotion) {
                                if (moveX == 0 && moveY == 0) {
                                    isLongClick = true
                                } else {
                                    if (abs(moveX - downX) < 20 && abs(moveY - downY) < 20) {
                                        isLongClick = true
                                    }
                                }
                                if (isLongClick && !isInMotion) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                        val vibratorManager =
                                            mContext.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                                        val defaultVibrator = vibratorManager.defaultVibrator
                                        defaultVibrator.vibrate(
                                            VibrationEffect.createOneShot(
                                                100,
                                                DEFAULT_AMPLITUDE
                                            )
                                        )
                                    } else {
                                        @Suppress("DEPRECATION")
                                        val vibrator =
                                            mContext.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                                        vibrator.vibrate(100)
                                    }
                                }
                            }
                        }
                    }, 500)
                } else {
                    isConsume = false
                }
            }
            MotionEvent.ACTION_MOVE -> {
                moveX = event.x.toInt()
                moveY = event.y.toInt()
                val offsetX: Int = rawX - lastX
                val offsetY: Int = rawY - lastY
                val childRectAfterMove = Rect(
                    this.child.left + offsetX,
                    this.child.top + offsetY,
                    this.child.right + offsetX,
                    this.child.bottom + offsetY
                )
                if (isConsume && isLongClick) {
                    isMoving = true
                    if (mFrameRect.contains(childRectAfterMove)) {
                        this.child.layout(
                            this.child.left + offsetX,
                            this.child.top + offsetY,
                            this.child.right + offsetX,
                            this.child.bottom + offsetY
                        )
                    } else {
                        //stored in right
                        if (this.child.right > right) {
                            this.child.layout(
                                right - this.child.width,
                                this.child.top,
                                right,
                                this.child.bottom
                            )
                        } else if (this.child.left < left) {
                            this.child.layout(
                                0,
                                this.child.top,
                                left + this.child.width,
                                this.child.bottom
                            )
                        }
                    }
                }
                lastX = rawX
                lastY = rawY
            }
            MotionEvent.ACTION_UP -> {
                val upX = event.x.toInt()
                val upY = event.y.toInt()
                isMoving = false
                if (isLongClick && !isInMotion) {
                    store()
                }
                if (isConsume && !isLongClick && abs(upX - downX) < 20 && abs(upY - downY) < 20) {
                    if (childRect.contains(downX, downY)) {
                        if (isFastClick()) {
                            if (!isInMotion) {
                                onClick?.invoke()
                            }
                        }
                    }
                }
                timer.cancel()
                isLongClick = false
            }
        }
        return isConsume
    }


    private fun store() {
        if (this.child.right >= right || child.left < left) {
            return
        }
        isInMotion = true
        val left: Int = child.left
        if (left < width / 2 - child.width / 2) {
            val animSet = AnimatorSet()
            val valueAnim =
                ValueAnimator.ofFloat(child.right.toFloat(), child.width.toFloat())
            valueAnim.duration = 500
            valueAnim.addUpdateListener { animation ->
                val animatedValue = animation.animatedValue as Float
                child.layout(
                    (animatedValue - child.width).toInt(),
                    child.top, animatedValue.toInt(),
                    child.bottom
                )
            }
            animSet.playTogether(valueAnim)
            animSet.addListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator) {}
                override fun onAnimationEnd(animation: Animator) {
                    isInMotion = false
                    savePosition(child.left, child.top, child.right, child.bottom)
                }

                override fun onAnimationCancel(animation: Animator) {}
                override fun onAnimationRepeat(animation: Animator) {}
            })
            animSet.start()
        } else {
            val animationSet = AnimatorSet()
            val valueAnimatorX = ValueAnimator.ofFloat(
                child.left.toFloat(),
                (width - child.width).toFloat()
            )
            valueAnimatorX.duration = 500
            valueAnimatorX.addUpdateListener { animation ->
                val animatedValue = animation.animatedValue as Float
                child.layout(
                    animatedValue.toInt(),
                    child.top, (animatedValue + child.width).toInt(),
                    child.bottom
                )
            }
            animationSet.playTogether(valueAnimatorX)
            animationSet.addListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator) {}
                override fun onAnimationEnd(animation: Animator) {
                    isInMotion = false
                    savePosition(child.left, child.top, child.right, child.bottom)
                }

                override fun onAnimationCancel(animation: Animator) {}
                override fun onAnimationRepeat(animation: Animator) {}
            })
            animationSet.start()
        }
    }

    fun savePosition(left: Int, top: Int, right: Int, bottom: Int) {
        MmkvManager.saveCustomerServiceIconLeft(left)
        MmkvManager.saveCustomerServiceIconTop(top)
        MmkvManager.saveCustomerServiceIconRight(right)
        MmkvManager.saveCustomerServiceIconBottom(bottom)
    }

    private fun getSavedLeft(): Int {
        return MmkvManager.getCustomerServiceIconLeft()
    }

    private fun getSavedTop(): Int {
        return MmkvManager.getCustomerServiceIconTop()
    }

    private fun getSavedRight(): Int {
        return MmkvManager.getCustomerServiceIconRight()
    }

    private fun getSavedBottom(): Int {
        return MmkvManager.getCustomerServiceIconBottom()
    }

    fun setMessageCount() {
        val messageCountStr = MessageManager.instance().getMessageCountStr()
        if (messageCountStr.isEmpty()) {
            tvMessageCount.visibility = INVISIBLE
        } else {
            tvMessageCount.text = messageCountStr
            tvMessageCount.visibility = View.VISIBLE
        }
        mHandler.removeCallbacksAndMessages(null)
        mHandler.postDelayed(layoutRunnable, 2000)
    }

    fun hide() {
        child.visibility = View.GONE
    }

    fun show() {
        child.visibility = View.VISIBLE
    }

    companion object {
        private const val TAG = "CustomerServiceView"
    }
}