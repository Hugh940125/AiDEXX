package com.microtech.aidexx.ui.setting

import android.content.Intent
import android.os.Bundle
import com.microtech.aidexx.base.BaseActivity
import com.microtech.aidexx.base.BaseViewModel
import com.microtech.aidexx.databinding.ActivitySettingBinding
import com.microtech.aidexx.ui.pair.TransmitterActivity
import com.microtech.aidexx.ui.setting.alert.AlertSettingsActivity
import com.microtech.aidexx.ui.setting.share.ShareFollowActivity

class SettingActivity : BaseActivity<BaseViewModel, ActivitySettingBinding>() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        initView()
    }

    private fun initView() {
        binding.apply {
            ivSettingBack.setOnClickListener { finish() }
            settingTrans.setOnClickListener {
                startActivity(Intent(this@SettingActivity, TransmitterActivity::class.java))
            }
            settingAbout.setOnClickListener {
                startActivity(Intent(this@SettingActivity, AboutActivity::class.java))
            }
            settingAlert.setOnClickListener {
                startActivity(Intent(this@SettingActivity, AlertSettingsActivity::class.java))
            }
            tvShare.setOnClickListener {
                startActivity(Intent(this@SettingActivity, ShareFollowActivity::class.java))
            }
        }

    }

    override fun getViewBinding(): ActivitySettingBinding {
        return ActivitySettingBinding.inflate(layoutInflater)
    }
}