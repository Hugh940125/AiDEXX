package com.microtech.aidexx.common.compliance

import android.content.Context
import com.microtech.aidexx.R
import com.microtech.aidexx.utils.mmkv.MmkvManager
import com.microtech.aidexx.views.dialog.standard.StandardDialog

class EnquireManager private constructor() {

    companion object {

        val FLAG_AVATAR = "ENQUIRE_FLAG_AVTAR"

        private val INSTANCE = EnquireManager()

        fun instance(): EnquireManager {
            return INSTANCE
        }
    }

    fun showEnquireOrNot(
        context: Context,
        title: String,
        content: String,
        onPositive: (() -> Unit)?,
        onNegative: (() -> Unit)? = null,
        flag: String? = null
    ): StandardDialog? {
        if ((flag != null && MmkvManager.getEnquireFlag(flag)) || flag == null) {
            val dialog = StandardDialog.Setter(context)
                .title(title)
                .content(content)
                .setPositive(
                    context.getString(R.string.allow)
                ) { dialog, _ ->
                    flag?.let {
                        MmkvManager.saveEnquireFlag(flag, true)
                    }
                    onPositive?.invoke()
                    dialog.dismiss()
                }.setCancel(
                    context.getString(R.string.dont_allow)
                ) { dialog, _ ->
                    onNegative?.invoke()
                    dialog.dismiss()
                }.create(StandardDialog.TYPE_VERTICAL)
            dialog.show()
            return dialog
        } else {
            onPositive?.invoke()
            return null
        }
    }
}