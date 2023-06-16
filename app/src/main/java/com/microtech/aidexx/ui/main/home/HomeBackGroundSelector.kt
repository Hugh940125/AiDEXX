package com.microtech.aidexx.ui.main.home

import com.microtech.aidexx.R
import com.microtech.aidexx.ble.device.model.DeviceModel
import com.microtech.aidexx.utils.ThemeManager

class HomeBackGroundSelector private constructor() {
    var lastLevel: DeviceModel.GlucoseLevel? = null
    var onLevelChange: ((res: Int) -> Unit)? = null

    companion object {
        private val INSTANCE = HomeBackGroundSelector()

        fun instance(): HomeBackGroundSelector {
            return INSTANCE
        }
    }

    fun getBgForTrend(trend: DeviceModel.GlucoseTrend?, level: DeviceModel.GlucoseLevel?): Int {
        return when (trend) {
            null -> {
                when (level) {
                    DeviceModel.GlucoseLevel.HIGH -> if (ThemeManager.isLight()) R.drawable.unknow_yellow_light
                    else R.drawable.unknow_yellow_dark
                    DeviceModel.GlucoseLevel.LOW -> if (ThemeManager.isLight()) R.drawable.unknow_red_light
                    else R.drawable.unknow_red_dark
                    DeviceModel.GlucoseLevel.NORMAL -> if (ThemeManager.isLight()) R.drawable.unknow_blue_light
                    else R.drawable.unknow_blue_dark
                    null -> if (ThemeManager.isLight()) R.drawable.bg_panel_blank_light
                    else R.drawable.bg_panel_blank_dark
                }
            }
            DeviceModel.GlucoseTrend.SUPER_FAST_UP, DeviceModel.GlucoseTrend.SUPER_FAST_DOWN -> {
                when (level) {
                    DeviceModel.GlucoseLevel.HIGH -> if (ThemeManager.isLight()) R.drawable.super_trends_yellow_light
                    else R.drawable.super_trends_yellow_dark
                    DeviceModel.GlucoseLevel.LOW -> if (ThemeManager.isLight()) R.drawable.super_trends_red_light
                    else R.drawable.super_trends_red_dark
                    else -> if (ThemeManager.isLight()) R.drawable.super_trends_blue_light
                    else R.drawable.super_trends_blue_dark
                }
            }
            DeviceModel.GlucoseTrend.FAST_UP, DeviceModel.GlucoseTrend.FAST_DOWN -> {
                when (level) {
                    DeviceModel.GlucoseLevel.HIGH -> if (ThemeManager.isLight()) R.drawable.trend_yellow_light
                    else R.drawable.trend_yellow_dark
                    DeviceModel.GlucoseLevel.LOW -> if (ThemeManager.isLight()) R.drawable.trend_red_light
                    else R.drawable.trend_red_dark
                    else -> if (ThemeManager.isLight()) R.drawable.trend_blue_light
                    else R.drawable.trend_blue_dark
                }
            }

            DeviceModel.GlucoseTrend.UP, DeviceModel.GlucoseTrend.DOWN -> {
                when (level) {
                    DeviceModel.GlucoseLevel.HIGH -> if (ThemeManager.isLight()) R.drawable.trends_fast_yellow_light
                    else R.drawable.trends_fast_yellow_dark
                    DeviceModel.GlucoseLevel.LOW -> if (ThemeManager.isLight()) R.drawable.trends_fast_red_light
                    else R.drawable.trends_fast_red_dark
                    else -> if (ThemeManager.isLight()) R.drawable.trends_fast_blue_light
                    else R.drawable.trends_fast_blue_dark
                }
            }
            else -> {
                when (level) {
                    DeviceModel.GlucoseLevel.HIGH -> if (ThemeManager.isLight()) R.drawable.trend_eq_yellow_light
                    else R.drawable.trend_eq_yellow_dark
                    DeviceModel.GlucoseLevel.LOW -> if (ThemeManager.isLight()) R.drawable.trend_eq_red_light
                    else R.drawable.trend_eq_red_dark
                    else -> if (ThemeManager.isLight()) R.drawable.trend_eq_blue_light
                    else R.drawable.trend_eq_blue_dark
                }
            }
        }
    }

    fun getHomeBg(level: DeviceModel.GlucoseLevel?) {
        if (ThemeManager.isLight()) {
            val homeBg = when (level) {
                DeviceModel.GlucoseLevel.HIGH -> R.drawable.bg_home_yellow
                DeviceModel.GlucoseLevel.LOW -> R.drawable.bg_home_red
                DeviceModel.GlucoseLevel.NORMAL -> R.drawable.bg_home_green
                else -> 0
            }
            onLevelChange?.invoke(homeBg)
        }
    }
}