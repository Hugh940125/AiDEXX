package com.microtech.aidexx.ui.main.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.microtech.aidexx.base.BaseFragment
import com.microtech.aidexx.base.BaseViewModel
import com.microtech.aidexx.databinding.FragmentNeedPairBinding
import com.microtech.aidexx.ui.pair.TransmitterActivity
import com.microtech.aidexx.utils.ActivityUtil

class NeedPairFragment : BaseFragment<BaseViewModel, FragmentNeedPairBinding>(), View.OnClickListener {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNeedPairBinding.inflate(layoutInflater)
        initView()
        return binding.root
    }

    private fun initView() {
        binding.btConfirm.setOnClickListener(this)
    }

    companion object {
        @JvmStatic
        fun newInstance() = NeedPairFragment()
    }

    override fun onClick(v: View?) {
        when (v) {
            binding.btConfirm -> {
                ActivityUtil.toActivity(requireContext(), TransmitterActivity::class.java)
            }
        }
    }
}