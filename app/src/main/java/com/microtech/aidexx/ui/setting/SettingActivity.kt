package com.microtech.aidexx.ui.setting

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.microtech.aidexx.AidexxApp
import com.microtech.aidexx.R
import com.microtech.aidexx.base.BaseActivity
import com.microtech.aidexx.base.BaseViewModel
import com.microtech.aidexx.ble.device.TransmitterManager
import com.microtech.aidexx.common.setDebounceClickListener
import com.microtech.aidexx.common.toast
import com.microtech.aidexx.data.resource.LanguageResourceManager
import com.microtech.aidexx.databinding.ActivitySettingBinding
import com.microtech.aidexx.ui.pair.TransmitterActivity
import com.microtech.aidexx.ui.setting.alert.AlertSettingsActivity
import com.microtech.aidexx.ui.setting.share.ShareFollowActivity
import com.microtech.aidexx.utils.ThemeManager
import com.microtech.aidexx.utils.UnitManager
import com.microtech.aidexx.utils.eventbus.EventBusKey
import com.microtech.aidexx.utils.eventbus.EventBusManager
import com.microtech.aidexx.widget.dialog.Dialogs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
                    AppCompatDelegate.setDefaultNightMode(
                        if (it == 1) AppCompatDelegate.MODE_NIGHT_YES
                        else AppCompatDelegate.MODE_NIGHT_NO
                    )
                    for (activity in AidexxApp.instance.activityStack) {
                        activity?.recreate()
                    }
                }
            }
            settingLanguage.setDebounceClickListener {
                lifecycleScope.launch {

                    withContext(Dispatchers.IO) {
                        LanguageResourceManager.getSupportLanguages()
                    }?.let { supportLanguages ->

                        Dialogs.Picker(this@SettingActivity).singlePick(
                                supportLanguages,
                                supportLanguages.indexOf(LanguageResourceManager.getCurLanguageTag()) ) {

                            "选中了第 $it 个，切换暂未实现".toast()
                            return@singlePick

                            lifecycleScope.launch {
                                withContext(Dispatchers.IO) {
                                    LanguageResourceManager.onLanguageChanged(supportLanguages[it])
                                }
                                settingLanguage.setValue(themes[it])
                                for (activity in AidexxApp.instance.activityStack) {
                                    activity?.recreate()
                                }
                            }
                        }
                    }

                }
            }

            settingOther.setOnClickListener {
                startActivity(Intent(this@SettingActivity, OtherSettingActivity::class.java))
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