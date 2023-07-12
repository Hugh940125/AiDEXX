package com.microtech.aidexx.ui.pair

import android.widget.LinearLayout
import com.microtech.aidexx.R
import com.microtech.aidexx.utils.adapter.BaseQuickAdapter
import com.microtech.aidexx.utils.adapter.BaseViewHolder
import com.microtechmd.blecomm.controller.BleControllerInfo

class TransmitterAdapter :
    BaseQuickAdapter<BleControllerInfo, BaseViewHolder>(R.layout.item_transmitter) {

    var onPairClick: ((item: BleControllerInfo) -> Unit)? = null
    override fun convert(holder: BaseViewHolder, item: BleControllerInfo) {
        holder.apply {
            setText(R.id.tv_sn, "${item.name}-${item.sn}")
            setGone(R.id.tv_trans_pair_state, true)
            val transItem = getView<LinearLayout>(R.id.trans_item)
            transItem.setOnClickListener {
                onPairClick?.invoke(item)
            }
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
//        if (position == 0) {
//            holder.setGone(R.id.trans_line, true)
//        }
    }
}
