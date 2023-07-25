package com.microtech.aidexx.ui.setting.alert

import android.os.Bundle
import android.widget.CompoundButton
import com.microtech.aidexx.R
import com.microtech.aidexx.base.BaseActivity
import com.microtech.aidexx.base.BaseViewModel
import com.microtech.aidexx.databinding.ActivitySettingsAlertBinding
import com.microtech.aidexx.ui.setting.SettingsManager
import com.microtech.aidexx.utils.ThresholdManager
import com.microtech.aidexx.utils.UnitManager
import com.microtech.aidexx.utils.eventbus.EventBusKey
import com.microtech.aidexx.utils.eventbus.EventBusManager
import com.microtech.aidexx.utils.toGlucoseStringWithUnit
import com.microtech.aidexx.views.SettingItemWidget
import com.microtech.aidexx.views.dialog.Dialogs
import com.microtech.aidexx.views.dialog.bottom.ThresholdSelectView
import com.microtech.aidexx.views.ruler.RulerWidget

/**
 * APP-SRC-A-2-7-1
 * 提醒设置
 */
private const val TYPE_SET_METHOD = 1
private const val TYPE_SET_FREQUENCY = 2

class AlertSettingsActivity : BaseActivity<BaseViewModel, ActivitySettingsAlertBinding>() {
    private var needInit: Boolean = true
    private var alertMethod: Int = 3
    private var alertFrequency: Int = 30
    private var urgentAlertMethod: Int = 3
    private var urgentAlertFrequency: Int = 5

