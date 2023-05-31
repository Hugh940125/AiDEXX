package com.microtech.aidexx.ui.main.bg

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.ThemeUtils
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.microtech.aidexx.R
import com.microtech.aidexx.utils.ThemeManager

class TimeSlopAdapter(val context: Context) : RecyclerView.Adapter<TimeSlopAdapter.TimeSlopViewHolder>() {

    var onSelect: ((type: Int) -> Unit)? = null
    private val mList: List<TimeSlotInfo> = listOf(
        TimeSlotInfo(context.getString(R.string.empty_stomach), false),
        TimeSlotInfo(context.getString(R.string.after_breakfast), false),
        TimeSlotInfo(context.getString(R.string.before_lunch), false),
        TimeSlotInfo(context.getString(R.string.after_lunch), false),
        TimeSlotInfo(context.getString(R.string.before_dinner), false),
        TimeSlotInfo(context.getString(R.string.after_dinner), false),
        TimeSlotInfo(context.getString(R.string.before_sleep), false),
        TimeSlotInfo(context.getString(R.string.morning), false),
        TimeSlotInfo(context.getString(R.string.random), false)
    )

    @SuppressLint("InflateParams")
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeSlopViewHolder {
        val view =
            LayoutInflater.from(context).inflate(R.layout.time_slop_item, null)
        return TimeSlopViewHolder(view)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: TimeSlopViewHolder, position: Int) {
        holder.timeSlotItem.text = mList[position].name
        if (ThemeManager.isLight()) {
            holder.timeSlotItem.setTextColor(
                ContextCompat.getColor(
                    context,
                    R.color.black_33
                )
            )
        } else {
            holder.timeSlotItem.setTextColor(
                ContextCompat.getColor(
                    context,
                    R.color.white
                )
            )
        }
        if (mList[position].isChecked) {
            if (ThemeManager.isLight()) {
                holder.timeSlotItem.background =
                    ContextCompat.getDrawable(context, R.drawable.bg_time_slop_checked_light)
                holder.timeSlotItem.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.light_colorAccent
                    )
                )
            } else {
                holder.timeSlotItem.background =
                    ContextCompat.getDrawable(context, R.drawable.bg_time_slop_checked)
                holder.timeSlotItem.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.colorAccent
                    )
                )
            }
        } else {
            if (ThemeManager.isLight()) {
                holder.timeSlotItem.background =
                    ContextCompat.getDrawable(context, R.drawable.bg_time_slop_unchecked_light)
                holder.timeSlotItem.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.gray_1d
                    )
                )
            } else {
                holder.timeSlotItem.background =
                    ContextCompat.getDrawable(context, R.drawable.bg_time_slop_unchecked)
                holder.timeSlotItem.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.white
                    )
                )
            }
        }
        holder.timeSlotItem.setOnClickListener {
            for ((index, item) in mList.withIndex()) {
                item.isChecked = index == position
                if (item.isChecked) {
                    if (index != 8)
                        onSelect?.invoke(index + 1)
                    else
                        onSelect?.invoke(99)
                }
            }
            notifyDataSetChanged()
        }
    }

    fun clearCheck() {
        for ((index, item) in mList.withIndex()) {
            if (item.isChecked) {
                item.isChecked = false
                notifyItemChanged(index)
            }
        }
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    inner class TimeSlopViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val timeSlotItem: TextView = itemView.findViewById(R.id.slot_item)
    }
}