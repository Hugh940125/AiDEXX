package com.microtech.aidexx.widget.dialog

import android.content.Context
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.microtech.aidexx.R
import com.microtech.aidexx.utils.ThemeManager
import com.microtech.aidexx.widget.dialog.lib.TipDialog
import com.microtech.aidexx.widget.dialog.lib.WaitDialog
import com.microtech.aidexx.widget.dialog.lib.interfaces.OnBindView
import com.microtech.aidexx.widget.dialog.standard.StandardDialog
import com.microtech.aidexx.widget.selector.option.OptionsPickerBuilder
import com.microtech.aidexx.widget.selector.option.OptionsPickerView

/**
 *@date 2023/2/9
 *@author Hugh
 *@desc 多次弹出只保留最后一个
 */
object Dialogs {
    private var dialogList = mutableListOf<StandardDialog>()

//    fun showBottom(onBindView: OnBindView<BottomDialog?>): BottomDialog {
//        return NoSlideBottomDialog(onBindView).show()
//    }

    class Picker(private val context: Context) {

        private lateinit var pickBuilder: OptionsPickerView<String>
        fun singlePick(list: List<String>, selectPos: Int, callBack: (pos: Int) -> Unit) {
            pickBuilder = OptionsPickerBuilder(context) { _, option2, _, _ ->
                callBack.invoke(option2)
            }.setLayoutRes(R.layout.layout_option_pick) { v ->
                val tvSubmit = v.findViewById<View>(R.id.tv_option_confirm) as TextView
                val ivCancel = v.findViewById<View>(R.id.tv_option_cancel) as TextView
                tvSubmit.setOnClickListener {
                    pickBuilder.returnData()
                    pickBuilder.dismiss()
                }
                ivCancel.setOnClickListener {
                    pickBuilder.dismiss()
                }
            }
                .setTextColorCenter(context.getColor(R.color.green_65))
                .setDividerColor(context.getColor(R.color.green_65))
                .isDialog(false)
                .setOutSideCancelable(true)
                .setSelectOptions(0, selectPos, 0)
                .setBgColor(ThemeManager.getTypeValue(context, R.attr.bgMainTab))
                .build()
            pickBuilder.setNPicker(emptyList(), list, emptyList()) //添加数据
            pickBuilder.show()
        }
    }

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
        context: Context,
        title: String? = null,
        content: String?,
        callBack: (() -> Unit)? = null
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
        content: String? = null,
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