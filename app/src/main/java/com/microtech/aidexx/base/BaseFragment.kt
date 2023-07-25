package com.microtech.aidexx.base

import android.content.res.Configuration
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import com.microtech.aidexx.utils.LogUtil
import com.microtech.aidexx.utils.ThemeManager
import com.microtech.aidexx.utils.Throttle
import com.microtech.aidexx.utils.statusbar.StatusBarHelper
import java.lang.reflect.ParameterizedType

abstract class BaseFragment<VM : BaseViewModel, VB : ViewBinding> : Fragment(), PageActions {
    lateinit var viewModel: VM
    lateinit var binding: VB
    protected lateinit var throttle: Throttle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LogUtil.eAiDEX("onCreate ----> ${this::class.java.name}")
        initViewModel()
        throttle = Throttle.instance()
    }

    override fun onResume() {
        super.onResume()
        if (ThemeManager.isLight()) {
            StatusBarHelper.setStatusBarLightMode(requireActivity())
        } else {
            StatusBarHelper.setStatusBarDarkMode(requireActivity())
        }
        LogUtil.eAiDEX("onResume ----> ${this::class.java.name}")
    }

    override fun onPause() {
        super.onPause()
        LogUtil.eAiDEX("onPause ----> ${this::class.java.name}")
    }

    override fun onDestroy() {
        super.onDestroy()
        LogUtil.eAiDEX("onDestroy ----> ${this::class.java.name}")
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(resources.configuration)
    }

    private fun initViewModel() {
        @Suppress("UNCHECKED_CAST")
        val clazz =
            (javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[0] as Class<VM>
        viewModel = ViewModelProvider(this)[clazz]
    }

    fun isBindingInit() = ::binding.isInitialized

    override fun canLeave(): AfterLeaveCallback? = null
}