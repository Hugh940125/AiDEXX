package com.microtech.aidexx.widget.dialog.lib.views;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import com.microtech.aidexx.R;
import com.microtech.aidexx.widget.dialog.lib.DialogX;

public class MaxRelativeLayout extends RelativeLayout {
    
    private int maxWidth;
    private int maxHeight;
    private int minWidth;
    private int minHeight;
    private boolean lockWidth;
    private boolean interceptTouch = true;
    private View contentView;
    
    public MaxRelativeLayout(Context context) {
        super(context);
        init(context, null);
    }
    
    public MaxRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }
    
    public MaxRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }
    
    private float startAnimValue = 0, endAnimValue = 0;
    
    private void init(Context context, AttributeSet attrs) {
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.DialogXMaxLayout);
            maxWidth = a.getDimensionPixelSize(R.styleable.DialogXMaxLayout_maxLayoutWidth, 0);
            maxHeight = a.getDimensionPixelSize(R.styleable.DialogXMaxLayout_maxLayoutHeight, 0);
            minWidth = a.getDimensionPixelSize(R.styleable.DialogXMaxLayout_minLayoutWidth, 0);
            minHeight = a.getDimensionPixelSize(R.styleable.DialogXMaxLayout_minLayoutHeight, 0);
            lockWidth = a.getBoolean(R.styleable.DialogXMaxLayout_lockWidth, false);
            interceptTouch = a.getBoolean(R.styleable.DialogXMaxLayout_interceptTouch, true);
            
            a.recycle();
        }
        minWidth = minWidth == 0 ? getMinimumWidth() : minWidth;
        minHeight = minHeight == 0 ? getMinimumHeight() : minHeight;
        
        if (!isInEditMode()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                animate().setUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        float progress = (float) animation.getAnimatedValue();
                        long value = (long) (startAnimValue + (endAnimValue - startAnimValue) * progress);
                        if (onYChangedListener != null) onYChangedListener.y(value);
                    }
                });
            }
        }
    }
    
    private ScrollView childScrollView;
    
    public MaxRelativeLayout setMaxHeight(int maxHeight) {
        if (maxHeight > 0) this.maxHeight = maxHeight;
        return this;
    }
    
    public MaxRelativeLayout setMaxWidth(int maxWidth) {
        if (maxWidth > 0) this.maxWidth = maxWidth;
        return this;
    }
    
    private int preWidth = -1;
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        
        if (preWidth == -1 && widthSize != 0) {
            preWidth = widthSize;
        }
        if (lockWidth) {
            maxWidth = Math.min(maxWidth, Math.min(widthSize, preWidth));
        }
        if (heightSize > maxHeight && maxHeight != 0) {
            heightSize = maxHeight;
        }
        if (widthSize > maxWidth && maxWidth != 0) {
            widthSize = maxWidth;
        }
        View blurView = findViewWithTag("blurView");
        View contentView = this.contentView == null ? findViewWithoutTag("blurView") : this.contentView;
        if (contentView != null && blurView != null) {
            int widthTemp = contentView.getMeasuredWidth() == 0 ? getMeasuredWidth() : contentView.getMeasuredWidth();
            int heightTemp = contentView.getMeasuredHeight() == 0 ? getMeasuredHeight() : contentView.getMeasuredHeight();
            if (widthTemp < minWidth) widthTemp = minWidth;
            if (heightTemp < minHeight) heightTemp = minHeight;
            
            LayoutParams lp = (LayoutParams) blurView.getLayoutParams();
            lp.addRule(RelativeLayout.CENTER_IN_PARENT);
            lp.width = widthTemp;
            lp.height = heightTemp;
            blurView.setLayoutParams(lp);
        } else {
            if (blurView != null) {
                LayoutParams lp = (LayoutParams) blurView.getLayoutParams();
                lp.width = getMeasuredWidth();
                lp.height = getMeasuredHeight();
                blurView.setLayoutParams(lp);
            }
        }
        
        int maxHeightMeasureSpec = MeasureSpec.makeMeasureSpec(heightSize, heightMode);
        int maxWidthMeasureSpec = MeasureSpec.makeMeasureSpec(widthSize, widthMode);
        super.onMeasure(maxWidthMeasureSpec, maxHeightMeasureSpec);
        
        childScrollView = findViewById(R.id.scrollView);
    }
    
    private View findViewWithoutTag(String tag) {
        for (int i = 0; i < getChildCount(); i++) {
            if (!tag.equals(getChildAt(i).getTag())) {
                return getChildAt(i);
            }
        }
        return null;
    }
    
    @Deprecated
    public boolean isChildScrollViewCanScroll() {
        if (childScrollView == null) return false;
        if (!childScrollView.isEnabled()) {
            return false;
        }
        View child = childScrollView.getChildAt(0);
        if (child != null) {
            int childHeight = child.getHeight();
            return childScrollView.getHeight() < childHeight;
        }
        return false;
    }
    
    public int dip2px(float dpValue) {
        final float scale = getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
    
    public boolean isLockWidth() {
        return lockWidth;
    }
    
    public MaxRelativeLayout setLockWidth(boolean lockWidth) {
        this.lockWidth = lockWidth;
        return this;
    }
    
    private OnYChanged onYChangedListener;
    
    int navBarHeight;
    Paint navBarPaint;
    
    public void setNavBarHeight(int height) {
        navBarHeight = height;
        invalidate();
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (navBarHeight != 0 && DialogX.bottomDialogNavbarColor != 0) {
            if (navBarPaint == null) {
                navBarPaint = new Paint();
                navBarPaint.setColor(DialogX.bottomDialogNavbarColor);
            }
            canvas.drawRect(0, getHeight() - navBarHeight, getWidth(), getHeight(), navBarPaint);
        }
    }
    
    public void setContentView(View contentView) {
        this.contentView = contentView;
    }
    
    public interface OnYChanged {
        void y(float y);
    }
    
    @Override
    public void setY(float y) {
        super.setY(y);
    }
    
    public OnYChanged getOnYChanged() {
        return onYChangedListener;
    }
    
    public MaxRelativeLayout setOnYChanged(OnYChanged onYChanged) {
        this.onYChangedListener = onYChanged;
        return this;
    }
    
    @Override
    public void setTranslationY(float translationY) {
        super.setTranslationY(translationY);
        if (onYChangedListener != null) onYChangedListener.y(translationY);
    }
    
    private OnTouchListener onTouchListener;
    
    @Override
    public void setOnTouchListener(OnTouchListener l) {
        onTouchListener = l;
    }
    
    boolean reInterceptTouch;
    
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (onTouchListener != null) {
            reInterceptTouch = onTouchListener.onTouch(this, ev);
        }
        return super.dispatchTouchEvent(ev);
    }
    
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return reInterceptTouch;
    }
}
