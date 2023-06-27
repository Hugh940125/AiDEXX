package com.microtech.aidexx.ui.setting

import android.os.Bundle
import android.os.CountDownTimer
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.microtech.aidexx.base.BaseActivity
import com.microtech.aidexx.base.BaseViewModel
import com.microtech.aidexx.common.net.entity.UpgradeInfo
import com.microtech.aidexx.common.user.UserInfoManager
import com.microtech.aidexx.data.resource.LanguageResourceManager
import com.microtech.aidexx.data.resource.LocalResourceManager
import com.microtech.aidexx.databinding.ActivityLoadResourceBinding
import com.microtech.aidexx.db.entity.event.preset.BaseSysPreset
import com.microtech.aidexx.db.entity.event.preset.DietSysPresetEntity
import com.microtech.aidexx.db.entity.event.preset.InsulinSysPresetEntity
import com.microtech.aidexx.db.entity.event.preset.MedicineSysPresetEntity
import com.microtech.aidexx.db.entity.event.preset.SportSysPresetEntity
import com.microtech.aidexx.db.repository.EventDbRepository
import com.microtech.aidexx.db.repository.LanguageDbRepository
import com.microtech.aidexx.ui.account.LoginActivity
import com.microtech.aidexx.ui.main.MainActivity
import com.microtech.aidexx.utils.ActivityUtil
import com.microtech.aidexx.utils.LogUtil
import com.microtech.aidexx.utils.mmkv.MmkvManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class LoadResourceActivity : BaseActivity<BaseViewModel, ActivityLoadResourceBinding>() {

    private var isTaskFinish = false
    private var isTimerFinish = false

    companion object {
        private val TAG = LoadResourceActivity::class.java.simpleName
    }

    override fun getViewBinding(): ActivityLoadResourceBinding {
        return ActivityLoadResourceBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.apply {

            lifecycleScope.launch {
                object : CountDownTimer(2 * 1000, 50) {
                    override fun onTick(millisUntilFinished: Long) {
                        progressBar.progress = ((2000 - millisUntilFinished) / 2000f * 100).toInt()
                    }
                    override fun onFinish() {
                        isTimerFinish = true
                        over()
                    }
                }.start()
            }

            lifecycleScope.launch {

                if (UserInfoManager.instance().isLogin()) {
                    MmkvManager.getUpgradeResourceZipFileInfo().ifEmpty { null }?.let {
                        runCatching {
                            Gson().fromJson(it, UpgradeInfo.VersionInfo::class.java)
                        }.getOrNull()?.let { upInfo ->
                            LocalResourceManager.startUpgrade(upInfo.info.downloadpath, upInfo.info.version)
                        }
                    }
                } else {
                    LocalResourceManager.upgradeFromAssets()
                }

                LanguageResourceManager.loadLanguageInfo()

                isTaskFinish = true
                over()
            }
        }
    }

    private fun over() {
        if (isTimerFinish && isTaskFinish) {
            if (UserInfoManager.instance().isLogin()) {
                ActivityUtil.toActivity(this, MainActivity::class.java)
            } else {
                ActivityUtil.toActivity(this, LoginActivity::class.java)
            }
            finish()
        }
    }

    private suspend fun updateSysPreset() {
        withContext(Dispatchers.IO) {
            suspend fun doIt(clazz: Class<out BaseSysPreset>) {
                MmkvManager.getEventSysPresetNewVersion(clazz).ifEmpty { null }?.let {
                    MmkvManager.setEventSysPresetVersion(it, clazz)
                    if (it == "0") {
                        EventDbRepository.removeSysPresetData(clazz)
                    } else {
                        EventDbRepository.removeSysPresetOfOtherVersion(it, clazz)
                    }
                    LogUtil.xLogI("${clazz.simpleName} $it 升级完成", TAG)
                }
            }
            listOf(
                async { doIt(DietSysPresetEntity::class.java) },
                async { doIt(SportSysPresetEntity::class.java) },
                async { doIt(MedicineSysPresetEntity::class.java) },
                async { doIt(InsulinSysPresetEntity::class.java) },
            ).awaitAll()
        }
    }

    private suspend fun updateUnit() {
        withContext(Dispatchers.IO) {
            MmkvManager.getUnitNewVersion().ifEmpty { null }?.let {
                MmkvManager.setUnitVersion(it)
                if (it == "0") {
                    EventDbRepository.removeAllUnit()
                } else {
                    EventDbRepository.removeUnitOfOtherVersion(it)
                }
                LogUtil.xLogI("单位 $it 升级完成", TAG)
            }
        }
    }

    private suspend fun updateAndLoadLanguage() {
        withContext(Dispatchers.IO) {
            MmkvManager.getLanguageNewVersion().ifEmpty { null }?.let {
                MmkvManager.setLanguageVersion(it)
                if (it == "0") {
                    LanguageDbRepository().removeAll()
                } else {
                    LanguageDbRepository().removeLanguageOfOtherVersion(it)
                }
                LogUtil.xLogI("语言 $it 升级完成", TAG)
            }

            // todo 加载当前语言资源


        }
    }
}