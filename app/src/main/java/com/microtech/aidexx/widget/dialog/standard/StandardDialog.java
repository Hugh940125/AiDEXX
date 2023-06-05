package com.microtech.aidexx.widget.dialog.standard;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.GravityInt;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.microtech.aidexx.R;

public class StandardDialog extends AlertDialog {
    public static final int TYPE_STANDARD = 0;
    public static final int TYPE_VERTICAL = 1;

    private long tag;

    protected StandardDialog(@NonNull Context context) {
        super(context);
    }

    protected StandardDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    public void setTimeTag() {
        this.tag = System.currentTimeMillis();
    }

    public long getTag() {
        return tag;
    }

    public static class Setter {
        private final Context mContext;
        private String mContent, mCancel, mConfirm, mTitle;
        private @GravityInt int mGravity = Gravity.CENTER;
        private OnClickListener positiveClickListener;
        private OnClickListener cancelClickListener;
        private StandardDialog mStandardDialog;
        private DialogDismissListener dismissListener;

        public StandardDialog create() {
            return create(0);
        }

        @SuppressLint("InflateParams")
        public StandardDialog create(int type) {
            mStandardDialog = new StandardDialog(mContext, R.style.StandardDialog);
            mStandardDialog.setTimeTag();
            View view = null;
            if (type == TYPE_VERTICAL) {
                view = LayoutInflater.from(mContext).inflate(R.layout.standard_dialog_vertical, null);
            }
            if (view == null) {
                view = LayoutInflater.from(mContext).inflate(R.layout.standard_dialog, null);
            }
            TextView cancelBtn = view.findViewById(R.id.button_cancel);
            TextView conformBtn = view.findViewById(R.id.button_confirm);
            TextView contentTv = view.findViewById(R.id.content);
            View stickTv = view.findViewById(R.id.stick);
            TextView titleTv = view.findViewById(R.id.title);

            if (mTitle == null) {
                titleTv.setVisibility(View.GONE);
            } else {
                titleTv.setText(mTitle);
            }
            if (mContent == null) {
                contentTv.setVisibility(View.GONE);
            } else {
                contentTv.setText(mContent);
            }

            if (cancelClickListener == null) {
                cancelBtn.setVisibility(View.GONE);
                stickTv.setVisibility(View.GONE);
            } else {
                cancelBtn.setText(mCancel);
                cancelBtn.setOnClickListener(view1 -> cancelClickListener.onClick(mStandardDialog, DialogInterface.BUTTON_NEGATIVE));
            }
            if (positiveClickListener == null) {
                conformBtn.setVisibility(View.GONE);
                stickTv.setVisibility(View.GONE);
            } else {
                conformBtn.setText(mConfirm);
                conformBtn.setOnClickListener(view1 -> positiveClickListener.onClick(mStandardDialog, DialogInterface.BUTTON_POSITIVE));
            }
            mStandardDialog.setOnDismissListener(dialog -> {
                if (dismissListener != null) {
                    dismissListener.onDismiss();
                }
            });
            mStandardDialog.setView(view);
            mStandardDialog.setCanceledOnTouchOutside(false);

            mStandardDialog.getWindow().setGravity(mGravity);

            return mStandardDialog;
        }

        public Setter(Context context) {
            this.mContext = context;
            mConfirm = mContext.getString(R.string.confirm_yes);
            mCancel = mContext.getString(R.string.btn_cancel);
        }

        public Setter setOnDismissListener(DialogDismissListener dismissListener) {
            this.dismissListener = dismissListener;
            return this;
        }

        public Setter setPositive(String confirm, OnClickListener positiveListener) {
            if (!TextUtils.isEmpty(confirm)) {
                this.mConfirm = confirm;
            }
            this.positiveClickListener = positiveListener;
            return this;
        }

        public Setter setPositive(OnClickListener positiveListener) {
            this.positiveClickListener = positiveListener;
            return this;
        }

        public Setter setCancel(String cancel, OnClickListener cancelListener) {
            if (!TextUtils.isEmpty(cancel)) {
                this.mCancel = cancel;
            }
            this.cancelClickListener = cancelListener;
            return this;
        }

        public Setter setCancel(OnClickListener cancelListener) {
            this.cancelClickListener = cancelListener;
            return this;
        }

        public Setter content(String content) {
            this.mContent = content;
            return this;
        }

        public Setter title(String title) {
            this.mTitle = title;
            return this;
        }

        public Setter gravity(@GravityInt int gravity) {
            this.mGravity = gravity;
            return this;
        }
    }
}