package com.microtech.aidexx.widget.dialog.x.interfaces;

import android.view.View;
import com.microtech.aidexx.widget.dialog.x.BaseDialog;

public interface OnBackgroundMaskClickListener<D extends BaseDialog> {
    boolean onClick(D dialog, View v);
}
