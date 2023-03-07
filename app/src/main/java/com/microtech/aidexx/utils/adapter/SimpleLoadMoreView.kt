package com.chad.library.adapter.base.loadmore

import android.view.View
import android.view.ViewGroup
import com.microtech.aidexx.R
import com.microtech.aidexx.utils.adapter.BaseLoadMoreView
import com.microtech.aidexx.utils.adapter.BaseViewHolder
import com.microtech.aidexx.utils.adapter.util.getItemView

class SimpleLoadMoreView : BaseLoadMoreView() {

    override fun getRootView(parent: ViewGroup): View =
            parent.getItemView(R.layout.brvah_quick_view_load_more)

    override fun getLoadingView(holder: BaseViewHolder): View =
            holder.getView(R.id.load_more_loading_view)

    override fun getLoadComplete(holder: BaseViewHolder): View =
            holder.getView(R.id.load_more_load_complete_view)

    override fun getLoadEndView(holder: BaseViewHolder): View =
            holder.getView(R.id.load_more_load_end_view)

    override fun getLoadFailView(holder: BaseViewHolder): View =
            holder.getView(R.id.load_more_load_fail_view)
}