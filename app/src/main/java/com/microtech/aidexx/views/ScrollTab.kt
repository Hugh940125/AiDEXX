package com.microtech.aidexx.views

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.text.TextUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.viewpager.widget.ViewPager
import com.microtech.aidexx.R
import com.microtech.aidexx.common.dp2px
import com.microtech.aidexx.utils.ThemeManager

class ScrollTab: HorizontalScrollView, OnClickListener, ViewPager.OnPageChangeListener {


    /**
     * TAB类型
     */
    private val TYPE_VIEW = 0
    private val TYPE_VIEW_GROUP = 1

    /**
     * 指示器类型
     */
    private val TYPE_INDICATOR_TREND = 0
    private val TYPE_INDICATOR_TRANSLATION = 1
    private val TYPE_INDICATOR_NONE = 2

    private var mWidth = 0
    private var mHeight = 0

    private var mContext: Context? = null
    private var mRectF: RectF? = null
    private lateinit var mPaint: Paint

    private var mType = 0
    private var mIsAvag = false
    private var mPadding // Item内部左右预留间距
            = 0f
    private var mStrTitles: String? = null
    private var mIndicatorType = 0
    private var mIndicatorColor = 0
    private var mIndicatorWidth = 0f
    private var mIndicatorWeight = 0f
    private var mIndicatorRadius = 0f
    private var mIndicatorPadding = 0f

    private var mTabs = ArrayList<View>()
    private var mItems = ArrayList<TabItem>()
    private var mCount = 0
    private var mPosition = 0
    private var mPositionOffset = 0f
    private var mIsFirst = true
    private var mViewPager: ViewPager? = null
    private var mListener: OnTabListener? = null


    constructor(context: Context): this(context, null, 0)

