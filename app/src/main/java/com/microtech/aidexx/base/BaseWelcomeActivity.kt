package com.microtech.aidexx.base

import android.graphics.drawable.Animatable2.AnimationCallback
import android.graphics.drawable.AnimatedVectorDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.microtech.aidexx.common.net.entity.UpgradeInfo
import com.microtech.aidexx.common.user.UserInfoManager
import com.microtech.aidexx.data.resource.LanguageResourceManager
import com.microtech.aidexx.data.resource.LocalResourceManager
import com.microtech.aidexx.databinding.ActivityWelcomeBinding
import com.microtech.aidexx.utils.LogUtil
import com.microtech.aidexx.utils.mmkv.MmkvManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

abstract class BaseWelcomeActivity<VM : BaseViewModel>: BaseActivity<VM, ActivityWelcomeBinding>() {

    private var resourceLoaded = false
    private var isAnimationFinish = false
    private val isSupportSplashProgress = SDK_INT >= Build.VERSION_CODES.S

    abstract fun afterAgreeUserProtocal()

    override fun getViewBinding(): ActivityWelcomeBinding {
        return ActivityWelcomeBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        initSplashProgressIfSupported()
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        initSplashProgressIfNoSupported()

        loadResource()
    }

    private fun loadResource() {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                kotlin.runCatching {
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
                }
            }.exceptionOrNull()?.let {
                LogUtil.xLogE("资源加载失败：$it", "BaseWelcomeActivity")
            }

            resourceLoaded = true
            if (!isSupportSplashProgress) {
                nextStepIfNeed()
            }
        }
    }

    private fun initSplashProgressIfSupported() {
        val splashScreen = installSplashScreen()
        splashScreen.setOnExitAnimationListener {
            it.remove()
            if (isSupportSplashProgress) {
                afterResourceLoaded()
            }
        }
        splashScreen.setKeepOnScreenCondition {
            if (isSupportSplashProgress) !resourceLoaded else false
        }
    }

    private val animationCallback = object: AnimationCallback() {
        override fun onAnimationEnd(drawable: Drawable?) {
            super.onAnimationEnd(drawable)
            isAnimationFinish = true
            nextStepIfNeed()
        }
    }
    private fun initSplashProgressIfNoSupported() {
        if (!isSupportSplashProgress) {
            binding.loadResource.icon.apply {
                val animatedVectorDrawable = drawable
                if (animatedVectorDrawable is AnimatedVectorDrawable) {
                    animatedVectorDrawable.registerAnimationCallback(animationCallback)
                    animatedVectorDrawable.start()
                }
            }
        } else {
            binding.loadResource.root.isVisible = false
        }
    }
    private fun nextStepIfNeed() {
        if (resourceLoaded && isAnimationFinish) {
            binding.loadResource.root.isVisible = false
            afterResourceLoaded()
        }
    }

    private fun afterResourceLoaded() {
        if (MmkvManager.isAppFirstLaunch()) {
            binding.viewAgreeProtocal.isVisible = true
            binding.viewAgreeProtocal.onClick = {
                binding.viewAgreeProtocal.isVisible = false
                MmkvManager.saveAppLaunched()
                afterAgreeUserProtocal()
            }
        } else {
            afterAgreeUserProtocal()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.viewAgreeProtocal.removeClick()
        if (!isSupportSplashProgress) {
            binding.loadResource.icon.apply {
                val animatedVectorDrawable = drawable
                if (animatedVectorDrawable is AnimatedVectorDrawable) {
                    animatedVectorDrawable.unregisterAnimationCallback(animationCallback)
                }
            }
        }
    }

}
