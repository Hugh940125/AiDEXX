package com.microtech.aidexx.ui.main.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.microtech.aidexx.base.BaseFragment
import com.microtech.aidexx.base.BaseViewModel
import com.microtech.aidexx.databinding.FragmentAlertHistoryBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class AlertHistoryFragment : BaseFragment<BaseViewModel, FragmentAlertHistoryBinding>() {

    private val vm by activityViewModels<HistoryViewModel>()

    private lateinit var adapter: HistoryRecyclerViewAdapter
    private val dataList: MutableList<HistoryDetailModel> = mutableListOf()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAlertHistoryBinding.inflate(layoutInflater)

        initData()

        return binding.root
    }

    private fun initView() {
        if (!::adapter.isInitialized) {
            adapter = HistoryRecyclerViewAdapter(dataList, null)
            binding.apply {
                rvHistoryDetail.adapter = adapter
                rvHistoryDetail.layoutManager =
                    LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false)
            }
        }
    }
    private fun initData() {
        lifecycleScope.launch {
            vm.alertModel.collectLatest { list ->
                list?.let {

                    initView()

                    dataList.clear()
                    dataList.addAll(it)
                    adapter.notifyDataSetChanged()

                }
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = AlertHistoryFragment()
    }
}