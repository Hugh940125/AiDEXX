package com.microtech.aidexx.ui.setting.share

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.microtech.aidexx.R
import com.microtech.aidexx.base.BaseActivity
import com.microtech.aidexx.base.BaseViewModel
import com.microtech.aidexx.common.toast
import com.microtech.aidexx.databinding.ActivityShareAndFollowEditBinding
import com.microtech.aidexx.db.entity.ShareUserEntity
import com.microtech.aidexx.utils.NetUtil
import com.microtech.aidexx.widget.dialog.Dialogs
import com.microtech.aidexx.widget.dialog.lib.WaitDialog
import kotlinx.coroutines.launch

const val EDIT_FROM_SHARE = 1
const val EDIT_FROM_FOLLOW = 2
const val EDIT_FROM = "EDIT_FROM"
const val EDIT_DATA = "EDIT_DATA"
const val OPERATION_DELETE = 1
const val OPERATION_SAVE = 2

class ShareAndFollowEditActivity :
    BaseActivity<BaseViewModel, ActivityShareAndFollowEditBinding>(), View.OnClickListener {

    private var hideState: Boolean? = null
    private var pushState: Boolean? = null
    private var emergeState: Boolean? = null
    private var editFrom: Int = 0
    private var editData: ShareUserEntity? = null
    private var operation = 0

    private val sfViewModel: ShareFollowViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        initView()
    }

    private fun initSwitchState(isHide: Boolean?, isNormal: Boolean?, isUrgent: Boolean?) {
        binding.apply {
            if (isHide != null) {
                showOrHide.editItemSwitch.isChecked = !isHide
                if (!isHide) {
                    normalNotice.clEditItem.visibility = View.VISIBLE
                    urgentNotice.clEditItem.visibility = View.VISIBLE
                    showOrHide.editDivider.visibility = View.VISIBLE
                    urgentNotice.editDivider.visibility = View.GONE
                } else {
                    normalNotice.clEditItem.visibility = View.GONE
                    urgentNotice.clEditItem.visibility = View.GONE
                    showOrHide.editDivider.visibility = View.GONE
                }
            }
            if (isNormal != null) {
                normalNotice.editItemSwitch.isChecked = !isNormal
            }
            if (isUrgent != null) {
                urgentNotice.editItemSwitch.isChecked = !isUrgent
            }
        }

    }

    private fun initView() {
        binding.apply {
            editFrom = intent.getIntExtra(EDIT_FROM, 0)
            editData = intent.getParcelableExtra(EDIT_DATA)
            when (editFrom) {
                EDIT_FROM_SHARE -> {
                    clEdit.visibility = View.GONE
                    btnEditDelete.text = getString(R.string.cancel_share)
                    shareAndFollowEditActionbar.setTitle(editData?.getDisplayName())
                    tvAccountValue.text = editData?.user?.emailAddress?.ifBlank { null }
                        ?: editData?.user?.phoneNumber

                    etAliasValue.setText(editData?.userAlias ?: "")
                }
                EDIT_FROM_FOLLOW -> {
                    clEdit.visibility = View.VISIBLE
                    btnEditDelete.text = getString(R.string.cancel_follow)
                    shareAndFollowEditActionbar.setTitle(
                        if (editData?.userAlias.isNullOrEmpty()) (if (editData?.user?.emailAddress.isNullOrBlank())
                            editData?.user?.phoneNumber else editData?.user?.emailAddress)
                        else editData?.userAlias
                    )
                    tvAccountValue.text = if (editData?.user?.emailAddress.isNullOrBlank())
                        editData?.user?.phoneNumber else editData?.user?.emailAddress
                    etAliasValue.setText(editData?.userAlias ?: "")
                }
            }
            shareAndFollowEditActionbar.getLeftIcon().setOnClickListener {
                finish()
            }
            showOrHide.switchEditTitle.text = getString(R.string.show_or_hide)
            showOrHide.editItemInfo.text = getString(R.string.show_or_hide_tip)

            normalNotice.switchEditTitle.text = getString(R.string.common_notice)
            normalNotice.editItemInfo.text = getString(R.string.common_include)

            urgentNotice.switchEditTitle.text = getString(R.string.urgent_notice)
            urgentNotice.editItemInfo.text = getString(R.string.urgent_include)

            initSwitchState(editData?.hide, editData?.pushState, editData?.emergeState)

            normalNotice.editItemSwitch.setOnCheckedChangeListener { _, isChecked ->
                pushState = !isChecked
            }
            urgentNotice.editItemSwitch.setOnCheckedChangeListener { _, isChecked ->
                emergeState = !isChecked
            }
            showOrHide.editItemSwitch.setOnCheckedChangeListener { _, isChecked ->
                hideState = !isChecked
                initSwitchState(!isChecked, null, null)
            }

            editData?.let {
                shareAndFollowEditActionbar.setTitle( it.userAlias?.ifEmpty { null }
                    ?: it.user?.emailAddress?.ifBlank { null }
                    ?: it.user?.phoneNumber
                )
            }
            btnEditSave.setOnClickListener(this@ShareAndFollowEditActivity)
            btnEditDelete.setOnClickListener(this@ShareAndFollowEditActivity)
        }


    }

    override fun getViewBinding(): ActivityShareAndFollowEditBinding {
        return ActivityShareAndFollowEditBinding.inflate(layoutInflater)
    }

    override fun onClick(v: View?) {
        when (v) {
            binding.btnEditSave -> {
                operation = OPERATION_SAVE
                val alias = binding.etAliasValue.text.toString()
                if (pushState != null || emergeState != null || alias != editData?.userAlias) {
                    if (NetUtil.isNetAvailable(this)) {
                        val map = mutableMapOf<String, Any>()
                        map["id"] = editData?.id ?: ""
                        if (editFrom == EDIT_FROM_SHARE) {
                            map["authorizedUserAlias"] = alias

                            WaitDialog.show(this, getString(R.string.loading))
                            lifecycleScope.launch {
                                sfViewModel.modifyShareUser().collect {
                                    WaitDialog.dismiss()
                                    // todo 接口
                                    "还没有接口".toast()
                                }
                            }
                        } else if (editFrom == EDIT_FROM_FOLLOW) {
                            map["id"] = editData?.id ?: ""
                            if (pushState != null) {
                                map["pushState"] = pushState!!
                            }
                            if (emergeState != null) {
                                map["emergeState"] = emergeState!!
                            }
                            if (hideState != null) {
                                map["hide"] = hideState!!
                            }
                            if ((editData?.userAlias ?: "") != alias) {
                                map["userAlias"] = alias
                            }
                            WaitDialog.show(this, getString(R.string.loading))
                            lifecycleScope.launch {
                                sfViewModel.modifyFollowUser().collect {
                                    WaitDialog.dismiss()
                                    // todo 接口
                                    "还没有接口".toast()
                                }
                            }
                        }
                    } else {
                        resources.getString(R.string.net_error).toast()
                    }
                } else {
                    finish()
                }
            }
            binding.btnEditDelete -> {
                operation = OPERATION_DELETE
                Dialogs.showAlert(
                    this,
                    resources.getString(R.string.title_share_delete),
                    ""
                ) {
                    if (NetUtil.isNetAvailable(this)) {
                        val map = mutableMapOf<String, String>()
                        map["id"] = editData?.id ?: ""
                        // todo 接口
                        WaitDialog.show(this, getString(R.string.loading))
                        lifecycleScope.launch {
                            sfViewModel.cancelShare().collect {
                                WaitDialog.dismiss()
                                if (it) {
                                    finish()
                                } else {
                                    "还没有接口".toast()
                                }
                            }
                        }

                    } else {
                        resources.getString(R.string.net_error).toast()
                    }
                }
            }
        }
    }
}