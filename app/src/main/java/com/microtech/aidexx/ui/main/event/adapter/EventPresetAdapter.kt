package com.microtech.aidexx.ui.main.event.adapter

import android.widget.TextView
import com.google.android.flexbox.FlexboxLayout
import com.microtech.aidexx.R
import com.microtech.aidexx.db.entity.event.preset.BasePresetEntity
import com.microtech.aidexx.utils.adapter.BaseQuickAdapter
import com.microtech.aidexx.utils.adapter.BaseViewHolder

class EventPresetAdapter(
    data: MutableList<out BasePresetEntity>,
    val onItemClick: (position: Int) -> Unit,
) :
    BaseQuickAdapter<BasePresetEntity, BaseViewHolder>(
        R.layout.item_preset_search,
        data as MutableList<BasePresetEntity>
    ) {

    override fun convert(holder: BaseViewHolder, item: BasePresetEntity) {
        holder.apply {
            item.apply {
                val flItem = getView<FlexboxLayout>(R.id.flItem)
                flItem.setOnClickListener {
                    onItemClick.invoke(adapterPosition)
                }
                val tvContent = getView<TextView>(R.id.tvContent)
                tvContent.text = getEventDesc()
                setVisible(R.id.ivFoodCustom, !isUserInputType && isUserPreset())
                setVisible(R.id.ivFoodInputIcon, isUserInputType)

            }
        }

    }

}
