package com.microtech.aidexx.ui.pair

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.microtech.aidexx.base.BaseActivity
import com.microtech.aidexx.base.BaseViewModel
import com.microtech.aidexx.common.net.ApiResult
import com.microtech.aidexx.common.net.ApiService
import com.microtech.aidexx.databinding.ActivityPairedHistoryBinding
import com.microtech.aidexx.utils.ToastUtil
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class PairedHistoryActivity : BaseActivity<BaseViewModel, ActivityPairedHistoryBinding>() {

    private lateinit var pairedHistoryAdapter: PairedHistoryAdapter

    override fun getViewBinding(): ActivityPairedHistoryBinding {
        return ActivityPairedHistoryBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        initView()
    }

    private fun initView() {
        binding.ivBack.setOnClickListener {
            finish()
        }
        binding.rvPairedHistory.layoutManager = LinearLayoutManager(this)
        pairedHistoryAdapter = PairedHistoryAdapter()
        binding.rvPairedHistory.adapter = pairedHistoryAdapter
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            when(val historyDevice = ApiService.instance.getHistoryDevice()){
                is ApiResult.Success -> {
                    historyDevice.result.data?.let {
                        pairedHistoryAdapter.setList(it)
                    }
                }

                is ApiResult.Failure -> {
                    ToastUtil.showShort(historyDevice.msg)
                }
            }
        }
    }
}