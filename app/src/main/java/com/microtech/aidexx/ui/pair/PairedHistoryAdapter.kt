package com.microtech.aidexx.ui.pair

import com.microtech.aidexx.R
import com.microtech.aidexx.ble.device.entity.DeviceInfo
import com.microtech.aidexx.common.formatToYMdHm
import com.microtech.aidexx.utils.adapter.BaseQuickAdapter
import com.microtech.aidexx.utils.adapter.BaseViewHolder

class PairedHistoryAdapter :
    BaseQuickAdapter<DeviceInfo, BaseViewHolder>(R.layout.item_paired_history) {

    override fun convert(holder: BaseViewHolder, item: DeviceInfo) {
        holder.apply {
            setText(R.id.history_device_sn, buildString {
                append(context.getString(R.string.sn))
                append(item.deviceSn)
            })
            setText(R.id.history_device_time, buildString {
                append(context.getString(R.string.pair_time))
                append(item.registerTime?.formatToYMdHm())
            })
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
//        if (position == 0) {
//            holder.setGone(R.id.trans_line, true)
//        }
    }
}
