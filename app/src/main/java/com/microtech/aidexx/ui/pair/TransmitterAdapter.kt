package com.microtech.aidexx.ui.pair

import com.microtech.aidexx.R
import com.microtech.aidexx.utils.adapter.BaseQuickAdapter
import com.microtech.aidexx.utils.adapter.BaseViewHolder
import com.microtech.aidexx.widget.button.StateButton
import com.microtechmd.blecomm.controller.BleControllerInfo

class TransmitterAdapter :
    BaseQuickAdapter<BleControllerInfo, BaseViewHolder>(R.layout.item_transmitter) {

    var onPairClick: ((item: BleControllerInfo) -> Unit)? = null
    override fun convert(holder: BaseViewHolder, item: BleControllerInfo) {
        holder.apply {
            setText(R.id.tv_sn, item.name)
            setGone(R.id.button_unpair, true)
            val buttonPair = holder.getView<StateButton>(R.id.button_pair)
            buttonPair.setOnClickListener {
                onPairClick?.invoke(item)
            }
            setGone(R.id.button_delete, true)
        }

    }
}
