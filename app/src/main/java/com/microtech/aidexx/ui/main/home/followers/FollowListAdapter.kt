package com.microtech.aidexx.ui.main.home.followers

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.microtech.aidexx.R
import com.microtech.aidexx.ble.device.model.DeviceModel
import com.microtech.aidexx.common.getContext
import com.microtech.aidexx.common.millisToMinutes
import com.microtech.aidexx.common.parseToTimestamp
import com.microtech.aidexx.common.user.UserInfoManager
import com.microtech.aidexx.databinding.LayoutFollowListItemBinding
import com.microtech.aidexx.ui.main.home.HomeBackGroundSelector
import com.microtech.aidexx.ui.setting.share.ShareUserInfo
import com.microtech.aidexx.utils.TimeUtils
import com.microtech.aidexx.utils.UnitManager
import com.microtech.aidexx.utils.toGlucoseValue


class FollowListAdapter(
    val context: Context,
) : RecyclerView.Adapter<FollowListAdapter.FollowListViewHolder>() {

    private val followList: MutableList<ShareUserInfo> = mutableListOf()
    var onSelectChange: ((pos: Int, entity: ShareUserInfo) -> Unit)? = null

    class FollowListViewHolder(private val vb: LayoutFollowListItemBinding) : RecyclerView.ViewHolder(vb.root) {

        fun bindData(position: Int, user: ShareUserInfo, changeToThisCallback: ()->Unit) {

            vb.apply {

                val ctx = vb.root.context
                userName.text = user.getDisplayName()
                user.isLooking = user.dataProviderId == UserInfoManager.shareUserInfo?.dataProviderId
                ivSelected.isVisible = user.isLooking

                val gValue = user.userTrend?.bloodGlucose?.toGlucoseValue()
                    ?:ctx.getString(R.string.data_place_holder)
                tvGlucoseValue.text = "$gValue"
                tvUnit.text = UnitManager.glucoseUnit.text

                lastTime.text = user.userTrend?.appTime?.let {
                    val timestamp = it.parseToTimestamp(user.userTrend?.appTimeZone!!)
                    getFriendlyTimeSpanByNow(timestamp)
                } ?: ctx.getString(R.string.data_place_holder)

                leftTime.text = user.getSensorStatusDesc()

                user.userTrend?.let {
                    bgPanel.rotation = when (it.getGlucoseTrend()) {
                        DeviceModel.GlucoseTrend.FAST_UP, DeviceModel.GlucoseTrend.UP -> 180f
                        DeviceModel.GlucoseTrend.SLOW_UP -> -90f
                        else -> 0f
                    }
                }

                bgPanel.setBackgroundResource(
                    HomeBackGroundSelector.instance()
                        .getBgForTrend(user.userTrend?.getGlucoseTrend(), user.userTrend?.getGlucoseLevel())
                )

                listFollowRoot.setOnClickListener {
                    if (!user.isLooking) {
                        changeToThisCallback.invoke()
                    }
                }
            }
        }

        private fun getFriendlyTimeSpanByNow(timestamp: Long?): String {
            return timestamp?.let {
                val minutesAgo = (TimeUtils.currentTimeMillis - it).millisToMinutes()
                return if (minutesAgo == 0) {
                    getContext().getString(R.string.now)
                } else {
                    buildString {
                        append(minutesAgo)
                        append(getContext().getString(R.string.min_ago))
                    }
                }
            } ?: getContext().getString(R.string.data_place_holder)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FollowListViewHolder {
        val vb = LayoutFollowListItemBinding.inflate(LayoutInflater.from(context), parent, false)
        return FollowListViewHolder(vb)
    }

    override fun onBindViewHolder(holder: FollowListViewHolder, position: Int) {
        val shareUserInfo = followList[position]
        holder.bindData(position, shareUserInfo) {
            onSelectChange?.invoke(position, followList[position])
            for (item in followList) {
                item.isLooking = item == followList[position]
            }
            notifyDataSetChanged()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun refreshData(list: List<ShareUserInfo>) {
        followList.clear()
        followList.addAll(list)
        notifyDataSetChanged()
    }

    fun unselectAll() {
        for ((index, item) in followList.withIndex()) {
            item.isLooking = false
            notifyItemChanged(index)
        }
    }

    override fun getItemCount(): Int {
        return followList.size
    }

}
