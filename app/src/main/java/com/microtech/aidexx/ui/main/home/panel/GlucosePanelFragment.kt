package com.microtech.aidexx.ui.main.home.panel

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.microtech.aidexx.R
import com.microtech.aidexx.base.BaseFragment
import com.microtech.aidexx.base.BaseViewModel
import com.microtech.aidexx.ble.MessageDispatcher
import com.microtech.aidexx.ble.device.TransmitterManager
import com.microtech.aidexx.ble.device.model.DeviceModel
import com.microtech.aidexx.common.user.UserInfoManager
import com.microtech.aidexx.databinding.FragmentGlucosePanelBinding
import com.microtech.aidexx.ui.main.home.HomeBackGroundSelector
import com.microtech.aidexx.utils.LogUtil
import com.microtech.aidexx.utils.Throttle
import com.microtech.aidexx.utils.TimeUtils
import com.microtech.aidexx.widget.dialog.x.util.toGlucoseStringWithLowAndHigh
import com.microtechmd.blecomm.constant.History
import java.math.BigDecimal
import java.util.*
import kotlin.concurrent.schedule

private const val REFRESH_PANEL = 2006
class GlucosePanelFragment : BaseFragment<BaseViewModel, FragmentGlucosePanelBinding>() {

    private var timer: Timer? = null
    private val handler = Handler(Looper.getMainLooper()) {
        update()
        false
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentGlucosePanelBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        timer = Timer()
        timer?.schedule(0, 60000) {
            handler.sendEmptyMessage(REFRESH_PANEL)
        }
        MessageDispatcher.instance().observer(lifecycleScope){
            Throttle.instance().emit(2000, REFRESH_PANEL) {
                update()
            }
        }
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        timer?.cancel()
    }

    companion object {
        @JvmStatic
        fun newInstance() = GlucosePanelFragment()
    }

    private fun update() {
        val deviceModel = TransmitterManager.instance().getDefault() ?: return
        if (deviceModel.latestHistory != null && deviceModel.latestHistory!!.timeOffset < 60) {
            return
        }
        if (isAdded && deviceModel.isDataValid() && activity != null
            && !requireActivity().isFinishing && !deviceModel.isMalfunction
        ) {
            if (deviceModel.minutesAgo == null) {
                binding.tvValueTime.text = ""
            } else {
                if (deviceModel.minutesAgo == 0) {
                    binding.tvValueTime.text = resources.getString(R.string.now)
                } else {
                    (buildString {
                        append(deviceModel.minutesAgo)
                        append(resources.getString(R.string.min_ago))
                    }).also {
                        binding.tvValueTime.text = it
                    }
                }
            }
            context?.let {
                binding.tvGlucoseValue.text =
                    deviceModel.glucose?.toGlucoseStringWithLowAndHigh(resources)
            }
            //设置当前血糖值
//            ChartManager.instance().setCurrentGlucose(model.lastHistoryDatetime, model.glucose)
            binding.bgPanel.rotation = when (deviceModel.glucoseTrend) {
                DeviceModel.GlucoseTrend.SUPER_FAST_UP, DeviceModel.GlucoseTrend.FAST_UP -> 180f
                DeviceModel.GlucoseTrend.UP -> -90f
                else -> 0f
            }
            binding.bgPanel.setBackgroundResource(
                HomeBackGroundSelector.instance().getBgForTrend(deviceModel.glucoseTrend, deviceModel.glucoseLevel)
            )
            if (UserInfoManager.shareUserInfo == null)
                HomeBackGroundSelector.instance().getHomeBg(deviceModel.glucoseLevel)
        } else {
            binding.tvValueTime.text = ""
            binding.tvGlucoseValue.text = "--"
            binding.tvGlucoseState.text = ""
            if (UserInfoManager.shareUserInfo == null)
                HomeBackGroundSelector.instance().getHomeBg(null)
        }
        binding.tvGlucoseState.visibility = View.GONE
        if (deviceModel.minutesAgo != null && deviceModel.minutesAgo!! in 0..15 && deviceModel.glucose != null) {
            binding.tvGlucoseState.visibility = View.GONE
            binding.tvGlucoseState.text = ""
            if (deviceModel.isMalfunction) {
                if (deviceModel.faultType == 1) {
                    binding.tvGlucoseState.visibility = View.VISIBLE
                    binding.tvGlucoseState.text =
                        resources.getString(R.string.Sensor_error)
                } else if (deviceModel.faultType == 2) {
                    binding.tvGlucoseState.visibility = View.VISIBLE
                    binding.tvGlucoseState.text =
                        resources.getString(R.string.insert_sensor_error)
                }
            } else {
                deviceModel.latestHistory?.let {
                    if (it.isValid == 1) {
                        if (it.status == History.STATUS_INVALID) {
                            binding.tvGlucoseState.visibility = View.VISIBLE
                            binding.tvGlucoseState.text =
                                resources.getString(R.string.transmitter_stableing)
                        } else if (it.status == History.STATUS_ERROR) {
                            binding.tvGlucoseState.visibility = View.VISIBLE
                            binding.tvGlucoseState.text =
                                resources.getString(R.string.Sensor_error)
                        }
                    }
                }
            }
        }
        if (activity != null && !requireActivity().isFinishing) {
            val remainingTime = deviceModel.getSensorRemainingTime()
            LogUtil.eAiDEX("Sensor remaining : $remainingTime hour")
            if (remainingTime == null || remainingTime < 0) {
                binding.tvSensorRemainTime.visibility = View.GONE
            } else if (remainingTime == 0) {
                binding.tvSensorRemainTime.text =
                    resources.getString(R.string.sensor_expired)
                binding.tvGlucoseState.text = ""
                binding.tvGlucoseState.visibility = View.GONE
                binding.tvSensorRemainTime.visibility = View.VISIBLE
            } else if (remainingTime < TimeUtils.oneDayHour) {
                if (remainingTime <= 1) {
                    binding.tvSensorRemainTime.text =
                        String.format(resources.getString(R.string.expiring_in_hour), 1)
                } else {
                    binding.tvSensorRemainTime.text =
                        String.format(resources.getString(R.string.expiring_in_hour), remainingTime)
                }
                binding.tvSensorRemainTime.visibility = View.VISIBLE
            } else if (remainingTime <= deviceModel.entity.expirationTime * TimeUtils.oneDayHour) {
                val days = BigDecimal(remainingTime).divide(BigDecimal(TimeUtils.oneDayHour)).toInt()
                binding.tvSensorRemainTime.text =
                    String.format(resources.getString(R.string.expiring_in_days), days)
                binding.tvSensorRemainTime.visibility = View.VISIBLE
            } else {
                binding.tvSensorRemainTime.visibility = View.GONE
            }
        }
    }
}