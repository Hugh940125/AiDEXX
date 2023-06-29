package com.microtech.aidexx.ui.main.home.panel

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.microtech.aidexx.R
import com.microtech.aidexx.base.BaseFragment
import com.microtech.aidexx.base.BaseViewModel
import com.microtech.aidexx.ble.MessageDistributor
import com.microtech.aidexx.ble.MessageObserver
import com.microtech.aidexx.ble.device.TransmitterManager
import com.microtech.aidexx.ble.device.model.DeviceModel
import com.microtech.aidexx.common.millisToSeconds
import com.microtech.aidexx.databinding.FragmentNewOrUsedSensorBinding
import com.microtech.aidexx.db.entity.TYPE_X
import com.microtech.aidexx.ui.main.home.HomeStateManager
import com.microtech.aidexx.ui.main.home.glucosePanel
import com.microtech.aidexx.utils.TimeUtils
import com.microtech.aidexx.views.dialog.Dialogs
import com.microtechmd.blecomm.constant.AidexXOperation
import com.microtechmd.blecomm.entity.AidexXDatetimeEntity
import com.microtechmd.blecomm.entity.BleMessage
import com.microtechmd.blecomm.entity.NewSensorEntity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar

class NewOrUsedSensorFragment : BaseFragment<BaseViewModel, FragmentNewOrUsedSensorBinding>() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNewOrUsedSensorBinding.inflate(layoutInflater)
        val model = TransmitterManager.instance().getDefault()
        initView(model)
        initEvent()
        processAuto(model)
        return binding.root
    }

    private fun processAuto(model: DeviceModel?) {
        if (model?.deviceType() == TYPE_X) {
            autoNewSensor(model)
        }
    }

    private fun initEvent() {
        MessageDistributor.instance().observer(object : MessageObserver {
            override fun onMessage(message: BleMessage) {
                val operation = message.operation
                val success = message.isSuccess
                when (operation) {
                    AidexXOperation.DISCONNECT -> {
                        Dialogs.dismissWait()
                    }

                    AidexXOperation.CONNECT -> {
                        if (!success) {
                            Dialogs.dismissWait()
                        }
                    }

                    AidexXOperation.SET_NEW_SENSOR -> {
                        TransmitterManager.instance().getDefault()?.reset()
                    }

                    else -> {

                    }
                }
            }
        })
    }

    private fun initView(model: DeviceModel?) {
        model?.let {
            if (model.deviceType() == TYPE_X) {
                binding.buttonOldSensor.visibility = View.GONE
            }
            binding.buttonNewSensor.setOnClickListener {
                Dialogs.showWhether(
                    requireContext(),
                    content = getString(R.string.content_new_sensor),
                    confirm = {
                        Dialogs.showWait(getString(R.string.sending))
                        model.getController().newSensor(
                            NewSensorEntity(AidexXDatetimeEntity(Calendar.getInstance()))
                        )
                        HomeStateManager.instance().setState(glucosePanel)
                    })
            }
            binding.buttonOldSensor.setOnClickListener {
                Dialogs.showWhether(
                    requireContext(),
                    content = getString(R.string.content_old_sensor),
                    confirm = {
                        model.getController().newSensor(
                            NewSensorEntity(false, TimeUtils.currentTimeMillis.millisToSeconds())
                        )
                    })
            }
        }
    }

    private fun autoNewSensor(model: DeviceModel) {
        lifecycleScope.launch {
            binding.llAutoNew.visibility = View.VISIBLE
            binding.llNewOrUsed.visibility = View.GONE
            model.getController().newSensor(NewSensorEntity(AidexXDatetimeEntity(Calendar.getInstance())))
            delay(2500)
            HomeStateManager.instance().setState(glucosePanel)
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = NewOrUsedSensorFragment()
    }
}