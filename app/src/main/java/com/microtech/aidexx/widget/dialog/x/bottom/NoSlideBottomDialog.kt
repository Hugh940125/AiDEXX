package com.microtech.aidexx.widget.dialog.x.bottom

import android.view.MotionEvent
import android.view.View
import com.microtech.aidexx.widget.dialog.x.interfaces.BottomDialogSlideEventLifecycleCallback
import com.microtech.aidexx.widget.dialog.x.interfaces.OnBindView

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