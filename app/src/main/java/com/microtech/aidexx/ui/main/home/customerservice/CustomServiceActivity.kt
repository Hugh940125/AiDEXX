package com.microtech.aidexx.ui.main.home.customerservice

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.ValueCallback
import com.microtech.aidexx.R
import com.microtech.aidexx.base.BaseActivity
import com.microtech.aidexx.base.BaseViewModel
import com.microtech.aidexx.common.user.UserInfoManager
import com.microtech.aidexx.databinding.ActivityCustomServiceBinding
import com.microtech.aidexx.ui.main.MainActivity
import com.microtech.aidexx.utils.StringUtils

class CustomServiceActivity : BaseActivity<BaseViewModel, ActivityCustomServiceBinding>() {
    private var uploadMessage: ValueCallback<Uri>? = null
    private var uploadMessageAboveL: ValueCallback<Array<Uri>>? = null
    private val FILE_CHOOSER_RESULT_CODE = 10000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        clearMessage()

       binding.actionBar.getLeftIcon().setOnClickListener {
//            if (!ActivityUtils.isActivityExistsInStack(MainActivity::class.java)) {
//                startActivity(Intent(this@CustomServiceActivity, MainActivity::class.java))
//            }
//            finish()
        }
       binding.actionBar.setTitle(getString(R.string.online_service))
        val mobile = StringUtils.getPrivacyPhone(UserInfoManager.instance().getPhone())
//        val nickName = UserManager.instance().getNickName()
//        val surName = UserManager.instance().getSurName()
//        val givenName = UserManager.instance().getGivenName()
//        val userId = UserManager.instance().getUserId()
//        val defaultModel = TransmitterManager.instance().getDefaultModel()
//        val name =
//            if (nickName.isNotEmpty()) nickName
//            else if (surName.isNotEmpty() && givenName.isNotEmpty())
//                "${surName}${givenName}" else mobile
//        val deviceSn = defaultModel?.entity?.deviceSn
//       binding.webCustomerService.loadUrl("$SERVICE_URL&mobile=$mobile&nickName=${name}&openid=${userId}&sn=$deviceSn&SN=$deviceSn")
    }

    override fun getViewBinding(): ActivityCustomServiceBinding {
        return ActivityCustomServiceBinding.inflate(layoutInflater)
    }


    override fun onDestroy() {
        super.onDestroy()
        clearMessage()

//        if (!ActivityUtils.isActivityExistsInStack(MainActivity::class.java)) {
//            startActivity(Intent(this@CustomServiceActivity, MainActivity::class.java))
//        }
       binding.webCustomerService.mWebView.destroy()
    }

    private fun clearMessage() {
//        MMKV.defaultMMKV().encode(LocalPreference.LAST_CUSTOMER_TIME, System.currentTimeMillis())
//        MessageManager.instance().clearMessage()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == FILE_CHOOSER_RESULT_CODE) { //处理返回的图片，并进行上传
            uploadMessageAboveL =binding.webCustomerService.uploadMessageAboveL
            uploadMessageAboveL?.apply {
                onActivityResultAboveL(requestCode, resultCode, data)

            }
        }
    }


    private fun onActivityResultAboveL(requestCode: Int, resultCode: Int, intent: Intent?) {
        uploadMessageAboveL =binding.webCustomerService.uploadMessageAboveL
        if (requestCode != FILE_CHOOSER_RESULT_CODE || uploadMessageAboveL == null) return
        var results: Array<Uri>? = null
        if (resultCode == RESULT_OK) {
            if (intent != null) {
                val dataString = intent.dataString
                val clipData = intent.clipData
                if (clipData != null) {
                    results = arrayOf<Uri>()
                    for (i in 0 until clipData.itemCount) {
                        val item = clipData.getItemAt(i)
                        results.plus(item.uri)
                    }
                }
                if (dataString != null) {
                    (Uri.parse(dataString))?.apply {
                        results = arrayOf<Uri>(this)
                    }
                }
            }
        }
        uploadMessageAboveL?.apply {
            onReceiveValue(results)
        }
       binding.webCustomerService.uploadMessageAboveL = null
        uploadMessageAboveL = null
    }
}