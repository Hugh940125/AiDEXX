package com.microtech.aidexx.views.dialog.lib.interfaces;

import com.microtech.aidexx.views.dialog.lib.BaseDialog;

public interface OnBackPressedListener<D extends BaseDialog> {
    boolean onBackPressed(D dialog);
}
