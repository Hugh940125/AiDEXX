package com.microtech.aidexx.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.ScrollView

class NoHorizontallyScrollView : ScrollView {
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    override fun canScrollHorizontally(direction: Int): Boolean {
        return false
    }
}