package com.microtech.aidexx.ui.main

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.microtech.aidexx.R
import com.microtech.aidexx.utils.ThemeManager

class MainTabView : ConstraintLayout {
    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(context)
    }

    private val grayD8: Int = ContextCompat.getColor(context, R.color.gray_d8)
    private val black4b: Int = ContextCompat.getColor(context, R.color.black_4b)
    private val green65: Int = ContextCompat.getColor(context, R.color.green_65)
    private val white: Int = ContextCompat.getColor(context, R.color.white)
    lateinit var tabRootTrend: ConstraintLayout
    lateinit var tabRootHistory: ConstraintLayout
    lateinit var tabRootBg: ConstraintLayout
    lateinit var tabRootEvent: ConstraintLayout
    lateinit var tabRootHome: RelativeLayout
    lateinit var tvTabHome: TextView
    lateinit var ivTabHome: ImageView
    lateinit var ivTabHistory: ImageView
    lateinit var tvTabHistory: TextView
    lateinit var ivTabTrend: ImageView
    lateinit var tvTabTrend: TextView
    lateinit var ivTabBg: ImageView
    lateinit var tvTabBg: TextView
    lateinit var ivTabEvent: ImageView
    lateinit var tvTabEvent: TextView
    private var currentSelect = 2
    var onTabChange: ((pos: Int) -> Boolean)? = null

    fun check(pos: Int) {
        if (currentSelect != pos) {
            when (pos) {
                0 -> {
                    check0()
                    currentSelect = 0
                }
                1 -> {
                    check1()
                    currentSelect = 1
                }
                2 -> {
                    check2()
                    currentSelect = 2
                }
                3 -> {
                    check3()
                    currentSelect = 3
                }
                4 -> {
                    check4()
                    currentSelect = 4
                }
            }
        }
    }

    private fun init(context: Context) {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val mainTabView = inflater.inflate(R.layout.main_tab_view, this, true)
        val tabHistory = mainTabView.findViewById<ConstraintLayout>(R.id.tab_history)
        val tabTrend = mainTabView.findViewById<ConstraintLayout>(R.id.tab_trend)
        val tabBg = mainTabView.findViewById<ConstraintLayout>(R.id.tab_bg)
        val tabEvent = mainTabView.findViewById<ConstraintLayout>(R.id.tab_event)
        tabRootHistory = tabHistory.findViewById(R.id.tab_root)
        tabRootHistory.setOnClickListener {
            val invoke = onTabChange?.invoke(0)
            if (invoke == true) {
                if (currentSelect == 0) return@setOnClickListener
                check0()
                currentSelect = 0
            }
        }
        ivTabHistory = tabHistory.findViewById(R.id.iv_tab)
        tvTabHistory = tabHistory.findViewById(R.id.tv_tab)
        tvTabHistory.text = context.getString(R.string.title_history)
        tabRootTrend = tabTrend.findViewById(R.id.tab_root)
        tabRootTrend.setOnClickListener {
            val invoke = onTabChange?.invoke(1)
            if (invoke == true) {
                if (currentSelect == 1) return@setOnClickListener
                check1()
                currentSelect = 1
            }
        }
        ivTabTrend = tabTrend.findViewById(R.id.iv_tab)
        tvTabTrend = tabTrend.findViewById(R.id.tv_tab)
        tvTabTrend.text = context.getString(R.string.title_trends)
        tabRootBg = tabBg.findViewById(R.id.tab_root)
        tabRootBg.setOnClickListener {
            val invoke = onTabChange?.invoke(3)
            if (invoke == true) {
                if (currentSelect == 3) return@setOnClickListener
                check3()
                currentSelect = 3
            }
        }
        ivTabBg = tabBg.findViewById(R.id.iv_tab)
        tvTabBg = tabBg.findViewById(R.id.tv_tab)
        tvTabBg.text = context.getString(R.string.title_bg)
        tabRootEvent = tabEvent.findViewById(R.id.tab_root)
        tabRootEvent.setOnClickListener {
            val invoke = onTabChange?.invoke(4)
            if (invoke == true) {
                if (currentSelect == 4) return@setOnClickListener
                check4()
                currentSelect = 4
            }
        }
        ivTabEvent = tabEvent.findViewById(R.id.iv_tab)
        tvTabEvent = tabEvent.findViewById(R.id.tv_tab)
        tvTabEvent.text = context.getString(R.string.title_event)
        tabRootHome = findViewById(R.id.tab_home)
        tabRootHome.setOnClickListener {
            val invoke = onTabChange?.invoke(2)
            if (invoke == true) {
                if (currentSelect == 2) return@setOnClickListener
                check2()
                currentSelect = 2
            }
        }
        ivTabHome = findViewById(R.id.iv_home)
        tvTabHome = findViewById(R.id.tv_home)
        tvTabHome.text = context.getString(R.string.title_home)
        check2()
    }

    private fun check2() {
        uncheckHistory()
        uncheckTrend()
        uncheckBg()
        uncheckEvent()
        checkHome()
    }

    private fun check4() {
        uncheckHistory()
        uncheckTrend()
        uncheckBg()
        checkEvent()
        uncheckHome()
    }

    private fun check3() {
        uncheckHistory()
        uncheckTrend()
        checkBg()
        uncheckEvent()
        uncheckHome()
    }

    private fun check1() {
        uncheckHistory()
        checkTrend()
        uncheckBg()
        uncheckEvent()
        uncheckHome()
    }

    private fun check0() {
        checkHistory()
        uncheckTrend()
        uncheckBg()
        uncheckEvent()
        uncheckHome()
    }

    private fun uncheckHistory() {
        ivTabHistory.setImageDrawable(ContextCompat.getDrawable(context,
            R.drawable.ic_history_uncheck))
        tvTabHistory.setTextColor(if (ThemeManager.isLight()) black4b else grayD8)
        tvTabHistory.typeface = Typeface.DEFAULT
    }

    private fun checkHistory() {
        ivTabHistory.setImageDrawable(ContextCompat.getDrawable(context,
            R.drawable.ic_history_checked))
        if (ThemeManager.isLight()) tvTabHistory.setTextColor(green65) else tvTabHistory.setTextColor(
            white)
        tvTabHistory.typeface = Typeface.DEFAULT_BOLD
    }

    private fun uncheckTrend() {
        ivTabTrend.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_trend_uncheck))
        tvTabTrend.setTextColor(if (ThemeManager.isLight()) black4b else grayD8)
        tvTabTrend.typeface = Typeface.DEFAULT
    }

    private fun checkTrend() {
        ivTabTrend.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_trend_checked))
        if (ThemeManager.isLight()) tvTabTrend.setTextColor(green65) else tvTabTrend.setTextColor(
            white)
        tvTabTrend.typeface = Typeface.DEFAULT_BOLD
    }

    private fun uncheckBg() {
        ivTabBg.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_bg_uncheck))
        tvTabBg.setTextColor(if (ThemeManager.isLight()) black4b else grayD8)
        tvTabBg.typeface = Typeface.DEFAULT
    }

    private fun checkBg() {
        ivTabBg.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_bg_checked))
        if (ThemeManager.isLight()) tvTabBg.setTextColor(green65) else tvTabBg.setTextColor(white)
        tvTabBg.typeface = Typeface.DEFAULT_BOLD
    }

    private fun uncheckEvent() {
        ivTabEvent.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_event_uncheck))
        tvTabEvent.setTextColor(if (ThemeManager.isLight()) black4b else grayD8)
        tvTabEvent.typeface = Typeface.DEFAULT
    }

    private fun checkEvent() {
        ivTabEvent.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_event_checked))
        if (ThemeManager.isLight()) tvTabEvent.setTextColor(green65) else tvTabEvent.setTextColor(
            white)
        tvTabEvent.typeface = Typeface.DEFAULT_BOLD
    }

    private fun uncheckHome() {
        ivTabHome.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_home_uncheck))
        tvTabHome.setTextColor(if (ThemeManager.isLight()) black4b else grayD8)
        tvTabHome.typeface = Typeface.DEFAULT
    }

    private fun checkHome() {
        ivTabHome.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_home_checked))
        if (ThemeManager.isLight()) tvTabHome.setTextColor(green65) else tvTabHome.setTextColor(white)
        tvTabHome.typeface = Typeface.DEFAULT_BOLD
    }
}