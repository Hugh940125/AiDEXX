package com.microtech.aidexx.ui.setting.share

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.microtech.aidexx.R
import com.microtech.aidexx.base.BaseActivity
import com.microtech.aidexx.base.BaseViewModel
import com.microtech.aidexx.databinding.ActivityShareBinding
import com.microtech.aidexx.views.ScrollTab

class ShareFollowActivity : BaseActivity<BaseViewModel, ActivityShareBinding>() {

    private lateinit var frgShareAuthor: ShareFollowFragment
    private var frgShareFollow: ShareFollowFragment? = null

    private val tagShare = "share"
    private val tagFollow = "follow"

    override fun getViewBinding(): ActivityShareBinding {
        return ActivityShareBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val listTitles = mutableListOf(
            getString(R.string.share_authorize),
            getString(R.string.share_follow)
        )

        binding.actionBar.getLeftIcon().setOnClickListener {
            finish()
        }
        frgShareAuthor = ShareFollowFragment.getShareInstance()
        replaceFragment(tagShare)

        binding.apply {
            shareNav.setTitles(listTitles)
            shareNav.setOnTabListener(object: ScrollTab.OnTabListener {
                override fun onChange(position: Int, v: View?): Boolean {
                    replaceFragment(if (position == 0) tagShare else tagFollow)
                    return true
                }
            })
        }
    }

    private fun replaceFragment(tag: String) {
        val fragment = when (tag) {
            tagFollow -> {
                if (frgShareFollow == null) {
                    frgShareFollow = ShareFollowFragment.getFollowInstance()
                }
                frgShareFollow
            }
            else -> frgShareAuthor
        }
        try {
            val transaction = supportFragmentManager.beginTransaction()
            transaction.replace(R.id.frg_share, fragment as Fragment, tag)
            transaction.commitAllowingStateLoss()
        } finally {
        }
    }
}
