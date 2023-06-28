package com.microtech.aidexx.ui.setting

import android.content.Intent
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.microtech.aidexx.AidexxApp
import com.microtech.aidexx.BuildConfig
import com.microtech.aidexx.R
import com.microtech.aidexx.base.BaseActivity
import com.microtech.aidexx.base.BaseViewModel
import com.microtech.aidexx.ble.device.TransmitterManager
import com.microtech.aidexx.common.setDebounceClickListener
import com.microtech.aidexx.common.toast
import com.microtech.aidexx.data.resource.LanguageResourceManager
import com.microtech.aidexx.common.user.UserInfoManager
import com.microtech.aidexx.data.LocalManager
import com.microtech.aidexx.databinding.ActivitySettingBinding
import com.microtech.aidexx.ui.pair.TransmitterActivity
import com.microtech.aidexx.ui.setting.alert.AlertSettingsActivity
import com.microtech.aidexx.ui.setting.share.ShareFollowActivity
import com.microtech.aidexx.ui.web.WebActivity
import com.microtech.aidexx.utils.ThemeManager
import com.microtech.aidexx.utils.UnitManager
import com.microtech.aidexx.utils.eventbus.EventBusKey
import com.microtech.aidexx.utils.eventbus.EventBusManager
import com.microtech.aidexx.utils.mmkv.MmkvManager
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

    private fun getWelfareCenterUrl(): String {
        val token = MmkvManager.getToken()
        val userId = UserInfoManager.instance().userId()
        return "${BuildConfig.welfareCenterUrl}?token=${token}&userId=${userId}"
    }

    override fun onResume() {
        super.onResume()
        val default = TransmitterManager.instance().getDefault()
        binding.settingTrans.setValue(default?.entity?.deviceSn ?: "")
    }

    private fun initView() {
        binding.apply {
            ivSettingBack.setDebounceClickListener { finish() }
            tvWelfare.setDebounceClickListener {
                WebActivity.loadWeb(
                    context = this@SettingActivity,
                    url = getWelfareCenterUrl(),
                    fullScreen = true,
                    from = "welfare_center"
                )
            }
            tvHelp.setDebounceClickListener {
                WebActivity.loadWeb(
                    this@SettingActivity,
                    getString(R.string.help_center), "https://aidexhelp.pancares.com/h5", true, "help_center"
                )
            }
            settingTrans.setDebounceClickListener {
                startActivity(Intent(this@SettingActivity, TransmitterActivity::class.java))
            }
            settingAbout.setDebounceClickListener {
                startActivity(Intent(this@SettingActivity, AboutActivity::class.java))
            }
            settingAlert.setDebounceClickListener {
                startActivity(Intent(this@SettingActivity, AlertSettingsActivity::class.java))
            }
            tvShare.setDebounceClickListener {
                startActivity(Intent(this@SettingActivity, ShareFollowActivity::class.java))
            }
            clSettingHeader.background =
                ContextCompat.getDrawable(
                    this@SettingActivity, if (ThemeManager.isLight())
                        R.drawable.bg_setting_header_light else R.drawable.bg_setting_header_dark
                )
            settingTrans.setDebounceClickListener {
                startActivity(Intent(this@SettingActivity, TransmitterActivity::class.java))
            }
            settingAlert.setDebounceClickListener {
                startActivity(Intent(this@SettingActivity, AlertSettingsActivity::class.java))
            }
            settingUnit.setValue(UnitManager.glucoseUnit.text)
            settingUnit.setDebounceClickListener {
                Dialogs.Picker(this@SettingActivity).singlePick(units, UnitManager.glucoseUnit.index) {
                    settingUnit.setValue(units[it])
                    UnitManager.glucoseUnit = UnitManager.getUnitByIndex(it)
                    EventBusManager.send(EventBusKey.EVENT_HYP_CHANGE, true)
                }
            }
            val themes = listOf(getString(R.string.theme_dark), getString(R.string.theme_light))
            settingTheme.setValue(themes[ThemeManager.theme.index])
            settingTheme.setDebounceClickListener {
                Dialogs.Picker(this@SettingActivity).singlePick(themes, ThemeManager.theme.index) {
                    if (it == ThemeManager.theme.index) {
                        return@singlePick
                    }
                    settingTheme.setValue(themes[it])
                    ThemeManager.theme = ThemeManager.themeByIndex(it)
                    ThemeManager.themeConfig()
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

            settingOther.setDebounceClickListener {
                startActivity(Intent(this@SettingActivity, OtherSettingActivity::class.java))
            }
            settingAbout.setDebounceClickListener {
                startActivity(Intent(this@SettingActivity, AboutActivity::class.java))
            }
        }
    }

    override fun getViewBinding(): ActivitySettingBinding {
        return ActivitySettingBinding.inflate(layoutInflater)
    }
}