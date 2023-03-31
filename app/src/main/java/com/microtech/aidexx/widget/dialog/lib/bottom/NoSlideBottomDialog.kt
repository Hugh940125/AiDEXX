package com.microtech.aidexx.widget.dialog.lib.bottom

import android.view.MotionEvent
import android.view.View
import com.microtech.aidexx.widget.dialog.lib.interfaces.BottomDialogSlideEventLifecycleCallback
import com.microtech.aidexx.widget.dialog.lib.interfaces.OnBindView

/**
 *@date 2023/3/9
 *@author Hugh
 *@desc
 */
class NoSlideBottomDialog(onBindView: OnBindView<BottomDialog?>) : BottomDialog(onBindView) {

    init {
        setDialogLifecycleCallback(object : BottomDialogSlideEventLifecycleCallback<BottomDialog>() {
            override fun onSlideTouchEvent(dialog: BottomDialog?, v: View?, event: MotionEvent?): Boolean {
                return true
            }
        })
    }
}