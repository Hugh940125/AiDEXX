package com.microtech.aidexx.ui.setting.alert

import android.os.Bundle
import com.microtech.aidexx.R
import com.microtech.aidexx.base.BaseActivity
import com.microtech.aidexx.base.BaseViewModel
import com.microtech.aidexx.databinding.ActivitySettingsAlertBinding
import com.microtech.aidexx.utils.ThresholdManager
import com.microtech.aidexx.utils.UnitManager
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
                        AlertUtil.setUrgentAlertMethod(it)
                        urgentAlertMethod = it
                    } else {
                        AlertUtil.setAlertMethod(it)
                        alertMethod = it
                    }
                    AlertUtil.alert(this, it, isUrgent)
                }
                TYPE_SET_FREQUENCY -> {
                    settingItem.setValue(getString(R.string.notice_inner, list[it]))
                    if (isUrgent) {
                        AlertUtil.setUrgentFrequency(it)
                        urgentAlertFrequency = it
                    } else {
                        AlertUtil.setAlertFrequency(it)
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
        alertMethod = AlertUtil.getAlertSettings().alertMethod
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
        alertFrequency = AlertUtil.getAlertSettings().alertFrequency
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
        urgentAlertMethod = AlertUtil.getAlertSettings().urgentAlertMethod
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
        urgentAlertFrequency = AlertUtil.getAlertSettings().urgentAlertFrequency
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
        val hypoAlertEnable = AlertUtil.getAlertSettings().isHypoEnable
        binding.hypoAlertSwitch.getSwitch().isChecked = hypoAlertEnable
        binding.hypoAlertSwitch.getSwitch().setOnCheckedChangeListener { _, isChecked ->
            AlertUtil.setHypoEnable(isChecked)
        }
        //
        val highNoticeEnable = AlertUtil.getAlertSettings().isHyperEnable
        binding.hyperAlertSwitch.getSwitch().isChecked = highNoticeEnable
        binding.hyperAlertSwitch.getSwitch().setOnCheckedChangeListener { _, isChecked ->
            AlertUtil.setHyperEnable(isChecked)
        }
        //
        binding.hyperThreshold.setValue(ThresholdManager.hyper.toGlucoseStringWithUnit())
        binding.hypoThreshold.setValue(ThresholdManager.hypo.toGlucoseStringWithUnit())
        //
        binding.switchRaiseAlert.getSwitch().isChecked = AlertUtil.getAlertSettings().isFastUpEnable
        binding.switchRaiseAlert.getSwitch().setOnCheckedChangeListener { _, isChecked ->
            AlertUtil.setFastUpEnable(isChecked)
        }
        binding.switchFallAlert.getSwitch().isChecked = AlertUtil.getAlertSettings().isFastDownEnable
        binding.switchFallAlert.getSwitch().setOnCheckedChangeListener { _, isChecked ->
            AlertUtil.setFastDownEnable(isChecked)
        }
        //
        binding.switchUrgentAlert.getSwitch().isChecked = AlertUtil.getAlertSettings().isUrgentLowEnable
        binding.switchUrgentAlert.getSwitch().setOnCheckedChangeListener { _, isChecked ->
            if (!isChecked) {
                Dialogs.showWhether(this@AlertSettingsActivity, content = getString(
                    R.string.content_close_urgent,
                    if (UnitManager.glucoseUnit.index == 1) "3.0mmol/L" else "54mg/dL"
                ), confirm = {
                    AlertUtil.setUrgentEnable(false)
                }, cancel = {
                    binding.switchUrgentAlert.getSwitch().isChecked = true
                    AlertUtil.setUrgentEnable(true)
                })
            } else {
                AlertUtil.setUrgentEnable(true)
            }
        }
        binding.lowUrgentValue.setValue(ThresholdManager.URGENT_HYPO.toGlucoseStringWithUnit())
        //
        binding.switchSignalLoss.getSwitch().isChecked = AlertUtil.getAlertSettings().isSignalLossEnable
        binding.switchSignalLoss.getSwitch().setOnCheckedChangeListener { _, isChecked ->
            AlertUtil.setSignalLossEnable(isChecked)
        }
        //
        binding.noticeMethodSignalLoss.setValue(
            listOfMethod[AlertUtil.getAlertSettings().signalLossMethod]
        )
        binding.noticeMethodSignalLoss.setOnClickListener {
            val signalLossAlertMethod = AlertUtil.getAlertSettings().signalLossMethod
            Dialogs.Picker(this@AlertSettingsActivity).singlePick(listOfMethod, signalLossAlertMethod) {
                binding.noticeMethodSignalLoss.setValue(listOfMethod[it])
                AlertUtil.alert(this, it, false)
                AlertUtil.setSignalLossMethod(it)
            }
        }
        //
        val signalLossAlertFrequency = AlertUtil.getAlertSettings().signalLossFrequency
        val subList = listOfFrequency.subList(1, listOfFrequency.size)
        binding.noticeFrequencySignal.setValue(
            getString(
                R.string.notice_inner, subList[signalLossAlertFrequency]
            )
        )
        binding.noticeFrequencySignal.setOnClickListener {
            Dialogs.Picker(this@AlertSettingsActivity)
                .singlePick(subList, signalLossAlertFrequency) {
                    binding.noticeFrequencySignal.setValue(
                        getString(
                            R.string.notice_inner,
                            subList[it]
                        )
                    )
                    AlertUtil.setSignalLossFrequency(it)
                }
        }
    }

    private fun initEvent() {
        binding.hypoThreshold.setOnClickListener {
            val hypoSelectView = ThresholdSelectView(this, RulerWidget.RulerType.HYPO) {
                binding.hypoThreshold.setValue(it)
            }
            hypoSelectView.show()
        }
        binding.hyperThreshold.setOnClickListener {
            val hyperSelectView = ThresholdSelectView(this, RulerWidget.RulerType.HYPER) {
                binding.hyperThreshold.setValue(it)
            }
            hyperSelectView.show()
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
