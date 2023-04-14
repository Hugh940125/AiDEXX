package com.microtech.aidexx.ui.main.bg.history

import com.microtech.aidexx.R
import com.microtech.aidexx.common.date2ymdhm
import com.microtech.aidexx.db.entity.BloodGlucoseEntity
import com.microtech.aidexx.utils.adapter.BaseQuickAdapter
import com.microtech.aidexx.utils.adapter.BaseViewHolder
import com.microtech.aidexx.widget.dialog.lib.util.toGlucoseStringWithUnit


class BloodGlucoseHistoryAdapter :
    BaseQuickAdapter<BloodGlucoseEntity, BaseViewHolder>(R.layout.item_glucose_history) {

    override fun convert(holder: BaseViewHolder, item: BloodGlucoseEntity) {
        holder.apply {
            setText(R.id.tvGlucoseTime, item.testTime.date2ymdhm())
            var tagText = item.getTagText(context.resources)
            if (tagText.isNullOrEmpty()) {
                tagText = "— —"
            }
            setText(R.id.tvGlucoseDescribe, tagText)
            setText(
                R.id.tvGlucoseValue,
                item.bloodGlucose.toGlucoseStringWithUnit()
            )
            if (absoluteAdapterPosition == data.size - 1) {
                setGone(R.id.viDivider, true)
            } else {
                setGone(R.id.viDivider, false)
            }
        }

    }
}
