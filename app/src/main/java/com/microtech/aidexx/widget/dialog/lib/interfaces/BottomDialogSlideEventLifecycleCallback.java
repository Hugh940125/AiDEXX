package com.microtech.aidexx.widget.dialog.lib.interfaces;

import android.view.MotionEvent;
import android.view.View;

import com.microtech.aidexx.widget.dialog.lib.BaseDialog;

public abstract class BottomDialogSlideEventLifecycleCallback<D extends BaseDialog> extends DialogLifecycleCallback<D> {
    
    public boolean onSlideClose(D dialog) {
        return false;
    }
    
    public boolean onSlideTouchEvent(D dialog, View v, MotionEvent event) {
        return false;
    }
}
