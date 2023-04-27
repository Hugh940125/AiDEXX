package com.microtech.aidexx.ui.main.home.panel

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.microtech.aidexx.R
import com.microtech.aidexx.base.BaseFragment
import com.microtech.aidexx.base.BaseViewModel
import com.microtech.aidexx.ble.MessageDistributor
import com.microtech.aidexx.ble.MessageObserver
import com.microtech.aidexx.ble.device.TransmitterManager
import com.microtech.aidexx.ble.device.model.DeviceModel
import com.microtech.aidexx.common.user.UserInfoManager
import com.microtech.aidexx.databinding.FragmentGlucosePanelBinding
import com.microtech.aidexx.ui.main.home.HomeBackGroundSelector
import com.microtech.aidexx.ui.main.home.chart.ChartViewModel
import com.microtech.aidexx.utils.TimeUtils
import com.microtech.aidexx.utils.UnitManager
import com.microtech.aidexx.utils.toGlucoseStringWithLowAndHigh
import com.microtechmd.blecomm.constant.History
import com.microtechmd.blecomm.entity.BleMessage
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*
import kotlin.concurrent.schedule

private const val REFRESH_PANEL = 2006

class GlucosePanelFragment : BaseFragment<BaseViewModel, FragmentGlucosePanelBinding>() {
    private val chartViewModel: ChartViewModel by viewModels(ownerProducer = { requireActivity() })
    private var timer: Timer? = null
    private val handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            update()
        }
    }
    private val mObserver = object : MessageObserver {
        override fun onMessage(message: BleMessage) {
            handler.sendEmptyMessage(REFRESH_PANEL)
        }
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
            handler.removeMessages(REFRESH_PANEL)
            handler.sendEmptyMessage(REFRESH_PANEL)
        }
        MessageDistributor.instance().observer(mObserver)
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
        MessageDistributor.instance().removeObserver(mObserver)
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
                binding.tvUnit.text = UnitManager.glucoseUnit.text
            }
            //设置当前血糖值
            chartViewModel.setCurrentGlucose(deviceModel.lastHistoryTime, deviceModel.glucose)

            binding.bgPanel.rotation = when (deviceModel.glucoseTrend) {
                DeviceModel.GlucoseTrend.SUPER_FAST_UP, DeviceModel.GlucoseTrend.FAST_UP -> 180f
                DeviceModel.GlucoseTrend.UP -> -90f
                else -> 0f
            }
            binding.bgPanel.setBackgroundResource(
                HomeBackGroundSelector.instance()
                    .getBgForTrend(deviceModel.glucoseTrend, deviceModel.glucoseLevel)
            )
            if (UserInfoManager.shareUserInfo == null) {
                HomeBackGroundSelector.instance().getHomeBg(deviceModel.glucoseLevel)
            }
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
                val days = BigDecimal(remainingTime).divide(
                    BigDecimal(TimeUtils.oneDayHour),
                    RoundingMode.HALF_UP
                ).toInt()
                binding.tvSensorRemainTime.text =
                    String.format(resources.getString(R.string.expiring_in_days), days)
                binding.tvSensorRemainTime.visibility = View.VISIBLE
            } else {
                binding.tvSensorRemainTime.visibility = View.GONE
            }
        }
    }
}