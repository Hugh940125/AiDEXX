package com.microtech.aidexx.ui.main.event.adapter

import com.flyco.roundview.RoundTextView
import com.microtech.aidexx.R
import com.microtech.aidexx.db.entity.event.EventActions
import com.microtech.aidexx.utils.adapter.BaseQuickAdapter
import com.microtech.aidexx.utils.adapter.BaseViewHolder

class EventInputAdapter(data: MutableList<out EventActions>, private val isHistory: Boolean = false) :
    BaseQuickAdapter<EventActions, BaseViewHolder>(
        R.layout.item_border_text,
        data as MutableList<EventActions>
    ) {

    override fun convert(holder: BaseViewHolder, item: EventActions) {
        holder.apply {
            item.apply {
                val tvName = getView<RoundTextView>(R.id.tvContent)
                tvName.text = getEventDesc()

                tvName.delegate.strokeColor = itemView.resources.getColor(
                    if (isHistory) R.color.event_tag_border else R.color.event_tag_color_selected,
                    itemView.context.theme
                )

                tvName.setTextColor(
                    itemView.resources.getColor(
                        if (isHistory) R.color.event_tag_color else R.color.event_tag_color_selected,
                        itemView.context.theme
                    )
                )

            }
        }

    }
}
