package com.microtech.aidexx.views.dialog.bottom;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import androidx.annotation.NonNull;

import com.microtech.aidexx.utils.blankj.KeyboardUtils;

public class BaseBottomDialog extends Dialog {


    public BaseBottomDialog(@NonNull Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);//去除标题
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onStart() {
        super.onStart();
        getWindow().setBackgroundDrawableResource(android.R.color.transparent); //去除自带的背景
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT); //宽度占满
        getWindow().setGravity(Gravity.BOTTOM);
        setCanceledOnTouchOutside(true);

        View focusView = getFocusView();
        if (focusView != null) {
            focusView.postDelayed(
                    () -> KeyboardUtils.INSTANCE.showSoftInput(focusView),
                    200);
        }

    }
    public View getFocusView(){
        return null;
    }

    private void hideSoftInput(){
        View focusView = getFocusView();
        if (focusView != null) {
            KeyboardUtils.INSTANCE.hideSoftInput(focusView);
        }
    }

    @Override
    public void dismiss() {
        hideSoftInput();
        super.dismiss();
    }
}
