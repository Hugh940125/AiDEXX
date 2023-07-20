package com.microtech.aidexx.base

import android.graphics.Color
import android.graphics.drawable.Animatable2.AnimationCallback
import android.graphics.drawable.AnimatedVectorDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.splashscreen.SplashScreenViewProvider
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.microtech.aidexx.common.dp2px
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

    @Volatile
    private var resourceLoaded = false
    @Volatile
    private var isAnimationFinish = false
    private var isSupportSplashProgress = true


    companion object {
        private const val TAG = "BaseWelcomeActivity"
        const val EXT_UPDATE_RESOURCE = "EXT_UPDATE_RESOURCE"
    }
    abstract fun afterAgreeUserProtocol()

    override fun getViewBinding(): ActivityWelcomeBinding {
        return ActivityWelcomeBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        isSupportSplashProgress = SDK_INT >= Build.VERSION_CODES.S
                && !intent.getBooleanExtra(EXT_UPDATE_RESOURCE, false)

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
                    LogUtil.d("资源加载成功", TAG)
                }
            }.exceptionOrNull()?.let {
                LogUtil.xLogE("资源加载失败：$it", TAG)
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
            LogUtil.d("OnExitAnimationListener", TAG)
            if (isSupportSplashProgress) {
                afterResourceLoaded(it)
            } else {
                it.remove()
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
            binding.loadResource.apply {
                splashIconContainer.setPadding(0,getNavBarHeight(),0,0)
                val animatedVectorDrawable = splashIcon.drawable
                if (animatedVectorDrawable is AnimatedVectorDrawable) {
                    animatedVectorDrawable.registerAnimationCallback(animationCallback)
                    animatedVectorDrawable.start()
                }
            }
        }
    }
    private fun getNavBarHeight(): Int {
        val resourceId: Int = resources.getIdentifier("navigation_bar_height", "dimen", "android")
        return if (resourceId != 0) {
            resources.getDimensionPixelSize(resourceId)
        } else {
            40.dp2px()
        }
    }

    private fun nextStepIfNeed() {
        if (resourceLoaded && isAnimationFinish) {
            afterResourceLoaded()
        }
    }

    private fun afterResourceLoaded(splashScreen: SplashScreenViewProvider? = null) {
        updateWindow()
        LogUtil.d("afterResourceLoaded", TAG)
        if (MmkvManager.isAppFirstLaunch()) {
            splashScreen?.remove()
            binding.loadResource.root.isVisible = false
            binding.viewAgreeProtocal.isVisible = true
            binding.viewAgreeProtocal.onClick = {
                binding.viewAgreeProtocal.isVisible = false
                MmkvManager.saveAppLaunched()
                afterAgreeUserProtocol()
            }
            LogUtil.d("AppFirstLaunch", TAG)
        } else {
            afterAgreeUserProtocol()
        }
    }

    private fun updateWindow() {
        val window = this.window
        val decorView = window.decorView
        if (SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
            window.statusBarColor = Color.TRANSPARENT
            window.insetsController?.also { controller ->
                controller.show(WindowInsets.Type.statusBars())
                controller.show(WindowInsets.Type.navigationBars())
            }
        } else {
            decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
            window.statusBarColor = Color.TRANSPARENT
            window.decorView.apply {
                // 设置状态栏系统栏覆盖在应用内容上
                systemUiVisibility =
                    systemUiVisibility or View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.viewAgreeProtocal.removeClick()
        if (!isSupportSplashProgress) {
            binding.loadResource.splashIcon.apply {
                val animatedVectorDrawable = drawable
                if (animatedVectorDrawable is AnimatedVectorDrawable) {
                    animatedVectorDrawable.unregisterAnimationCallback(animationCallback)
                }
            }
        }
    }

}
