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
import com.microtech.aidexx.utils.ThemeManager
import com.microtech.aidexx.utils.UnitManager
import com.microtech.aidexx.widget.dialog.Dialogs

class SettingActivity : BaseActivity<BaseViewModel, ActivitySettingBinding>() {
    private val units = listOf(UnitManager.GlucoseUnit.MMOL_PER_L.text, UnitManager.GlucoseUnit.MG_PER_DL.text)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        initView()
    }

    private fun initView() {
        val default = TransmitterManager.instance().getDefault()
        binding.clSettingHeader.background =
            ContextCompat.getDrawable(
                this, if (ThemeManager.theme.index == 0)
                    R.drawable.bg_setting_header_light else R.drawable.bg_setting_header_dark
            )
        binding.ivSettingBack.setOnClickListener { finish() }
        binding.settingTrans.setValue(default?.entity?.deviceSn ?: "")
        binding.settingTrans.setOnClickListener {
            startActivity(Intent(this@SettingActivity, TransmitterActivity::class.java))
        }
        binding.settingAlert.setOnClickListener {
            startActivity(Intent(this@SettingActivity, AlertSettingsActivity::class.java))
        }
        binding.settingUnit.setValue(UnitManager.glucoseUnit.text)
        binding.settingUnit.setOnClickListener {
            Dialogs.Picker(this).singlePick(units, UnitManager.glucoseUnit.index) {
                binding.settingUnit.setValue(units[it])
                UnitManager.glucoseUnit = UnitManager.getUnitByIndex(it)
            }
        }
        val themes = listOf(getString(R.string.theme_light), getString(R.string.theme_dark))
        binding.settingTheme.setValue(themes[ThemeManager.theme.index])
        binding.settingTheme.setOnClickListener {
            Dialogs.Picker(this).singlePick(themes, ThemeManager.theme.index) {
                binding.settingTheme.setValue(themes[it])
                ThemeManager.theme = ThemeManager.themeByIndex(it)
                for (activity in AidexxApp.instance.activityStack) {
                    activity.recreate()
                }
            }
        }
        binding.settingAbout.setOnClickListener {
            startActivity(Intent(this@SettingActivity, AboutActivity::class.java))
        }
    }

    override fun getViewBinding(): ActivitySettingBinding {
        return ActivitySettingBinding.inflate(layoutInflater)
    }
}