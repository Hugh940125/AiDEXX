package com.microtech.aidexx.ui.alert

/**
 *@date 2023/3/1
 *@author Hugh
 *@desc
 */
class AlertManager {

    companion object{
        //"5分钟", "15分钟", "30分钟", "45分钟", "60分钟"
        fun calculateFrequency(index: Int): Long {
            return when (index) {
                0 -> 5 * 60
                1 -> 15 * 60
                2 -> 30 * 60
                3 -> 45 * 60
                4 -> 60 * 60
                else -> 30 * 60
            }
        }
    }
}