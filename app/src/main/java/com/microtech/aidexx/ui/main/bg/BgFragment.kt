package com.microtech.aidexx.ui.main.bg

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.lifecycle.lifecycleScope
import com.google.android.flexbox.AlignItems
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.microtech.aidexx.R
import com.microtech.aidexx.base.BaseFragment
import com.microtech.aidexx.base.BaseViewModel
import com.microtech.aidexx.common.date2ymdhm
import com.microtech.aidexx.databinding.FragmentBgBinding
import com.microtech.aidexx.db.entity.BloodGlucoseEntity
import com.microtech.aidexx.utils.Throttle
import com.microtech.aidexx.utils.UnitManager
import com.microtech.aidexx.utils.clickFlow
import com.microtech.aidexx.utils.statusbar.Log
import com.microtech.aidexx.utils.throttle
import com.microtech.aidexx.widget.dialog.x.util.toGlucoseStringWithUnit
import com.microtech.aidexx.widget.selector.time.TimePicker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.util.*

class BgFragment : BaseFragment<BaseViewModel, FragmentBgBinding>() {
    private var timeSlot: Int? = null
    private var timeSlopAdapter: TimeSlopAdapter? = null
    private var selectDate: Date? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        refreshView()
        Throttle().input().throttle(3000).onEach {
            updateLastRecord()
        }.launchIn(lifecycleScope)
    }

    private fun refreshView() {
        binding.tvTime.text = Date().date2ymdhm()
        binding.tvGlucoseUnit.text = UnitManager.glucoseUnit.text
    }

    override fun onPause() {
        super.onPause()
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
    }

    private fun updateLastRecord() {
        lifecycleScope.launch {
            val lastGlucoseRecord: BloodGlucoseEntity?
            withContext(Dispatchers.IO) {
                lastGlucoseRecord = BgRepositoryApi.getLastGlucoseHistory()
            }
            if (null == lastGlucoseRecord) {
                binding.llBgRecode.llContainer.visibility = View.INVISIBLE
                binding.tvNoneRecord.visibility = View.VISIBLE
            } else {
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
        binding.etGlucoseValue.onFocusChangeListener = View.OnFocusChangeListener { view, hasFocus ->
            if (!hasFocus) {
                val manager: InputMethodManager =
                    requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                manager.hideSoftInputFromWindow(
                    view.windowToken,
                    InputMethodManager.HIDE_NOT_ALWAYS
                )
            }
        }
        val minGlucose: Double
        val maxGlucose: Double
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
        val hint = String.format(
            Locale.getDefault(),
            getString(R.string.input_glucose_value),
            minGlucoseString,
            maxGlucoseString
        )
        binding.etGlucoseValue.hint = hint
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
}