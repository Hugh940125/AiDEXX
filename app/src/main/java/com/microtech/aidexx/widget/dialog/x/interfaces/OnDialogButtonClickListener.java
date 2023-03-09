package com.microtech.aidexx.widget.dialog.x.interfaces;

import android.view.View;

import com.microtech.aidexx.widget.dialog.x.BaseDialog;

public interface OnDialogButtonClickListener<D extends BaseDialog> extends BaseOnDialogClickCallback{
    
    boolean onClick(D dialog, View v);
    
}
