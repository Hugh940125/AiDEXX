package com.microtech.aidexx.widget.dialog.x;

import java.util.ArrayList;

public class DialogListBuilder {
    
    ArrayList<BaseDialog> dialogs;
    
    public static DialogListBuilder create(BaseDialog... dialogs) {
        DialogListBuilder builder = new DialogListBuilder();
        for (BaseDialog d : dialogs) {
            builder.add(d);
        }
        return builder;
    }
    
    public DialogListBuilder add(BaseDialog dialog) {
        if (dialogs == null) {
            dialogs = new ArrayList<>();
        }
        if (dialog.isShow() || dialog.isPreShow()) {
            return this;
        }
        dialog.setDialogListBuilder(this);
        dialogs.add(dialog);
        return this;
    }
    
    public DialogListBuilder show() {
        if (dialogs == null || dialogs.isEmpty()) {
            return this;
        }
        dialogs.get(0).show();
        return this;
    }
    
    public void showNext() {
        if (dialogs == null || dialogs.isEmpty()) {
            return;
        }
        dialogs.remove(dialogs.get(0));
        if (!dialogs.isEmpty()) {
            dialogs.get(0).show();
        }
    }
    
    public boolean isEmpty() {
        if (dialogs == null) {
            return true;
        }
        return dialogs.isEmpty();
    }
}
