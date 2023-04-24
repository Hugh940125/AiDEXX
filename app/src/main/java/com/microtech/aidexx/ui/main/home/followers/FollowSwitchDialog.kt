package com.microtech.aidexx.ui.main.home.followers

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.jeremyliao.liveeventbus.LiveEventBus
import com.microtech.aidexx.R
import com.microtech.aidexx.common.user.UserInfoManager
import com.microtech.aidexx.data.CloudHistorySync
import com.microtech.aidexx.databinding.LayoutFollowListDialogBinding
import com.microtech.aidexx.db.entity.ShareUserEntity
import com.microtech.aidexx.utils.DensityUtils
import com.microtech.aidexx.utils.ThemeManager
import com.microtech.aidexx.utils.eventbus.EventBusKey
import com.microtech.aidexx.utils.mmkv.MmkvManager
import com.microtech.aidexx.widget.dialog.Dialogs


class FollowSwitchDialog : Dialog {
    constructor(context: Context) : super(context)
    constructor(context: Context, themeResId: Int) : super(context, themeResId)
    constructor(
        context: Context,
        cancelable: Boolean,
        cancelListener: DialogInterface.OnCancelListener?
    ) : super(context, cancelable, cancelListener)

    class Setter {
        private var mFollowListDialog: FollowSwitchDialog? = null

        fun create(activity: Activity, followList: List<ShareUserEntity>): FollowSwitchDialog? {

            mFollowListDialog = FollowSwitchDialog(activity, R.style.BottomDialog)

            val vb = LayoutFollowListDialogBinding.inflate(LayoutInflater.from(activity))
            initView(vb, followList)

            mFollowListDialog?.setContentView(vb.root)
            mFollowListDialog?.setCanceledOnTouchOutside(true) // 外部点击取消
            val window: Window? = mFollowListDialog?.window
            val lp: WindowManager.LayoutParams? = window?.attributes
            lp?.gravity = Gravity.BOTTOM // 紧贴底部
            lp?.width = WindowManager.LayoutParams.MATCH_PARENT // 宽度持平
            val windowHeight = DensityUtils.getWindowHeight(activity)
            lp?.height = (windowHeight * 0.8).toInt()
            window?.attributes = lp
            window?.setBackgroundDrawable(
                if (ThemeManager.isLight()) ContextCompat.getDrawable(
                    activity,
                    R.drawable.bg_follow_list_dialog_light
                ) else ContextCompat.getDrawable(
                    activity,
                    R.drawable.bg_follow_list_dialog_dark
                )
            )

            mFollowListDialog?.setOnShowListener {
                if (!MmkvManager.isAlreadyShowFollowersGuide()) {
                    vb.clShadow.visibility = View.VISIBLE
                    vb.clShadow.setOnClickListener {
                        vb.clShadow.visibility = View.GONE
                    }
                    MmkvManager.setAlreadyShowFollowersGuide()
                } else {
                    vb.clShadow.visibility = View.GONE
                }
            }

            return mFollowListDialog
        }

        private fun initView(vb: LayoutFollowListDialogBinding, followList: List<ShareUserEntity>) {

            val followListAdapter = FollowListAdapter(vb.root.context)
            followListAdapter.onSelectChange = { _: Int, shareUserEntity: ShareUserEntity ->

                if (UserInfoManager.shareUserInfo?.id != shareUserEntity.id) {
//                todo 待实现    TrendBgSelector.instance().getHomeBg(null)
                    UserInfoManager.shareUserInfo = shareUserEntity
                    Dialogs.showWait("假装正在-${vb.root.context.getString(R.string.loading)}")

                    // 下载该用户的数据
                    CloudHistorySync.downloadAllData(UserInfoManager.shareUserInfo?.id) {
                        Dialogs.dismissWait()
                        //通知其他页面刷新
                        LiveEventBus
                            .get(EventBusKey.EVENT_SWITCH_USER, ShareUserEntity::class.java)
                            .post(shareUserEntity) //通知刷新历史页面
                    }

                    mFollowListDialog?.dismiss()
                }
            }

            vb.apply {

                ivCloseDialog.setOnClickListener {
                    mFollowListDialog?.dismiss()
                }

                tvMyInfo.text = UserInfoManager.instance().getDisplayName()
                clMy.setOnClickListener {
                    if (UserInfoManager.shareUserInfo != null) {

                        UserInfoManager.shareUserInfo = null
                        val shareUserEntity = ShareUserEntity()
                        shareUserEntity.id = UserInfoManager.instance().userId()

                        LiveEventBus
                            .get(EventBusKey.EVENT_SWITCH_USER, ShareUserEntity::class.java)
                            .post(shareUserEntity) //通知刷新历史页面

                        followListAdapter.unselectAll()

                        mFollowListDialog?.dismiss()
                    }
                }

                rvFollowList.layoutManager = LinearLayoutManager(vb.root.context)
                rvFollowList.adapter = followListAdapter
                followListAdapter.refreshData(followList)

            }
        }

    }
}