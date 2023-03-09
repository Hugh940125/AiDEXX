package com.microtech.aidexx.widget.dialog.x.util;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.view.MotionEvent;
import android.view.View;

import com.microtech.aidexx.widget.dialog.x.DialogX;
import com.microtech.aidexx.widget.dialog.x.bottom.BottomDialog;
import com.microtech.aidexx.widget.dialog.x.interfaces.BottomDialogSlideEventLifecycleCallback;
import com.microtech.aidexx.widget.dialog.x.interfaces.ScrollController;

public class BottomDialogTouchEventInterceptor {

    /**
     * 下边三个值用于判断触控过程，
     * isBkgTouched：标记是否已按下
     * bkgTouchDownY：记录起始触控位置
     * scrolledY：记录 ScrollView 已滚动过的距离，下次触控事件将接着上次的位置继续滑动
     * bkgOldY：记录按下时 bkg 的位置，用于区分松开手指时，bkg 移动的方向。
     */
    private boolean isBkgTouched = false;
    private float bkgTouchDownY;
    private float scrolledY;
    private float bkgOldY;
    /**
     * 0：bkg接收触控事件，-1：scrollView进行滚动
     * 此标记的意义在于，当从 [scrollView滚动] 与 [bkg接收触控事件] 状态切换时，
     * 需要对bkgTouchDownY、scrolledY的值进行刷新，否则触控连续过程会出现闪跳。
     */
    private int oldMode;

    public BottomDialogTouchEventInterceptor(BottomDialog me, BottomDialog.DialogImpl impl) {
        refresh(me, impl);
    }

    @SuppressLint("ClickableViewAccessibility")
    public void refresh(final BottomDialog me, final BottomDialog.DialogImpl impl) {
        if (me == null || impl == null || impl.bkg == null || impl.scrollView == null) {
            return;
        }
        /**
         * BottomDialog 触控事件说明：
         * bkg 将拦截并接管所有触控操作。
         * BottomDialog 的启动方式依据是内容布局高度是否大于可显示安全区域的高度。
         * bkg 会在合适的时机，直接接管控制 ScrollView 的滚动。
         * 因此，请确保内容布局的高度计算方式一定是按照内容高度计算，
         * 即，请重写 onMeasure 方法：
         * @Override
         * protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
         *     int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
         *     super.onMeasure(widthMeasureSpec, expandSpec);
         * }
         */
        if (me.isAllowInterceptTouch()) {
            impl.bkg.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (me.getDialogLifecycleCallback() instanceof BottomDialogSlideEventLifecycleCallback) {
                        if (((BottomDialogSlideEventLifecycleCallback) me.getDialogLifecycleCallback()).onSlideTouchEvent(me, v, event)) {
                            return true;
                        }
                    }
                    //这里 return 什么实际上无关紧要，重点在于 MaxRelativeLayout.java(dispatchTouchEvent:184) 的事件分发会独立触发此处的额外滑动事件
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            bkgTouchDownY = event.getY();
                            isBkgTouched = true;
                            bkgOldY = impl.boxBkg.getY();
                            break;
                        case MotionEvent.ACTION_MOVE:
                            if (isBkgTouched) {
                                float aimY = impl.boxBkg.getY() + event.getY() - bkgTouchDownY;
                                if (impl.scrollView.isCanScroll()) {
                                    if (aimY > impl.boxRoot.getUnsafePlace().top) {
                                        if (impl.scrollView.getScrollDistance() == 0) {
                                            if (impl.scrollView instanceof ScrollController) {
                                                ((ScrollController) impl.scrollView).lockScroll(true);
                                            }
                                            impl.boxBkg.setY(aimY);
                                        } else {
                                            bkgTouchDownY = event.getY();
                                        }
                                    } else {
                                        if (impl.scrollView instanceof ScrollController) {
                                            ((ScrollController) impl.scrollView).lockScroll(false);
                                        }
                                        impl.boxBkg.setY(impl.boxRoot.getUnsafePlace().top);
                                    }
                                } else {
                                    if (aimY > impl.boxRoot.getUnsafePlace().top) {
                                        impl.boxBkg.setY(aimY);
                                        return true;
                                    } else {
                                        impl.boxBkg.setY(impl.boxRoot.getUnsafePlace().top);
                                    }
                                }
                            }
                            break;
                        case MotionEvent.ACTION_UP:
                        case MotionEvent.ACTION_CANCEL:
                            scrolledY = impl.scrollView.getScrollDistance();
                            isBkgTouched = false;
                            if (bkgOldY == impl.boxRoot.getUnsafePlace().top) {
                                if (impl.boxBkg.getY() > impl.boxRoot.getUnsafePlace().top + impl.bkgEnterAimY + DialogX.touchSlideTriggerThreshold) {
                                    impl.preDismiss();
                                } else if (impl.boxBkg.getY() != bkgOldY) {
                                    ObjectAnimator enterAnim = ObjectAnimator.ofFloat(impl.boxBkg, "y", impl.boxBkg.getY(),
                                            impl.bkgEnterAimY);
                                    enterAnim.setDuration(300);
                                    enterAnim.start();
                                }
                            } else {
                                if (impl.boxBkg.getY() > bkgOldY + DialogX.touchSlideTriggerThreshold) {
                                    impl.preDismiss();
                                } else if (impl.boxBkg.getY() != bkgOldY) {
                                    ObjectAnimator enterAnim = ObjectAnimator.ofFloat(impl.boxBkg, "y", impl.boxBkg.getY(), impl.boxRoot.getUnsafePlace().top);
                                    enterAnim.setDuration(300);
                                    enterAnim.start();
                                }
                            }
                            break;
                    }
                    return false;
                }
            });
        } else {
            if (impl.scrollView instanceof ScrollController) {
                ((ScrollController) impl.scrollView).lockScroll(false);
            }
            impl.bkg.setOnTouchListener(null);
        }
    }

    private int dip2px(float dpValue) {
        final float scale = Resources.getSystem().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
}
