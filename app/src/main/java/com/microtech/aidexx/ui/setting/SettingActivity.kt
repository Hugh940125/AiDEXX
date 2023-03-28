package com.microtech.aidexx.ui.setting

import android.os.Bundle
import com.microtech.aidexx.base.BaseActivity
import com.microtech.aidexx.base.BaseViewModel
import com.microtech.aidexx.databinding.ActivitySettingBinding

class SettingActivity : BaseActivity<BaseViewModel, ActivitySettingBinding>() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
    }

    override fun getViewBinding(): ActivitySettingBinding {
        return ActivitySettingBinding.inflate(layoutInflater)
    }
}