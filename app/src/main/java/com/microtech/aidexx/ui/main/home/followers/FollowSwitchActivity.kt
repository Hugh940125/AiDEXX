package com.microtech.aidexx.ui.main.home.followers

import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.View
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import com.jeremyliao.liveeventbus.LiveEventBus
import com.microtech.aidexx.R
import com.microtech.aidexx.base.BaseActivity
import com.microtech.aidexx.base.BaseViewModel
import com.microtech.aidexx.ble.MessageDistributor
import com.microtech.aidexx.ble.MessageObserver
import com.microtech.aidexx.ble.device.TransmitterManager
import com.microtech.aidexx.ble.device.model.DeviceModel
import com.microtech.aidexx.common.setDebounceClickListener
import com.microtech.aidexx.common.toast
import com.microtech.aidexx.common.user.UserInfoManager
import com.microtech.aidexx.data.CloudHistorySync
import com.microtech.aidexx.databinding.ActivityFollowListBinding
import com.microtech.aidexx.ui.main.home.HomeBackGroundSelector
import com.microtech.aidexx.ui.setting.share.ShareFollowActivity
import com.microtech.aidexx.ui.setting.share.ShareFollowViewModel
import com.microtech.aidexx.ui.setting.share.ShareUserInfo
import com.microtech.aidexx.utils.ActivityUtil
import com.microtech.aidexx.utils.NetUtil
import com.microtech.aidexx.utils.TimeUtils
import com.microtech.aidexx.utils.UnitManager
import com.microtech.aidexx.utils.eventbus.EventBusKey
import com.microtech.aidexx.utils.eventbus.EventBusManager
import com.microtech.aidexx.utils.mmkv.MmkvManager
import com.microtech.aidexx.utils.toGlucoseStringWithLowAndHigh
import com.microtech.aidexx.views.dialog.Dialogs
import com.microtechmd.blecomm.constant.History
import com.microtechmd.blecomm.entity.BleMessage
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Date
import java.util.Timer
import kotlin.concurrent.fixedRateTimer


class FollowSwitchActivity : BaseActivity<BaseViewModel, ActivityFollowListBinding>() {

    companion object {
        const val EXTRA_LIST_DATA = "EXTRA_LIST_DATA"
    }

    private val shareVm by viewModels<ShareFollowViewModel>()
    private lateinit var followListAdapter: FollowListAdapter
    private var fixedRateToGetFollowListJob: Job? = null

    override fun getViewBinding(): ActivityFollowListBinding =
        ActivityFollowListBinding.inflate(layoutInflater)

