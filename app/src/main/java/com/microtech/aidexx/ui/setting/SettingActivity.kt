package com.microtech.aidexx.ui.setting

import android.content.Intent
import android.os.Bundle
import androidx.core.content.ContextCompat
import com.microtech.aidexx.AidexxApp
import com.microtech.aidexx.R
import com.microtech.aidexx.base.BaseActivity
import com.microtech.aidexx.base.BaseViewModel
import com.microtech.aidexx.ble.device.TransmitterManager
import com.microtech.aidexx.databinding.ActivitySettingBinding
import com.microtech.aidexx.ui.pair.TransmitterActivity
import com.microtech.aidexx.ui.setting.alert.AlertSettingsActivity
import com.microtech.aidexx.ui.setting.share.ShareFollowActivity
import com.microtech.aidexx.utils.ThemeManager
import com.microtech.aidexx.utils.UnitManager
import com.microtech.aidexx.utils.eventbus.EventBusKey
import com.microtech.aidexx.utils.eventbus.EventBusManager
import com.microtech.aidexx.widget.dialog.Dialogs

class SettingActivity : BaseActivity<BaseViewModel, ActivitySettingBinding>() {
    private val units = listOf(UnitManager.GlucoseUnit.MMOL_PER_L.text, UnitManager.GlucoseUnit.MG_PER_DL.text)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        initView()
    }

    override fun onResume() {
        super.onResume()
        val default = TransmitterManager.instance().getDefault()
        binding.settingTrans.setValue(default?.entity?.deviceSn ?: "")
    }

    private fun initView() {
        binding.apply {
            ivSettingBack.setOnClickListener { finish() }
            settingTrans.setOnClickListener {
                startActivity(Intent(this@SettingActivity, TransmitterActivity::class.java))
            }
            settingAbout.setOnClickListener {
                startActivity(Intent(this@SettingActivity, AboutActivity::class.java))
            }
            settingAlert.setOnClickListener {
                startActivity(Intent(this@SettingActivity, AlertSettingsActivity::class.java))
            }
            tvShare.setOnClickListener {
                startActivity(Intent(this@SettingActivity, ShareFollowActivity::class.java))
            }
            clSettingHeader.background =
                ContextCompat.getDrawable(
                    this@SettingActivity, if (ThemeManager.theme.index == 0)
                        R.drawable.bg_setting_header_light else R.drawable.bg_setting_header_dark
                )
            ivSettingBack.setOnClickListener { finish() }
            settingTrans.setOnClickListener {
                startActivity(Intent(this@SettingActivity, TransmitterActivity::class.java))
            }
            settingAlert.setOnClickListener {
                startActivity(Intent(this@SettingActivity, AlertSettingsActivity::class.java))
            }
            settingUnit.setValue(UnitManager.glucoseUnit.text)
            settingUnit.setOnClickListener {
                Dialogs.Picker(this@SettingActivity).singlePick(units, UnitManager.glucoseUnit.index) {
                    settingUnit.setValue(units[it])
                    UnitManager.glucoseUnit = UnitManager.getUnitByIndex(it)
                    EventBusManager.send(EventBusKey.EVENT_HYP_CHANGE, true)
                }
            }
            val themes = listOf(getString(R.string.theme_light), getString(R.string.theme_dark))
            settingTheme.setValue(themes[ThemeManager.theme.index])
            settingTheme.setOnClickListener {
                Dialogs.Picker(this@SettingActivity).singlePick(themes, ThemeManager.theme.index) {
                    settingTheme.setValue(themes[it])
                    ThemeManager.theme = ThemeManager.themeByIndex(it)
                    for (activity in AidexxApp.instance.activityStack) {
                        activity?.recreate()
                    }
                }
            }
            settingAbout.setOnClickListener {
                startActivity(Intent(this@SettingActivity, AboutActivity::class.java))
            }
        }
    }

    override fun getViewBinding(): ActivitySettingBinding {
        return ActivitySettingBinding.inflate(layoutInflater)
    }
}