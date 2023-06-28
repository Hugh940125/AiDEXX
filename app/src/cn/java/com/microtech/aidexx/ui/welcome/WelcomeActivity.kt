package com.microtech.aidexx.ui.welcome

import android.os.Bundle
import androidx.core.view.isVisible
import com.microtech.aidexx.base.BaseActivity
import com.microtech.aidexx.base.BaseViewModel
import com.microtech.aidexx.databinding.ActivityWelcomeBinding
import com.microtech.aidexx.ui.setting.LoadResourceActivity
import com.microtech.aidexx.utils.ActivityUtil
import com.microtech.aidexx.utils.ThemeManager
import com.microtech.aidexx.utils.mmkv.MmkvManager
import com.microtech.aidexx.widget.dialog.lib.DialogX

class WelcomeActivity : BaseActivity<BaseViewModel, ActivityWelcomeBinding>() {

    override fun getViewBinding(): ActivityWelcomeBinding {
        return ActivityWelcomeBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        initView()
    }

    private fun initView() {
//        binding.buttonRegist.isVisible = true
//        binding.buttonLogin.isVisible = true
        if (MmkvManager.isAppFirstLaunch()) {
            binding.viewAgreeProtocal.isVisible = true
            binding.viewAgreeProtocal.onClick = {
                binding.viewAgreeProtocal.isVisible = false
                MmkvManager.saveAppLaunched()
                greenLight()
            }
        } else {
            greenLight()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.viewAgreeProtocal.removeClick()
    }

    private fun greenLight() {

        // 冷启动时先去加载动态资源 如语言资源需要堵界面
        ActivityUtil.toActivity(this, LoadResourceActivity::class.java)
        finish()

//        if (UserInfoManager.instance().isLogin()) {
//            // 冷启动时先去加载动态资源 如语言资源需要堵界面
//            ActivityUtil.toActivity(this, LoadResourceActivity::class.java)
//            finish()
//        } else {
//            // 登录前判断是否需要做内置资源释放
//            if (StringUtils.versionCompare(MmkvManager.getResourceVersion(), BuildConfig.resourceVersion)) {
//                ActivityUtil.toActivity(this, LoadResourceActivity::class.java)
//            } else {
//                ActivityUtil.toActivity(this, LoginActivity::class.java)
//            }
//            finish()
//        }
    }

}
