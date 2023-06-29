package com.microtech.aidexx.ui.main.history

import android.text.TextUtils
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import com.bumptech.glide.Glide
import com.microtech.aidexx.R
import com.microtech.aidexx.common.hourMinute
import com.microtech.aidexx.common.setDebounceClickListener
import com.microtech.aidexx.db.entity.BaseEventEntity
import com.microtech.aidexx.db.entity.event.OthersEntity
import com.microtech.aidexx.utils.LogUtil
import com.microtech.aidexx.utils.adapter.BaseQuickAdapter
import com.microtech.aidexx.utils.adapter.BaseViewHolder
import com.microtech.aidexx.views.SwipeDeleteMenuLayout


data class HistoryDetailModel(
    var time: Long? = null,
    var title: String? = null,
    var contentList: MutableList<String> = mutableListOf(),
    var resourceId: Int? = null,
    var idForRealEntity: Long? = null,
    var expand: Boolean = false,
    var deletable: Boolean = true,
    var clazz: Class<BaseEventEntity>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as HistoryDetailModel

        if (time != other.time) return false
        if (clazz != other.clazz) return false
        if (title != other.title) return false
        if (resourceId != other.resourceId) return false
        if (idForRealEntity != other.idForRealEntity) return false

        return true
    }

    override fun hashCode(): Int {
        var result = time?.hashCode() ?: 0
        result = 31 * result + (clazz.hashCode())
        result = 31 * result + (title?.hashCode() ?: 0)
        result = 31 * result + (resourceId ?: 0)
        result = 31 * result + (idForRealEntity?.hashCode() ?: 0)
        return result
    }
}

class HistoryRecyclerViewAdapter(
    data: MutableList<HistoryDetailModel>?,
    val onDeleteClick: ((model: HistoryDetailModel) -> Unit)?
) :
    BaseQuickAdapter<HistoryDetailModel, BaseViewHolder>(R.layout.item_event_history, data) {

    override fun convert(holder: BaseViewHolder, item: HistoryDetailModel) {
        holder.apply {
            setTimeLine(item)
            setHistory(item)
            setDetailUi(item)
            setContent(item)
            setDeleteUi(item)
            setCollapse(item)
        }
    }

    private fun BaseViewHolder.setDetailUi(
        historyDetailModel: HistoryDetailModel
    ): TextView {
        val tvDetail = getView<TextView>(R.id.tvDetail)
        tvDetail.text = historyDetailModel.title
        return tvDetail
    }

    private fun BaseViewHolder.setHistory(historyDetailModel: HistoryDetailModel) {
        val ivHistory = getView<ImageView>(R.id.ivHistory)
        Glide.with(context).load(historyDetailModel.resourceId).into(ivHistory)
    }

    private fun BaseViewHolder.setTimeLine(historyDetailModel: HistoryDetailModel) {
        val tvTimeLine = getView<TextView>(R.id.tvTimeLine)
        tvTimeLine.text = historyDetailModel.time?.hourMinute()
    }

    private fun BaseViewHolder.setContent(
        historyDetailModel: HistoryDetailModel
    ) {
        val tvContent = getView<TextView>(R.id.tvContent)
        if (!historyDetailModel.contentList.isNullOrEmpty()) {
            tvContent.visibility = View.VISIBLE
            tvContent.text = historyDetailModel.contentList.joinToString("\n")
        } else {
            tvContent.visibility = View.GONE
        }
    }

    private fun BaseViewHolder.setDeleteUi(historyDetailModel: HistoryDetailModel) {
        val smlDelete = getView<SwipeDeleteMenuLayout>(R.id.sml_delete)
        smlDelete.isSwipeEnable = historyDetailModel.deletable
        if (historyDetailModel.deletable) {
            val flHistoryDelete = getView<ConstraintLayout>(R.id.flHistoryDelete)
            flHistoryDelete.setDebounceClickListener {
                onDeleteClick?.invoke(historyDetailModel)
            }
        }
    }

    private fun BaseViewHolder.setCollapse(
        historyDetailModel: HistoryDetailModel,
    ) {
        val tvDetail = getView<TextView>(R.id.tvDetail)
        val ivCollapse = getView<ImageView>(R.id.ivCollapse)
        val flCollapse = getView<FrameLayout>(R.id.flCollapse)
        if (OthersEntity::class.java == historyDetailModel.clazz &&
            getContentLength(historyDetailModel) > 55
        ) {
            flCollapse.visibility = View.VISIBLE
            collapse(tvDetail, ivCollapse, historyDetailModel)
            flCollapse.setOnClickListener {
                if (historyDetailModel.expand) {
                    LogUtil.d("收缩")
                    collapse(tvDetail, ivCollapse, historyDetailModel)
                } else {
                    LogUtil.d("展开")
                    expand(tvDetail, ivCollapse, historyDetailModel)
                }
            }
        } else {
            flCollapse.visibility = View.GONE
        }
    }

    private fun getContentLength(historyDetailModel: HistoryDetailModel) =
        if (historyDetailModel.title != null) {
            historyDetailModel.title!!.length
        } else {
            0
        }

    private fun collapse(tvDetail: TextView, ivCollapse: ImageView, entity: HistoryDetailModel) {
        tvDetail.ellipsize = TextUtils.TruncateAt.END
        tvDetail.maxLines = 3
        entity.expand = false
        ivCollapse.setImageDrawable(
            ResourcesCompat.getDrawable(
                context.resources,
                R.drawable.ic_expand,
                null
            )
        )
    }

    private fun expand(tvDetail: TextView, ivCollapse: ImageView, entity: HistoryDetailModel) {
        tvDetail.ellipsize = TextUtils.TruncateAt.END
        tvDetail.maxLines = 500
        entity.expand = true
        ivCollapse.setImageDrawable(
            ResourcesCompat.getDrawable(
                context.resources,
                R.drawable.ic_collapse,
                null
            )
        )
    }
}