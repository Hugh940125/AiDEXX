package com.microtech.aidexx.ui.pair

import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.microtech.aidexx.BuildConfig
import com.microtech.aidexx.R
import com.microtech.aidexx.base.BaseActivity
import com.microtech.aidexx.base.BaseViewModel
import com.microtech.aidexx.ble.AidexBleAdapter
import com.microtech.aidexx.ble.device.TransmitterManager
import com.microtech.aidexx.databinding.ActivityTransOperationBinding
import com.microtech.aidexx.utils.ActivityUtil
import com.microtech.aidexx.utils.ToastUtil
import com.microtech.aidexx.utils.eventbus.EventBusKey
import com.microtech.aidexx.utils.eventbus.EventBusManager
import com.microtech.aidexx.views.dialog.Dialogs
import com.microtechmd.blecomm.controller.BleControllerInfo
import kotlinx.coroutines.delay
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
                EventBusKey.EVENT_PAIR_RESULT,
                EventBusKey.EVENT_UNPAIR_RESULT
            ),
            this
        ) { key, data ->
            if (data) {
                if (key == EventBusKey.EVENT_PAIR_RESULT) {
                    lifecycleScope.launch {
                        Dialogs.showSuccess(getString(R.string.Pairing_Succeed))
                        if (BuildConfig.keepAlive){
                            AidexBleAdapter.getInstance().executeDisconnect()
                        }
                        delay(2500)
                        ActivityUtil.finishToMain()
                    }
                } else {
                    EventBusManager.send(EventBusKey.UPDATE_NOTIFICATION, false)
                    finish()
                }
            } else {
                if (key == EventBusKey.EVENT_UNPAIR_RESULT) {
                    ToastUtil.showShort(getString(R.string.pair_fail))
                } else {
                    ToastUtil.showShort(getString(R.string.unpair_fail))
                }
            }
        }
    }

    private fun initView() {
        binding.actionbarTransOperation.getLeftIcon().setOnClickListener { finish() }
        val bleControllerInfo = intent.getParcelableExtra<BleControllerInfo>(BLE_INFO)
        binding.actionbarTransOperation.setTitle(bleControllerInfo?.sn)
        when (intent.getIntExtra(OPERATION_TYPE, 0)) {
            1 -> binding.llUnpair.visibility = View.GONE
            2 -> binding.llPair.visibility = View.GONE
        }
        binding.tvPair.setOnClickListener {
            bleControllerInfo?.let {
                PairUtil.startPair(this@TransOperationActivity, bleControllerInfo)
            }
        }
        binding.tvUnpair.setOnClickListener {
            PairUtil.startUnpair(this@TransOperationActivity, false)
        }
        binding.tvForceDelete.setOnClickListener {
            lifecycleScope.launch {
                PairUtil.startUnpair(this@TransOperationActivity, true)
                TransmitterManager.instance().getDefault()?.deletePair()
            }
        }
    }

    override fun getViewBinding(): ActivityTransOperationBinding {
        return ActivityTransOperationBinding.inflate(layoutInflater)
    }
}