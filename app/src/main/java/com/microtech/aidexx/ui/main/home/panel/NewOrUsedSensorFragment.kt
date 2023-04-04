package com.microtech.aidexx.ui.main.home.panel

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.microtech.aidexx.R
import com.microtech.aidexx.base.BaseFragment
import com.microtech.aidexx.base.BaseViewModel
import com.microtech.aidexx.ble.MessageDispatcher
import com.microtech.aidexx.ble.device.TransmitterManager
import com.microtech.aidexx.common.millisToSeconds
import com.microtech.aidexx.databinding.FragmentNewOrUsedSensorBinding
import com.microtech.aidexx.db.entity.TYPE_X
import com.microtech.aidexx.ui.main.home.HomeStateManager
import com.microtech.aidexx.ui.main.home.glucosePanel
import com.microtech.aidexx.utils.TimeUtils
import com.microtech.aidexx.widget.dialog.Dialogs
import com.microtechmd.blecomm.constant.CgmOperation
import com.microtechmd.blecomm.entity.AidexXDatetimeEntity
import com.microtechmd.blecomm.entity.NewSensorEntity
import java.util.*

class NewOrUsedSensorFragment : BaseFragment<BaseViewModel, FragmentNewOrUsedSensorBinding>() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNewOrUsedSensorBinding.inflate(layoutInflater)
        initView()
        return binding.root
    }

    private fun initView() {
        val model = TransmitterManager.instance().getDefault()
        model?.let {
            if (model.deviceType() == TYPE_X) {
                binding.buttonOldSensor.visibility = View.GONE
            }
            binding.buttonNewSensor.setOnClickListener {
                Dialogs.showWhether(requireContext(), content = getString(R.string.content_new_sensor), confirm = {
                    Dialogs.showWait(getString(R.string.sending))
                    model.getController().newSensor(
                        NewSensorEntity(AidexXDatetimeEntity(Calendar.getInstance()))
                    )
                    HomeStateManager.instance().setState(glucosePanel)
                })
            }
            binding.buttonOldSensor.setOnClickListener {
                Dialogs.showWhether(requireContext(), content = getString(R.string.content_old_sensor), confirm = {
                    model.getController().newSensor(
                        NewSensorEntity(false, TimeUtils.currentTimeMillis.millisToSeconds())
                    )
                    HomeStateManager.instance().setState(glucosePanel)
                })
            }
        }
    }

    override fun onResume() {
        super.onResume()
        MessageDispatcher.instance().observer(lifecycleScope) {
            val operation = it.operation
            val success = it.isSuccess
            when (operation) {
                CgmOperation.CONNECT -> {
                    if (!success){
                        Dialogs.dismissWait()
                    }
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
    }

    companion object {
        @JvmStatic
        fun newInstance() = NewOrUsedSensorFragment()
    }
}