package com.microtech.aidexx.ui.main.home

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.microtech.aidexx.R
import com.microtech.aidexx.base.BaseFragment
import com.microtech.aidexx.base.BaseViewModel
import com.microtech.aidexx.ble.AidexBleAdapter
import com.microtech.aidexx.ble.device.TransmitterManager
import com.microtech.aidexx.databinding.FragmentHomeBinding
import com.microtech.aidexx.db.entity.RealCgmHistoryEntity
import com.microtech.aidexx.ui.main.MainActivity
import com.microtech.aidexx.ui.main.home.chart.ChartViewHolder
import com.microtech.aidexx.ui.main.home.panel.GlucosePanelFragment
import com.microtech.aidexx.ui.main.home.panel.NeedPairFragment
import com.microtech.aidexx.ui.main.home.panel.NewOrUsedSensorFragment
import com.microtech.aidexx.ui.main.home.panel.WarmingUpFragment
import com.microtech.aidexx.ui.setting.SettingActivity
import com.microtech.aidexx.utils.LogUtil
import com.microtech.aidexx.utils.eventbus.CgmDataChangedInfo
import com.microtech.aidexx.utils.eventbus.DataChangedType
import com.microtech.aidexx.utils.eventbus.EventBusKey
import com.microtech.aidexx.utils.eventbus.EventBusManager
import com.microtechmd.blecomm.constant.History
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.time.Duration.Companion.seconds

/**
 *@date 2023/2/15
 *@author Hugh
 *@desc
 */
const val needPair = "needPair"
const val glucosePanel = "glucosePanel"
const val newOrUsedSensor = "newOrUsedSensor"
const val warmingUp = "warmingUp"

class HomeFragment : BaseFragment<BaseViewModel, FragmentHomeBinding>() {

    private val initOrientation: Int = 0
    private val switchOrientation: Int = 1
    private var mainActivity: MainActivity? = null
    private var lastPageTag: String? = null

    private lateinit var chartViewHolder: ChartViewHolder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainActivity = activity as MainActivity
        judgeState()
        HomeStateManager.onHomeStateChange = {
            replaceFragment(it)
        }
        HomeBackGroundSelector.instance().onLevelChange = { bg->
            binding.homeRoot.setBackgroundResource(bg)
        }
        TransmitterManager.setOnTransmitterChangeListener {
            judgeState()
        }
    }

    override fun onResume() {
        super.onResume()
        orientation(initOrientation)
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        HomeStateManager.instance().cancel()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentHomeBinding.inflate(layoutInflater)
        initView()

        chartViewHolder = ChartViewHolder(binding, this) {
            if (switchOrientation == 2) {
                orientation(initOrientation)
                EventBusManager.sendDelay(EventBusKey.EVENT_GO_TO_HISTORY, true, 500)
            } else {
                EventBusManager.send(EventBusKey.EVENT_GO_TO_HISTORY, true)
            }
        }

        initEvent()
        return binding.root
    }

    private fun initView() {
        binding.ivScale.setOnClickListener {
            orientation(switchOrientation)
        }
        binding.userCenter.setOnClickListener {
            startActivity(Intent(activity, SettingActivity::class.java))
        }
    }

    private fun initEvent() {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                for (i in 0..100) {
                    delay(2.seconds)
                    val historyEntity = RealCgmHistoryEntity()
                    historyEntity.deviceTime =
                        Date(Date().time - (1000 * 60 * 60 * 6) + (i * 1000 * 60 * 10))
                    historyEntity.eventData = (i % 36).toFloat()
                    historyEntity.eventType = History.HISTORY_GLUCOSE
                    EventBusManager.send(
                        EventBusKey.EVENT_CGM_DATA_CHANGED,
                        CgmDataChangedInfo(DataChangedType.ADD, listOf(historyEntity))
                    )
                }
            }
        }
        EventBusManager.onReceive<Boolean>(EventBusKey.EVENT_UNPAIR_RESULT, this) {
            if (it) {
                HomeStateManager.instance().setState(needPair)
            }
        }
        EventBusManager.onReceive<Boolean>(EventBusKey.EVENT_PAIR_RESULT, this) {
            if (it) {
                HomeStateManager.instance().setState(glucosePanel)
            }
        }
    }

    private fun judgeState() {
        val default = TransmitterManager.instance().getDefault()
        if ((default != null && default.isPaired())) {
            replaceFragment(glucosePanel)
        } else {
            replaceFragment(needPair)
        }
    }

    private fun replaceFragment(pageTag: String) {
        if (pageTag == lastPageTag) return
        if (lastPageTag == glucosePanel && pageTag != glucosePanel) {
            binding.homeRoot.setBackgroundResource(0)
        }
        val fragment = when (pageTag) {
            needPair -> NeedPairFragment.newInstance()
            glucosePanel -> GlucosePanelFragment.newInstance()
            warmingUp -> WarmingUpFragment.newInstance()
            newOrUsedSensor -> NewOrUsedSensorFragment.newInstance()
            else -> return
        }
        if (childFragmentManager.findFragmentByTag(tag) == null) {
            lastPageTag = pageTag
            try {
                val transaction = childFragmentManager.beginTransaction()
                transaction.replace(R.id.fcv_panel, fragment, pageTag)
                transaction.commitAllowingStateLoss()
            } catch (e: Exception) {
                LogUtil.eAiDEX("Transaction commitAllowingStateLoss error")
            }
        }
    }

    @SuppressLint("SourceLockedOrientationActivity")
    private fun orientation(type: Int) {
        if (mainActivity?.mCurrentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            if (switchOrientation == type) {
                mainActivity?.mCurrentOrientation = Configuration.ORIENTATION_PORTRAIT
                activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                portrait()
            } else {
                landSpace()
            }
        } else if (mainActivity?.mCurrentOrientation == Configuration.ORIENTATION_PORTRAIT) {
            if (switchOrientation == type) {
                mainActivity?.mCurrentOrientation = Configuration.ORIENTATION_LANDSCAPE
                activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                landSpace()
            } else {
                portrait()
            }
        }
        mainActivity?.fitOrientation()
    }

    private fun landSpace() {
        binding.bottomSpace.visibility = View.GONE
        binding.layoutState.visibility = View.GONE
        binding.layoutActionbar.visibility = View.GONE
        binding.ivScale.setImageDrawable(context?.let {
            ContextCompat.getDrawable(it, R.drawable.ic_scale_to_small)
        })
        binding.serviceView.hide()
    }

    private fun portrait() {
        binding.layoutState.visibility = View.VISIBLE
        binding.bottomSpace.visibility = View.VISIBLE
        binding.layoutActionbar.visibility = View.VISIBLE
        binding.ivScale.setImageDrawable(context?.let {
            ContextCompat.getDrawable(it, R.drawable.ic_scale)
        })
        binding.serviceView.show()
    }

    companion object {
        @JvmStatic
        fun newInstance() = HomeFragment()

        const val TAG = "HomeFragment"
    }
}