    constructor(context: Context, attrs: AttributeSet):
        this(context, attrs, 0)


    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int)
            : super(context, attrs, defStyleAttr) {
        initTypedArray(context, attrs)
        init(context)
    }


    @SuppressLint("CustomViewStyleable")
    private fun initTypedArray(context: Context, attrs: AttributeSet?) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.lib_pub_ScrollTab)
        mType = typedArray.getInt(R.styleable.lib_pub_ScrollTab_lib_pub_stab_type, TYPE_VIEW)
        mIsAvag = typedArray.getBoolean(R.styleable.lib_pub_ScrollTab_lib_pub_stab_avag, false)
        mPadding = typedArray.getDimension(
            R.styleable.lib_pub_ScrollTab_lib_pub_stab_padding,
            (12.dp2px()).toFloat()
        )
        mStrTitles = typedArray.getString(R.styleable.lib_pub_ScrollTab_lib_pub_stab_titles)
        mIndicatorType = typedArray.getInt(
            R.styleable.lib_pub_ScrollTab_lib_pub_stab_indicatorType,
            TYPE_INDICATOR_TREND
        )
        mIndicatorColor = typedArray.getColor(
            R.styleable.lib_pub_ScrollTab_lib_pub_stab_indicatorColor,
            ContextCompat.getColor(context, R.color.lib_pub_color_main)
        )
        mIndicatorWidth = typedArray.getDimension(
            R.styleable.lib_pub_ScrollTab_lib_pub_stab_indicatorWidth,
            30.dp2px().toFloat()
        )
        mIndicatorWeight = typedArray.getDimension(
            R.styleable.lib_pub_ScrollTab_lib_pub_stab_indicatorWeight,
            1.dp2px().toFloat()
        )
        mIndicatorRadius = typedArray.getDimension(
            R.styleable.lib_pub_ScrollTab_lib_pub_stab_indicatorRadius,
            0.5.dp2px().toFloat()
        )
        mIndicatorPadding = typedArray.getDimension(
            R.styleable.lib_pub_ScrollTab_lib_pub_stab_indicatorPadding,
           5.dp2px().toFloat()
        )
        typedArray.recycle()
    }

    private fun init(context: Context) {
        this.mContext = context
        setWillNotDraw(false)
        isHorizontalScrollBarEnabled = false
        overScrollMode = OVER_SCROLL_NEVER
        isFillViewport = mIsAvag
        mRectF = RectF()
        mPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mPaint.color = mIndicatorColor

        mStrTitles?.ifEmpty { null } ?.let {
            val strs: Array<String> = it.split(";".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray()
            for (t in strs) {
                mItems.add(TabItem(t, ""))
            }
        }
    }


    /**
     * 设置Titles
     */
    fun setTitles(ts: List<String?>?) {
        if (mItems != null && ts != null) {
            mItems.clear()
            for (t in ts) {
                mItems.add(TabItem(t!!, ""))
            }
            resetTab()
            invalidate()
            requestLayout()
        }
    }

    private fun resetTab() {
        if (mItems == null || mItems.size <= 0 || mWidth <= 0) {
            return
        }
        mIsFirst = false
        mCount = mItems.size
        mTabs.clear()
        removeAllViews()
        val parent = LinearLayout(mContext)
        val lp = LayoutParams(
            if (mIsAvag) LayoutParams.MATCH_PARENT else LayoutParams.WRAP_CONTENT,
            LayoutParams.MATCH_PARENT
        )
        parent.orientation = LinearLayout.HORIZONTAL
        parent.layoutParams = lp
        for (i in 0 until mCount) {
            val child = getTabView(i)
            parent.addView(child)
            mTabs.add(child)
        }
        addView(parent)
    }

    private fun getTabView(i: Int): View {
        val child: View
        child = if (mType == TYPE_VIEW) {
            TabTextView(mContext!!)
        } else {
            TabViewGroup(mContext!!)
        }
        (child as TabView).setText(mItems[i].title)
        (child as TabView).setNumber(
            mItems[i].text,
            if (TextUtils.isEmpty(mItems[i].text)) GONE else VISIBLE
        )
        if (!mIsAvag) {
            (child as TabView).setPadding(mPadding.toInt())
        }
        (child as TabView).notifyData(i == mPosition)
        child.layoutParams = LinearLayout.LayoutParams(
            if (mIsAvag) mWidth / (if (mCount > 0) mCount else 1) else ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        child.tag = i
        child.setOnClickListener(this)
        return child
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (isInEditMode || mCount <= 0 || mPosition < 0 || mPosition > mCount - 1) {
            return
        }
        if (mIndicatorType == TYPE_INDICATOR_TREND) {
            var left = mTabs[mPosition].left + mIndicatorPadding
            var right = mTabs[mPosition].right - mIndicatorPadding
            if (mPosition < mCount - 1) {
                val nextLeft = mTabs[mPosition + 1].left + mIndicatorPadding
                val nextRight = mTabs[mPosition + 1].right - mIndicatorPadding
                if (mPositionOffset < 0.5) {
                    right = right + (nextRight - right) * mPositionOffset * 2
                } else {
                    left = left + (nextLeft - left) * (mPositionOffset - 0.5f) * 2
                    right = nextRight
                }
            }
            mRectF!![left, mHeight - mIndicatorWeight, right] = mHeight.toFloat()
        } else if (mIndicatorType == TYPE_INDICATOR_TRANSLATION) {
            var left = mTabs[mPosition].left.toFloat()
            var right = mTabs[mPosition].right.toFloat()
            var middle = left + (right - left) / 2
            if (mPosition < mCount - 1) {
                val nextLeft = mTabs[mPosition + 1].left.toFloat()
                val nextRight = mTabs[mPosition + 1].right.toFloat()
                val nextMiddle = nextLeft + (nextRight - nextLeft) / 2
                middle = middle + (nextMiddle - middle) * mPositionOffset
            }
            left = middle - mIndicatorWidth / 2
            right = middle + mIndicatorWidth / 2
            mRectF!![left, mHeight - mIndicatorWeight, right] = mHeight.toFloat()
        } else {
            var left = mTabs[mPosition].left.toFloat()
            var right = mTabs[mPosition].right.toFloat()
            val middle = left + (right - left) / 2
            left = middle - mIndicatorWidth / 2
            right = middle + mIndicatorWidth / 2
            mRectF!![left, mHeight - mIndicatorWeight, right] = mHeight.toFloat()
        }
        canvas.drawRoundRect(mRectF!!, mIndicatorRadius, mIndicatorRadius, mPaint)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        mWidth = MeasureSpec.getSize(widthMeasureSpec)
        mHeight = MeasureSpec.getSize(heightMeasureSpec)
        if (mIsFirst) {
            resetTab()
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onClick(v: View) {
        val index = v.tag as Int

        if (mListener == null || mListener!!.onChange(index, v)) {
            setCurrentIndex(index)
        }
    }

    fun setCurrentIndex(index: Int) {
        if (mViewPager == null) {
            mPosition = index
            mPositionOffset = 0f
            onChange(index)
            adjustScrollY(index)
            invalidate()
        }
    }

    private fun onChange(position: Int) {
        for (i in 0 until mCount) {
            val view = mTabs[i] as TabView
            view.notifyData(i == position)
        }
    }

    fun setViewPager(viewPager: ViewPager) {
        mViewPager = viewPager
        viewPager.addOnPageChangeListener(this)
    }

    /**
     * 设置红点
     */
    fun setNumber(position: Int, text: String?, visibility: Int) {
        if (position < 0 || position > mItems.size - 1) {
            return
        }
        mItems[position].text = text!!
        if (position < 0 || position > mCount - 1) {
            return
        }
        val view = mTabs[position] as TabView
        view.setNumber(text, visibility)
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
        if (mIndicatorType != TYPE_INDICATOR_NONE) {
            mPosition = position
            mPositionOffset = positionOffset
            invalidate()
        }
    }

    override fun onPageSelected(position: Int) {
        onChange(position)
        adjustScrollY(position)
        if (mIndicatorType == TYPE_INDICATOR_NONE) {
            mPosition = position
            invalidate()
        }
    }

    override fun onPageScrollStateChanged(state: Int) {}

    private fun adjustScrollY(position: Int) {
        if (mIsAvag) {
            return
        }
        val v = mTabs[position]
        val dr = v.right - (mWidth + scrollX)
        val dl = scrollX - v.left
        if (dr > 0) {
            smoothScrollBy(dr, 0)
        } else if (dl > 0) {
            smoothScrollBy(-dl, 0)
        }
    }

    fun setOnTabListener(l: OnTabListener) {
        mListener = l
    }

    interface OnTabListener {
        fun onChange(position: Int, v: View?): Boolean
    }


}

data class TabItem(
    var title: String,
    var text: String
)

interface TabView {
    fun setText(text: String?)
    fun setPadding(padding: Int)
    fun setNumber(text: String?, visibility: Int)
    fun notifyData(focus: Boolean)
    fun onScroll(factor: Float)
}

class TabTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) :
    View(context, attrs, defStyleAttr), TabView {
    private var mWidth = 0
    private var mHeight = 0
    private var mPaint: Paint? = null
    private var mText = "title"
    private var mTextHeight = 0f

    /**
     * Define
     */
    private var mTextSize // Title文字大小
            = 0
    private var mTextColor // Title文字颜色
            = 0
    private var mTextColorFocus // Title文字颜色
            = 0
    private var mPadding // Title文字左右预留间距
            = 0

    init {
        init(context)
    }

    private fun init(context: Context) {
        mTextSize = 18.dp2px()
        //        mTextColor = ContextCompat.getColor(context, R.color.lib_pub_color_gray);
        mTextColor = ThemeManager.getTypeValue(getContext(), R.attr.colorFormTitle)
        mTextColorFocus = ThemeManager.getTypeValue(getContext(), R.attr.appColorAccent)

//        mTextColorFocus = ContextCompat.getColor(context, R.color.lib_pub_color_main);
        mPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mPaint!!.textAlign = Paint.Align.CENTER
        mPaint!!.textSize = mTextSize.toFloat()
        mPaint!!.isFakeBoldText = true
        mPaint!!.color = mTextColor
        mTextHeight = getTextHeight(mPaint!!)
    }

    fun getTextHeight(p: Paint): Float {
        val fm = p.fontMetrics
        return ((Math.ceil((fm.descent - fm.top).toDouble()) + 2) / 2).toFloat()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val x = mWidth / 2f
        val y = mHeight / 2f + mTextHeight / 2f
        canvas.drawText(mText, x, y, mPaint!!)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        mWidth = if (MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.EXACTLY) {
            MeasureSpec.getSize(widthMeasureSpec)
        } else {
            getTextWidth(mText, mPaint) + mPadding * 2
        }
        mHeight = getDefaultSize(suggestedMinimumWidth, heightMeasureSpec)
        setMeasuredDimension(mWidth, mHeight)
    }

    fun getTextWidth(str: String, paint: Paint?): Int {
        val bounds = Rect()
        paint!!.getTextBounds(str, 0, str.length, bounds)
        return bounds.width()
    }


    override fun setText(text: String?) {
        mText = text ?: "title"
        requestLayout()
    }

    override fun setPadding(padding: Int) {
        mPadding = padding
        requestLayout()
    }

    override fun setNumber(text: String?, visibility: Int) {}
    override fun notifyData(focus: Boolean) {
        mPaint!!.color = if (focus) mTextColorFocus else mTextColor
        invalidate()
    }

    override fun onScroll(factor: Float) {}
}


class TabViewGroup : RelativeLayout, TabView {
    private var mContext: Context? = null
    private var mTvTitle: TextView? = null
    private var mTvNumber: TextView? = null

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(context)
    }

    private fun init(context: Context) {
        mContext = context
        val root: View = LayoutInflater.from(context).inflate(R.layout.lib_pub_view_tab, this)
        mTvTitle = root.findViewById<View>(R.id.tv_title) as TextView
        mTvNumber = root.findViewById<View>(R.id.tv_number) as TextView
    }

    override fun setText(text: String?) {
        mTvTitle!!.text = text
    }

    override fun setPadding(padding: Int) {
        setPadding(padding, 0, padding, 0)
    }

    override fun setNumber(text: String?, visibility: Int) {
        mTvNumber!!.text = text
        mTvNumber!!.visibility = visibility
    }

    override fun notifyData(focus: Boolean) {
        mTvTitle!!.setTextColor(
            ContextCompat.getColor(
                mContext!!,
                if (focus) R.color.lib_pub_color_main else R.color.lib_pub_color_gray
            )
        )
    }

    override fun onScroll(factor: Float) {}
}