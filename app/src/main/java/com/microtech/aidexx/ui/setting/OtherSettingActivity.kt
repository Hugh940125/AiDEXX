package com.microtech.aidexx.ui.setting

import android.os.Bundle
import com.microtech.aidexx.base.BaseActivity
import com.microtech.aidexx.base.BaseViewModel
import com.microtech.aidexx.databinding.ActivityOtherSettingBinding

class OtherSettingActivity : BaseActivity<BaseViewModel, ActivityOtherSettingBinding>() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
    }

    override fun getViewBinding(): ActivityOtherSettingBinding {
        return ActivityOtherSettingBinding.inflate(layoutInflater)
    }
}