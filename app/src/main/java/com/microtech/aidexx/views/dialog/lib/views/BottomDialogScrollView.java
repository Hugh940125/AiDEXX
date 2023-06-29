package com.microtech.aidexx.views.dialog.lib.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ScrollView;
import com.microtech.aidexx.views.dialog.lib.interfaces.ScrollController;

public class BottomDialogScrollView extends ScrollView implements ScrollController {
    
    public BottomDialogScrollView(Context context) {
        super(context);
    }
    
    public BottomDialogScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    
    public BottomDialogScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    
    public BottomDialogScrollView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }
    
    boolean lockScroll;
    
    @Override
    public boolean isLockScroll() {
        return lockScroll;
    }
    
    public void lockScroll(boolean lockScroll) {
        this.lockScroll = lockScroll;
    }
    
    @Override
    public int getScrollDistance() {
        return getScrollY();
    }
    
    @Override
    public boolean isCanScroll() {
        View child = getChildAt(0);
        if (child != null) {
            int childHeight = child.getHeight();
            return getHeight() < childHeight;
        }
        return false;
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (lockScroll) {
            return false;
        }
        return super.onTouchEvent(ev);
    }
    
    
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return super.dispatchTouchEvent(ev);
    }
}
