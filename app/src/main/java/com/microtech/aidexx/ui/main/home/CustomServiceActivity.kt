package com.microtech.aidexx.ui.main.home

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.ValueCallback
import com.microtech.aidexx.R
import com.microtech.aidexx.base.BaseActivity
import com.microtech.aidexx.base.BaseViewModel
import com.microtech.aidexx.databinding.ActivityCustomServiceBinding


private const val SERVICE_URL =
    "http://kf.microtechmd.com/api/mobileweb/home?channel_id=35267&channel_key=352675f3u&wechatapp_id=349445&key=79201r2ok"

class CustomServiceActivity : BaseActivity<BaseViewModel, ActivityCustomServiceBinding>() {
    private var uploadMessage: ValueCallback<Uri>? = null
    private var valueCallback: ValueCallback<Array<Uri>>? = null
    private val fileChooserResultCode = 10000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.actionBar.getLeftIcon().setOnClickListener {
            finish()
        }
        binding.actionBar.setTitle(getString(R.string.online_service))
        //TODO 需要完善
//        val mobile = StringUtils.instance()
//            .getPrivacyPhone(UserInfoManager.instance().getPhone())
//        val nickName = UserInfoManager.instance().getNickName()
//        val surName = UserInfoManager.instance().getSurName()
//        val givenName = UserInfoManager.instance().getGivenName()
//        val userId = UserInfoManager.instance().getUserId()
//        val defaultModel = TransmitterManager.instance().getDefaultModel()
//        val name =
//            if (nickName.isNotEmpty()) nickName
//            else if (surName.isNotEmpty() && givenName.isNotEmpty())
//                "${surName}${givenName}" else mobile
//        val deviceSn = defaultModel?.entity?.deviceSn
//        binding.webCustomerService.loadUrl("$SERVICE_URL&mobile=$mobile&nickName=${name}&openid=${userId}&sn=$deviceSn&SN=$deviceSn")
    }

    override fun getViewBinding(): ActivityCustomServiceBinding {
        return ActivityCustomServiceBinding.inflate(layoutInflater)
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.webCustomerService.mWebView.destroy()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == fileChooserResultCode) { //处理返回的图片，并进行上传
            valueCallback = binding.webCustomerService.valueCallback
            valueCallback?.apply {
                onActivityResultAboveL(requestCode, resultCode, data)

            }
        }
    }


    private fun onActivityResultAboveL(requestCode: Int, resultCode: Int, intent: Intent?) {
        valueCallback = binding.webCustomerService.valueCallback
        if (requestCode != fileChooserResultCode || valueCallback == null) return
        var results: Array<Uri>? = null
        if (resultCode == RESULT_OK) {
            if (intent != null) {
                val dataString = intent.dataString
                val clipData = intent.clipData
                if (clipData != null) {
                    results = arrayOf()
                    for (i in 0 until clipData.itemCount) {
                        val item = clipData.getItemAt(i)
                        results.plus(item.uri)
                    }
                }
                if (dataString != null) {
                    (Uri.parse(dataString))?.apply {
                        results = arrayOf(this)
                    }
                }
            }
        }
        valueCallback?.apply {
            onReceiveValue(results)
        }
        binding.webCustomerService.valueCallback = null
        valueCallback = null
    }
}