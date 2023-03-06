package com.microtech.aidexx.ui.main.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.microtech.aidexx.base.BaseFragment
import com.microtech.aidexx.base.BaseViewModel
import com.microtech.aidexx.databinding.FragmentGlucosePanelBinding
import com.microtech.aidexx.databinding.FragmentNewOrUsedSensorBinding

class NewOrUsedSensorFragment: BaseFragment<BaseViewModel, FragmentNewOrUsedSensorBinding>() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNewOrUsedSensorBinding.inflate(layoutInflater)
        return binding.root
    }

    companion object {
        @JvmStatic
        fun newInstance() = NewOrUsedSensorFragment()
    }
}