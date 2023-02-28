package com.microtech.aidexx.widget.dialog.x.util;

import static android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN;
import static android.view.WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
import static android.view.WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION;
import static android.view.WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
import static android.view.WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
import static android.view.WindowManager.LayoutParams.MATCH_PARENT;
import static android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE;
import static android.view.WindowManager.LayoutParams.TYPE_APPLICATION_ATTACHED_DIALOG;
import static android.view.WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
import static android.view.WindowManager.LayoutParams.TYPE_PHONE;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.provider.Settings;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.microtech.aidexx.widget.dialog.x.BaseDialog;
import com.microtech.aidexx.widget.dialog.x.DialogX;
import com.microtech.aidexx.widget.dialog.x.interfaces.NoTouchInterface;

public class WindowUtil {
    
    public static void show(Activity activity, View dialogView, boolean touchEnable) {
        try {
            if (activity.getWindow().getDecorView().isAttachedToWindow()) {
                showNow(activity, dialogView, touchEnable);
            } else {
                activity.getWindow().getDecorView().post(new Runnable() {
                    @Override
                    public void run() {
                        showNow(activity, dialogView, touchEnable);
                    }
                });
            }
        } catch (Exception e) {
            if (activity != null && !activity.isDestroyed()) {
                showNow(activity, dialogView, touchEnable);
            }
        }
    }
    
    private static void showNow(Activity activity, View dialogView, boolean touchEnable) {
        if (DialogX.globalHoverWindow && !Settings.canDrawOverlays(activity)) {
            Toast.makeText(activity, "使用 DialogX.globalHoverWindow 必须开启悬浮窗权限", Toast.LENGTH_LONG).show();
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
            activity.startActivity(intent);
            return;
        }
        FrameLayout rootLayout = new FrameLayout(activity);
        if (dialogView.getParent() != null) {
            ((ViewGroup) dialogView.getParent()).removeView(dialogView);
        }
        rootLayout.addView(dialogView, new FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT));
        WindowManager manager = (WindowManager) activity.getSystemService(Context.WINDOW_SERVICE);
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.gravity = Gravity.CENTER_VERTICAL;
        layoutParams.format = PixelFormat.TRANSPARENT;
        if (DialogX.globalHoverWindow) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                layoutParams.type = TYPE_APPLICATION_OVERLAY;
            } else {
                layoutParams.type = TYPE_PHONE;
            }
        } else {
            layoutParams.type = TYPE_APPLICATION_ATTACHED_DIALOG;
        }
        layoutParams.flags = FLAG_FULLSCREEN |
                FLAG_TRANSLUCENT_STATUS |
                FLAG_TRANSLUCENT_NAVIGATION |
                FLAG_LAYOUT_IN_SCREEN
        ;
        layoutParams.softInputMode = SOFT_INPUT_ADJUST_RESIZE;
        if (!touchEnable) {
            dialogView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    for (int i = BaseDialog.getRunningDialogList().size() - 1; i >= 0; i--) {
                        BaseDialog baseDialog = BaseDialog.getRunningDialogList().get(i);
                        if (!(baseDialog instanceof NoTouchInterface) && baseDialog.getOwnActivity() == activity) {
                            if (baseDialog.getDialogView() == null) {
                                return false;
                            }
                            return baseDialog.getOwnActivity().dispatchTouchEvent(event);
                        }
                    }
                    return activity.dispatchTouchEvent(event);
                }
            });
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            layoutParams.layoutInDisplayCutoutMode = LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }
        manager.addView(rootLayout, layoutParams);
    }
    
    public static void dismiss(View dialogView) {
        BaseDialog baseDialog = (BaseDialog) dialogView.getTag();
        if (baseDialog != null && baseDialog.getOwnActivity() != null) {
            WindowManager manager = (WindowManager) baseDialog.getOwnActivity().getSystemService(Context.WINDOW_SERVICE);
            manager.removeViewImmediate((View) dialogView.getParent());
        }
    }
}
