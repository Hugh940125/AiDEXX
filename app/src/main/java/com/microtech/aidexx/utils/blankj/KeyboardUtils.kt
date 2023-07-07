package com.microtech.aidexx.utils.blankj

import android.R
import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.ResultReceiver
import android.os.SystemClock
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import com.microtech.aidexx.common.getContext
import java.lang.reflect.Field


object KeyboardUtils {

    private const val TAG_ON_GLOBAL_LAYOUT_LISTENER = -8

    private fun KeyboardUtils() {
        throw UnsupportedOperationException("u can't instantiate me...")
    }

    /**
     * Show the soft input.
     */
    fun showSoftInput() {
        val imm =
            getContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                ?: return
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY)
    }

    /**
     * Show the soft input.
     */
    fun showSoftInput(activity: Activity?) {
        if (activity == null) {
            return
        }
        if (!isSoftInputVisible(activity)) {
            toggleSoftInput()
        }
    }

    /**
     * Show the soft input.
     *
     * @param view The view.
     */
    fun showSoftInput(view: View) {
        showSoftInput(view, 0)
    }

    /**
     * Show the soft input.
     *
     * @param view The view.
     * @param flags Provides additional operating flags.  Currently may be
     * 0 or have the [InputMethodManager.SHOW_IMPLICIT] bit set.
     */
    fun showSoftInput(view: View, flags: Int) {
        val imm =
            getContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                ?: return
        view.isFocusable = true
        view.isFocusableInTouchMode = true
        view.requestFocus()
        imm.showSoftInput(view, flags, object : ResultReceiver(Handler()) {
            override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
                if (resultCode == InputMethodManager.RESULT_UNCHANGED_HIDDEN
                    || resultCode == InputMethodManager.RESULT_HIDDEN
                ) {
                    toggleSoftInput()
                }
            }
        })
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY)
    }

    /**
     * Hide the soft input.
     *
     * @param activity The activity.
     */
    fun hideSoftInput( activity: Activity?) {
        if (activity == null) {
            return
        }
        hideSoftInput(activity.window)
    }

    /**
     * Hide the soft input.
     *
     * @param window The window.
     */
    fun hideSoftInput( window: Window?) {
        if (window == null) {
            return
        }
        var view = window.currentFocus
        if (view == null) {
            val decorView = window.decorView
            val focusView = decorView.findViewWithTag<View>("keyboardTagView")
            if (focusView == null) {
                view = EditText(window.context)
                view.setTag("keyboardTagView")
                (decorView as ViewGroup).addView(view, 0, 0)
            } else {
                view = focusView
            }
            view.requestFocus()
        }
        hideSoftInput(view)
    }

    /**
     * Hide the soft input.
     *
     * @param view The view.
     */
    fun hideSoftInput(view: View) {
        val imm =
            getContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                ?: return
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private var millis: Long = 0

    /**
     * Hide the soft input.
     *
     * @param activity The activity.
     */
    fun hideSoftInputByToggle( activity: Activity?) {
        if (activity == null) {
            return
        }
        val nowMillis = SystemClock.elapsedRealtime()
        val delta = nowMillis - millis
        if (Math.abs(delta) > 500 && isSoftInputVisible(activity)) {
            toggleSoftInput()
        }
        millis = nowMillis
    }

    /**
     * Toggle the soft input display or not.
     */
    fun toggleSoftInput() {
        val imm =
            getContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                ?: return
        imm.toggleSoftInput(0, 0)
    }

    private var sDecorViewDelta = 0

    /**
     * Return whether soft input is visible.
     *
     * @param activity The activity.
     * @return `true`: yes<br></br>`false`: no
     */
    fun isSoftInputVisible(activity: Activity): Boolean {
        return getDecorViewInvisibleHeight(activity.window) > 0
    }

    private fun getDecorViewInvisibleHeight(window: Window): Int {
        val decorView = window.decorView
        val outRect = Rect()
        decorView.getWindowVisibleDisplayFrame(outRect)
        Log.d(
            "KeyboardUtils",
            "getDecorViewInvisibleHeight: " + (decorView.bottom - outRect.bottom)
        )
        val delta = Math.abs(decorView.bottom - outRect.bottom)
        if (delta <= getNavBarHeight() + getStatusBarHeight()) {
            sDecorViewDelta = delta
            return 0
        }
        return delta - sDecorViewDelta
    }

    /**
     * Register soft input changed listener.
     *
     * @param activity The activity.
     * @param listener The soft input changed listener.
     */
    fun registerSoftInputChangedListener(
        activity: Activity,
        listener: OnSoftInputChangedListener
    ) {
        registerSoftInputChangedListener(activity.window, listener)
    }

    /**
     * Register soft input changed listener.
     *
     * @param window The window.
     * @param listener The soft input changed listener.
     */
    fun registerSoftInputChangedListener(
        window: Window,
        listener: OnSoftInputChangedListener
    ) {
        val flags = window.attributes.flags
        if (flags and WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS != 0) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        }
        val contentView = window.findViewById<FrameLayout>(R.id.content)
        val decorViewInvisibleHeightPre = intArrayOf(getDecorViewInvisibleHeight(window))
        val onGlobalLayoutListener = OnGlobalLayoutListener {
            val height = getDecorViewInvisibleHeight(window)
            if (decorViewInvisibleHeightPre[0] != height) {
                listener.onSoftInputChanged(height)
                decorViewInvisibleHeightPre[0] = height
            }
        }
        contentView.viewTreeObserver.addOnGlobalLayoutListener(onGlobalLayoutListener)
        contentView.setTag(TAG_ON_GLOBAL_LAYOUT_LISTENER, onGlobalLayoutListener)
    }

    /**
     * Unregister soft input changed listener.
     *
     * @param window The window.
     */
    fun unregisterSoftInputChangedListener(window: Window) {
        val contentView = window.findViewById<View>(R.id.content) ?: return
        val tag = contentView.getTag(TAG_ON_GLOBAL_LAYOUT_LISTENER)
        if (tag is OnGlobalLayoutListener) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                contentView.viewTreeObserver.removeOnGlobalLayoutListener(tag)
                //这里会发生内存泄漏 如果不设置为null
                contentView.setTag(TAG_ON_GLOBAL_LAYOUT_LISTENER, null)
            }
        }
    }

    /**
     * Fix the bug of 5497 in Android.
     *
     * Don't set adjustResize
     *
     * @param activity The activity.
     */
    fun fixAndroidBug5497(activity: Activity) {
        fixAndroidBug5497(activity.window)
    }

    /**
     * Fix the bug of 5497 in Android.
     *
     * It will clean the adjustResize
     *
     * @param window The window.
     */
    fun fixAndroidBug5497(window: Window) {
        val softInputMode = window.attributes.softInputMode
        window.setSoftInputMode(
            softInputMode and WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE.inv()
        )
        val contentView = window.findViewById<FrameLayout>(R.id.content)
        val contentViewChild = contentView.getChildAt(0)
        val paddingBottom = contentViewChild.paddingBottom
        val contentViewInvisibleHeightPre5497 = intArrayOf(getContentViewInvisibleHeight(window))
        contentView.viewTreeObserver.addOnGlobalLayoutListener {
            val height = getContentViewInvisibleHeight(window)
            if (contentViewInvisibleHeightPre5497[0] != height) {
                contentViewChild.setPadding(
                    contentViewChild.paddingLeft,
                    contentViewChild.paddingTop, contentViewChild.paddingRight,
                    paddingBottom + getDecorViewInvisibleHeight(window)
                )
                contentViewInvisibleHeightPre5497[0] = height
            }
        }
    }

    fun getStatusBarHeight(): Int {
        val resources: Resources = Resources.getSystem()
        val resourceId: Int = resources.getIdentifier("status_bar_height", "dimen", "android")
        return resources.getDimensionPixelSize(resourceId)
    }

    fun getNavBarHeight(): Int {
        val res = Resources.getSystem()
        val resourceId = res.getIdentifier("navigation_bar_height", "dimen", "android")
        return if (resourceId != 0) {
            res.getDimensionPixelSize(resourceId)
        } else {
            0
        }
    }

    private fun getContentViewInvisibleHeight(window: Window): Int {
        val contentView = window.findViewById<View>(R.id.content) ?: return 0
        val outRect = Rect()
        contentView.getWindowVisibleDisplayFrame(outRect)
        Log.d(
            "KeyboardUtils",
            "getContentViewInvisibleHeight: " + (contentView.bottom - outRect.bottom)
        )
        val delta = Math.abs(contentView.bottom - outRect.bottom)
        return if (delta <= getStatusBarHeight() + getNavBarHeight()) {
            0
        } else delta
    }

    /**
     * Fix the leaks of soft input.
     *
     * @param activity The activity.
     */
    fun fixSoftInputLeaks(activity: Activity) {
        fixSoftInputLeaks(activity.window)
    }

    /**
     * Fix the leaks of soft input.
     *
     * @param window The window.
     */
    fun fixSoftInputLeaks(window: Window) {
        val imm =
            getContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                ?: return
        val leakViews = arrayOf("mLastSrvView", "mCurRootView", "mServedView", "mNextServedView")
        for (leakView in leakViews) {
            try {
                val leakViewField: Field = InputMethodManager::class.java.getDeclaredField(leakView)
                if (!leakViewField.isAccessible) {
                    leakViewField.isAccessible = true
                }
                val obj: Any = leakViewField.get(imm) as? View ?: continue
                val view = obj as View
                if (view.rootView === window.decorView.rootView) {
                    leakViewField.set(imm, null)
                }
            } catch (ignore: Throwable) { /**/
            }
        }
    }

    /**
     * Click blank area to hide soft input.
     *
     * Copy the following code in ur activity.
     */
    fun clickBlankArea2HideSoftInput() {
//        Log.i("KeyboardUtils", "Please refer to the following code.")
        /*
        @Override
        public boolean dispatchTouchEvent(MotionEvent ev) {
            if (ev.getAction() == MotionEvent.ACTION_DOWN) {
                View v = getCurrentFocus();
                if (isShouldHideKeyboard(v, ev)) {
                    KeyboardUtils.hideSoftInput(this);
                }
            }
            return super.dispatchTouchEvent(ev);
        }

        // Return whether touch the view.
        private boolean isShouldHideKeyboard(View v, MotionEvent event) {
            if ((v instanceof EditText)) {
                int[] l = {0, 0};
                v.getLocationOnScreen(l);
                int left = l[0],
                        top = l[1],
                        bottom = top + v.getHeight(),
                        right = left + v.getWidth();
                return !(event.getRawX() > left && event.getRawX() < right
                        && event.getRawY() > top && event.getRawY() < bottom);
            }
            return false;
        }
        */
    }

    ///////////////////////////////////////////////////////////////////////////
    // interface
    ///////////////////////////////////////////////////////////////////////////
    interface OnSoftInputChangedListener {
        fun onSoftInputChanged(height: Int)
    }

}