package com.microtech.aidexx.ui.pair

import android.widget.Button
import com.microtech.aidexx.R
import com.microtech.aidexx.utils.adapter.BaseQuickAdapter
import com.microtech.aidexx.utils.adapter.BaseViewHolder
import com.microtechmd.blecomm.controller.BleControllerInfo

class FindTransmitterAdapter :
    BaseQuickAdapter<BleControllerInfo, BaseViewHolder>(R.layout.item_transmitter) {

    var callBack: ((item: BleControllerInfo) -> Unit)? = null

    override fun convert(holder: BaseViewHolder, item: BleControllerInfo) {
        holder.apply {
            setText(R.id.tv_sn, "${item.name}:${item.address}")
            setGone(R.id.button_unpair, true)
            val buttonPair = holder.getView<Button>(R.id.button_pair)
            buttonPair.setOnClickListener {
                callBack?.invoke(item)
            }
            setGone(R.id.button_delete,true)
        }

    }
}
