package com.microtech.aidexx.ui.main.event.adapter

import android.widget.TextView
import com.flyco.roundview.RoundFrameLayout
import com.microtech.aidexx.R
import com.microtech.aidexx.data.resource.SpecificationModel
import com.microtech.aidexx.utils.adapter.BaseQuickAdapter
import com.microtech.aidexx.utils.adapter.BaseViewHolder

class SpecificationAdapter(data: MutableList<SpecificationModel>) :
    BaseQuickAdapter<SpecificationModel, BaseViewHolder>(R.layout.item_specification, data) {

    override fun convert(holder: BaseViewHolder, item: SpecificationModel) {
        item.apply {
            holder.apply {
                val tvName = getView<TextView>(R.id.tvSpecification)
                val flSpecificationContainer =
                    getView<RoundFrameLayout>(R.id.flSpecificationContainer)

                holder.setText(R.id.tvSpecification, item.specification)

                tvName.setTextColor(itemView.resources.getColor(
                    if(check) R.color.event_tag_color_selected else R.color.event_unit_color,
                    itemView.context.theme))

                flSpecificationContainer.delegate.strokeColor = itemView.resources.getColor(
                    if(check) R.color.event_tag_color_selected else R.color.event_unit_border,
                    itemView.context.theme)

            }
        }


    }
}
