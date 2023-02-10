package com.microtech.aidexx.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.microtech.aidexx.R
import com.microtech.aidexx.databinding.WidgetActionBarBinding

open class ActionBarWidget : ConstraintLayout {

    var ivRightIcon: View? = null
    lateinit var mViewBinding: WidgetActionBarBinding

    constructor(context: Context) : super(context) {
        initLayout()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initLayout()
        val typedArray =
            context.theme.obtainStyledAttributes(attrs, R.styleable.ActionBarWidget, 0, 0)
        mViewBinding.tvTitle.text = typedArray.getString(R.styleable.ActionBarWidget_titleText)
        val leftIcon = typedArray.getResourceId(R.styleable.ActionBarWidget_leftIcon, 0)
        if (leftIcon != 0) mViewBinding.ivLeft.setImageResource(leftIcon) else mViewBinding.ivLeft.visibility =
            INVISIBLE
        val rightIcon = typedArray.getResourceId(R.styleable.ActionBarWidget_rightIcon, 0)
        if (rightIcon != 0) mViewBinding.ivRight.setImageResource(rightIcon) else mViewBinding.ivRight.visibility =
            INVISIBLE
        val titleRightIcon = typedArray.getResourceId(R.styleable.ActionBarWidget_titleRightIcon, 0)
        if (titleRightIcon != 0) mViewBinding.ivTitleRight.setImageResource(titleRightIcon) else mViewBinding.ivTitleRight.visibility =
            GONE
    }


    //设置标题右边的图标
    fun setTitleRightIcon(res: Int) {
        mViewBinding.ivTitleRight.visibility = (if (res == 1) VISIBLE else GONE)
    }

    private fun initLayout() {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val parent = inflater.inflate(R.layout.widget_action_bar, this, true)
        mViewBinding = WidgetActionBarBinding.bind(parent)
        ivRightIcon = parent.findViewById(R.id.ll_title)
    }

    fun getLeftIcon(): ImageView {
        return mViewBinding.ivLeft
    }

    fun getRightIcon(): ImageView {
        return mViewBinding.ivRight
    }

    fun getRightTitleIcon(): View? {
        return ivRightIcon
    }


    fun getTitle(): TextView {
        return mViewBinding.tvTitle
    }

    fun setTitle(title: String?) {
        if (title != null) {
            mViewBinding.tvTitle.text = title
        }
    }
}