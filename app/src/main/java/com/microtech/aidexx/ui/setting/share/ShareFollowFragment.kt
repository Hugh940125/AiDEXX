package com.microtech.aidexx.ui.setting.share

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.microtech.aidexx.base.BaseFragment
import com.microtech.aidexx.base.BaseViewModel
import com.microtech.aidexx.databinding.FragShareOrFollowBinding
import kotlinx.coroutines.launch


class ShareFollowFragment(private val isShare: Boolean) : BaseFragment<BaseViewModel, FragShareOrFollowBinding>() {

    private val messageWhat = 9
    private lateinit var timeHandler: TimeHandler
    private var listAdapter: ShareFollowListAdapter? = null
    private val sfViewModel: ShareFollowViewModel by viewModels(ownerProducer = { requireActivity() })

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragShareOrFollowBinding.inflate(inflater)
        timeHandler = TimeHandler(binding)
        initView()
        initEvent()
        return binding.root
    }

    private fun initView() {
        binding.apply {
            llBtn.isVisible = isShare
        }
    }

    private fun initEvent() {
        binding.apply {

            shareRefreshLayout.setOnRefreshListener {
                getData()
            }

            btnAccountAdd.setOnClickListener{
                context?.startActivity(Intent(context, ShareAddUserActivity::class.java))
            }

            btnWechatAdd.setOnClickListener {
                context?.startActivity(Intent(context, ShareAddUserByWechatActivity::class.java))
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun getData() {
        lifecycleScope.launch {
            timeHandler.sendEmptyMessageDelayed(messageWhat, 10 * 1000)
            sfViewModel.loadData(isShare).collect {
                binding.apply {
                    shareRefreshLayout.finishRefresh()

                    listShare.isVisible = it.isNotEmpty()
                    if (it.isNotEmpty()) {
                        listShare.layoutManager = LinearLayoutManager(context)
                        listAdapter = ShareFollowListAdapter()
                        listAdapter!!.onItemClickListener = {
                            val intent = Intent(context, ShareAndFollowEditActivity::class.java)
                            intent.putExtra(EDIT_FROM, if (isShare) EDIT_FROM_SHARE else EDIT_FROM_FOLLOW)
                            intent.putExtra(EDIT_DATA, it)
                            startActivity(intent)
                        }
                        listShare.adapter = listAdapter
                        listAdapter!!.data = it
                        listAdapter!!.notifyDataSetChanged()
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        binding.shareRefreshLayout.autoRefresh()
    }

    class TimeHandler(val binding: FragShareOrFollowBinding) : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            binding.shareRefreshLayout.finishRefresh()
        }
    }

    companion object {
        fun getShareInstance(): ShareFollowFragment = ShareFollowFragment(true)
        fun getFollowInstance(): ShareFollowFragment = ShareFollowFragment(false)
    }

}

