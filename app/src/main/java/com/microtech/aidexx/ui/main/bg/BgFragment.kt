package com.microtech.aidexx.ui.main.bg

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.flexbox.AlignItems
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.microtech.aidexx.R
import com.microtech.aidexx.base.BaseFragment
import com.microtech.aidexx.base.BaseViewModel
import com.microtech.aidexx.ble.MessageDistributor
import com.microtech.aidexx.ble.MessageObserver
import com.microtech.aidexx.ble.device.TransmitterManager
import com.microtech.aidexx.ble.device.entity.CalibrationInfo
import com.microtech.aidexx.ble.device.model.DeviceModel
import com.microtech.aidexx.common.date2ymdhm
import com.microtech.aidexx.common.toColor
import com.microtech.aidexx.common.user.UserInfoManager
import com.microtech.aidexx.databinding.FragmentBgBinding
import com.microtech.aidexx.db.ObjectBox
import com.microtech.aidexx.db.entity.BloodGlucoseEntity
import com.microtech.aidexx.ui.main.bg.history.BloodGlucoseHistoryActivity
import com.microtech.aidexx.utils.ThemeManager
import com.microtech.aidexx.utils.TimeUtils
import com.microtech.aidexx.utils.UnitManager
import com.microtech.aidexx.utils.eventbus.BgDataChangedInfo
import com.microtech.aidexx.utils.eventbus.DataChangedType
import com.microtech.aidexx.utils.eventbus.EventBusKey
import com.microtech.aidexx.utils.eventbus.EventBusManager
import com.microtech.aidexx.widget.dialog.Dialogs
import com.microtech.aidexx.widget.dialog.lib.util.fromGlucoseValue
import com.microtech.aidexx.widget.dialog.lib.util.toGlucoseStringWithUnit
import com.microtech.aidexx.widget.dialog.standard.StandardDialog
import com.microtech.aidexx.widget.selector.time.TimePicker
import com.microtechmd.blecomm.constant.AidexXOperation
import com.microtechmd.blecomm.entity.BleMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

private const val ANTI_FAST_RESUME = 1

