package com.microtech.aidexx.common.compliance

import android.content.Context
import com.microtech.aidexx.R
import com.microtech.aidexx.utils.mmkv.MmkvManager
import com.microtech.aidexx.widget.dialog.standard.StandardDialog

class EnquireManager private constructor() {

    private val enquireList = mutableListOf<StandardDialog>()

    companion object {
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
        onNegative: (() -> Unit)?,
        flag: String
    ) {
        if (!MmkvManager.getEnquireFlag(flag)) {
            if (enquireList.isNotEmpty()) {
                enquireList.forEach {
                    it.dismiss()
                }
                enquireList.clear()
            }
            val dialog = StandardDialog.Setter(context)
                .title(title)
                .content(content)
                .setPositive(
                    context.getString(R.string.allow)
                ) { dialog, _ ->
                    MmkvManager.saveEnquireFlag(flag, true)
                    onPositive?.invoke()
                    dialog.dismiss()
                }.setCancel(
                    context.getString(R.string.dont_allow)
                ) { dialog, _ ->
                    onNegative?.invoke()
                    dialog.dismiss()
                }.create(StandardDialog.TYPE_VERTICAL)
            dialog.show()
            enquireList.add(dialog)
        } else {
            onPositive?.invoke()
        }
    }
}