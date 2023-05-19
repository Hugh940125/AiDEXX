package com.microtech.aidexx.ui.pair

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.View
import android.view.View.OnClickListener
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.microtech.aidexx.AidexxApp
import com.microtech.aidexx.R
import com.microtech.aidexx.base.BaseActivity
import com.microtech.aidexx.base.BaseViewModel
import com.microtech.aidexx.ble.AidexBleAdapter
import com.microtech.aidexx.ble.MessageDistributor
import com.microtech.aidexx.ble.device.TransmitterManager
import com.microtech.aidexx.ble.device.model.X_NAME
import com.microtech.aidexx.databinding.ActivityTransmitterBinding
import com.microtech.aidexx.db.ObjectBox
import com.microtech.aidexx.db.entity.TYPE_G7
import com.microtech.aidexx.db.entity.TYPE_X
import com.microtech.aidexx.db.entity.TransmitterEntity
import com.microtech.aidexx.utils.ToastUtil
import com.microtech.aidexx.utils.eventbus.EventBusKey
import com.microtech.aidexx.utils.eventbus.EventBusManager
import com.microtech.aidexx.widget.dialog.Dialogs
import com.microtechmd.blecomm.controller.BleControllerInfo
import io.objectbox.reactive.DataObserver
import io.objectbox.reactive.DataSubscription
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.ref.WeakReference

private const val DISMISS_LOADING = 2004

const val OPERATION_TYPE_PAIR: Int = 1
const val OPERATION_TYPE_UNPAIR: Int = 2
const val OPERATION_TYPE = "type"
const val BLE_INFO = "info"