class BgFragment : BaseFragment<BaseViewModel, FragmentBgBinding>(), View.OnClickListener {
    private var minGlucose: Double = 0.0
    private var maxGlucose: Double = 0.0
    private var inputHint: String = ""
    private var timeSlot: Int? = null
    private lateinit var selectDate: Date
    private var defaultMode: DeviceModel? = null
    private var calibrationAllowed: Boolean = false
    private var timeSlopAdapter: TimeSlopAdapter? = null
    private var appColorAccent: Int = 0
    private var buttonPressColor: Int = 0
    private val mObserver = object : MessageObserver {
        override fun onMessage(message: BleMessage) {
            when (message.operation) {
                AidexXOperation.DISCONNECT -> {
                    Dialogs.dismissWait()
                }
                AidexXOperation.DISCOVER -> {
                    if (!message.isSuccess) {
                        Dialogs.showError(getString(R.string.Search_Timeout))
                        MessageDistributor.instance().removeObserver(this)
                    }
                }
                AidexXOperation.CONNECT -> {
                    if (!message.isSuccess) {
                        Dialogs.showError(getString(R.string.Connecting_Failed))
                        MessageDistributor.instance().removeObserver(this)
                    }
                }
                AidexXOperation.SET_CALIBRATION -> {
                    if (message.isSuccess) {
                        Dialogs.showSuccess(getString(R.string.calibration_success))
                        binding.etGlucoseValue.setText("")
                    } else {
                        Dialogs.showSuccess(getString(R.string.calibration_fail))
                    }
                    MessageDistributor.instance().removeObserver(this)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appColorAccent = ThemeManager.getTypeValue(requireContext(), R.attr.appColorAccent)
        buttonPressColor = ThemeManager.getTypeValue(requireContext(), R.attr.buttonPressColor)
    }

    override fun onResume() {
        super.onResume()
        defaultMode = TransmitterManager.instance().getDefault()
        defaultMode?.onCalibrationPermitChange = {
            calibrationAllowed = it
        }
        calibrationAllowed = defaultMode?.isAllowCalibration() == true
        refreshView()
        throttle.emit(3000, ANTI_FAST_RESUME) {
            updateLastRecord()
        }
    }

    private fun refreshView() {
        refreshBtnState()
        binding.tvTime.text = TimeUtils.currentDate.date2ymdhm()
        selectDate = TimeUtils.currentDate
        binding.tvGlucoseUnit.text = UnitManager.glucoseUnit.text
    }

    private fun refreshBtnState() {
        binding.buttonCalibration.setNormalBackgroundColor(
            if (calibrationAllowed) appColorAccent
            else R.color.button_unclick_color.toColor(requireContext())
        )
        binding.buttonCalibration.setPressedBackgroundColor(
            if (calibrationAllowed) buttonPressColor
            else R.color.button_unclick_color.toColor(requireContext())
        )
        binding.buttonCalibration.setTextColor(
            if (calibrationAllowed) R.color.white.toColor(requireContext())
            else R.color.whiteAlpha30.toColor(requireContext())
        )
    }

    override fun onPause() {
        super.onPause()
        timeSlopAdapter?.clearCheck()
        MessageDistributor.instance().removeObserver(mObserver)
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBgBinding.inflate(layoutInflater)
        initView()
        return binding.root
    }

    private fun initView() {
        binding.buttonCalibration.setOnClickListener(this)
        binding.buttonRecord.setOnClickListener(this)
        initGlucoseValueEditor()
        val layoutManager = FlexboxLayoutManager(context)
        layoutManager.justifyContent = JustifyContent.FLEX_START
        layoutManager.alignItems = AlignItems.CENTER
        binding.rvTimeSlop.layoutManager = layoutManager
        timeSlopAdapter = context?.let { TimeSlopAdapter(it) }
        timeSlopAdapter?.onSelect = {
            timeSlot = it
        }
        binding.rvTimeSlop.adapter = timeSlopAdapter
        binding.tvTime.setOnClickListener {
            TimePicker(requireContext()).pick {
                selectDate = it
                binding.tvTime.text = it.date2ymdhm()
            }
        }
        binding.tvMoreHistory.setOnClickListener {
            startActivity(Intent(requireContext(), BloodGlucoseHistoryActivity::class.java))
        }
    }

    private fun updateLastRecord() {
        lifecycleScope.launch {
            val lastGlucoseRecord: BloodGlucoseEntity?
            withContext(Dispatchers.IO) {
                lastGlucoseRecord = BgRepositoryApi.getLastGlucoseHistory()
            }
            if (null == lastGlucoseRecord) {
                binding.tvMoreHistory.visibility = View.INVISIBLE
                binding.llBgRecode.llContainer.visibility = View.INVISIBLE
                binding.tvNoneRecord.visibility = View.VISIBLE
            } else {
                binding.tvMoreHistory.visibility = View.VISIBLE
                binding.llBgRecode.apply {
                    llContainer.visibility = View.VISIBLE
                    tvGlucoseTime.text = lastGlucoseRecord.testTime.date2ymdhm()
                    var tagText = lastGlucoseRecord.getTagText(requireContext().resources)
                    if (tagText.isNullOrEmpty()) {
                        tagText = "— —"
                    }
                    tvGlucoseDescribe.text = tagText
                    tvGlucoseRecordValue.text =
                        lastGlucoseRecord.bloodGlucose.toGlucoseStringWithUnit()
                    glucoseHistoryDivider.visibility = View.GONE
                }
                binding.tvNoneRecord.visibility = View.GONE
            }
        }
    }

    private fun initGlucoseValueEditor() {
        binding.etGlucoseValue.onFocusChangeListener =
            View.OnFocusChangeListener { view, hasFocus ->
                if (!hasFocus) {
                    val manager: InputMethodManager =
                        requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    manager.hideSoftInputFromWindow(
                        view.windowToken,
                        InputMethodManager.HIDE_NOT_ALWAYS
                    )
                }
            }
        if (UnitManager.glucoseUnit == UnitManager.GlucoseUnit.MMOL_PER_L) {
            minGlucose = 0.6
            maxGlucose = 33.3
        } else {
            minGlucose = 10.0
            maxGlucose = 600.0
        }
        val minGlucoseString =
            BigDecimal(minGlucose.toString()).stripTrailingZeros().toPlainString()
        val maxGlucoseString =
            BigDecimal(maxGlucose.toString()).stripTrailingZeros().toPlainString()
        inputHint = String.format(
            Locale.getDefault(),
            getString(R.string.input_glucose_value),
            minGlucoseString,
            maxGlucoseString
        )
        binding.etGlucoseValue.hint = inputHint
        binding.etGlucoseValue.filters = arrayOf(GlucoseInputFilter().apply {
            isIntOnly = UnitManager.glucoseUnit != UnitManager.GlucoseUnit.MMOL_PER_L
        })
        binding.etGlucoseValue.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (start >= 0) {//从一输入就开始判断，
                    try {
                        val num = s.toString().toFloatOrNull()
                        //判断当前edittext中的数字(可能一开始Edittext中有数字)是否大于max
                        num?.apply {
                            if (num > maxGlucose) {
                                if (UnitManager.glucoseUnit == UnitManager.GlucoseUnit.MMOL_PER_L) {
                                    binding.etGlucoseValue.setText(maxGlucose.toString())
                                    binding.etGlucoseValue.setSelection(maxGlucose.toString().length)
                                } else {
                                    binding.etGlucoseValue.setText(maxGlucose.toInt().toString())
                                    binding.etGlucoseValue.setSelection(
                                        maxGlucose.toInt().toString().length
                                    )
                                }
                            }
                        }

                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    //edittext中的数字在max和min之间，则不做处理，正常显示即可。
                    return
                }
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })
    }

    companion object {
        @JvmStatic
        fun newInstance() = BgFragment()
    }

    override fun onClick(v: View?) {
        when (v) {
            binding.buttonCalibration -> {
                val model = TransmitterManager.instance().getDefault()
                if (model != null && model.isPaired()) {
                    if (calibrationAllowed) {
                        val glucoseValue = binding.etGlucoseValue.text.toString().toFloatOrNull()
                        if (!isBgExpired() && isBgFilled(glucoseValue)) {
                            Dialogs.showWhether(
                                requireContext(), content = String.format(
                                    getString(R.string.content_format_calibrate),
                                    "$glucoseValue " + UnitManager.glucoseUnit.text
                                ), confirm = {
                                    Dialogs.showWait()
                                    val value =
                                        (if (UnitManager.glucoseUnit == UnitManager.GlucoseUnit.MMOL_PER_L)
                                            (glucoseValue!! * 18)
                                        else glucoseValue!!).roundToInt()
                                    MessageDistributor.instance().observer(mObserver)
                                    model.calibration(
                                        CalibrationInfo(
                                            value,
                                            model.latestHistory!!.timeOffset
                                        )
                                    )
                                })
                        }
                    }
                } else {
                    Dialogs.showMessage(requireContext(), content = getString(R.string.bg_pair))
                }
            }
            binding.buttonRecord -> {
                val glucoseValue = binding.etGlucoseValue.text.toString().toFloatOrNull()
                if (null == glucoseValue || glucoseValue > maxGlucose || glucoseValue < minGlucose) {
                    Dialogs.showMessage(
                        activity as AppCompatActivity,
                        inputHint, null
                    )
                    return
                }
                val recordGlucose = glucoseValue.fromGlucoseValue()
                val bgEntity = BloodGlucoseEntity(selectDate, recordGlucose)
                timeSlot?.let {
                    bgEntity.testTag = it
                }
                bgEntity.authorizationId = UserInfoManager.instance().userId()
                ObjectBox.runAsync({
                    ObjectBox.bgHistoryBox!!.put(bgEntity)
                }, {
                    Handler(Looper.getMainLooper()).post {
                        StandardDialog.Setter(context)
                            .content(context?.getString(R.string.save_complete))
                            .setPositive { dialog, _ ->
                                dialog.dismiss()
                                binding.etGlucoseValue.text?.clear()
                                timeSlot = null
                                updateLastRecord()

                                EventBusManager.send(EventBusKey.EVENT_BG_DATA_CHANGED,
                                    BgDataChangedInfo(DataChangedType.ADD, listOf(bgEntity)))

                            }
                            .setOnDismissListener { updateLastRecord() }
                            ?.create(0)?.show()
                    }
                })
            }
        }
    }

    private fun isBgExpired(): Boolean {
        if (TimeUtils.currentTimeMillis - selectDate.time > 5 * 60 * 1000) {
            Dialogs.showMessage(
                requireContext(),
                content = getString(R.string.calibration_with_in_notice)
            )
            return true
        }
        return false
    }

    private fun isBgFilled(glucoseValue: Float?): Boolean {
        if (null == glucoseValue) {
            Dialogs.showMessage(requireContext(), content = binding.etGlucoseValue.hint.toString())
            return false
        }
        return true
    }
}