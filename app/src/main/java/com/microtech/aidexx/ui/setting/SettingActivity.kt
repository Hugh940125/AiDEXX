package com.microtech.aidexx.ui.setting

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.microtech.aidexx.AidexxApp
import com.microtech.aidexx.BuildConfig
import com.microtech.aidexx.R
import com.microtech.aidexx.base.BaseActivity
import com.microtech.aidexx.base.BaseViewModel
import com.microtech.aidexx.ble.device.TransmitterManager
import com.microtech.aidexx.common.ioScope
import com.microtech.aidexx.common.net.ApiResult
import com.microtech.aidexx.common.net.repository.AccountRepository
import com.microtech.aidexx.common.setDebounceClickListener
import com.microtech.aidexx.common.toast
import com.microtech.aidexx.common.user.UserInfoManager
import com.microtech.aidexx.data.resource.LanguageResourceManager
import com.microtech.aidexx.databinding.ActivitySettingBinding
import com.microtech.aidexx.ui.pair.TransmitterActivity
import com.microtech.aidexx.ui.setting.alert.AlertSettingsActivity
import com.microtech.aidexx.ui.setting.profile.ProfileSettingsActivity
import com.microtech.aidexx.ui.setting.share.ShareFollowActivity
import com.microtech.aidexx.ui.web.WebActivity
import com.microtech.aidexx.ui.welcome.WelcomeActivity
import com.microtech.aidexx.utils.ActivityUtil
import com.microtech.aidexx.utils.FileUtils
import com.microtech.aidexx.utils.LogUtil
import com.microtech.aidexx.utils.ThemeManager
import com.microtech.aidexx.utils.UnitManager
import com.microtech.aidexx.utils.eventbus.EventBusKey
import com.microtech.aidexx.utils.eventbus.EventBusManager
import com.microtech.aidexx.utils.mmkv.MmkvManager
import com.microtech.aidexx.utils.permission.PermissionGroups
import com.microtech.aidexx.utils.permission.PermissionsUtil
import com.microtech.aidexx.views.dialog.Dialogs
import com.microtech.aidexx.views.dialog.lib.WaitDialog
import com.yalantis.ucrop.UCrop
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Calendar

fun getWelfareCenterUrl(): String {
    val token = MmkvManager.getToken()
    val userId = UserInfoManager.instance().userId()
    return "${BuildConfig.welfareCenterUrl}?token=${token}&userId=${userId}"
}
class SettingActivity : BaseActivity<BaseViewModel, ActivitySettingBinding>() {

