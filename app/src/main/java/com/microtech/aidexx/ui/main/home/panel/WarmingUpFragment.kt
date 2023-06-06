package com.microtech.aidexx.ui.main.home.panel

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import com.microtech.aidexx.base.BaseFragment
import com.microtech.aidexx.base.BaseViewModel
import com.microtech.aidexx.databinding.FragmentWarmingUpBinding
import com.microtech.aidexx.ui.main.home.HomeStateManager

class WarmingUpFragment : BaseFragment<BaseViewModel, FragmentWarmingUpBinding>() {

    var rotateAnimation: RotateAnimation? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentWarmingUpBinding.inflate(layoutInflater)
        initAnim()
        HomeStateManager.onWarmingUpTimeLeftListener = { timeOffset ->
            if (timeOffset != null) {
                binding.tvRemain.text = (60 - timeOffset).toString()
            } else {
                binding.tvRemain.text = "--"
            }
        }
        return binding.root
    }

    private fun initAnim() {
        rotateAnimation =
            RotateAnimation(
                0f, 360f, Animation.RELATIVE_TO_SELF,
                0.5f, Animation.RELATIVE_TO_SELF, 0.5f
            )
        rotateAnimation?.duration = 1500
        rotateAnimation?.interpolator = LinearInterpolator()
        rotateAnimation?.repeatCount = Animation.INFINITE
        rotateAnimation?.fillAfter = true
        binding.bgPanel.startAnimation(rotateAnimation)
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.bgPanel.clearAnimation()
        HomeStateManager.onWarmingUpTimeLeftListener = null
    }

    companion object {
        @JvmStatic
        fun newInstance() = WarmingUpFragment()
    }
}