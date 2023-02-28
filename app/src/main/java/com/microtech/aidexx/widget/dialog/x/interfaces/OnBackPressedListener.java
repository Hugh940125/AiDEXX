package com.microtech.aidexx.widget.dialog.x.interfaces;

import com.microtech.aidexx.widget.dialog.x.BaseDialog;

public interface OnBackPressedListener<D extends BaseDialog> {
    boolean onBackPressed(D dialog);
}
