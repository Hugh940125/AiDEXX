package com.microtech.aidexx.ui.main.home

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.microtech.aidexx.R
import com.microtech.aidexx.base.BaseFragment
import com.microtech.aidexx.base.BaseViewModel
import com.microtech.aidexx.ble.device.TransmitterManager
import com.microtech.aidexx.ble.device.model.DeviceModel
import com.microtech.aidexx.common.formatToYMd
import com.microtech.aidexx.common.net.entity.WelfareInfo
import com.microtech.aidexx.common.user.UserInfoManager
import com.microtech.aidexx.databinding.FragmentHomeBinding
import com.microtech.aidexx.ui.main.MainActivity
import com.microtech.aidexx.ui.main.home.chart.ChartViewHolder
import com.microtech.aidexx.ui.main.home.followers.FollowSwitchActivity
import com.microtech.aidexx.ui.main.home.panel.GlucosePanelFragment
import com.microtech.aidexx.ui.main.home.panel.NeedPairFragment
import com.microtech.aidexx.ui.main.home.panel.NewOrUsedSensorFragment
import com.microtech.aidexx.ui.main.home.panel.WarmingUpFragment
import com.microtech.aidexx.ui.setting.SettingActivity
import com.microtech.aidexx.ui.setting.getWelfareCenterUrl
import com.microtech.aidexx.ui.setting.share.ShareFollowViewModel
import com.microtech.aidexx.ui.setting.share.ShareUserInfo
import com.microtech.aidexx.ui.web.WebActivity
import com.microtech.aidexx.utils.ActivityUtil
import com.microtech.aidexx.utils.LogUtil
import com.microtech.aidexx.utils.TimeUtils
import com.microtech.aidexx.utils.UnitManager
import com.microtech.aidexx.utils.eventbus.EventBusKey
import com.microtech.aidexx.utils.eventbus.EventBusManager
import com.microtech.aidexx.utils.mmkv.MmkvManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.*

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
    private var welfareInfo: WelfareInfo? = null
    private var welfareDialog: AlertDialog? = null
    private val initOrientation: Int = 0
    private val switchOrientation: Int = 1
    private var mainActivity: MainActivity? = null
    private var lastPageTag: String? = null
    private val homeViewModel: HomeViewModel by activityViewModels()
    private val shareVm by viewModels<ShareFollowViewModel>()

    private lateinit var chartViewHolder: ChartViewHolder

    private var transChangeCallback = fun(_: DeviceModel?) { judgeState() }

    private var fixedRateToGetFollowListJob: Job? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainActivity = activity as MainActivity
    }

    fun showSn(sn: String) {
        binding.tvSn.text = sn
    }

    override fun onResume() {
        super.onResume()
        orientation(initOrientation)
        UserInfoManager.shareUserInfo?.let {
            startFixedRateToGetFollowListJob()
        }
        //拉关注人列表 控制切换用户按钮是否显示
        lifecycleScope.launch {
            binding.switchUserData.isVisible = homeViewModel.getFollowers()
            welfareInfo = homeViewModel.getActivities()
            welfareInfo?.let {
                binding.welfareCenter.isVisible = it.viewIndexTag
                val showRedDot = it.activityList.any { activity -> activity.isLook != 1 }
                binding.welfareCenter
                    .setImageDrawable(
                        ContextCompat.getDrawable(
                            requireContext(),
                            if (showRedDot) R.drawable.ic_gift_with_dot else R.drawable.ic_gift
                        )
                    )
                if (it.viewIndexBanner) {
                    showWelfareDialog(it.activityList[0].url)
                }
            }
        }
    }

    private fun showWelfareDialog(url: String) {
        val welfareDialogTime = MmkvManager.getWelfareDialogTime()
        if (welfareDialog?.isShowing == true || (TimeUtils.currentTimeMillis > welfareDialogTime &&
                    Date().formatToYMd().equals(Date(welfareDialogTime).formatToYMd()))
        ) {
            return
        }
        MmkvManager.saveWelfareDialogTime(TimeUtils.currentTimeMillis)
        val builder = AlertDialog.Builder(context)
        builder.setCancelable(false)
        val dialogView = LayoutInflater.from(context).inflate(R.layout.layout_advert_dialog, null)
        val ivClose = dialogView.findViewById<ImageView>(R.id.iv_close)
        val ivDetail = dialogView.findViewById<ImageView>(R.id.iv_detail)
        context?.let {
            Glide.with(it).load(url).placeholder(R.drawable.ic_aidex)
                .into(ivDetail)
        }
        ivDetail.setOnClickListener {
            welfareDialog?.dismiss()
            welfareDialog?.cancel()
            WebActivity.loadWeb(
                requireContext(),
                url = getWelfareCenterUrl(),
                fullScreen = true,
                from = "welfare_center"
            )
        }
        ivClose.setOnClickListener {
            welfareDialog?.dismiss()
            welfareDialog?.cancel()
        }
        builder.setView(dialogView)
        welfareDialog = builder.create()
        welfareDialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        welfareDialog?.show()
    }


    override fun onPause() {
        super.onPause()
        stopFixedRateToGetFollowListJob()
    }

    override fun onDetach() {
        super.onDetach()
        HomeStateManager.instance().cancel()
        TransmitterManager.removeOnTransmitterChangeListener(transChangeCallback)
        HomeStateManager.onHomeStateChange = null
        HomeBackGroundSelector.instance().onLevelChange = null
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        LogUtil.d("ChartViewModel homefragment onCreateView", TAG)
        binding = FragmentHomeBinding.inflate(layoutInflater)
        initView()
        initEvent()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        LogUtil.d("ChartViewModel homefragment onViewCreated", TAG)
        chartViewHolder = ChartViewHolder(binding, this) {
            if (switchOrientation == 2) {
                orientation(initOrientation)
                EventBusManager.sendDelay(EventBusKey.EVENT_GO_TO_HISTORY, it, 500)
            } else {
                EventBusManager.send(EventBusKey.EVENT_GO_TO_HISTORY, it)
            }
        }
    }

    private fun initView() {
        judgeState()
        HomeStateManager.onHomeStateChange = { tag ->
            replaceFragment(tag)
        }
        HomeBackGroundSelector.instance().onLevelChange = { bg ->
            binding.homeRoot.setBackgroundResource(bg)
        }
        TransmitterManager.setOnTransmitterChangeListener(transChangeCallback)
        binding.ivScale.setOnClickListener {
            orientation(switchOrientation)
        }
        binding.userCenter.setOnClickListener {
            startActivity(Intent(activity, SettingActivity::class.java))
        }
    }

    private fun initEvent() {
        binding.apply {
            switchUserData.setOnClickListener {
                ActivityUtil.toActivity(
                    requireContext(),
                    Bundle().also {
                        it.putParcelableArrayList(
                            FollowSwitchActivity.EXTRA_LIST_DATA,
                            ArrayList<Parcelable?>().also { list ->
                                list.addAll(homeViewModel.mFollowers)
                            })
                    },
                    FollowSwitchActivity::class.java
                )
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

        EventBusManager.onReceive<ShareUserInfo>(EventBusKey.EVENT_SWITCH_USER, this) {
            binding.apply {

                fun changeUi(isMyself: Boolean) {
                    userCenter.isVisible = isMyself
                    welfareCenter.isVisible = isMyself
                    dataOwner.isVisible = !isMyself
                    frgShare.root.isVisible = !isMyself
                    fcvPanel.isInvisible = !isMyself
                }

                if (it.dataProviderId == UserInfoManager.instance().userId()) { // 自己
                    changeUi(true)
                    tvSn.text =
                        TransmitterManager.instance().getDefault()?.entity?.deviceSn ?: ""

                } else { // 其他人
                    changeUi(false)
//                  todo 添加第一次查看分享人时的引导  GuideManager.instance().startHomeGuide(activity, this@HomeFragment, vb)
                    updateShareUserData()
                }
            }
        }

        EventBusManager.onReceive<MutableList<ShareUserInfo>>(EventBusKey.EVENT_FOLLOWERS_UPDATED, this) {
            homeViewModel.updateFollowers(it)
            binding.switchUserData.isVisible = homeViewModel.mFollowers.isNotEmpty()
        }

    }

    private fun judgeState() {
        val default = TransmitterManager.instance().getDefault()
        if (default != null) {
            binding.tvSn.text = default.entity.deviceSn
        } else {
            binding.tvSn.text = ""
        }
        if (default != null && default.isPaired()) {
            replaceFragment(glucosePanel)
        } else {
            replaceFragment(needPair)
        }
    }

    private fun replaceFragment(pageTag: String) {
        if (pageTag == lastPageTag) return
        if (lastPageTag == glucosePanel) {
            binding.homeRoot.setBackgroundResource(0)
        }
        val fragment = when (pageTag) {
            needPair -> NeedPairFragment.newInstance()
            glucosePanel -> GlucosePanelFragment.newInstance()
            warmingUp -> WarmingUpFragment.newInstance()
            newOrUsedSensor -> NewOrUsedSensorFragment.newInstance()
            else -> return
        }
        if (childFragmentManager.findFragmentByTag(pageTag) == null) {
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
        mainActivity?.fitHomeOrientation()
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


    private fun startFixedRateToGetFollowListJob() {
        fixedRateToGetFollowListJob?.cancel()
        fixedRateToGetFollowListJob = lifecycleScope.launch {
            shareVm.fixedRateToGetFollowList().collectLatest {
                it?.let {
                    it.find { shareUserInfo ->
                        shareUserInfo.userAuthorizationId == UserInfoManager.shareUserInfo?.userAuthorizationId
                    }?.let { latestShareUserInfo ->
                        UserInfoManager.shareUserInfo = latestShareUserInfo
                        updateShareUserData()
                    } ?: let {
                        LogUtil.d("当前关注的用户已经取消授权", TAG)
                    }
                }
            }
        }
    }

    private fun stopFixedRateToGetFollowListJob() {
        fixedRateToGetFollowListJob?.cancel()
    }

    private fun updateShareUserData() {
        UserInfoManager.shareUserInfo?.let {
            binding.apply {
                dataOwner.text = it.getDisplayName()
                tvSn.text = it.cgmDevice?.deviceSn ?: ""

                frgShare.apply {

                    val glucoseValue = it.getGlucoseValue()
                    tvGlucoseValueShare.text = "${glucoseValue ?: getString(R.string.data_place_holder)}"
                    tvUnitShare.isVisible = glucoseValue != null
                    if (tvUnitShare.isVisible) {
                        tvUnitShare.text = UnitManager.glucoseUnit.text
                    }

                    tvValueTimeShare.text = it.getLatestValueTimeStr()
                    tvGlucoseStateShare.isVisible = false
                    tvGlucoseStateShare.text = ""
                    tvSensorRemainTimeShare.text = it.getSensorStatusDesc()
                    it.userTrend?.let {
                        bgPanelShare.rotation = when (it.getGlucoseTrend()) {
                            DeviceModel.GlucoseTrend.FAST_UP, DeviceModel.GlucoseTrend.UP -> 180f
                            DeviceModel.GlucoseTrend.SLOW_UP -> -90f
                            else -> 0f
                        }
                    }
                    bgPanelShare.setBackgroundResource(
                        HomeBackGroundSelector.instance()
                            .getBgForTrend(it.userTrend?.getGlucoseTrend(), it.userTrend?.getGlucoseLevel())
                    )
                    HomeBackGroundSelector.instance().getHomeBg(it.userTrend?.getGlucoseLevel())
                }

            }
        }
    }


    companion object {
        @JvmStatic
        fun newInstance() = HomeFragment()

        const val TAG = "HomeFragment"
    }
}
