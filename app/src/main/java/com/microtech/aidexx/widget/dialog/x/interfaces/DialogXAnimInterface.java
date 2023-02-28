package com.microtech.aidexx.widget.dialog.x.interfaces;

import com.microtech.aidexx.widget.dialog.x.ObjectRunnable;

public abstract class DialogXAnimInterface<D> {
    
    public void doShowAnim(D dialog, ObjectRunnable<Float> animProgress) {
    }
    
    public void doExitAnim(D dialog, ObjectRunnable<Float> animProgress) {
    }
}
