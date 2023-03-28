package com.microtech.aidexx.ui.pair

import android.os.Bundle
import android.view.View
import com.microtech.aidexx.base.BaseActivity
import com.microtech.aidexx.base.BaseViewModel
import com.microtech.aidexx.databinding.ActivityTransOperationBinding
import com.microtechmd.blecomm.controller.BleControllerInfo

class TransOperationActivity : BaseActivity<BaseViewModel, ActivityTransOperationBinding>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        initView()
    }

    private fun initView() {
        val parcelableExtra = intent.getParcelableExtra<BleControllerInfo>(BLE_INFO)
        binding.actionbarTransOperation.setTitle(parcelableExtra?.sn)
        when (intent.getIntExtra(OPERATION_TYPE, 0)) {
            1 -> {
                binding.llUnpair.visibility = View.GONE
            }
            2 -> {
                binding.llPair.visibility = View.GONE
            }
        }
    }

    override fun getViewBinding(): ActivityTransOperationBinding {
        return ActivityTransOperationBinding.inflate(layoutInflater)
    }
}