package com.microtech.aidexx.ui.main.trend

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.microtech.aidexx.base.BaseFragment
import com.microtech.aidexx.base.BaseViewModel
import com.microtech.aidexx.databinding.FragmentTrendBinding

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class TrendFragment : BaseFragment<BaseViewModel, FragmentTrendBinding>() {
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.e("OnCreate","trend")
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onResume() {
        super.onResume()
        Log.e("onResume",this::class.java.toString())
    }

    override fun onPause() {
        super.onPause()
        Log.e("onPause",this::class.java.toString())
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.e("onDestroy",this::class.java.toString())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTrendBinding.inflate(layoutInflater)
        return binding.root
    }

    companion object {
        @JvmStatic
        fun newInstance() = TrendFragment()
    }
}