class TransmitterActivity : BaseActivity<BaseViewModel, ActivityTransmitterBinding>(),
    OnClickListener {
    private var needSetMessageCallback = true
    private var scanStarted = false
    private lateinit var rotateAnimation: RotateAnimation
    private var subscription: DataSubscription? = null
    private lateinit var transmitterHandler: TransmitterHandler
    private lateinit var transmitterAdapter: TransmitterAdapter
    private var transmitter: TransmitterEntity? = null
    private lateinit var transmitterList: MutableList<BleControllerInfo>

    class TransmitterHandler(val activity: TransmitterActivity) : Handler(Looper.getMainLooper()) {
        private val reference = WeakReference(activity)
        override fun handleMessage(msg: Message) {
            reference.get()?.let {
                if (!it.isFinishing) {
                    when (msg.what) {
                        DISMISS_LOADING -> {
                            Dialogs.dismissWait()
                        }
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        AidexxApp.isPairing = true
        transmitterList = mutableListOf()
        transmitterHandler = TransmitterHandler(this)
        loadSavedTransmitter()
        initAnim()
        initView()
        initEvent()
    }

    private fun initEvent() {
        EventBusManager.onReceive<Boolean>(
            EventBusKey.EVENT_PAIR_RESULT,
            this
        ) {
            transmitterHandler.removeMessages(DISMISS_LOADING)
            if (it) {
                if (window.decorView.visibility == View.VISIBLE) {
                    Dialogs.showSuccess(getString(R.string.Pairing_Succeed))
                }
            } else {
                TransmitterManager.instance().removeDefault()
            }
        }
    }

    private fun onDeviceDiscover() {
        AidexBleAdapter.getInstance().onDeviceDiscover = {
            if (needSetMessageCallback) {
                PairUtil.observeMessage(this, lifecycleScope)
                needSetMessageCallback = false
            }
            if (it.name.contains(X_NAME)) {
                val address = it.address
                if ((transmitter == null || address != transmitter?.deviceMac)
                    && !transmitterList.contains(it)
                ) {
                    transmitterList.add(it)
                }
                transmitterAdapter.setList(transmitterList)
            }
        }
    }

    private fun initAnim() {
        rotateAnimation =
            RotateAnimation(
                0f, 360f, RotateAnimation.RELATIVE_TO_SELF,
                0.5f, RotateAnimation.RELATIVE_TO_SELF, 0.5f
            )
        rotateAnimation.fillAfter = true
        rotateAnimation.interpolator = LinearInterpolator()
        rotateAnimation.repeatCount = Animation.INFINITE
        rotateAnimation.repeatMode = Animation.RESTART
        rotateAnimation.duration = 1500
        binding.ivRefreshScan.startAnimation(rotateAnimation)
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
    }

    private fun initView() {
        binding.actionbarTransmitter.getLeftIcon().setOnClickListener { finish() }
        binding.rvOtherTrans.layoutManager = LinearLayoutManager(this)
        transmitterAdapter = TransmitterAdapter()
        transmitterAdapter.onPairClick = {
            checkEnvironment {
                transmitter?.let {
                    when (it.deviceType) {
                        TYPE_G7 -> {
                            ToastUtil.showLong("请先解配存在的设备")
                            return@checkEnvironment
                        }
                        TYPE_X -> {

                        }
                    }
                }
                PairUtil.startPair(this@TransmitterActivity, it)
            }
        }
        binding.layoutMyTrans.transItem.setOnClickListener(this)
        binding.rvOtherTrans.adapter = transmitterAdapter
        Dialogs.showWait(getString(R.string.loading))
        transmitterHandler.sendEmptyMessageDelayed(DISMISS_LOADING, 3 * 1000)
    }

    override fun onDestroy() {
        super.onDestroy()
        rotateAnimation.cancel()
        MessageDistributor.instance().removeObserver()
        binding.ivRefreshScan.clearAnimation()
        AidexxApp.isPairing = false
        if ((transmitter == null || transmitter?.accessId == null) && scanStarted) {
            AidexBleAdapter.getInstance().stopBtScan(false)
        }
        transmitterHandler.removeMessages(DISMISS_LOADING)
        subscription?.cancel()
        AidexBleAdapter.getInstance().removeDiscoverCallback()
    }

    override fun getViewBinding(): ActivityTransmitterBinding {
        return ActivityTransmitterBinding.inflate(layoutInflater)
    }

    private fun loadSavedTransmitter() {
        val observer = DataObserver<Class<TransmitterEntity>> { refreshMine() }
        subscription = ObjectBox.store.subscribe(TransmitterEntity::class.java).observer(observer)
    }

    private fun refreshMine() {
        lifecycleScope.launch {
            transmitterList.clear()
            withContext(Dispatchers.IO) {
                transmitter = TransmitterManager.instance().getDefault()?.entity
            }
            if (transmitter == null) {
                binding.tvPlsSelectTrans.visibility = View.VISIBLE
                binding.layoutMyTrans.root.visibility = View.GONE
            } else {
                binding.tvPlsSelectTrans.visibility = View.GONE
                binding.layoutMyTrans.root.visibility = View.VISIBLE
                binding.layoutMyTrans.tvSn.text = transmitter!!.deviceName
                binding.layoutMyTrans.tvTransPairState.visibility = View.VISIBLE
                if (transmitter!!.accessId == null) {
                    binding.layoutMyTrans.tvTransPairState.text = "未配对"
                } else {
                    binding.layoutMyTrans.tvTransPairState.text = "已配对"
                }
            }
            if (transmitter == null || transmitter?.accessId == null) {
                AidexBleAdapter.getInstance().startBtScan(true)
                scanStarted = true
            }
            onDeviceDiscover()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1) {
            PairUtil.observeMessage(this, lifecycleScope)
        }
    }

    override fun onClick(v: View?) {
        when (v) {
            binding.layoutMyTrans.transItem -> {
                MessageDistributor.instance().removeObserver()
                val intent = Intent(this, TransOperationActivity::class.java)
                intent.putExtra(
                    BLE_INFO,
                    BleControllerInfo(
                        transmitter?.deviceMac,
                        transmitter?.deviceName,
                        transmitter?.deviceSn,
                        130
                    )
                )
                if (transmitter!!.accessId == null) {
                    intent.putExtra(OPERATION_TYPE, OPERATION_TYPE_PAIR)
                } else {
                    intent.putExtra(OPERATION_TYPE, OPERATION_TYPE_UNPAIR)
                }
                startActivityForResult(intent, 1)
            }
        }
    }
}