package com.microtech.aidexx.ui.setting.alert

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import com.google.gson.Gson
import com.jeremyliao.liveeventbus.LiveEventBus
import com.microtech.aidexx.R
import com.microtech.aidexx.base.BaseActivity
import com.microtech.aidexx.base.BaseViewModel
import com.microtech.aidexx.ble.device.TransmitterManager
import com.microtech.aidexx.databinding.ActivitySettingsAlertBinding
import com.microtech.aidexx.databinding.LayoutRulerDialogBinding
import com.microtech.aidexx.utils.ThresholdManager
import com.microtech.aidexx.utils.mmkv.MmkvManager
import com.microtech.aidexx.widget.SettingItemWidget
import com.microtech.aidexx.widget.dialog.Dialogs
import com.microtech.aidexx.widget.dialog.lib.bottom.BottomDialog
import com.microtech.aidexx.widget.dialog.lib.interfaces.OnBindView
import com.microtech.aidexx.widget.dialog.lib.util.toGlucoseStringWithUnit
import com.microtech.aidexx.widget.ruler.RulerWidget
import com.tencent.mmkv.MMKV

/**
 * APP-SRC-A-2-7-1
 * 提醒设置
 */
private const val TYPE_SET_METHOD = 1
private const val TYPE_SET_FREQUENCY = 2

class AlertSettingsActivity : BaseActivity<BaseViewModel, ActivitySettingsAlertBinding>() {
    private lateinit var listOfMethod: List<String>
    private lateinit var listOfFrequency: List<String>

    override fun getViewBinding(): ActivitySettingsAlertBinding {
        return ActivitySettingsAlertBinding.inflate(layoutInflater)
    }

    var recordID: String? = null
    var isAlert = false

    private fun methodPreview(index: Int, isUrgent: Boolean) {
        when (index) {
            0 -> AlertUtil.playSound(if (isUrgent) URGENT_NOTICE else COMMON_NOTICE)
            1 -> AlertUtil.vibrate(this, isUrgent)
            2 -> {
                AlertUtil.apply {
                    this.playSound(if (isUrgent) URGENT_NOTICE else COMMON_NOTICE)
                    this.vibrate(this@AlertSettingsActivity, isUrgent)
                }
            }
        }
    }

    private fun setMethodOrFrequency(
        settingItem: SettingItemWidget,
        list: List<String>,
        selectPos: Int,
        type: Int,
        isUrgent: Boolean
    ) {
        settingItem.setOnClickListener {
            val method: Int
            val frequency: Int
            when (type) {
                TYPE_SET_METHOD -> {
                    method = if (isUrgent) {
                        MmkvManager.getAlertMethod()
                    } else {
                        MmkvManager.getUrgentAlertMethod()
                    }
                }
                TYPE_SET_FREQUENCY -> {
                    frequency = if (isUrgent) {
                        MmkvManager.getUrgentAlertFrequency()
                    } else {
                        MmkvManager.getAlertFrequency()
                    }
                }
            }
            Dialogs.Picker(this@AlertSettingsActivity).singlePick(list, selectPos) {
                when (type) {
                    TYPE_SET_METHOD -> {
                        settingItem.setValue(list[it])
                        if (isUrgent) {
                            MmkvManager.saveUrgentAlertMethod(it)
                        } else {
                            MmkvManager.saveAlertMethod(it)
                        }
                        methodPreview(it, isUrgent)
                    }
                    TYPE_SET_FREQUENCY -> {
                        settingItem.setValue(getString(R.string.notice_inner, list[it]))
                        if (isUrgent) {
                            MmkvManager.saveUrgentAlertFrequency(it)
                        } else {
                            MmkvManager.saveAlertFrequency(it)
                        }
                    }
                }
            }
        }
    }

    fun initData() {
        listOfMethod = listOf(
            getString(R.string.sound),
            getString(R.string.shake),
            getString(R.string.soudAndShake)
        )
        val listOfFrequency = listOf(
            getString(R.string.five), getString(R.string.fifteen),
            getString(R.string.thirty), getString(R.string.halfAndQuarter),
            getString(R.string.oneHour)
        )
    }

