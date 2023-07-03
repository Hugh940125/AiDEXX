package com.microtech.aidexx.ui.setting.share

import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.microtech.aidexx.R
import com.microtech.aidexx.base.BaseActivity
import com.microtech.aidexx.base.BaseViewModel
import com.microtech.aidexx.common.toast
import com.microtech.aidexx.databinding.ActivityShareAddUserBinding
import com.microtech.aidexx.utils.NetUtil
import com.microtech.aidexx.views.dialog.lib.WaitDialog
import kotlinx.coroutines.launch

class ShareAddUserActivity : BaseActivity<BaseViewModel, ActivityShareAddUserBinding>() {

    private val sfViewModel: ShareFollowViewModel by viewModels()

    override fun getViewBinding(): ActivityShareAddUserBinding {
        return ActivityShareAddUserBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.apply {
            actionBar.getLeftIcon().setOnClickListener {
                finish()
            }
            btnAddShareSave.setOnClickListener {
                val account = etAddAccountValue.text.toString()
                val alias = etAddAliasValue.text.toString()
                if (account.isEmpty()){
                    getString(R.string.hint_share_add_account).toast()
                    return@setOnClickListener
                }
                if (NetUtil.isNetAvailable(this@ShareAddUserActivity)) {
                    WaitDialog.show(this@ShareAddUserActivity, getString(R.string.loading))
                    lifecycleScope.launch {
                        sfViewModel.shareMyselfToOther(account, alias).collect {
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

