package com.microtech.aidexx.ui.setting

import android.content.Intent
import android.os.Bundle
import com.microtech.aidexx.base.BaseActivity
import com.microtech.aidexx.base.BaseViewModel
import com.microtech.aidexx.databinding.ActivitySettingBinding
import com.microtech.aidexx.ui.pair.TransmitterActivity

class SettingActivity : BaseActivity<BaseViewModel, ActivitySettingBinding>() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        initView()
    }

    private fun initView() {
        binding.ivSettingBack.setOnClickListener { finish() }
        binding.settingTrans.setOnClickListener {
            startActivity(Intent(this@SettingActivity, TransmitterActivity::class.java))
        }
    }

    override fun getViewBinding(): ActivitySettingBinding {
        return ActivitySettingBinding.inflate(layoutInflater)
    }
}