package com.microtech.aidexx.ui.qrcode

import android.os.Bundle
import com.microtech.aidexx.base.BaseActivity
import com.microtech.aidexx.base.BaseViewModel
import com.microtech.aidexx.databinding.ActivityQrCodeScanBinding

class QrCodeScanActivity : BaseActivity<BaseViewModel, ActivityQrCodeScanBinding>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
    }

    override fun getViewBinding(): ActivityQrCodeScanBinding {
        return ActivityQrCodeScanBinding.inflate(layoutInflater)
    }
}