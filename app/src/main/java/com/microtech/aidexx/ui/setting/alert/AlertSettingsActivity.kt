package com.microtech.aidexx.ui.setting.alert

import android.os.Bundle
import com.microtech.aidexx.R
import com.microtech.aidexx.base.BaseActivity
import com.microtech.aidexx.base.BaseViewModel
import com.microtech.aidexx.databinding.ActivitySettingsAlertBinding
import com.microtech.aidexx.utils.ThresholdManager
import com.microtech.aidexx.utils.UnitManager
import com.microtech.aidexx.utils.mmkv.MmkvManager
import com.microtech.aidexx.widget.SettingItemWidget
import com.microtech.aidexx.widget.dialog.Dialogs
import com.microtech.aidexx.widget.dialog.bottom.ThresholdSelectView
import com.microtech.aidexx.widget.dialog.lib.util.toGlucoseStringWithUnit
import com.microtech.aidexx.widget.ruler.RulerWidget

/**
 * APP-SRC-A-2-7-1
 * 提醒设置
 */
private const val TYPE_SET_METHOD = 1
private const val TYPE_SET_FREQUENCY = 2

class AlertSettingsActivity : BaseActivity<BaseViewModel, ActivitySettingsAlertBinding>() {
    private var alertMethod: Int = 2
    private var alertFrequency: Int = 2
    private var urgentAlertMethod: Int = 2
    private var urgentAlertFrequency: Int = 0

    private lateinit var listOfMethod: List<String>
    private lateinit var listOfFrequency: List<String>

