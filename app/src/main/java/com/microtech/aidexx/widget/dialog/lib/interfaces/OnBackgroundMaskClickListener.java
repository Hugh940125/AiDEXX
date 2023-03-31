package com.microtech.aidexx.widget.dialog.lib.interfaces;

import android.view.View;
import com.microtech.aidexx.widget.dialog.lib.BaseDialog;

public interface OnBackgroundMaskClickListener<D extends BaseDialog> {
    boolean onClick(D dialog, View v);
}
