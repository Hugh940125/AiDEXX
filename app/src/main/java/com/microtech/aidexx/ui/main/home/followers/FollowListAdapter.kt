package com.microtech.aidexx.ui.main.home.followers

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.microtech.aidexx.R
import com.microtech.aidexx.common.user.UserInfoManager
import com.microtech.aidexx.databinding.LayoutFollowListItemBinding
import com.microtech.aidexx.db.entity.ShareUserEntity
import com.microtech.aidexx.utils.ThemeManager

class FollowListAdapter(
    val context: Context,
) : RecyclerView.Adapter<FollowListAdapter.FollowListViewHolder>() {

    private val followList: MutableList<ShareUserEntity> = mutableListOf()
    var onSelectChange: ((pos: Int, entity: ShareUserEntity) -> Unit)? = null

    class FollowListViewHolder(private val vb: LayoutFollowListItemBinding) : RecyclerView.ViewHolder(vb.root) {

        fun bindData(position: Int, user: ShareUserEntity, changeToThisCallback: ()->Unit) {

            vb.apply {

                @SuppressLint("SetTextI18n")
                accountPos.text = "${position + 1}"

                if (ThemeManager.isLight()) {
                    tvAccountInfo.setTextColor(root.context.getColor(R.color.black_1d))
                } else {
                    tvAccountInfo.setTextColor(root.context.getColor(R.color.white))
                }
                tvAccountInfo.text = user.getDisplayName()

                user.isLooking = user.id == UserInfoManager.shareUserInfo?.id
                cbIsSelect.setImageDrawable(
                    if (user.isLooking) ContextCompat.getDrawable(
                        root.context,
                        R.drawable.ic_is_looking
                    ) else ContextCompat.getDrawable(
                        root.context,
                        R.drawable.ic_go_see_see
                    )
                )

                listFollowRoot.setOnClickListener {
                    if (!user.isLooking) {
                        changeToThisCallback.invoke()
                    }
                }
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FollowListViewHolder {
        val vb = LayoutFollowListItemBinding.inflate(LayoutInflater.from(context), parent, false)
        return FollowListViewHolder(vb)
    }

    override fun onBindViewHolder(holder: FollowListViewHolder, position: Int) {
        val shareUserEntity = followList[position]
        holder.bindData(position, shareUserEntity) {
            onSelectChange?.invoke(position, followList[position])
            for (item in followList) {
                item.isLooking = item == followList[position]
            }
            notifyDataSetChanged()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun refreshData(list: List<ShareUserEntity>) {
        if (list.isNotEmpty()) {
            followList.clear()
            followList.addAll(list)
            notifyDataSetChanged()
        }
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
