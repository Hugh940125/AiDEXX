package com.microtech.aidexx.ui.main.trend

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.microtech.aidexx.R
import com.microtech.aidexx.ui.main.trend.view.DotView
import com.microtech.aidexx.utils.ThemeManager

class MultiDateAdapter(
    val context: Context
) : RecyclerView.Adapter<MultiDateAdapter.MultiDateViewHolder>() {

    val listOfAll: MutableList<MultiDayBGItem> = mutableListOf()
    val listToShow: MutableList<MultiDayBGItem> = mutableListOf()
    var onDataSetChange: ((list: MutableList<MultiDayBGItem>, isAllChecked: Boolean) -> Unit)? =
        null

    @SuppressLint("InflateParams")
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MultiDateViewHolder {
        val view =
            LayoutInflater.from(context)
                .inflate(R.layout.multi_day_bg_date_item, null)
        return MultiDateViewHolder(view)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: MultiDateViewHolder, position: Int) {
        val item = listToShow[position]
        holder.dateItem.text = item.dateDesc
        if (item.checked) {
            holder.dateItem.setTextColor(
                ContextCompat.getColor(
                    context,
                    if (ThemeManager.isLight()) R.color.black_33 else R.color.white
                )
            )
            holder.dotView.changeColor(item.color)
        } else {
            holder.dateItem.setTextColor(
                ContextCompat.getColor(
                    context,
                    R.color.gray_cc
                )
            )
            holder.dotView.changeColor(
                ContextCompat.getColor(
                    context,
                    R.color.gray_cc
                )
            )
        }
        holder.clItem.setOnClickListener {
            item.checked = !item.checked
            notifyItemChanged(position)
            val predicate: (MultiDayBGItem) -> Boolean = { it.checked }
            val toMutableList = listOfAll.filter(predicate).toMutableList()
            onDataSetChange?.invoke(toMutableList, toMutableList.size == listOfAll.size)
        }
    }

    fun getDataSet(): MutableList<MultiDayBGItem> {
        val predicate: (MultiDayBGItem) -> Boolean = { it.checked }
        return listOfAll.filter(predicate).toMutableList()
    }

    fun isAllChecked(): Boolean {
        return getDataSet().size == listOfAll.size
    }

    override fun getItemCount(): Int {
        return listToShow.size
    }

    inner class MultiDateViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dateItem: TextView = itemView.findViewById(R.id.date_item)
        val dotView: DotView = itemView.findViewById(R.id.dot_view)
        val clItem: ConstraintLayout = itemView.findViewById(R.id.cl_item)
    }

    fun refreshData(list: MutableList<MultiDayBGItem>) {
        listOfAll.clear()
        listOfAll.addAll(list)
        contract()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun contract() {
        if (listOfAll.size < maxShowDefault) {
            listToShow.clear()
            listToShow.addAll(listOfAll)
            notifyDataSetChanged()
            return
        }
        listToShow.clear()
        listToShow.addAll(listOfAll.subList(0, maxShowDefault))
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun expand() {
        listToShow.clear()
        listToShow.addAll(listOfAll)
        notifyDataSetChanged()
    }

    fun selectAll() {
        for ((index, item) in listOfAll.withIndex()) {
            if (!item.checked) {
                item.checked = true
                notifyItemChanged(index)
            }
        }
        onDataSetChange?.invoke(listOfAll, true)
    }

    fun unselectAll() {
        for ((index, item) in listOfAll.withIndex()) {
            if (item.checked) {
                item.checked = false
                notifyItemChanged(index)
            }
        }
        onDataSetChange?.invoke(mutableListOf(), false)
    }

    fun selectDefault() {
        if (listOfAll.size <= maxShowDefault) {
            selectAll()
        } else {
            for ((index, item) in listOfAll.withIndex()) {
                if (index < maxShowDefault) {
                    if (!item.checked) {
                        item.checked = true
                        notifyItemChanged(index)
                    }
                } else {
                    if (item.checked) {
                        item.checked = false
                        notifyItemChanged(index)
                    }
                }
            }
            onDataSetChange?.invoke(getDataSet(), false)
        }
    }
}