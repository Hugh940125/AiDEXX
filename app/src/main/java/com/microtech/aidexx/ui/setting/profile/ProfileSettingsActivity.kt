package com.microtech.aidexx.ui.setting.profile

import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.microtech.aidexx.R
import com.microtech.aidexx.base.BaseActivity
import com.microtech.aidexx.base.BaseViewModel
import com.microtech.aidexx.common.dateAndYM
import com.microtech.aidexx.common.setDebounceClickListener
import com.microtech.aidexx.common.toast
import com.microtech.aidexx.common.user.UserInfoManager
import com.microtech.aidexx.databinding.ActivitySettingsProfileBinding
import com.microtech.aidexx.db.entity.UserEntity
import com.microtech.aidexx.views.dialog.Dialogs
import com.microtech.aidexx.views.selector.time.TimePicker
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.util.Calendar
import java.util.Date
import kotlin.math.pow

class ProfileSettingsActivity : BaseActivity<BaseViewModel, ActivitySettingsProfileBinding>() {

    private val listHeight = (51..200).toList()
    private val listHeightData = listHeight.map { "$it cm" }
    private var defHeightIndex = 119
    private var listWeight = (21..160).toList()
    private val listWeightData = listWeight.map { "$it kg" }
    private var defWeightIndex = 39
    private val genderList = listOf("其他", "男","女")

    private val pVm by viewModels<ProfileViewModel>()

    companion object {
        private val TAG = ProfileSettingsActivity::class.java.simpleName
    }

