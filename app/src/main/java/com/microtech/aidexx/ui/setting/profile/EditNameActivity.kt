package com.microtech.aidexx.ui.setting.profile

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.microtech.aidexx.R
import com.microtech.aidexx.base.BaseActivity
import com.microtech.aidexx.base.BaseViewModel
import com.microtech.aidexx.common.setDebounceClickListener
import com.microtech.aidexx.common.toast
import com.microtech.aidexx.databinding.ActivityEditNameBinding
import com.microtech.aidexx.utils.ActivityUtil
import com.microtech.aidexx.views.dialog.Dialogs
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class EditNameActivity : BaseActivity<BaseViewModel, ActivityEditNameBinding>() {

    private val pVm by viewModels<ProfileViewModel>()

    companion object {
        private val TAG = EditNameActivity::class.java.simpleName
        private val EXTRA_NAME = "EXTRA_NAME"
        private const val TYPE_NICK_NAME = 0
        private const val TYPE_FULL_NAME = 1
        fun startEditNickName(context: Context, name: String?) {
            ActivityUtil.toActivity(context, Bundle().also {
                it.putInt(TAG, TYPE_NICK_NAME)
                it.putString(EXTRA_NAME, name)
            }, EditNameActivity::class.java )
        }
        fun startEditFullName(context: Context, name: String?) {
            ActivityUtil.toActivity(context, Bundle().also {
                it.putInt(TAG, TYPE_FULL_NAME)
                it.putString(EXTRA_NAME, name)
            }, EditNameActivity::class.java )
        }
    }

    override fun getViewBinding(): ActivityEditNameBinding =
        ActivityEditNameBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        initView()
    }

    private fun initView() {
        binding.apply {

            val type = intent.getIntExtra(TAG, TYPE_NICK_NAME)
            when (type) {
                TYPE_NICK_NAME -> {
                    actionBar.setTitle("${getString(R.string.Profile_Edit)} ${getString(R.string.nick_name)}")
                    tvNotice.text = "${getString(R.string.Profile_Enter)} ${getString(R.string.nick_name)}"
                    etInput.hint = getString(R.string.Improve_Enter_Nickname)
                }
                else -> {
                    actionBar.setTitle("${getString(R.string.Profile_Edit)} ${getString(R.string.Profile_Name)}")
                    tvNotice.text = "${getString(R.string.Profile_Enter)} ${getString(R.string.Profile_Name)}"
                    etInput.hint = getString(R.string.Improve_Enter_name)
                }
            }
            etInput.setText(intent.getStringExtra(EXTRA_NAME))


            actionBar.getLeftIcon().setDebounceClickListener {
                finish()
            }

            tvSave.setDebounceClickListener {

                val value = etInput.text.toString()

                lifecycleScope.launch {
                    Dialogs.showWait()
                    pVm.modifyProfileInfo(
                        name = if (type == TYPE_NICK_NAME) value else null,
                        fullName = if (type == TYPE_NICK_NAME) null else value,
                    ).collectLatest {
                        Dialogs.dismissWait()
                        getString(if (it.first == 0) R.string.save_complete else R.string.State_Fail).toast()
                        if (it.first == 0) finish()
                    }
                }
            }
        }
    }
}