    fun initView() {
        binding.actionBar.getLeftIcon().setOnClickListener {
            finish()
        }
        val alertMethod = MmkvManager.getAlertMethod()
        binding.noticeMethod.setValue(listOfMethod[alertMethod])
        setMethodOrFrequency(binding.noticeMethod, listOfMethod, alertMethod, TYPE_SET_METHOD, false)
        //
        val alertFrequency = MmkvManager.getAlertFrequency()
        binding.noticeFrequency.setValue(
            getString(R.string.notice_inner, listOfFrequency[alertFrequency])
        )
        setMethodOrFrequency(binding.noticeFrequency, listOfFrequency, TYPE_SET_FREQUENCY, alertFrequency, false)
        //
        val urgentAlertMethod = MmkvManager.getUrgentAlertMethod()
        binding.noticeMethodUrgent.setValue(listOfMethod[urgentAlertMethod])
        setMethodOrFrequency(binding.noticeMethodUrgent, listOfMethod, urgentAlertMethod, TYPE_SET_FREQUENCY, true)
        //
        val urgentAlertFrequency = MmkvManager.getUrgentAlertFrequency()
        binding.noticeFrequencyUrgent.setValue(
            getString(R.string.notice_inner, listOfFrequency[urgentAlertFrequency])
        )
        setMethodOrFrequency(
            binding.noticeFrequencyUrgent, listOfFrequency,
            urgentAlertFrequency, TYPE_SET_FREQUENCY, true
        )
        //
        val hypoAlertEnable = MmkvManager.isHypoAlertEnable()
        binding.hypoAlertSwitch.getSwitch().isChecked = hypoAlertEnable
        binding.hypoAlertSwitch.getSwitch().setOnCheckedChangeListener { _, isChecked ->
            MmkvManager.setHypoAlertEnable(isChecked)
        }
        //
        val highNoticeEnable = MmkvManager.isHyperAlertEnable()
        binding.hyperAlertSwitch.getSwitch().isChecked = highNoticeEnable
        binding.hyperAlertSwitch.getSwitch().setOnCheckedChangeListener { _, isChecked ->
            MmkvManager.setHyperAlertEnable(isChecked)
        }
    }