    private lateinit var listOfMethod: List<String>
    private lateinit var listOfFrequencyString: List<String>
    private lateinit var listOfFrequency: Array<Int>
    private var isHypChanged = false

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
                        AlertUtil.setUrgentAlertMethod(it + 1)
                        urgentAlertMethod = it + 1
                    } else {
                        AlertUtil.setAlertMethod(it + 1)
                        alertMethod = it + 1
                    }
                    AlertUtil.alert(this, it, isUrgent)
                }

                TYPE_SET_FREQUENCY -> {
                    settingItem.setValue(getString(R.string.notice_inner, list[it]))
                    if (isUrgent) {
                        AlertUtil.setUrgentFrequency(listOfFrequency[it])
                        urgentAlertFrequency = listOfFrequency[it]
                    } else {
                        AlertUtil.setAlertFrequency(listOfFrequency[it])
                        alertFrequency = listOfFrequency[it]
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
        listOfFrequencyString = listOf(
            getString(R.string.five), getString(R.string.fifteen),
            getString(R.string.thirty), getString(R.string.halfAndQuarter),
            getString(R.string.oneHour)
        )
        listOfFrequency = arrayOf(5, 15, 30, 45, 60)
    }

    fun initView() {
        binding.actionBar.getLeftIcon().setOnClickListener {
            finish()
        }
        val alertSettings = SettingsManager.settingEntity!!
        alertMethod = alertSettings.alertType - 1
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
        alertFrequency = alertSettings.alertRate
        binding.noticeFrequency.setValue(
            getString(
                R.string.notice_inner,
                listOfFrequencyString[if (listOfFrequency.indexOf(alertFrequency) == -1) 0
                else listOfFrequency.indexOf(alertFrequency)]
            )
        )
        binding.noticeFrequency.setOnClickListener {
            setMethodOrFrequency(
                binding.noticeFrequency,
                listOfFrequencyString,
                if (listOfFrequency.indexOf(alertFrequency) == -1) 0
                else listOfFrequency.indexOf(alertFrequency),
                TYPE_SET_FREQUENCY,
                false
            )
        }
        //
        urgentAlertMethod = alertSettings.urgentAlertType - 1
        binding.noticeMethodUrgent.setValue(
            listOfMethod[if (urgentAlertMethod > listOfMethod.size - 1)
                listOfMethod.size - 1 else urgentAlertMethod]
        )
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
        urgentAlertFrequency = alertSettings.urgentAlertRate
        binding.noticeFrequencyUrgent.setValue(
            getString(
                R.string.notice_inner,
                listOfFrequencyString[if (listOfFrequency.indexOf(urgentAlertFrequency) == -1) 0
                else listOfFrequency.indexOf(urgentAlertFrequency)]
            )
        )
        binding.noticeFrequencyUrgent.setOnClickListener {
            setMethodOrFrequency(
                binding.noticeFrequencyUrgent, listOfFrequencyString,
                if (listOfFrequency.indexOf(urgentAlertFrequency) == -1) 0
                else listOfFrequency.indexOf(urgentAlertFrequency), TYPE_SET_FREQUENCY, true
            )
        }
        //
        val hypoAlertEnable = alertSettings.lowAlertSwitch
        binding.hypoAlertSwitch.getSwitch().isChecked = hypoAlertEnable == 0
        binding.hypoAlertSwitch.getSwitch().setOnCheckedChangeListener { _, isChecked ->
            AlertUtil.setHypoEnable(isChecked)
        }
        //
        val highNoticeEnable = alertSettings.highAlertSwitch
        binding.hyperAlertSwitch.getSwitch().isChecked = highNoticeEnable == 0
        binding.hyperAlertSwitch.getSwitch().setOnCheckedChangeListener { _, isChecked ->
            AlertUtil.setHyperEnable(isChecked)
        }
        //
        binding.hyperThreshold.setValue(ThresholdManager.hyper.toGlucoseStringWithUnit())
        binding.hypoThreshold.setValue(ThresholdManager.hypo.toGlucoseStringWithUnit())
        //
        binding.switchRaiseAlert.getSwitch().isChecked = alertSettings.fastUpSwitch == 0
        binding.switchRaiseAlert.getSwitch().setOnCheckedChangeListener { _, isChecked ->
            AlertUtil.setFastUpEnable(isChecked)
        }
        binding.switchFallAlert.getSwitch().isChecked = alertSettings.fastDownSwitch == 0
        binding.switchFallAlert.getSwitch().setOnCheckedChangeListener { _, isChecked ->
            AlertUtil.setFastDownEnable(isChecked)
        }
        //
        binding.switchUrgentAlert.getSwitch().isChecked = alertSettings.urgentLowAlertSwitch == 0
        binding.switchUrgentAlert.getSwitch().setOnCheckedChangeListener { _, isChecked ->
            if (!isChecked) {
                Dialogs.showWhether(this@AlertSettingsActivity, content = getString(
                    R.string.content_close_urgent,
                    if (UnitManager.glucoseUnit.index == 0) "3.0mmol/L" else "54mg/dL"
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
        binding.switchSignalLoss.getSwitch().isChecked = alertSettings.signalMissingSwitch == 0
        val function: (buttonView: CompoundButton, isChecked: Boolean) -> Unit = { _, isChecked ->
            AlertUtil.setSignalLossEnable(isChecked)
        }
        binding.switchSignalLoss.getSwitch().setOnCheckedChangeListener(function)
        //
        val signalMissingAlertType = alertSettings.signalMissingAlertType - 1
        binding.noticeMethodSignalLoss.setValue(
            listOfMethod[if (signalMissingAlertType > listOfMethod.size - 1)
                listOfMethod.size - 1 else signalMissingAlertType]
        )
        binding.noticeMethodSignalLoss.setOnClickListener {
            val signalLossAlertMethod = alertSettings.signalMissingAlertType
            Dialogs.Picker(this@AlertSettingsActivity)
                .singlePick(listOfMethod, signalLossAlertMethod) {
                    binding.noticeMethodSignalLoss.setValue(listOfMethod[it])
                    AlertUtil.alert(this@AlertSettingsActivity, it, false)
                    AlertUtil.setSignalLossMethod(it + 1)
                }
        }
        //
        val signalLossAlertFrequency = alertSettings.signalMissingAlertRate
        val subList = listOfFrequencyString.subList(1, listOfFrequencyString.size)
        binding.noticeFrequencySignal.setValue(
            getString(
                R.string.notice_inner,
                subList[if (listOfFrequency.indexOf(signalLossAlertFrequency) == -1) 0
                else listOfFrequency.indexOf(signalLossAlertFrequency) - 1]
            )
        )
        binding.noticeFrequencySignal.setOnClickListener {
            Dialogs.Picker(this@AlertSettingsActivity)
                .singlePick(subList, if (listOfFrequency.indexOf(signalLossAlertFrequency) == -1) 0
                else listOfFrequency.indexOf(signalLossAlertFrequency) - 1) {
                    binding.noticeFrequencySignal.setValue(
                        getString(
                            R.string.notice_inner,
                            subList[it]
                        )
                    )
                    AlertUtil.setSignalLossFrequency(listOfFrequency[it + 1])
                }
        }
    }

    private fun initEvent() {
        binding.hypoThreshold.setOnClickListener {
            val hypoSelectView = ThresholdSelectView(this, RulerWidget.RulerType.HYPO) {
                if (it != ThresholdManager.hypo) {
                    ThresholdManager.hypo = it
                    binding.hypoThreshold.setValue(it.toGlucoseStringWithUnit())
                    isHypChanged = true
                }
            }
            hypoSelectView.show()
        }
        binding.hyperThreshold.setOnClickListener {
            val hyperSelectView = ThresholdSelectView(this, RulerWidget.RulerType.HYPER) {
                if (it != ThresholdManager.hyper) {
                    ThresholdManager.hyper = it
                    binding.hyperThreshold.setValue(it.toGlucoseStringWithUnit())
                    isHypChanged = true
                }
            }
            hyperSelectView.show()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
    }

    override fun onResume() {
        super.onResume()
        if (needInit) {
            initData()
            initView()
            initEvent()
            needInit = false
        }
    }

    override fun onPause() {
        super.onPause()
        if (isHypChanged) {
            EventBusManager.send(EventBusKey.EVENT_HYP_CHANGE, true)
        }
    }
}
