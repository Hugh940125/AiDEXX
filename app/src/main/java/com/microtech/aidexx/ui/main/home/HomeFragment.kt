package com.microtech.aidexx.ui.main.home

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.jeremyliao.liveeventbus.LiveEventBus
import com.microtech.aidexx.R
import com.microtech.aidexx.base.BaseFragment
import com.microtech.aidexx.base.BaseViewModel
import com.microtech.aidexx.ble.AidexBleAdapter
import com.microtech.aidexx.ble.device.TransmitterManager
import com.microtech.aidexx.databinding.FragmentHomeBinding
import com.microtech.aidexx.ui.main.MainActivity
import com.microtech.aidexx.ui.main.home.chart.ChartViewModel
import com.microtech.aidexx.widget.chart.GlucoseChart
import com.microtech.aidexx.widget.chart.MyChart
import com.microtech.aidexx.ui.main.home.panel.GlucosePanelFragment
import com.microtech.aidexx.ui.main.home.panel.NeedPairFragment
import com.microtech.aidexx.ui.main.home.panel.NewOrUsedSensorFragment
import com.microtech.aidexx.ui.main.home.panel.WarmingUpFragment
import com.microtech.aidexx.utils.LogUtil
import kotlinx.coroutines.launch
import com.microtech.aidexx.utils.LogUtils
import com.microtech.aidexx.utils.eventbus.EventBusKey
import com.microtech.aidexx.widget.chart.MyChart.Companion.G_HALF_DAY
import com.microtech.aidexx.widget.chart.MyChart.Companion.G_ONE_DAY
import com.microtech.aidexx.widget.chart.MyChart.Companion.G_SIX_HOURS
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

    private val chartViewModel: ChartViewModel by viewModels(ownerProducer = { requireActivity() })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainActivity = activity as MainActivity
        HomeStateManager.onHomeStateChange = {
            replaceFragment(it)
        }
        TransmitterManager.onTransmitterChange = {
            judgeState()
            AidexBleAdapter.getInstance().startBtScan(true)
        }
        lifecycleScope.launch {
            TransmitterManager.instance().loadTransmitter()
        }
    }

    override fun onResume() {
        super.onResume()
        orientation(initOrientation)
        judgeState()
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
        initChart()
        initEvent()
        return binding.root
    }

    private fun initView() {
        binding.ivScale.setOnClickListener {
            orientation(switchOrientation)
        }
    }

    private fun initChart() {
        binding.run {

            chart.extraParams = object: GlucoseChart.ExtraParams {
                override var outerDescriptionView: View? = descriptions

                override var llValue: LinearLayout? = llDescValue
                override var outerDescriptionY: TextView? = descriptionTvValue
                override var outerDescriptionUnit: TextView? = descriptionTvUnit
                override var outerDescriptionX: TextView? = descriptionTvTime

                override var rlDescription: RelativeLayout? = binding.rlDescription
                override var outerDescriptionU: TextView? = descriptionTvContent
                override var goToHistory: ImageView? = binding.goToHistory
                override var onGoToHistory: (() -> Unit)? = {
                    if (switchOrientation == 2) {
                        orientation(initOrientation)
                        LiveEventBus.get<Boolean>(EventBusKey.GO_TO_HISTORY).postDelay(true, 500)
                    } else {
                        LiveEventBus.get<Boolean>(EventBusKey.GO_TO_HISTORY).post(true)
                    }
                }
            }

            chart.onScrollListener = object: MyChart.ScrollListener {
                override fun onXAxisVisibleAreaChanged(
                    isLtr: Boolean,
                    visibleLeftX: Float,
                    visibleRightX: Float,
                    xAxisMin: Float,
                    xAxisMax: Float
                ) {
                    if (chartViewModel.needLoadNextPage(isLtr, visibleLeftX, xAxisMin)) {
                        chartViewModel.startLoadNextPage.compareAndSet(expect = false, true)
                    }
                }

                override fun onToEndLeft() {
                    chartViewModel.startLoadNextPage.compareAndSet(expect = false, true)
                }

                override fun onToEndRight() {
                    LogUtils.debug("","onToEndRight")
                }
            }

            lifecycleScope.launch {
                launch {
                    chartViewModel.granularityFlow.collectLatest {
                        it?.let {
                            chart.updateGranularity(it)
                            chart.notifyChanged()
                        }
                    }
                }
                launch {
                    chartViewModel.initData().collectLatest {
                        chart.initData(it)
                    }
                }

                launch {
                    chartViewModel.mDataChangedFlow.collect {
                        it?.let {
                            chart.notifyChanged(it.second)
                        }
                    }
                }
            }
        }
    }

    private fun initEvent() {
        binding.run {
            homeTimeTab.onTabChange = {
                val newModel = when(it) {
                    1 -> G_HALF_DAY
                    2 -> G_ONE_DAY
                    else -> G_SIX_HOURS
                }
                chartViewModel.updateGranularity(newModel)
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
    }
}
