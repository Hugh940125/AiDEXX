package com.microtech.aidexx.ui.setting

import android.graphics.Color
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.microtech.aidexx.BuildConfig
import com.microtech.aidexx.IntentKey
import com.microtech.aidexx.R
import com.microtech.aidexx.base.BaseActivity
import com.microtech.aidexx.base.BaseViewModel
import com.microtech.aidexx.data.AppUpgradeManager
import com.microtech.aidexx.databinding.ActivityAboutBinding
import com.microtech.aidexx.ui.upgrade.AppUpdateFragment
import com.microtech.aidexx.ui.web.WebActivity
import kotlinx.coroutines.launch


class AboutActivity : BaseActivity<BaseViewModel, ActivityAboutBinding>() {

    override fun getViewBinding(): ActivityAboutBinding {
        return ActivityAboutBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.run {
            actionBar.getLeftIcon().setOnClickListener {
                finish()
            }

            launcher.setOnClickListener{
            }

            settingSoftVersion.setValue("V${BuildConfig.VERSION_NAME}")
            settingName.setValue(getString(R.string.software_name))
            settingOpenVersion.setValue("V1")
            settingType.setValue("AiDEX-X")
            settingService.setOnClickListener {

                val bundle = Bundle()
                bundle.putString(
                    IntentKey.INTENT_URL,
                    resources.getString(R.string.Terms_of_Service_url)
                )
                bundle.putString(
                    IntentKey.INTENT_TITLE,
                    resources.getString(R.string.Terms_of_Service)
                )
                startActivity(bundle, WebActivity::class.java)
            }

            settingCheckVersion.setOnClickListener {
                lifecycleScope.launch {
                    AppUpgradeManager.fetchVersionInfo(true)?.let {
                        AppUpdateFragment(it).show(supportFragmentManager, AppUpdateFragment.TAG)
                    }
                }

            }

            settingProtocal.setOnClickListener {

                val bundle = Bundle()
                bundle.putString(
                    IntentKey.INTENT_URL,
                    resources.getString(R.string.Privacy_Policy_url)
                )
                bundle.putString(IntentKey.INTENT_TITLE, resources.getString(R.string.Privacy_Policy))
                startActivity(bundle, WebActivity::class.java)
            }
        }

        lifecycleScope.launch {
            AppUpgradeManager.fetchVersionInfo(true)?.let {
                binding.settingCheckVersion.setValue(null)
                val textVerion = binding.settingCheckVersion.getSecondTextView()
                textVerion.text = getString(R.string.txt_new)
                textVerion.setTextColor(Color.WHITE)
                textVerion.textSize = 13f
                textVerion.setBackgroundResource(R.drawable.shape_version_new); textVerion.setPadding(
                18,
                2,
                18,
                2
            )
            } ?:let {
                binding.settingCheckVersion.setValue(getString(R.string.verson_last))
            }
        }
    }
}