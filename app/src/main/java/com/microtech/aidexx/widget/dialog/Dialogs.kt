package com.microtech.aidexx.widget.dialog

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import com.microtech.aidexx.widget.dialog.standard.StandardDialog
import com.microtech.aidexx.widget.dialog.x.TipDialog
import com.microtech.aidexx.widget.dialog.x.WaitDialog

/**
 *@date 2023/2/9
 *@author Hugh
 *@desc 多次弹出只保留最后一个
 */
object Dialogs {

    private var dialogList = mutableListOf<StandardDialog>()

    fun showAlert(
        context: AppCompatActivity,
        title: String?,
        content: String?,
        callBack: (() -> Unit)?
    ) {
        if (dialogList.isNotEmpty()) {
            dialogList.forEach {
                it.dismiss()
            }
            dialogList.clear()
        }
        val standardDialog = StandardDialog.Setter(context)
            .content(content)
            .title(title)
            .setPositive { dialog, _ ->
                dialog.dismiss()
                callBack?.invoke()
            }.create()
        dialogList.add(standardDialog)
        standardDialog.show()
    }

    fun showMessage(
        context: AppCompatActivity,
        title: String? = null,
        content: String?,
        callBack: (() -> Unit)?
    ) {
        StandardDialog.Setter(context)
            .content(content)
            .title(title)
            .setPositive { dialog, _ ->
                dialog.dismiss()
                callBack?.invoke()
            }.create().show()
    }

    fun showWhether(
        context: Context,
        title: String? = null,
        content: String?,
        confirm: (() -> Unit)?,
        cancel: (() -> Unit)? = null
    ) {
        StandardDialog.Setter(context)
            .content(content)
            .title(title)
            .setPositive { dialog, _ ->
                dialog.dismiss()
                confirm?.invoke()
            }.setCancel { dialog, _ ->
                dialog.dismiss()
                cancel?.invoke()
            }.create().show()
    }

    fun showWait(
        content: String? = null,
    ) {
        WaitDialog.show(content)
    }

    fun showSuccess(
        content: String? = null,
    ) {
        TipDialog.show(content, WaitDialog.TYPE.SUCCESS)
    }

    fun showError(
        content: String? = null,
    ) {
        TipDialog.show(content, WaitDialog.TYPE.ERROR)
    }

    fun dismissWait() {
        WaitDialog.dismiss()
    }
}