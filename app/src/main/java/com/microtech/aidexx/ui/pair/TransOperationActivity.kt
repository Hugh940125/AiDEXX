package com.microtech.aidexx.ui.pair

import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.microtech.aidexx.base.BaseActivity
import com.microtech.aidexx.base.BaseViewModel
import com.microtech.aidexx.ble.device.TransmitterManager
import com.microtech.aidexx.databinding.ActivityTransOperationBinding
import com.microtech.aidexx.utils.eventbus.EventBusKey
import com.microtech.aidexx.utils.eventbus.EventBusManager
import com.microtechmd.blecomm.controller.BleControllerInfo
import kotlinx.coroutines.launch

class TransOperationActivity : BaseActivity<BaseViewModel, ActivityTransOperationBinding>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        initView()
        initEvents()
    }

    private fun initEvents() {
        EventBusManager.onReceive<Boolean>(
            keys = arrayOf(
                EventBusKey.EVENT_PAIR_SUCCESS,
                EventBusKey.EVENT_UNPAIR_SUCCESS
            ),
            this
        ) {
            finish()
        }
    }

    private fun initView() {
        val bleControllerInfo = intent.getParcelableExtra<BleControllerInfo>(BLE_INFO)
        binding.actionbarTransOperation.setTitle(bleControllerInfo?.sn)
        when (intent.getIntExtra(OPERATION_TYPE, 0)) {
            1 -> binding.llUnpair.visibility = View.GONE
            2 -> binding.llPair.visibility = View.GONE
        }
        binding.tvPair.setOnClickListener {
            bleControllerInfo?.let {
                checkEnvironment {
                    PairUtil.startPair(this@TransOperationActivity, bleControllerInfo)
                }
            }
        }
        binding.tvUnpair.setOnClickListener {
            PairUtil.startUnpair(this@TransOperationActivity)
        }
        binding.tvForceDelete.setOnClickListener {
            lifecycleScope.launch {
                TransmitterManager.instance().getDefault()?.deletePair()
            }
        }
    }

    override fun getViewBinding(): ActivityTransOperationBinding {
        return ActivityTransOperationBinding.inflate(layoutInflater)
    }
}