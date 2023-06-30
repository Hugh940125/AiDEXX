package com.microtech.aidexx.ui.setting.share

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.microtech.aidexx.BuildConfig
import com.microtech.aidexx.databinding.ItemShareFollowListBinding
import com.microtech.aidexx.utils.LogUtil

class ShareFollowListAdapter: RecyclerView.Adapter<ShareFollowListAdapter.ViewHolder>() {

    var data: MutableList<ShareUserInfo>? = null
        set(value) {
            value?.let {
                if (field == null) {
                    field = mutableListOf()
                }
                field!!.clear()
                field!!.addAll(it)
                LogUtil.d(field.toString())
            }
        }

    var onItemClickListener: ((item: ShareUserInfo)->Unit)? = null

    class ViewHolder(private val vb: ItemShareFollowListBinding): RecyclerView.ViewHolder(vb.root) {

        fun bind(item: ShareUserInfo, position: Int) {
            vb.apply {
                dividerTop.isVisible = position != 0

                ivShareWechat.isVisible = false //item.source == 3

                Glide.with(root.context)
                    .load("${ BuildConfig.baseUrl }${item.information?.avatar}")
                    .transform(CircleCrop())
                    .into(ivAvatar)

                txtName.text = item.getDisplayName()

                root.setOnClickListener {
                }
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val vb = ItemShareFollowListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(vb)
    }

    override fun getItemCount(): Int = data?.size ?: 0

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        data?.let {
            val user = data!![position]
            holder.bind(user, position)
            onItemClickListener?.let { itemClick ->
                holder.itemView.setOnClickListener {
                    itemClick.invoke(data!![position])
                }
            }
        }

    }

}