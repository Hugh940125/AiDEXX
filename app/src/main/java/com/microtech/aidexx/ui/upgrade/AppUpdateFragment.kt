package com.microtech.aidexx.ui.upgrade

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.microtech.aidexx.common.dp2px
import com.microtech.aidexx.common.net.entity.AppUpdateInfo
import com.microtech.aidexx.databinding.DialogAppUpdateBinding

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

        binding.run {

            tvContent.text = updateInfo.data.description

            btOk.setOnClickListener {

            }
            btCancel.visibility = if (updateInfo.data.force == 1) View.GONE else View.VISIBLE
            btCancel.setOnClickListener {
                dismissAllowingStateLoss()
            }
        }

    }

    override fun onStart() {
        super.onStart()
        isCancelable = false
        dialog?.setCanceledOnTouchOutside(false)
        dialog?.window?.let {
            val lp = it.attributes
            context?.resources?.displayMetrics?.widthPixels?.let { width ->
                lp.width = width - 2 * 40.dp2px()
            }
            it.attributes = lp
        }
    }
}