    override fun getViewBinding(): ActivitySettingsProfileBinding =
        ActivitySettingsProfileBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        initView()
    }

    override fun onResume() {
        super.onResume()
        initData()
    }

    private fun initView() {
        binding.apply {
            actionBar.getLeftIcon().setDebounceClickListener {
                finish()
            }

            siwNickName.setDebounceClickListener {
                EditNameActivity.startEditNickName(
                    this@ProfileSettingsActivity,
                    UserInfoManager.instance().userEntity?.name)
            }
            siwHeight.setDebounceClickListener {
                var currentIndex = listHeight.indexOfLast {
                    it == UserInfoManager.instance().userEntity?.height
                }

                if (currentIndex < 0) {
                    currentIndex = defHeightIndex
                }

                Dialogs.Picker(this@ProfileSettingsActivity).singlePick(
                    listHeightData, currentIndex
                ) {
                    Dialogs.showWait()
                    lifecycleScope.launch {
                        pVm.modifyProfileInfo(height = listHeight[it]).collectLatest { ret ->
                            Dialogs.dismissWait()
                            getString(if (ret.first == 0) R.string.save_complete else R.string.State_Fail).toast()
                            if (ret.first == 0) {
                                siwHeight.setValue("${listHeight[it]} cm")
                                updateBmi(UserInfoManager.instance().userEntity)
                            }
                        }
                    }
                }
            }
            siwWeight.setDebounceClickListener {
                var currentIndex = listWeight.indexOfLast {
                    it == UserInfoManager.instance().userEntity?.bodyWeight
                }

                if (currentIndex < 0) {
                    currentIndex = defWeightIndex
                }

                Dialogs.Picker(this@ProfileSettingsActivity).singlePick(
                    listWeightData, currentIndex
                ) {
                    Dialogs.showWait()
                    lifecycleScope.launch {
                        pVm.modifyProfileInfo(bodyWeight = listWeight[it]).collectLatest { ret ->
                            Dialogs.dismissWait()
                            getString(if (ret.first == 0) R.string.save_complete else R.string.State_Fail).toast()
                            if (ret.first == 0) {
                                siwWeight.setValue("${listWeight[it]} kg")
                                updateBmi(UserInfoManager.instance().userEntity)
                            }
                        }
                    }
                }
            }

            siwFullName.setDebounceClickListener {
                EditNameActivity.startEditFullName(
                    this@ProfileSettingsActivity,
                    UserInfoManager.instance().userEntity?.fullName)
            }
            siwPhone.setDebounceClickListener {

            }
            siwGender.setDebounceClickListener {
                val currentIndex = UserInfoManager.instance().userEntity?.gender ?: 1
                Dialogs.Picker(this@ProfileSettingsActivity).singlePick(
                    genderList, currentIndex
                ) {
                    Dialogs.showWait()
                    lifecycleScope.launch {
                        pVm.modifyProfileInfo(gender = it).collectLatest { ret ->
                            Dialogs.dismissWait()
                            getString(if (ret.first == 0) R.string.save_complete else R.string.State_Fail).toast()
                            if (ret.first == 0) {
                                siwGender.setValue(genderList[it])
                            }
                        }
                    }
                }
            }
            siwBirth.setDebounceClickListener {
                val curDate = Calendar.getInstance()
                UserInfoManager.instance().userEntity?.let {
                    curDate.time = it.birthDate?.dateAndYM() ?: Date()
                }
                TimePicker(this@ProfileSettingsActivity).pick(
                    TimePicker.yearMonthType,
                    Calendar.getInstance().also {
                        it.set(Calendar.YEAR, it[Calendar.YEAR] - 100,)
                        it.set(Calendar.MONTH, 1)
                    }
                ) {
                    Dialogs.showWait()
                    lifecycleScope.launch {
                        pVm.modifyProfileInfo(birthDate = it.dateAndYM()).collectLatest { ret ->
                            Dialogs.dismissWait()
                            getString(if (ret.first == 0) R.string.save_complete else R.string.State_Fail).toast()
                            if (ret.first == 0) {
                                siwBirth.setValue(it.dateAndYM())
                                updateAge(UserInfoManager.instance().userEntity)
                            }
                        }
                    }
                }
            }


        }
    }

    private fun initData() {
        binding.apply {
            val userEntity = UserInfoManager.instance().userEntity
            siwNickName.setValue(userEntity?.name)
            siwHeight.setValue(userEntity?.height?.let { "$it cm" })
            siwWeight.setValue(userEntity?.bodyWeight?.let { "$it kg" })
            updateBmi(userEntity)
            siwFullName.setValue(userEntity?.fullName)
            siwPhone.setValue(userEntity?.getMaskedPhone())
            userEntity?.gender?.let {
                siwGender.setValue(genderList[it])
            }
            siwBirth.setValue(userEntity?.birthDate)
            updateAge(userEntity)

        }
    }

    private fun updateBmi(userEntity: UserEntity?) {
        binding.tvBmi.text = userEntity?.let {
            it.height?.let { h ->
                if (h >0) {
                    it.bodyWeight?.let { w ->
                        if (w > 0) {
                            val bmi = w.toDouble() / (h.toDouble() / 100).pow(2)
                            BigDecimal(bmi).setScale(1, BigDecimal.ROUND_HALF_UP).toDouble().toString()
                        } else null
                    }
                } else null
            }
        }
    }

    private fun updateAge(userEntity: UserEntity?) {
        binding.tvAge.text = userEntity?.let {
            it.birthDate?.dateAndYM()?.let { date ->

                val birthday = Calendar.getInstance()
                birthday.time = date
                val today = Calendar.getInstance()

                if (birthday.after(today)) {
                    // 如果出生日期在未来，将出生日期设置为今天
                    birthday.time = today.time
                }

                var age = today.get(Calendar.YEAR) - birthday.get(Calendar.YEAR)

                if (today.get(Calendar.MONTH) < birthday.get(Calendar.MONTH) ||
                    (today.get(Calendar.MONTH) == birthday.get(Calendar.MONTH) && today.get(Calendar.DAY_OF_MONTH) < birthday.get(
                        Calendar.DAY_OF_MONTH
                    ))
                ) {
                    age--
                }
                "$age"
            }
        }
    }

}