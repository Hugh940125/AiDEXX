package com.microtech.aidexx.ui.upgrade

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.microtech.aidexx.R
import com.microtech.aidexx.common.dp2px
import com.microtech.aidexx.common.net.entity.AppUpdateInfo
import com.microtech.aidexx.data.AppUpgradeManager
import com.microtech.aidexx.data.AppUpgradeManager.DOWNLOAD_STATUS_DONE
import com.microtech.aidexx.data.AppUpgradeManager.DOWNLOAD_STATUS_ERROR
import com.microtech.aidexx.databinding.DialogAppUpdateBinding
import com.microtech.aidexx.utils.LogUtil
import com.microtech.aidexx.utils.ToastUtil
import kotlinx.coroutines.launch

class AppUpdateFragment(private val updateInfo: AppUpdateInfo): DialogFragment() {

    companion object {
        const val TAG = "AppUpdateFragment"
    }

    lateinit var binding: DialogAppUpdateBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogAppUpdateBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launch {
            AppUpgradeManager.upgradeProgressFlow.collect {
                it?.let { ret ->
                    when (ret.first) {
                        DOWNLOAD_STATUS_DONE -> {
                            //do something
                            if (updateInfo.data.force != 1) {
                                dismissAllowingStateLoss()
                            }
                        }
                        DOWNLOAD_STATUS_ERROR -> {
                            ToastUtil.showLong("升级失败,请重试")
                            binding.run {
                                llDownload.visibility = View.VISIBLE
                                txtUpdateProgress.visibility = View.GONE
                                txtUpdateProgress.text = getString(R.string.download_update, "0%")
                            }
                        }
                        else -> {
                            binding.run {
                                llDownload.visibility = View.GONE
                                txtUpdateProgress.visibility = View.VISIBLE
                                txtUpdateProgress.text = getString(R.string.download_update, "${ret.first}%")
                            }
                        }
                    }
                }

            }
        }

        binding.run {

//            tvContent.text = updateInfo.data.description

            btOk.setOnClickListener {
                AppUpgradeManager.startUpgrade(updateInfo)
            }
            btCancel.visibility = if (updateInfo.data.force == 1) View.GONE else View.VISIBLE
            btCancel.setOnClickListener {
                dismissAllowingStateLoss()
                LogUtil.xLogE("暂不更新", TAG)
            }
        }

    }

    override fun onStart() {
        super.onStart()
        isCancelable = false
        dialog?.setCanceledOnTouchOutside(false)
        dialog?.window?.let {

            it.setBackgroundDrawableResource(R.color.transparent) // 透明

            val lp = it.attributes
            context?.resources?.displayMetrics?.widthPixels?.let { width ->
                lp.width = width - 2 * 40.dp2px()
                lp.height = (lp.width * 1.3).toInt()
            }
            it.attributes = lp
        }
    }
}