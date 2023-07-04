package com.microtech.aidexx.ui.main.trend.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.microtech.aidexx.databinding.LayoutExpandableGridBinding
import com.microtech.aidexx.ui.main.trend.MultiDateAdapter
import com.microtech.aidexx.ui.main.trend.MultiDayBgItem
import com.microtech.aidexx.ui.main.trend.maxShowDefault
import com.microtech.aidexx.utils.ThemeManager
import com.microtech.aidexx.R

const val STATE_CONTRACT = 1
const val STATE_EXPAND = 2
const val SELECT_ALL = 3
const val NOT_SELECT_ALL = 4

class ExpandableGrid : ConstraintLayout {

    private var currentState = STATE_CONTRACT
    private var currentDataState = NOT_SELECT_ALL
    private var multiDateAdapter: MultiDateAdapter? = null
    var onDataChange: ((list: MutableList<MultiDayBgItem>) -> Unit)? = null
    lateinit var vb: LayoutExpandableGridBinding

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(context)
    }

    fun init(context: Context) {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val inflate = inflater.inflate(R.layout.layout_expandable_grid, this, true)
        vb = LayoutExpandableGridBinding.bind(inflate)
        val flexboxLayoutManager = GridLayoutManager(context, 3)
//        flexboxLayoutManager.flexDirection = FlexDirection.ROW
//        flexboxLayoutManager.flexWrap = FlexWrap.WRAP
//        flexboxLayoutManager.justifyContent = JustifyContent.FLEX_START
//        flexboxLayoutManager.alignItems = AlignItems.STRETCH
        vb.rvDates.layoutManager = flexboxLayoutManager
        multiDateAdapter = MultiDateAdapter(context)
        vb.rvDates.adapter = multiDateAdapter
        vb.stateSwitch.setOnClickListener {
            if (currentState == STATE_CONTRACT) {
                multiDateAdapter?.expand()
                currentState = STATE_EXPAND
                vb.stateSwitch.rotation = 0f
            } else {
                executeContract(context)
            }
        }
        multiDateAdapter?.onDataSetChange =
            { mutableList: MutableList<MultiDayBgItem>, b: Boolean ->
                onDataChange?.invoke(mutableList)
                if (b) {
                    switchOn(context)
                } else {
                    switchOff(context)
                }
            }
        vb.btSelectAll.background = ContextCompat.getDrawable(context, R.drawable.bg_bt_unchecked)
        vb.btSelectAll.setOnClickListener {
            if (currentDataState == NOT_SELECT_ALL) {
                switchOn(context)
                multiDateAdapter?.selectAll()
            } else {
                switchOff(context)
                multiDateAdapter?.unselectAll()
            }
        }
    }

    private fun executeContract(context: Context) {
        multiDateAdapter?.contract()
        currentState = STATE_CONTRACT
        vb.stateSwitch.rotation = 180f
    }

    private fun switchOff(context: Context) {
        vb.btSelectAll.setTextColor(
            ContextCompat.getColor(
                context,
                if (ThemeManager.isLight()) R.color.light_colorAccent else R.color.colorAccent
            )
        )
        vb.btSelectAll.background =
            ContextCompat.getDrawable(context, R.drawable.bg_bt_unchecked)
        currentDataState = NOT_SELECT_ALL
    }

    private fun switchOn(context: Context) {
        vb.btSelectAll.setTextColor(ContextCompat.getColor(context, R.color.white))
        vb.btSelectAll.background =
            ContextCompat.getDrawable(context, R.drawable.bg_bt_checked)
        currentDataState = SELECT_ALL
    }

    fun getDataSet(): MutableList<MultiDayBgItem> {
        return multiDateAdapter?.getDataSet() ?: mutableListOf()
    }

    fun refreshData(list: MutableList<MultiDayBgItem>) {
        val set = list.filter { !it.checked }
        if (set.isEmpty()) {
            switchOn(context)
        } else {
            switchOff(context)
        }
        multiDateAdapter?.refreshData(list)
        multiDateAdapter?.selectDefault()
        if (list.size <= maxShowDefault) {
            vb.stateSwitch.visibility = GONE
            vb.edgeWithoutArrow.visibility = VISIBLE
        } else {
            vb.stateSwitch.visibility = VISIBLE
            vb.edgeWithoutArrow.visibility = GONE
        }
        executeContract(context)
    }
}