    private val handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            updateMySelfInfo()
        }
    }
    private val mObserver = object : MessageObserver {
        override fun onMessage(message: BleMessage) {
            handler.sendEmptyMessage(0)
        }
    }
    private lateinit var timer: Timer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val dataList = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            intent.getParcelableArrayListExtra(EXTRA_LIST_DATA, ShareUserInfo::class.java)
        }else {
            intent.getParcelableArrayListExtra(EXTRA_LIST_DATA)
        }
        dataList?.ifEmpty { null } ?: finish()

        initData(dataList!!.toList())
        timer = fixedRateTimer(startAt = Date(), period = TimeUtils.oneMinuteMillis) {
            lifecycleScope.launch {
                updateMySelfInfo()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        MessageDistributor.instance().observer(mObserver)
        fixedRateToGetFollowListJob?.cancel()
        fixedRateToGetFollowListJob = lifecycleScope.launch {
            shareVm.fixedRateToGetFollowList().collectLatest {
                it?.let {
                    followListAdapter.refreshData(it)
                }
            }
        }

        binding.apply {
            if (!MmkvManager.isAlreadyShowFollowersGuide()) { //默认是true 先隐藏掉
                binding.clShadow.visibility = View.VISIBLE
                clShadow.setOnClickListener {
                    clShadow.visibility = View.GONE
                }
                MmkvManager.setAlreadyShowFollowersGuide()
            } else {
                clShadow.visibility = View.GONE
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        timer.cancel()
        MessageDistributor.instance().removeObserver(mObserver)
    }

    private fun initData(dataList: List<ShareUserInfo>) {

        followListAdapter = FollowListAdapter(this)
        followListAdapter.onSelectChange = { _: Int, shareUserInfo: ShareUserInfo ->

            if (shareUserInfo.dataProviderId != null && UserInfoManager.shareUserInfo?.dataProviderId != shareUserInfo.dataProviderId) {

                if (NetUtil.isNetAvailable(this)) {
                    // 该用户的数据下载成功后再执行切换
                    lifecycleScope.launch {
                        Dialogs.showWait(getString(R.string.download_data))
                        if (CloudHistorySync.downloadRecentData(shareUserInfo.dataProviderId!!)) {
                            Dialogs.dismissWait()
                            UserInfoManager.shareUserInfo = shareUserInfo
                            LiveEventBus
                                .get(EventBusKey.EVENT_SWITCH_USER, ShareUserInfo::class.java)
                                .post(shareUserInfo)

                            finish()
                        } else {
                            Dialogs.dismissWait()
                            getString(R.string.switch_user_fail).toast()
                        }
                    }
                } else {
                    getString(R.string.net_error).toast()
                }
            }
        }

        binding.apply {
            actionBar.getLeftIcon().setOnClickListener {
                finish()
            }
            updateMySelfInfo()
            myCountInfo.root.setDebounceClickListener {
                if (UserInfoManager.shareUserInfo != null) {

                    UserInfoManager.shareUserInfo = null
                    val shareUserInfo = ShareUserInfo()
                    shareUserInfo.dataProviderId = UserInfoManager.instance().userId()

                    EventBusManager.send(EventBusKey.EVENT_SWITCH_USER, shareUserInfo)

                    followListAdapter.unselectAll()

                    finish()
                }
            }
            ibToShareFollowPage.setDebounceClickListener {
                ActivityUtil.toActivity(this@FollowSwitchActivity, ShareFollowActivity::class.java)
            }
            rvFollowList.layoutManager = LinearLayoutManager(this@FollowSwitchActivity)
            rvFollowList.addItemDecoration(object: ItemDecoration() {
                override fun getItemOffsets(
                    outRect: Rect,
                    view: View,
                    parent: RecyclerView,
                    state: RecyclerView.State
                ) {
                    super.getItemOffsets(outRect, view, parent, state)
                    outRect.bottom = 20
                }
            })
            rvFollowList.adapter = followListAdapter
            followListAdapter.refreshData(dataList)
        }

    }

    private fun updateMySelfInfo() {
        binding.myCountInfo.apply {

            userName.text = UserInfoManager.instance().getDisplayName()
            ivSelected.isVisible = UserInfoManager.shareUserInfo == null

            val deviceModel = TransmitterManager.instance().getDefault()

            val availableModel = if (deviceModel?.latestHistory != null && deviceModel.latestHistory!!.timeOffset < 60) {
                    null
                } else {
                    deviceModel
                }

            val errorStateDes = if (availableModel?.minutesAgo != null && availableModel.glucose != null
                && availableModel.minutesAgo!! in 0..15
            ) {
                if (availableModel.malFunctionList.isNotEmpty()) {
                    when (availableModel.malFunctionList[0]) {
                        History.GENERAL_DEVICE_FAULT -> resources.getString(R.string.Sensor_error)
                        History.DEVICE_BATTERY_LOW -> resources.getString(R.string.battery_low)
                        else -> null
                    }
                } else {
                    availableModel.latestHistory?.let {
                        if (it.isValid == 1) {
                            when (it.status) {
                                History.STATUS_INVALID -> resources.getString(R.string.transmitter_stableing)
                                History.STATUS_ERROR -> resources.getString(R.string.Sensor_error)
                                else -> null
                            }
                        } else null
                    }
                }
            } else null

            latestValueTime.text = errorStateDes ?: availableModel?.let {
                if (it.minutesAgo == null) {
                    getString(R.string.data_place_holder)
                } else {
                    if (it.minutesAgo == 0) {
                         resources.getString(R.string.now)
                    } else {
                        buildString {
                            append(it.minutesAgo)
                            append(resources.getString(R.string.min_ago))
                        }
                    }
                }
            } ?: getString(R.string.data_place_holder)

            tvGlucoseValue.text = errorStateDes?.let { getString(R.string.data_place_holder) }
                ?: availableModel?.glucose?.toGlucoseStringWithLowAndHigh(resources)
                ?: getString(R.string.data_place_holder)

            tvUnit.isVisible = tvGlucoseValue.text != getString(R.string.data_place_holder)
            if (tvUnit.isVisible) {
                tvUnit.text = UnitManager.glucoseUnit.text
            }

            errorStateDes?.let {
                bgPanel.rotation = 0f
                bgPanel.setBackgroundResource(
                    HomeBackGroundSelector.instance().getBgForTrend(null, null)
                )
            } ?: availableModel?.let {
                bgPanel.rotation = when (it.glucoseTrend) {
                    DeviceModel.GlucoseTrend.FAST_UP, DeviceModel.GlucoseTrend.UP -> 180f
                    DeviceModel.GlucoseTrend.SLOW_UP -> -90f
                    else -> 0f
                }
                bgPanel.setBackgroundResource(
                    HomeBackGroundSelector.instance()
                        .getBgForTrend(it.glucoseTrend, it.glucoseLevel)
                )
            }

            val remainingTime = availableModel?.getSensorRemainingTime()
            leftTime.text = remainingTime?.let {
                if (it == -1) {
                    resources.getString(R.string.sensor_expired)
                } else if (it <= availableModel!!.entity.expirationTime * TimeUtils.oneDayHour) {
                    val days = BigDecimal(it).divide(
                        BigDecimal(TimeUtils.oneDayHour),
                        RoundingMode.CEILING
                    ).toInt()
                    String.format(resources.getString(R.string.expiring_in_days), days)
                } else {
                    String.format(getString(R.string.left_day), getString(R.string.data_place_holder))
                }
            } ?: String.format(getString(R.string.left_day), getString(R.string.data_place_holder))

        }
    }

}