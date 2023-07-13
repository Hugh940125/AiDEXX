package com.microtech.aidexx.ui.setting.share

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.microtech.aidexx.R
import com.microtech.aidexx.base.BaseActivity
import com.microtech.aidexx.base.BaseViewModel
import com.microtech.aidexx.common.toast
import com.microtech.aidexx.databinding.ActivityShareAndFollowEditBinding
import com.microtech.aidexx.utils.NetUtil
import com.microtech.aidexx.views.dialog.Dialogs
import com.microtech.aidexx.views.dialog.lib.WaitDialog
import kotlinx.coroutines.launch

const val EDIT_FROM_SHARE = 1
const val EDIT_FROM_FOLLOW = 2
const val EDIT_FROM = "EDIT_FROM"
const val EDIT_DATA = "EDIT_DATA"

class ShareAndFollowEditActivity :
    BaseActivity<BaseViewModel, ActivityShareAndFollowEditBinding>(), View.OnClickListener {

    private var hideState: Boolean? = null
    private var normalPush: Boolean? = null
    private var emergePush: Boolean? = null
    private var editFrom: Int = 0
    private lateinit var editData: ShareUserInfo

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

            val editDataTmp = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                intent.getParcelableExtra(EDIT_DATA, ShareUserInfo::class.java)
            }else {
                intent.getParcelableExtra(EDIT_DATA)
            }

            editDataTmp?.let { shareUserInfo ->
                shareUserInfo.userAuthorizationId?.let {
                    editData = shareUserInfo
                } ?: finish()
            } ?: finish()

            val accountMasked = editData.getDisplayName()
            shareAndFollowEditActionbar.setTitle(accountMasked)
            tvAccountValue.text = accountMasked
            etAliasValue.setText(editData.providerAlias ?: "")
            when (editFrom) {
                EDIT_FROM_SHARE -> {
                    clEdit.visibility = View.GONE
                    btnEditDelete.text = getString(R.string.cancel_share)

                }
                EDIT_FROM_FOLLOW -> {
                    clEdit.visibility = View.VISIBLE
                    btnEditDelete.text = getString(R.string.cancel_follow)
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

            initSwitchState(editData.hide, editData.normalPush, editData.emergePush)

            normalNotice.editItemSwitch.setOnCheckedChangeListener { _, isChecked ->
                normalPush = !isChecked
            }
            urgentNotice.editItemSwitch.setOnCheckedChangeListener { _, isChecked ->
                emergePush = !isChecked
            }
            showOrHide.editItemSwitch.setOnCheckedChangeListener { _, isChecked ->
                hideState = !isChecked
                initSwitchState(!isChecked, null, null)
            }

            shareAndFollowEditActionbar.setTitle(editData.getDisplayName())
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
                val alias = binding.etAliasValue.text.toString()
                if (normalPush != null || emergePush != null || alias != editData.providerAlias) {
                    if (NetUtil.isNetAvailable(this)) {
                        val map = mutableMapOf<String, Any>()

                        if (editFrom == EDIT_FROM_SHARE) {

                            WaitDialog.show(this, getString(R.string.loading))
                            lifecycleScope.launch {
                                sfViewModel.modifyShareUser(
                                    alias,
                                    editData.userAuthorizationId!!
                                ).collect {
                                    WaitDialog.dismiss()
                                    if (it) {
                                        finish()
                                    } else {
                                        getString(R.string.failure).toast()
                                    }
                                }
                            }
                        } else if (editFrom == EDIT_FROM_FOLLOW) {
                            WaitDialog.show(this, getString(R.string.loading))
                            lifecycleScope.launch {
                                sfViewModel.modifyFollowUser(
                                    alias,
                                    editData.readerAlias,
                                    if (hideState == true) 1 else 0,
                                    if (emergePush == true) 1 else 0,
                                    if (normalPush == true) 1 else 0,
                                    editData.userAuthorizationId!!
                                ).collect {
                                    WaitDialog.dismiss()
                                    if (it) {
                                        finish()
                                    } else {
                                        getString(R.string.failure).toast()
                                    }
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
                Dialogs.showAlert(
                    this,
                    resources.getString(R.string.title_share_delete),
                    ""
                ) {
                    if (NetUtil.isNetAvailable(this)) {
                        WaitDialog.show(this, getString(R.string.loading))
                        lifecycleScope.launch {
                            sfViewModel.cancelShare(editData.userAuthorizationId!!).collect {
                                WaitDialog.dismiss()
                                if (it) {
                                    finish()
                                } else {
                                    getString(R.string.failure).toast()
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