    override fun getViewBinding(): ActivitySettingsAlertBinding {
        return ActivitySettingsAlertBinding.inflate(layoutInflater)
    }

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
        Dialogs.Picker(this@AlertSettingsActivity).singlePick(list, selectPos) {
            when (type) {
                TYPE_SET_METHOD -> {
                    settingItem.setValue(list[it])
                    if (isUrgent) {
                        MmkvManager.saveUrgentAlertMethod(it)
                        urgentAlertMethod = it
                    } else {
                        MmkvManager.saveAlertMethod(it)
                        alertMethod = it
                    }
                    methodPreview(it, isUrgent)
                }
                TYPE_SET_FREQUENCY -> {
                    settingItem.setValue(getString(R.string.notice_inner, list[it]))
                    if (isUrgent) {
                        MmkvManager.saveUrgentAlertFrequency(it)
                        urgentAlertFrequency = it
                    } else {
                        MmkvManager.saveAlertFrequency(it)
                        alertFrequency = it
                    }
                }
            }
        }
    }

    private fun initData() {
        listOfMethod = listOf(
            getString(R.string.sound),
            getString(R.string.shake),
            getString(R.string.soudAndShake)
        )
        listOfFrequency = listOf(
            getString(R.string.five), getString(R.string.fifteen),
            getString(R.string.thirty), getString(R.string.halfAndQuarter),
            getString(R.string.oneHour)
        )
    }

    fun initView() {
        binding.actionBar.getLeftIcon().setOnClickListener {
            finish()
        }
        alertMethod = MmkvManager.getAlertMethod()
        binding.noticeMethod.setValue(listOfMethod[alertMethod])
        binding.noticeMethod.setOnClickListener {
            setMethodOrFrequency(
                binding.noticeMethod,
                listOfMethod,
                alertMethod,
                TYPE_SET_METHOD,
                false
            )
        }
        //
        alertFrequency = MmkvManager.getAlertFrequency()
        binding.noticeFrequency.setValue(
            getString(R.string.notice_inner, listOfFrequency[alertFrequency])
        )
        binding.noticeFrequency.setOnClickListener {
            setMethodOrFrequency(
                binding.noticeFrequency,
                listOfFrequency,
                alertFrequency,
                TYPE_SET_FREQUENCY,
                false
            )
        }
        //
        urgentAlertMethod = MmkvManager.getUrgentAlertMethod()
        binding.noticeMethodUrgent.setValue(listOfMethod[urgentAlertMethod])
        binding.noticeMethodUrgent.setOnClickListener {
            setMethodOrFrequency(
                binding.noticeMethodUrgent,
                listOfMethod,
                urgentAlertMethod,
                TYPE_SET_METHOD,
                true
            )
        }
        //
        urgentAlertFrequency = MmkvManager.getUrgentAlertFrequency()
        binding.noticeFrequencyUrgent.setValue(
            getString(R.string.notice_inner, listOfFrequency[urgentAlertFrequency])
        )
        binding.noticeFrequencyUrgent.setOnClickListener {
            setMethodOrFrequency(
                binding.noticeFrequencyUrgent, listOfFrequency,
                urgentAlertFrequency, TYPE_SET_FREQUENCY, true
            )
        }
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
        //
        binding.hyperThreshold.setValue(ThresholdManager.hyper.toGlucoseStringWithUnit())
        binding.hypoThreshold.setValue(ThresholdManager.hypo.toGlucoseStringWithUnit())
        //
        binding.switchRaiseAlert.getSwitch().isChecked = MmkvManager.isFastUpAlertEnable()
        binding.switchRaiseAlert.getSwitch().setOnCheckedChangeListener { _, isChecked ->
            MmkvManager.setFastUpAlertEnable(isChecked)
        }
        binding.switchFallAlert.getSwitch().isChecked = MmkvManager.isFastDownAlertEnable()
        binding.switchFallAlert.getSwitch().setOnCheckedChangeListener { _, isChecked ->
            MmkvManager.setFastDownAlertEnable(isChecked)
        }
        //
        binding.switchUrgentAlert.getSwitch().isChecked = MmkvManager.isUrgentAlertEnable()
        binding.switchUrgentAlert.getSwitch().setOnCheckedChangeListener { _, isChecked ->
            if (!isChecked) {
                Dialogs.showWhether(this@AlertSettingsActivity, content = getString(
                    R.string.content_close_urgent,
                    if (UnitManager.glucoseUnit.index == 1) "3.0mmol/L" else "54mg/dL"
                ), confirm = {
                    MmkvManager.setUrgentAlertEnable(false)
                }, cancel = {
                    binding.switchUrgentAlert.getSwitch().isChecked = true
                    MmkvManager.setUrgentAlertEnable(true)
                })
            }else{
                MmkvManager.setUrgentAlertEnable(true)
            }
        }
        binding.lowUrgentValue.setValue(ThresholdManager.URGENT_HYPO.toGlucoseStringWithUnit())
        //
        binding.switchSignalLoss.getSwitch().isChecked = MmkvManager.isUrgentAlertEnable()
        binding.switchSignalLoss.getSwitch().setOnCheckedChangeListener { _, isChecked ->
            MmkvManager.setUrgentAlertEnable(isChecked)
        }
        //
        binding.noticeMethodSignalLoss.setValue(
            listOfMethod[MmkvManager.signalLossAlertMethod()]
        )
        binding.noticeMethodSignalLoss.setOnClickListener {
            val signalLossAlertMethod = MmkvManager.signalLossAlertMethod()
            Dialogs.Picker(this@AlertSettingsActivity).singlePick(listOf(), signalLossAlertMethod) {
                binding.noticeMethodSignalLoss.setValue(listOfMethod[it])
                methodPreview(it, false)
                MmkvManager.setSignalLossMethod(it)
            }
        }
        //
        val subList = listOfFrequency.subList(1, listOfFrequency.size)
        binding.noticeFrequencySignal.setValue(
            getString(
                R.string.notice_inner, subList[MmkvManager.signalLossAlertFrequency()]
            )
        )
        binding.noticeFrequencySignal.setOnClickListener {
            val signalLossAlertFrequency = MmkvManager.signalLossAlertFrequency()
            Dialogs.Picker(this@AlertSettingsActivity).singlePick(subList,signalLossAlertFrequency){
                binding.noticeFrequencySignal.setValue(getString(R.string.notice_inner, subList[it]))
                MmkvManager.setSignalLossAlertFrequency(it)
            }
        }
    }

    private fun initEvent() {
        binding.hypoThreshold.setOnClickListener {
            val methodSelectView = ThresholdSelectView(this, RulerWidget.RulerType.HYPO) {
                binding.hypoThreshold.setValue(it)
            }
            methodSelectView.show()
        }
        binding.hyperThreshold.setOnClickListener {
            val methodSelectView = ThresholdSelectView(this, RulerWidget.RulerType.HYPER) {
                binding.hyperThreshold.setValue(it)
            }
            methodSelectView.show()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        initData()
        initView()
        initEvent()
    }
}
