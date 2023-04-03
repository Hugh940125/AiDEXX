package com.microtech.aidexx.widget.dialog.bottom

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.view.*
import android.view.View.OnTouchListener
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import com.microtech.aidexx.R

abstract class BaseBottomPopupView(private val context: Context) {

    protected lateinit var contentContainer: ViewGroup
    private lateinit var rootView: ViewGroup
    private var isShowing = false
    private var dismissing = false
    private var isAnim = true
    private lateinit var outAnim: Animation
    private lateinit var inAnim: Animation
    private var animGravity = Gravity.BOTTOM
    private lateinit var decorView: ViewGroup
    private var onDismissListener: OnDismissListener? = null
    protected var clickView: View? = null

    init {
        initView()
    }

    private fun initView() {
        val params = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            Gravity.BOTTOM)

        val layoutInflater = LayoutInflater.from(context)
        //如果只是要显示在屏幕的下方
        //decorView是activity的根View,包含 contentView 和 titleView
        decorView = (context as Activity).window.decorView as ViewGroup
        //将控件添加到decorView中
        rootView = layoutInflater.inflate(
            R.layout.layout_basepickerview,
            decorView,
            false) as ViewGroup
        rootView.layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT)
        //这个是真正要加载时间选取器的父布局
        contentContainer = rootView.findViewById(R.id.content_container)
        contentContainer.layoutParams = params
        inAnim = getInAnimation()
        outAnim = getOutAnimation()
    }

    fun show(v: View, isAnim: Boolean) {
        this.clickView = v
        this.isAnim = isAnim
        show()
    }

    fun show() {
        if (isShowing()) {
            return
        }
        isShowing = true
        onAttached(rootView)
        rootView.requestFocus()
    }

    fun show(v: View?) {
        clickView = v
        show()
    }

    fun setKeyBackCancelable(isCancelable: Boolean) {
        rootView.isFocusable = isCancelable
        rootView.isFocusableInTouchMode = isCancelable
        if (isCancelable) {
            rootView.setOnKeyListener(onKeyBackListener)
        } else {
            rootView.setOnKeyListener(null)
        }
    }

    private fun onAttached(view: View) {
        decorView.addView(view)
        if (isAnim) {
            contentContainer.startAnimation(inAnim)
        }
    }

    private val onKeyBackListener =
        View.OnKeyListener { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_BACK && event.action == MotionEvent.ACTION_DOWN && isShowing()) {
                dismiss()
                return@OnKeyListener true
            }
            false
        }

    fun isShowing(): Boolean {
        return rootView.parent != null || isShowing
    }

    fun dismiss() {
        if (dismissing) {
            return
        }
        if (isAnim) {
            //消失动画
            outAnim.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation) {}
                override fun onAnimationEnd(animation: Animation) {
                    dismissImmediately()
                }

                override fun onAnimationRepeat(animation: Animation) {}
            })
            contentContainer.startAnimation(outAnim)
        } else {
            dismissImmediately()
        }
        dismissing = true
    }

    private fun getInAnimation(): Animation {
        val res = PickerViewAnimateUtil.getAnimationResource(this.animGravity, true)
        return AnimationUtils.loadAnimation(context, res)
    }

    private fun getOutAnimation(): Animation {
        val res = PickerViewAnimateUtil.getAnimationResource(this.animGravity, false)
        return AnimationUtils.loadAnimation(context, res)
    }

    fun dismissImmediately() {
        decorView.post { //从根视图移除
            decorView.removeView(rootView)
            isShowing = false
            dismissing = false
            onDismissListener?.onDismiss(this)
        }
    }

    protected open fun setOutSideCancelable(isCancelable: Boolean): BaseBottomPopupView? {
        val view = rootView.findViewById<View>(R.id.outmost_container)
        if (isCancelable) {
            view.setOnTouchListener(onCancelableTouchListener)
        } else {
            view.setOnTouchListener(null)
        }
        return this
    }

    open fun <T> findViewById(id: Int): View? {
        return contentContainer.findViewById(id)
    }

    @SuppressLint("ClickableViewAccessibility")
    private val onCancelableTouchListener =
        OnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                dismiss()
            }
            false
        }

    open fun setOnDismissListener(onDismissListener: OnDismissListener?): BaseBottomPopupView? {
        this.onDismissListener = onDismissListener
        return this
    }
}