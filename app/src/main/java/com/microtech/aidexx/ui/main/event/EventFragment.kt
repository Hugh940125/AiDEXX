package com.microtech.aidexx.ui.main.event

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.microtech.aidexx.R
import com.microtech.aidexx.base.AfterLeaveCallback
import com.microtech.aidexx.base.BaseFragment
import com.microtech.aidexx.base.BaseViewModel
import com.microtech.aidexx.base.PageActions
import com.microtech.aidexx.common.net.repository.EventRepository
import com.microtech.aidexx.data.EventUnitManager
import com.microtech.aidexx.databinding.FragmentEventBinding
import com.microtech.aidexx.db.entity.event.preset.DietUsrPresetEntity
import com.microtech.aidexx.db.entity.event.preset.InsulinUsrPresetEntity
import com.microtech.aidexx.db.entity.event.preset.MedicineUsrPresetEntity
import com.microtech.aidexx.db.entity.event.preset.SportUsrPresetEntity
import com.microtech.aidexx.ui.main.event.viewmodels.BaseEventViewModel
import com.microtech.aidexx.utils.LogUtil
import com.microtech.aidexx.widget.ScrollTab.OnTabListener
import com.microtech.aidexx.widget.dialog.Dialogs
import kotlinx.coroutines.launch


class EventFragment : BaseFragment<BaseViewModel, FragmentEventBinding>() {

    private lateinit var mTitles: List<String>

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mTitles = listOf(
            getString(R.string.event_carbs),
            getString(R.string.event_exercise),
            getString(R.string.event_medicine),
            getString(R.string.event_insulin),
            getString(R.string.event_other)
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        BaseEventViewModel.periodMgr = EventParameterManager.instance()

        binding = FragmentEventBinding.inflate(layoutInflater)

        binding.apply {
            stIndicator.setTitles(mTitles)

            stIndicator.setOnTabListener(object: OnTabListener {
                override fun onChange(position: Int, v: View?): Boolean {
                    return if (!needConfirmLeave {
                            stIndicator.setCurrentIndex(position)
                            vpEventContent.setCurrentItem(position, false)
                        }) {
                        vpEventContent.setCurrentItem(position, false)
                        true
                    } else {
                        false
                    }
                }

            })

            val pagerAdapter = EventPageAdapter(requireActivity())
            vpEventContent.adapter = pagerAdapter
            vpEventContent.isUserInputEnabled = false
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        EventUnitManager.update()

        lifecycleScope.launch {
            when (binding.vpEventContent.currentItem) {
                0 -> EventRepository.syncEventPreset<DietUsrPresetEntity>().collect {
                    LogUtil.d("down diet preset isDone=${it.first} page=${it.second}", TAG)
                }
                1 -> EventRepository.syncEventPreset<SportUsrPresetEntity>().collect {
                    LogUtil.d("down sport preset isDone=${it.first} page=${it.second}", TAG)
                }
                2 -> EventRepository.syncEventPreset<MedicineUsrPresetEntity>().collect {
                    LogUtil.d("down medicine preset isDone=${it.first} page=${it.second}", TAG)
                }
                3 -> EventRepository.syncEventPreset<InsulinUsrPresetEntity>().collect {
                    LogUtil.d("down insulin preset isDone=${it.first} page=${it.second}", TAG)
                }
            }
        }

        binding.vpEventContent.run {
            (adapter as EventPageAdapter?)?.getFragmentByPosition(currentItem)?.onResume()
        }

    }

    fun needConfirmLeave(confirmCallback: () -> Unit): Boolean {
        val leaveCallback = canLeave()
        return leaveCallback?.let { cb ->
            // 弹框提示 确定离开后再赋值
            Dialogs.showWhether(
                activity as AppCompatActivity,
                content = getString(R.string.event_not_save_tip),
                confirm = {
                    cb.invoke()
                    confirmCallback.invoke()
                }
            )
            true
        } ?: let { false }

    }

    override fun canLeave(): AfterLeaveCallback? {
        return try {
            if (isBindingInit()) {
                binding.vpEventContent.run {
                    ((adapter as EventPageAdapter?)
                        ?.getFragmentByPosition(currentItem) as PageActions?)
                        ?.canLeave()
                }
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    class EventPageAdapter(fragmentActivity: FragmentActivity): FragmentStateAdapter(fragmentActivity) {
        private val mFragments = listOf<Fragment>(
            EventDietFragment(),
            EventSportFragment(),
            EventMedicineFragment(),
            EventInsulinFragment(),
            EventOthersFragment()
        )
        override fun getItemCount(): Int {
            return mFragments.size
        }
        override fun createFragment(position: Int): Fragment {
            return mFragments[position]
        }

        fun getFragmentByPosition(position: Int): Fragment? {
            if (position in mFragments.indices) {
                return mFragments[position]
            }
            return null
        }
    }

    companion object {
        const val TAG = "EventFragment"
        @JvmStatic
        fun newInstance() = EventFragment()
    }
}