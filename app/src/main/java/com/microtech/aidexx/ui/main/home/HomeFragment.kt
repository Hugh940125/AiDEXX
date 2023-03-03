package com.microtech.aidexx.ui.main.home

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.microtech.aidexx.R
import com.microtech.aidexx.base.BaseFragment
import com.microtech.aidexx.base.BaseViewModel
import com.microtech.aidexx.databinding.FragmentHomeBinding
import com.microtech.aidexx.ui.main.MainActivity
import com.microtech.aidexx.utils.TimeUtils

/**
 *@date 2023/2/15
 *@author Hugh
 *@desc
 */
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class HomeFragment : BaseFragment<BaseViewModel, FragmentHomeBinding>() {

    private val initOrientation: Int = 0
    private val switchOrientation: Int = 1
    private var param1: String? = null
    private var param2: String? = null
    private var mainActivity: MainActivity? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainActivity = activity as MainActivity
        Log.e("OnCreate", "Home")
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onResume() {
        super.onResume()
        orientation(initOrientation)
        Log.e("onResume", this::class.java.toString())
    }

    override fun onPause() {
        super.onPause()
        Log.e("onPause", this::class.java.toString())
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.e("onDestroy", this::class.java.toString())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(layoutInflater)
        initView()
        return binding.root
    }

    private fun initView() {
        binding.ivScale.setOnClickListener {
            orientation(switchOrientation)
        }
    }

    @SuppressLint("SourceLockedOrientationActivity")
    private fun orientation(type: Int) {
        if (mainActivity?.mCurrentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            if (switchOrientation == type) {
                mainActivity?.mCurrentOrientation = Configuration.ORIENTATION_PORTRAIT
                activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                portrait()
            } else {
                landSpace()
            }
        } else if (mainActivity?.mCurrentOrientation == Configuration.ORIENTATION_PORTRAIT) {
            if (switchOrientation == type) {
                mainActivity?.mCurrentOrientation = Configuration.ORIENTATION_LANDSCAPE
                activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                landSpace()
            } else {
                portrait()
            }
        }
        mainActivity?.fitOrientation()
    }

    private fun landSpace() {
        binding.bottomSpace.visibility = View.GONE
        binding.layoutState.visibility = View.GONE
        binding.layoutActionbar.visibility = View.GONE
        binding.ivScale.setImageDrawable(context?.let {
            ContextCompat.getDrawable(it, R.drawable.ic_scale_to_small)
        })
        binding.serviceView.hide()
    }

    private fun portrait() {
        binding.layoutState.visibility = View.VISIBLE
        binding.bottomSpace.visibility = View.VISIBLE
        binding.layoutActionbar.visibility = View.VISIBLE
        binding.ivScale.setImageDrawable(context?.let {
            ContextCompat.getDrawable(it, R.drawable.ic_scale)
        })
        binding.serviceView.show()
    }

    companion object {
        @JvmStatic
        fun newInstance() = HomeFragment()
    }
}
