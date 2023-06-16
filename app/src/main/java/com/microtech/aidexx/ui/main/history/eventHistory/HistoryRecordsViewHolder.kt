package com.microtech.aidexx.ui.main.history.eventHistory

import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.microtech.aidexx.AidexxApp
import com.microtech.aidexx.common.ioScope
import com.microtech.aidexx.databinding.FragmentHistoryEventBinding
import com.microtech.aidexx.db.repository.EventDbRepository
import com.microtech.aidexx.ui.main.history.EventHistoryFragment
import com.microtech.aidexx.ui.main.history.HistoryRecyclerViewAdapter
import com.microtech.aidexx.ui.main.history.HistoryDetailModel
import com.microtech.aidexx.ui.main.history.HistoryViewModel
import com.microtech.aidexx.utils.LogUtil
import com.microtech.aidexx.utils.eventbus.DataChangedType
import com.microtech.aidexx.utils.eventbus.EventBusKey
import com.microtech.aidexx.utils.eventbus.EventBusManager
import com.microtech.aidexx.utils.eventbus.EventDataChangedInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EventHistoryRecordsViewHolder(
    private val mFragment: EventHistoryFragment
) {

    private val vm by mFragment.activityViewModels<HistoryViewModel>()
    private val binding: FragmentHistoryEventBinding = mFragment.binding
    private lateinit var adapter: HistoryRecyclerViewAdapter
    private val dataList: MutableList<HistoryDetailModel> = mutableListOf()

    init {

        mFragment.lifecycleScope.launch {
            vm.recordsModel.collectLatest {
                it?.let {
                    initRecyclerView()

                    dataList.clear()
                    dataList.addAll(it)
                    adapter.notifyDataSetChanged()

                }
            }
        }
    }

    private fun initRecyclerView() {
        if (!::adapter.isInitialized) {
            adapter = HistoryRecyclerViewAdapter(dataList, ::onDeleteClick)
            binding.apply {
                rvHistoryDetail.adapter = adapter
                rvHistoryDetail.layoutManager =
                    LinearLayoutManager(mFragment.requireActivity(), LinearLayoutManager.VERTICAL, false)
            }
        }
    }

    private fun onDeleteClick(model: HistoryDetailModel) {

        AidexxApp.instance.ioScope.launch {
            model.idForRealEntity?.let {

                EventDbRepository.removeEventById(it, model.clazz)?.let { removed ->
                    withContext(Dispatchers.Main) {
                        val position = dataList.indexOf(model)
                        dataList.removeAt(position)
                        adapter.notifyItemRemoved(position)
                    }
                    vm.onDelete(removed)
                    EventBusManager.send(EventBusKey.EVENT_DATA_CHANGED, EventDataChangedInfo(DataChangedType.DELETE, listOf(removed)))
                }?:let {
                    LogUtil.d("删除失败 $model")
                }
            }?:let {
                LogUtil.d("删除失败  $model")
            }
        }
    }

}