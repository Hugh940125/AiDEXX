package com.microtech.aidexx.utils;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.WindowManager;

import com.microtech.aidexx.AidexxApp;

/**
 * 常用单位转换的辅助类
 */
public class DensityUtils {

    private DensityUtils() {
        throw new UnsupportedOperationException("cannot be instantiated");
    }

    /**
     * dp转px
     *
     * @return
     */
    public static int dp2px(float dpVal) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                dpVal, AidexxApp.instance.getResources().getDisplayMetrics());
    }

    /**
     * dp转px
     *
     * @param context
     * @return
     */
    public static float dp2pix(Context context, float dpVal) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                dpVal, context.getResources().getDisplayMetrics());
    }

    /**
     * sp转px
     *
     * @return
     */
    public static float sp2px(float spVal) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                spVal, AidexxApp.instance.getResources().getDisplayMetrics());
    }

    /**
     * px转dp
     *
     * @param context
     * @param pxVal
     * @return
     */
    public static float px2dp(Context context, float pxVal) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (pxVal / scale);
    }

    /**
     * px转sp
     *
     * @param pxVal
     * @return
     */
    public static float px2sp(Context context, float pxVal) {
        return (pxVal / context.getResources().getDisplayMetrics().scaledDensity);
    }

    // 获取屏幕的高度
    public static int getWindowHeight(Activity activity) {
        DisplayMetrics dm = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        return dm.heightPixels;
    }

    //获取屏幕的高度
    public static int getWindowWidth(Activity activity) {
        DisplayMetrics dm = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        return dm.widthPixels;
    }

    private static DisplayMetrics getDisplayMetrics(Context context) {
        DisplayMetrics outMetrics = new DisplayMetrics();
        ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(outMetrics);
        return outMetrics;
    }

}  