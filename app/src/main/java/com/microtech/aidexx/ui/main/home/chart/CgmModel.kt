package com.microtech.aidexx.ui.main.home.chart

// todo 临时占位
object CgmModel {
        const val GLUCOSE_LOWER = 2f
        const val GLUCOSE_UPPER = 25f
        const val URGENT_HYPO = 3f
        const val DEFAULT_HYPO = 3.9f
        const val DEFAULT_HYPER = 10f

        const val SUPER_FAST_DOWN = -0.17
        const val FAST_DOWN = -0.11
        const val SLOW_DOWN = -0.06
        const val SLOW_UP = 0.06
        const val FAST_UP = 0.11
        const val SUPER_FAST_UP = 0.17

        var notify: ((time: String, type: Int) -> Unit)? = null
        var notifyNotication: (() -> Unit)? = null
}