package com.microtech.aidexx.ui.main.home.panel

import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import com.microtech.aidexx.base.BaseFragment
import com.microtech.aidexx.base.BaseViewModel
import com.microtech.aidexx.common.millisToMinutes
import com.microtech.aidexx.common.minutesToMillis
import com.microtech.aidexx.databinding.FragmentWarmingUpBinding
import com.microtech.aidexx.ui.main.home.HomeStateManager
import com.microtech.aidexx.utils.TimeUtils

class WarmingUpFragment : BaseFragment<BaseViewModel, FragmentWarmingUpBinding>() {

    var countTimer: CountDownTimer? = null
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
                createTimer(60 - timeOffset)
            } else {
                binding.tvRemain.text = "--"
            }
        }
        return binding.root
    }

    private fun createTimer(remain: Int) {
        if (remain > 2) {
            val remainLong = remain.minutesToMillis()
            countTimer?.cancel()
            countTimer = object : CountDownTimer(remainLong, TimeUtils.oneMinuteMillis) {
                override fun onTick(millisUntilFinished: Long) {
                    val remainTime = millisUntilFinished.millisToMinutes()
                    if (remainTime > 2) {
                        binding.tvRemain.text = remainTime.toString()
                    } else {
                        binding.tvRemain.text = "2"
                        cancel()
                    }
                }

                override fun onFinish() {
                    cancel()
                }
            }
            countTimer?.start()
        } else {
            countTimer?.cancel()
        }
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