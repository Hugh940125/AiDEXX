package com.microtech.aidexx.ui.pair

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.microtech.aidexx.R
import com.microtech.aidexx.databinding.ItemTransmitterBinding
import com.microtechmd.blecomm.controller.BleControllerInfo

class TransmitterAdapter(val context: Context) : RecyclerView.Adapter<TransmitterAdapter.TransmitterViewHolder>() {

    private var mList: List<BleControllerInfo> = mutableListOf()
    var onPairClick: ((item: BleControllerInfo) -> Unit)? = null
    var onShowMoreClick: (() -> Unit)? = null
    var canShowMore = false

    inner class TransmitterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val bind: ItemTransmitterBinding

        init {
            bind = ItemTransmitterBinding.bind(itemView)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransmitterViewHolder {
        val inflate = LayoutInflater.from(context).inflate(R.layout.item_transmitter, null)
        return TransmitterViewHolder(inflate)
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    override fun onBindViewHolder(holder: TransmitterViewHolder, position: Int) {
        if (mList.isNotEmpty()) {
            holder.bind.tvSn.text = buildString {
                append("AiDEX X-")
                append(mList[position].sn)
            }
            holder.bind.transItem.setOnClickListener {
                if (mList.size > position) {
                    onPairClick?.invoke(mList[position])
                }
            }
            holder.bind.tvTransPairState.isVisible = false
            holder.bind.tvMoreDevice.isVisible = (position == mList.size - 1 && canShowMore)
            holder.bind.tvMoreDevice.setOnClickListener {
                onShowMoreClick?.invoke()
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun refreshData(list: List<BleControllerInfo>) {
        mList = list
        notifyDataSetChanged()
    }
}
