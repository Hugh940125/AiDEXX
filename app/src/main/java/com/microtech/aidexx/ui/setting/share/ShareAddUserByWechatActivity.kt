package com.microtech.aidexx.ui.setting.share

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.os.Message
import androidx.activity.viewModels
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.microtech.aidexx.AidexxApp
import com.microtech.aidexx.R
import com.microtech.aidexx.base.BaseActivity
import com.microtech.aidexx.base.BaseViewModel
import com.microtech.aidexx.common.toast
import com.microtech.aidexx.common.user.UserInfoManager
import com.microtech.aidexx.databinding.ActivityShareAddUserByWechatBinding
import com.microtech.aidexx.utils.LogUtil
import com.microtech.aidexx.utils.LogUtils
import com.microtech.aidexx.widget.dialog.lib.WaitDialog
import kotlinx.coroutines.launch
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


class ShareAddUserByWechatActivity :
    BaseActivity<BaseViewModel, ActivityShareAddUserByWechatBinding>() {
    private var base64ToBitmap: Bitmap? = null
    private var imgFile: File? = null
    private val sfViewModel: ShareFollowViewModel by viewModels()


    val handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            WaitDialog.dismiss()

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        initView()
    }

    fun base64ToBitmap(img: String): Bitmap? {
        try {
            val dec =
                android.util.Base64.decode(img.toByteArray(), android.util.Base64.NO_WRAP)
            return BitmapFactory.decodeByteArray(dec, 0, dec.size)
        } catch (e: Exception) {
            LogUtil.eAiDEX("${e.printStackTrace()}")
        }
        return null
    }

    private fun bitmapToFile(bitmap: Bitmap): File? {
        imgFile =
            File(AidexxApp.instance.getExternalFilesDir(Environment.DIRECTORY_PICTURES)?.absolutePath + File.separator + "qrcode.jpeg")
        try {
            val bos = BufferedOutputStream(FileOutputStream(imgFile))
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos)
            bos.flush()
            bos.close()
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }
        return imgFile
    }

    private fun initView() {
        binding.myQrCodeActionBar.getLeftIcon().setOnClickListener {
            finish()
        }
        val hashMap = HashMap<String, Any>()
        hashMap["deviceType"] = "Android"
        hashMap["deviceUid"] = UserInfoManager.instance().getPhone()
        hashMap["systemVersion"] = "unknown"
//        hashMap["timestamp"] = Date().dateToYmdhms()
        hashMap["operationType"] = 3

        WaitDialog.show(this, getString(R.string.loading))

        handler.sendEmptyMessageDelayed(1, 20 * 1000)

        // todo 接口
        lifecycleScope.launch {

            sfViewModel.getQrCodeToShareMySelf().collect {
                "接口还没提供".toast()
                WaitDialog.dismiss()
                handler.removeMessages(1)
//                info.img?.let {
//                    base64ToBitmap = base64ToBitmap(it)
//                    binding.ivQrCode.setImageBitmap(base64ToBitmap)
//                }
            }
        }

        binding.btnSendQrCode.setOnClickListener {
            base64ToBitmap?.let {
                val bitFile = bitmapToFile(it)
                bitFile?.let {
                    val pngUri: Uri = FileProvider.getUriForFile(
                        this,
                        this.packageName.toString() + ".FileProvider",
                        bitFile
                    )
                    val picMessageIntent = Intent(Intent.ACTION_SEND)
                    picMessageIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    picMessageIntent.type = "image/*"
                    picMessageIntent.putExtra(Intent.EXTRA_STREAM, pngUri)
                    startActivity(
                        Intent.createChooser(
                            picMessageIntent,
                            getString(R.string.title_share_file)
                        )
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        imgFile?.delete()
    }

    override fun getViewBinding(): ActivityShareAddUserByWechatBinding {
        return ActivityShareAddUserByWechatBinding.inflate(layoutInflater)
    }
}