    private fun initEvent() {
        binding.hypoThreshold.setOnClickListener {
            Dialogs.showBottom(object : OnBindView<BottomDialog?>(R.layout.layout_ruler_dialog) {
                override fun onBind(dialog: BottomDialog?, v: View?) {
                    v?.let {
                        val bind = LayoutRulerDialogBinding.bind(it)
                        bind.rwNumber.setType(RulerWidget.RulerType.HYPO, ThresholdManager.hypo)
                        bind.btOk.setOnClickListener {
                            val currentValue = bind.rwNumber.getCurrentValue()
                            ThresholdManager.hypo = currentValue
                            binding.hypoThreshold.setValue(ThresholdManager.hypo.toGlucoseStringWithUnit())
                            dialog?.dismiss()
                        }
                        bind.btCancel.setOnClickListener {
                            dialog?.dismiss()
                        }
                    }
                }
            })
        }
        binding.hyperThreshold.setOnClickListener {
            Dialogs.showBottom(object : OnBindView<BottomDialog?>(R.layout.layout_ruler_dialog) {
                override fun onBind(dialog: BottomDialog?, v: View?) {
                    v?.let {
                        val bind = LayoutRulerDialogBinding.bind(it)
                        bind.rwNumber.setType(RulerWidget.RulerType.HYPER, ThresholdManager.hyper)
                        bind.btOk.setOnClickListener {
                            val currentValue = bind.rwNumber.getCurrentValue()
                            ThresholdManager.hyper = currentValue
                            binding.hyperThreshold.setValue(ThresholdManager.hyper.toGlucoseStringWithUnit())
                            dialog?.dismiss()
                        }
                        bind.btCancel.setOnClickListener {
                            dialog?.dismiss()
                        }
                    }
                }
            })
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        initData()
        initView()
        initEvent()

//        vb.siwRaise.getSwitch().setOnCheckedChangeListener { _, isChecked ->
//            MMKV.defaultMMKV().encode(LocalPreference.RAISE_ALERT, isChecked)
//        }
//
//        vb.siwFall.getSwitch().setOnCheckedChangeListener { _, isChecked ->
//            MMKV.defaultMMKV().encode(LocalPreference.FALL_ALERT, isChecked)
//        }
//
//        vb.siwSignal.getSwitch().setOnCheckedChangeListener { _, isChecked ->
//            MMKV.defaultMMKV().encode(LocalPreference.LOSS_ALERT, isChecked)
//        }
//        vb.noticeMethodSignal.setValue(
//            listOfMethod[MMKV.defaultMMKV().decodeInt(LocalPreference.SIGNAL_NOTICE_METHOD, 2)]
//        )
//        vb.noticeMethodSignal.setOnClickListener {
//            OptionsDialog.show(this, listOfMethod, null) { index ->
//                vb.noticeMethodSignal.setValue(listOfMethod[index])
//                methodPreview(index, false)
//                MMKV.defaultMMKV().encode(LocalPreference.SIGNAL_NOTICE_METHOD, index)
//            }
//        }
//        val subList = listOfFrequency.subList(1, listOfFrequency.size)
//        vb.noticeFrequencySignal.setValue(
//            getString(
//                R.string.notice_inner, subList[MMKV.defaultMMKV().decodeInt(
//                    LocalPreference.SIGNAL_NOTICE_FREQUENCY,
//                    0
//                )]
//            )
//        )
//        vb.noticeFrequencySignal.setOnClickListener {
//            OptionsDialog.show(this, subList, null) { index ->
//                vb.noticeFrequencySignal.setValue(getString(R.string.notice_inner, subList[index]))
//                MMKV.defaultMMKV().encode(LocalPreference.SIGNAL_NOTICE_FREQUENCY, index)
//            }
//        }
//        vm.getUserBgSetting()
//        vm.mUserBg.observe(this, Observer { list ->
//            list.records.let {
//                if (list.records.isNotEmpty()) {
//                    recordID = it[0].id
//                }
//            }
//        })
//
//        //报警消息弹框
//        LiveEventBus
//            .get(
//                EventKey.ALERT_TIP, AlertEvent::
//                class.java
//            )
//            .observe(this, androidx.lifecycle.Observer
//            {
//                runOnUiThread {
//
//                    if (!isAlert) {
//                        isAlert = true
//                        com.microtechmd.cgms.dialog.MessageDialog.show(
//                            this,
//                            getString(R.string.message_alert_error),
//                            null
//                        )
//                    }
//                }
//            })
    }

    override fun onResume() {
        super.onResume()
//        vb.lowUrgentValue.setValue(CgmModel.URGENT_HYPO.toGlucoseStringWithUnit(resources))
//        LogUtils.debug("model high :" + ThresholdManager.hyperThreshold)
//        LogUtils.debug("model low :" + ThresholdManager.hypoThreshold)
//
//        vb.siwLow.setValue(
//            (ThresholdManager.hypoThreshold).toGlucoseStringWithUnit(
//                resources
//            )
//        )
//        vb.siwHigh.setValue(
//            (ThresholdManager.hyperThreshold).toGlucoseStringWithUnit(
//                resources
//            )
//        )
//
//        vb.siwUrgent.getSwitch().isChecked =
//            MMKV.defaultMMKV().decodeBool(LocalPreference.URGENT_NOTICE_ENABLE, true)
//        vb.siwUrgent.getSwitch().setOnCheckedChangeListener { _, isChecked ->
//            MMKV.defaultMMKV().encode(LocalPreference.URGENT_NOTICE_ENABLE, isChecked)
//            if (!isChecked) {
//                ConfirmDialog.showCloseUrgent(
//                    this, {
//                        MMKV.defaultMMKV().encode(LocalPreference.URGENT_NOTICE_ENABLE, isChecked)
//                    }, {
//                        MMKV.defaultMMKV().encode(LocalPreference.URGENT_NOTICE_ENABLE, !isChecked)
//                        vb.siwUrgent.getSwitch().isChecked = !isChecked
//                    }
//                )
//            }
//        }
//
//        vb.siwRaise.getSwitch().isChecked =
//            MMKV.defaultMMKV().decodeBool(LocalPreference.RAISE_ALERT, true)
//        vb.siwFall.getSwitch().isChecked =
//            MMKV.defaultMMKV().decodeBool(LocalPreference.FALL_ALERT, true)
//        vb.siwSignal.getSwitch().isChecked =
//            MMKV.defaultMMKV().decodeBool(LocalPreference.LOSS_ALERT, true)
    }
}
