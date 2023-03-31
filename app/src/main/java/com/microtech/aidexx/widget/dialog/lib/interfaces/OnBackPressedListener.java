package com.microtech.aidexx.widget.dialog.lib.interfaces;

import com.microtech.aidexx.widget.dialog.lib.BaseDialog;

public interface OnBackPressedListener<D extends BaseDialog> {
    boolean onBackPressed(D dialog);
}
