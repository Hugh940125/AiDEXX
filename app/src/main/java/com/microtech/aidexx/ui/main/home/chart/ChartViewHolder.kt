package com.microtech.aidexx.ui.main.home.chart

import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.microtech.aidexx.databinding.FragmentHomeBinding
import com.microtech.aidexx.db.entity.ShareUserEntity
import com.microtech.aidexx.ui.main.home.HomeFragment
import com.microtech.aidexx.utils.LogUtils
import com.microtech.aidexx.utils.eventbus.BgDataChangedInfo
import com.microtech.aidexx.utils.eventbus.CgmDataChangedInfo
import com.microtech.aidexx.utils.eventbus.EventBusKey
import com.microtech.aidexx.utils.eventbus.EventBusManager
import com.microtech.aidexx.widget.chart.GlucoseChart
import com.microtech.aidexx.widget.chart.MyAnimatedZoomJob
import com.microtech.aidexx.widget.chart.MyChart
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

class ChartViewHolder(
    private val vb: FragmentHomeBinding,
    private val fragment: HomeFragment,
    private val toHistory: ()->Unit
    ) {

    private val chartViewModel: ChartViewModel by fragment.viewModels(ownerProducer = { fragment.requireActivity() })

    init {

        vb.run {

            touchView.setOnTouchListener { _, event ->
                touchView.performClick()
                if (MyAnimatedZoomJob.animators > 0) true
                else chart.onTouchEvent(event)
            }

            homeTimeTab.onTabChange = {
                val newModel = when(it) {
                    1 -> MyChart.G_HALF_DAY
                    2 -> MyChart.G_ONE_DAY
                    else -> MyChart.G_SIX_HOURS
                }
                chartViewModel.updateGranularity(newModel)
            }

            chart.extraParams = object: GlucoseChart.ExtraParams {
                override var outerDescriptionView: View? = descriptions

                override var llValue: LinearLayout? = llDescValue
                override var outerDescriptionY: TextView? = descriptionTvValue
                override var outerDescriptionUnit: TextView? = descriptionTvUnit
                override var outerDescriptionX: TextView? = descriptionTvTime

                override var rlDescription: RelativeLayout? = vb.rlDescription
                override var outerDescriptionU: TextView? = descriptionTvContent
                override var goToHistory: ImageView? = vb.goToHistory
                override var onGoToHistory: (() -> Unit)? = { toHistory.invoke() }

                override var curDateTv: TextView? = tvXTime

                override fun xMax(): Float = chartViewModel.xMax()
                override fun xMin(): Float = chartViewModel.xMin()
                override fun xRange(): Float = chartViewModel.xRange()
                override fun lowerLimit(): Float = chartViewModel.lowerLimit
                override fun upperLimit(): Float = chartViewModel.upperLimit
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
                        val ret = chartViewModel.startLoadNextPage.compareAndSet(expect = false, true)
                        LogUtils.debug(TAG,"===CHART===onXAxisVisibleAreaChanged do=$ret")
                    }
                }

                override fun onToEndLeft() {
                    val ret = chartViewModel.startLoadNextPage.compareAndSet(expect = false, true)
                    LogUtils.debug(TAG,"===CHART===onToEndLeft do=$ret")
                }

                override fun onToEndRight() {
                    LogUtils.debug(TAG,"===CHART===onToEndRight")
                }
            }

            fragment.lifecycleScope.launch {
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
                    chartViewModel.mDataChangedFlow.debounce{
                        if (it?.needScrollToLatest != false) 0.seconds else 1.seconds
                    }.collect {
                        it?.let {
                            chart.notifyChanged(it.needScrollToLatest)
                        }
                    }
                }
            }
        }

        EventBusManager.onReceive<CgmDataChangedInfo>(EventBusKey.EVENT_CGM_DATA_CHANGED,fragment) {
            fragment.lifecycleScope.launch {
                chartViewModel.onCgmDataChanged(it)
            }
        }
        EventBusManager.onReceive<BgDataChangedInfo>(EventBusKey.EVENT_BG_DATA_CHANGED,fragment) {
            fragment.lifecycleScope.launch {
                chartViewModel.onBgDataChanged(it)
            }
        }

        /** 切换用户 */
        EventBusManager.onReceive<ShareUserEntity>(EventBusKey.EVENT_SWITCH_USER, fragment) {
            chartViewModel.reload()
        }

    }

    companion object {
        const val TAG = "ChartViewHolder"
    }

}