    private val REQUEST_CODE_GALLERY = 0x10// 图库选取图片标识请求码
    private val units = listOf(UnitManager.GlucoseUnit.MMOL_PER_L.text, UnitManager.GlucoseUnit.MG_PER_DL.text)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        initView()
    }

    override fun onResume() {
        super.onResume()
        val default = TransmitterManager.instance().getDefault()
        binding.apply {
            settingTrans.setValue(default?.entity?.deviceSn ?: "")
            userName.text = UserInfoManager.instance().getDisplayName()

            UserInfoManager.instance().userEntity?.apply {
                when (gender) {
                    1 -> ivSex.setImageDrawable(
                        AppCompatResources.getDrawable(
                            this@SettingActivity, R.drawable.ic_male)
                    )
                    2 -> ivSex.setImageDrawable(
                        AppCompatResources.getDrawable(
                            this@SettingActivity,R.drawable.ic_female)
                    )
                    else -> ivSex.setImageDrawable(null)
                }
            }
        }

    }

    private fun initView() {

        loadAvatar()

        binding.apply {
            ivSettingBack.setDebounceClickListener { finish() }

            ivSettingAvatar.setDebounceClickListener {

                PermissionsUtil.checkAndRequestPermissions(this@SettingActivity, PermissionGroups.Storage) {
                    val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    startActivityForResult(intent, REQUEST_CODE_GALLERY)
                }
            }

            settingEditProfileLl.setDebounceClickListener {
                ActivityUtil.toActivity(this@SettingActivity, ProfileSettingsActivity::class.java)
            }

            tvWelfare.setDebounceClickListener {
                WebActivity.loadWeb(
                    context = this@SettingActivity,
                    url = getWelfareCenterUrl(),
                    fullScreen = true,
                    from = "welfare_center"
                )
            }
            tvHelp.setDebounceClickListener {
                WebActivity.loadWeb(
                    this@SettingActivity,
                    getString(R.string.help_center), "https://aidexhelp.pancares.com/h5", true, "help_center"
                )
            }
            settingTrans.setDebounceClickListener {
                startActivity(Intent(this@SettingActivity, TransmitterActivity::class.java))
            }
            settingAbout.setDebounceClickListener {
                startActivity(Intent(this@SettingActivity, AboutActivity::class.java))
            }
            settingAlert.setDebounceClickListener {
                startActivity(Intent(this@SettingActivity, AlertSettingsActivity::class.java))
            }
            tvShare.setDebounceClickListener {
                startActivity(Intent(this@SettingActivity, ShareFollowActivity::class.java))
            }
            clSettingHeader.background =
                ContextCompat.getDrawable(
                    this@SettingActivity, if (ThemeManager.isLight())
                        R.drawable.bg_setting_header_light else R.drawable.bg_setting_header_dark
                )
            settingTrans.setDebounceClickListener {
                startActivity(Intent(this@SettingActivity, TransmitterActivity::class.java))
            }
            settingAlert.setDebounceClickListener {
                startActivity(Intent(this@SettingActivity, AlertSettingsActivity::class.java))
            }
            settingUnit.setValue(UnitManager.glucoseUnit.text)
            settingUnit.setDebounceClickListener {
                Dialogs.Picker(this@SettingActivity).singlePick(units, UnitManager.glucoseUnit.index) {
                    settingUnit.setValue(units[it])
                    UnitManager.glucoseUnit = UnitManager.getUnitByIndex(it)
                    EventBusManager.send(EventBusKey.EVENT_HYP_CHANGE, true)
                }
            }
            val themes = listOf(getString(R.string.theme_dark), getString(R.string.theme_light))
            settingTheme.setValue(themes[ThemeManager.theme.index])
            settingTheme.setDebounceClickListener {
                Dialogs.Picker(this@SettingActivity).singlePick(themes, ThemeManager.theme.index) {
                    if (it == ThemeManager.theme.index) {
                        return@singlePick
                    }
                    settingTheme.setValue(themes[it])
                    ThemeManager.theme = ThemeManager.themeByIndex(it)
                    ThemeManager.themeConfig()
                    for (activity in AidexxApp.instance.activityStack) {
                        activity?.recreate()
                    }
                }
            }

            settingPermission.setDebounceClickListener {
                ActivityUtil.toActivity(this@SettingActivity, PermissionCheckActivity::class.java)
            }

            lifecycleScope.launch {
                settingLanguage.setValue(
                    LanguageResourceManager.getCurLanguageConfEntity()?.name
                        ?: LanguageResourceManager.getCurLanguageTag()
                )
            }
            settingLanguage.setDebounceClickListener {
                lifecycleScope.launch {

                    withContext(Dispatchers.IO) {
                        LanguageResourceManager.getSupportLanguages()
                    }.let { supportLanguages ->

                        val languageStrList = supportLanguages.fold(mutableListOf<String>()) { list, conf ->
                            conf.langId?.let {
                                list.add(conf.name ?: it)
                            }
                            list
                        }
                        Dialogs.Picker(this@SettingActivity).singlePick(
                            languageStrList,
                            languageStrList.indexOf(LanguageResourceManager.getCurLanguageTag()) ) {

                            lifecycleScope.launch {
                                withContext(Dispatchers.IO) {
                                    LanguageResourceManager.onLanguageChanged(supportLanguages[it])
                                }
                                settingLanguage.setValue(languageStrList[it])

                                ActivityUtil.toActivity(this@SettingActivity, WelcomeActivity::class.java)
                                for (activity in AidexxApp.instance.activityStack) {
                                    activity?.finish()
                                }
                            }
                        }
                    }

                }
            }

            settingOther.setDebounceClickListener {
                startActivity(Intent(this@SettingActivity, OtherSettingActivity::class.java))
            }
            settingAbout.setDebounceClickListener {
                startActivity(Intent(this@SettingActivity, AboutActivity::class.java))
            }

            txtVersion.text = buildString {
                append("Version ")
                append(BuildConfig.VERSION_NAME)
            }
            txtTrademark.text = buildString {
                append("Copyright ©2011-")
                append(Calendar.getInstance().get(Calendar.YEAR))
            }

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {// 操作成功了
            when (requestCode) {
                REQUEST_CODE_GALLERY -> {
                    val imageUri = genAvatarFile()
                    imageUri?.let {
                        val options = UCrop.Options()
                        options.setCompressionQuality(100)
                        options.setActiveControlsWidgetColor(getColor(R.color.green_65))
                        options.setLogoColor(getColor(R.color.green_65))
                        UCrop.of(data?.data!!, it)
                            .withAspectRatio(1f, 1f)
                            .withMaxResultSize(200, 200)
                            .withOptions(options)
                            .start(this)
                    }
                }

                UCrop.REQUEST_CROP -> {
                    data?.let {
                        val resultUri = UCrop.getOutput(data)
                        LogUtil.d("剪裁结果 : $resultUri")
                        resultUri?.let {
                            WaitDialog.show(this@SettingActivity, getString(R.string.loading))
                            AidexxApp.instance.ioScope.launch {
                                when (val ret = AccountRepository.userUploadAvatar(resultUri)) {
                                    is ApiResult.Success -> {
                                        ret.result.data?.let {
                                            when (val updateRet =
                                                AccountRepository.updateUserInformation(avatar = it)
                                            ) {
                                                is ApiResult.Success -> {
                                                    // 更新本地数据
                                                    UserInfoManager.instance().updateProfile(avatar = it)
                                                    delay(1000) // 停顿1s再加载图片 否则可能图片链接报404 服务端小周说的
                                                    withContext(Dispatchers.Main) {
                                                        if (!isFinishing) {
                                                            loadAvatar()
                                                        }
                                                        WaitDialog.dismiss()
                                                    }
                                                }

                                                is ApiResult.Failure -> {
                                                    Dialogs.dismissWait()
                                                    (updateRet.msg.ifEmpty { null }
                                                        ?: getString(R.string.failure)).toast()
                                                }
                                            }
                                        } ?: getString(R.string.failure).toast()
                                    }

                                    is ApiResult.Failure -> {
                                        Dialogs.dismissWait()
                                        (ret.msg.ifEmpty { null }
                                            ?: getString(R.string.failure)).toast()
                                    }
                                }
                            }
                        } ?: getString(R.string.failure).toast()
                    }
                }
            }
        } else if (requestCode == UCrop.REQUEST_CROP && resultCode == UCrop.RESULT_ERROR) {
            data?.let {
                val cropError = UCrop.getError(it)
                LogUtil.xLogE("图片裁剪出错：$cropError")
            }
        }
    }

    private fun loadAvatar() {
        Glide.with(this@SettingActivity)
            .load(UserInfoManager.instance().userEntity?.avatar)
            .error(R.drawable.ic_default_avatar)
            .apply(RequestOptions.circleCropTransform())
            .into(binding.ivSettingAvatar)
    }
    private fun genAvatarFile(): Uri {
        val dirPath = FileUtils.getDownloadDir("avatar")
        val avatarFileName = "avatar.jpg"
        val file = File(dirPath, avatarFileName)
        FileUtils.delete(file)
        return Uri.fromFile(file)
    }

    override fun getViewBinding(): ActivitySettingBinding {
        return ActivitySettingBinding.inflate(layoutInflater)
    }
}