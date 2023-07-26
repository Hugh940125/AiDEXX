package com.microtech.aidexx.ui.main.home.chart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.mikephil.charting.data.CombinedData
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.ScatterData
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.interfaces.datasets.IScatterDataSet
import com.microtech.aidexx.R
import com.microtech.aidexx.common.formatToYMdHm
import com.microtech.aidexx.common.getContext
import com.microtech.aidexx.common.user.UserInfoManager
import com.microtech.aidexx.db.entity.BaseEventEntity
import com.microtech.aidexx.db.entity.BloodGlucoseEntity
import com.microtech.aidexx.db.entity.CalibrateEntity
import com.microtech.aidexx.db.entity.RealCgmHistoryEntity
import com.microtech.aidexx.db.repository.CgmCalibBgRepository
import com.microtech.aidexx.db.repository.EventDbRepository
import com.microtech.aidexx.utils.CalibrateManager
import com.microtech.aidexx.utils.LogUtil
import com.microtech.aidexx.utils.ThresholdManager
import com.microtech.aidexx.utils.TimeUtils
import com.microtech.aidexx.utils.eventbus.DataChangedType
import com.microtech.aidexx.utils.eventbus.EventDataChangedInfo
import com.microtech.aidexx.utils.toGlucoseValue
import com.microtech.aidexx.views.chart.ChartUtil
import com.microtech.aidexx.views.chart.GlucoseChart
import com.microtech.aidexx.views.chart.GlucoseChart.Companion.CHART_LABEL_COUNT
import com.microtech.aidexx.views.chart.MyChart.ChartGranularityPerScreen
import com.microtech.aidexx.views.chart.MyChart.Companion.G_SIX_HOURS
import com.microtech.aidexx.views.chart.dataset.BgDataSet
import com.microtech.aidexx.views.chart.dataset.CalDataSet
import com.microtech.aidexx.views.chart.dataset.GlucoseDataSet
import com.microtech.aidexx.views.chart.dataset.IconDataSet
import com.microtech.aidexx.views.chart.dataset.toChartEntry
import com.microtech.aidexx.views.dialog.Dialogs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.math.abs

class ChartViewModel: ViewModel() {

    companion object {
        private const val TAG = "ChartViewModel"
    }

    // 国际版代码先放这里 后面做打包渠道配置
    private val isGp = false

    /**
     * 图表数据总集
     */
    private lateinit var combinedData: CombinedData

    private val glucoseSets: MutableList<LineDataSet> = CopyOnWriteArrayList()
    private val calSet = CalDataSet()
    private val bgSet = BgDataSet()
    private val eventSet = IconDataSet()

    private var timeMin: Float? = null
    private var timeMax: Float? = null

    @Volatile
    private var isDataInit = false

    class ChartChangedInfo(
        var timeMin: Float?,
        var needScrollToLatest: Boolean
    )

    private val mGranularityFlow = MutableStateFlow<Int?>(null)
    private fun getGranularity(): Int = mGranularityFlow.value ?: G_SIX_HOURS
    val granularityFlow = mGranularityFlow.asStateFlow()

    /** 启动加载下一页任务 */
    val startLoadNextPage = MutableStateFlow(false)
    val startApplyNextPageData = MutableStateFlow(false)

    /**
     * 数据图表数据发生变化通知外面刷新
     * value-Pair<Float?, Boolean>
     *   first: [timeMin] 当前x轴最小值
     *   second: true-滚动到最右端 false-不滚动
     */
    val mDataChangedFlow = MutableStateFlow<ChartChangedInfo?>(null)

    /** 标记当前最小日期数据是否已经触发了下一页的加载 滚动时防止重复触发下一页 */
    private var loadedMinDate = -Float.MAX_VALUE

    /** 当前页面数据最小日期 */
    private var curPageMinDate = Date()

    /**
     * 切换每屏的图表时间跨度
     */
    fun updateGranularity(@ChartGranularityPerScreen granularity: Int) {
        mGranularityFlow.tryEmit(granularity)
    }

    init {
        viewModelScope.launch {
            launch {
                startLoadNextPage.debounce(200) .collect {
                    if (it) {
                        LogUtil.d("===CHART===开始加载下一页")
                        var needNotify: Boolean
                        withContext(Dispatchers.IO) {

                            LogUtil.d("===CHART=== 当前数据最小日期${curPageMinDate.formatToYMdHm()} ${curPageMinDate.time}")
                            var maxTime = curPageMinDate // ob between是前闭后闭
                            //获取当前最小日期前的最近一条数据时间 因为可能有断层 导致图表无法滚动
                            val latestOne = CgmCalibBgRepository.queryNextByTargetDate(
                                UserInfoManager.getCurShowUserId(),
                                maxTime
                            )
                            LogUtil.d("===CHART=== 当前数据最小日期最靠近的一条数据${latestOne?.timestamp}")
                            maxTime = latestOne?.let { rche ->
                                Date(rche.timestamp)
                            } ?: maxTime

                            val curMinTime = getCurPageStartDate(maxTime.time)
//                            val curMinTime = getCurPageStartDate(curPageMinDate.time)

                            needNotify = loadNextPageData(curMinTime, maxTime, false)

                            LogUtil.d("===CHART=== 加载之后 timeMin=$timeMin start=${curMinTime} end=${maxTime}")
                        }
                        // 如果这个期间没发生用户切换就外部
                        if (needNotify) {
//                        mDataChangedFlow.emit(ChartChangedInfo(timeMin, false))
                        } else {
                            LogUtil.e("===CHART=== 分页查询结束 发现换人了 终止通知")
                        }
                        //重置标记
                        val ret = startLoadNextPage.compareAndSet(true, update = false)
                        LogUtil.d("===CHART=== 通知及标记重置了 ret=$ret")
                    }
                }
            }

            launch {
                startApplyNextPageData.collect {
                    if (it) {
                        if (startLoadNextPage.value) {
                            val ret = startApplyNextPageData.compareAndSet(true, update = false)
                            LogUtil.d("===CHART=== 发现正在加载分页数据 应用分页数据终止 ret=$ret")
                            return@collect
                        }
                        val dataUserId = UserInfoManager.getCurShowUserId()
                        var needNotify = false

                        withContext(Dispatchers.IO) {
                            val mergeDataTasks = listOf(
                                async {
                                    if (nextPageCgmData.isNotEmpty()) {
                                        LogUtil.d("===CHART=== 下一页数据 nextPageCgmData size=${nextPageCgmData.size}")
                                        addCgmData(nextPageCgmData)
                                        nextPageCgmData.clear()
                                        needNotify = true
                                    }
                                },
                                async {
                                    if (nextPageBgData.isNotEmpty()) {
                                        LogUtil.d("===CHART=== 下一页数据 nextPageBgData size=${nextPageBgData.size}")
                                        addBgData(nextPageBgData)
                                        nextPageBgData.clear()
                                        needNotify = true
                                    }
                                },
                                async {
                                    if (nextPageCalData.isNotEmpty()) {
                                        LogUtil.d("===CHART=== 下一页数据 nextPageCalData size=${nextPageCalData.size}")
                                        addCalData(nextPageCalData)
                                        nextPageCalData.clear()
                                        needNotify = true
                                    }
                                },
                                async {
                                    if (nextPageEventData.isNotEmpty()) {
                                        LogUtil.d("===CHART=== 下一页数据 nextPageEventData size=${nextPageEventData.size}")
                                        addEvent(nextPageEventData)
                                        nextPageEventData.clear()
                                        needNotify = true
                                    }
                                }
                            )
                            mergeDataTasks.awaitAll()
                        }

                        if (UserInfoManager.getCurShowUserId() != dataUserId) {
                            LogUtil.xLogE("数据合并完成后发现切换用户", TAG)
                            reset()
                            needNotify = false
                            //重置标记
                            val ret = startApplyNextPageData.compareAndSet(true, update = false)
                            LogUtil.d("===CHART=== 应用分页数据标记重置 ret=$ret")
                            return@collect
                        }

                        if (needNotify) {
                            LogUtil.d("===CHART=== 下一页数据已添加到图表待刷新")
                            mDataChangedFlow.emit(ChartChangedInfo(timeMin, false))
                        } else {
                            val ret = startLoadNextPage.compareAndSet(expect = false, true)
                            LogUtil.d("===CHART=== 滚到最左边后发现没有数据 startLoadNextPage: $ret")
                        }
                        //重置标记
                        val ret = startApplyNextPageData.compareAndSet(true, update = false)
                        LogUtil.d("===CHART=== 应用分页数据标记重置 ret=$ret")
                    }
                }
            }

        }
    }

    override fun onCleared() {
        super.onCleared()
        LogUtil.d("HomeFragment chartvm onCleared ${this@ChartViewModel}", TAG)
    }

    /**
     * 首次 加载第一页数据
     */
    fun initData(needReloadData: Boolean = false) = flow {
        LogUtil.d("initData ${this@ChartViewModel}", TAG)
        if (::combinedData.isInitialized && !needReloadData) {
            LogUtil.d("${::combinedData.isInitialized}  $needReloadData ${this@ChartViewModel}", TAG)
            emit(combinedData)
            return@flow
        }
        if (!::combinedData.isInitialized) {
            combinedData = CombinedData()
        }
        delay(1000) // vivo 2048A 不延迟有可能查询不到数据 怀疑于objectBox初始化有关系
        val latestOne = CgmCalibBgRepository.queryNextByTargetDate(
            UserInfoManager.getCurShowUserId(),
            Date()
        ) //"f03550ef07a7b2164f06deaef597ce37"

        LogUtil.d("查询第一条 $latestOne ${this@ChartViewModel}", TAG)

        val cgmMaxDate = latestOne?.let {
            Date(it.timestamp)
        } ?: Date()
        LogUtil.d("查询第一条 日期 ${cgmMaxDate} ${this@ChartViewModel}", TAG)
        val minDate = getCurPageStartDate(cgmMaxDate.time, true)
        LogUtil.d("查询第一条 最小日期 ${minDate} ${this@ChartViewModel}", TAG)
        // 加载所有该日期区间的数据
        loadNextPageData(minDate, Date())
        LogUtil.d("查询区间数据 结束 ${this@ChartViewModel}", TAG)
        val lineDataSets: ArrayList<ILineDataSet> = ArrayList()
        lineDataSets.addAll(GlucoseChart.generateLimitLines(xMin(), xMax(), lowerLimit, upperLimit))

//        val glucoseSets: List<LineDataSet> = formatGlucoseSet()
        GlucoseChart.formatGlucoseSetAfterInitData(glucoseSets, lowerLimit, upperLimit)

        LogUtil.d("数据加载完毕 ${this@ChartViewModel}", TAG)
        lineDataSets.addAll(glucoseSets)

        val scatterDataSets: ArrayList<IScatterDataSet> = ArrayList()
        scatterDataSets.add(calSet)
        scatterDataSets.add(bgSet)
        scatterDataSets.add(eventSet)

        combinedData.setData(LineData(lineDataSets).also {
            // 不需要渲染数值及icon 提升性能
            it.dataSets.forEach { ld ->
                ld.setDrawIcons(false)
                ld.setDrawValues(false)
            }
        })
        combinedData.setData(ScatterData(scatterDataSets))

        if (timeMax == null) {
            timeMax = ChartUtil.dateToX(Date())
        }
        if (timeMin == null) {
            timeMin = ChartUtil.dateToX(
                Date(Date().time - getGranularity() * 6 * TimeUtils.oneHourSeconds * 1000))
        }

        emit(combinedData)
        isDataInit = true

    }.flowOn(Dispatchers.IO)

    /**
     *  重新加载图表数据
     */
    fun reload() {
        LogUtil.d("reload()", TAG)
        if (!isDataInit) return

        fun startReset() {
            reset()
            viewModelScope.launch {
                initData(true).collect {
                    LogUtil.d("===CHART=== reload initdata")
                    mDataChangedFlow.emit(ChartChangedInfo(timeMin, true))
                }
            }
        }

//        startReset()

        var job: Job? = null
        if (startApplyNextPageData.value) {
            Dialogs.showWait(getContext().getString(R.string.loading))
            job = viewModelScope.launch {
                startApplyNextPageData.collectLatest {
                    if (!it) {
                        startReset()
                        Dialogs.dismissWait()
                        job?.cancel()
                    }
                }
            }
        } else {
            startReset()
        }
    }

    /** 是否需要加载下一页 */
    fun needLoadNextPage(isLtr: Boolean, visibleLeftX: Float, xAxisMin: Float): Boolean {

        if (isLtr || !isDataInit) return false

        if(loadedMinDate == xAxisMin ) {
            if (nextPageBgData.isNotEmpty() ||
                        nextPageCalData.isNotEmpty() ||
                        nextPageCgmData.isNotEmpty() ||
                        nextPageEventData.isNotEmpty()) {
//                LogUtil.d("===CHART=== 滚动过程发现下一页数据已经加载 不再触发")
                return false
            } else {
                // 这种情况依赖 startLoadNextPage cas 来保证有数据的同一页只加载一次
//                LogUtil.d("===CHART=== 滚动过程已经触发下一页但没数据 继续触发")
            }
        }

        val isLeftTwoDays = abs(
            ChartUtil.xToSecond(visibleLeftX) - ChartUtil.xToSecond(xAxisMin)
        ) <= TimeUtils.oneDaySeconds * 2

        if (isLeftTwoDays) {
//            LogUtil.d("===CHART=== 滚动过程触发加载下一页")
            loadedMinDate = xAxisMin
        }
        return isLeftTwoDays
    }

    /**
     * 外部有数据变动通知过来后进行清洗合并
     */
    suspend fun onCgmDataChanged(data: EventDataChangedInfo) {
        withContext(Dispatchers.IO) {
            when (data.first) {
                DataChangedType.ADD -> {
                    val cgmData = data.second as List<RealCgmHistoryEntity>
                    val rets = cgmData.filter {
                        checkCgmHistory(it)
                                && it.timestamp > curXMinTimeMillis()
                    }
                    if (rets.isNotEmpty()) {
                        addCgmData(rets)
                        LogUtil.d("===CHART=== onCgmDataChanged")
                        mDataChangedFlow.emit(ChartChangedInfo(timeMin, false))
                    }
                }
                else -> {}
            }
        }
    }

    /**
     * 外部有数据变动通知过来后进行清洗合并
     */
    suspend fun onBgDataChanged(data: EventDataChangedInfo) {
        withContext(Dispatchers.IO) {
            val bgData = data.second as List<BloodGlucoseEntity>
            when (data.first) {
                DataChangedType.ADD -> {
                    val rets = bgData.filter {
                        it.timestamp >= curXMinTimeMillis()
                    }
                    if (rets.isNotEmpty()) {
                        addBgData(rets)
                        LogUtil.d("===CHART=== onBgDataChanged add")
                        mDataChangedFlow.emit(ChartChangedInfo(timeMin, false))
                    }
                }
                DataChangedType.DELETE -> {
                    val needRefresh = bgData.fold(false) { needRefresh, it ->
                        needRefresh || if (it.timestamp >= curXMinTimeMillis()){
                            bgSet.removeEntry(it.toChartEntry())
                        } else false
                    }
                    if (needRefresh) {
                        LogUtil.d("===CHART=== onBgDataChanged del")
                        mDataChangedFlow.emit(ChartChangedInfo(timeMin, false))
                    }
                }
                else -> {}
            }
        }
    }

    suspend fun onCalDataChanged(data: EventDataChangedInfo) {
        withContext(Dispatchers.IO) {
            when (data.first) {
                DataChangedType.ADD -> {
                    val calData = data.second as List<CalibrateEntity>
                    val rets = calData.filter {
                        it.timestamp >= curXMinTimeMillis()
                    }
                    if (rets.isNotEmpty()) {
                        addCalData(rets)
                        LogUtil.d("===CHART=== onCalDataChanged add")
                        mDataChangedFlow.emit(ChartChangedInfo(timeMin, false))
                    }
                }
                else -> {}
            }
        }
    }


    // region x轴 y轴 参数相关
    fun xRange(): Float {
        return getGranularity() * CHART_LABEL_COUNT.toFloat()
    }

    fun xMargin(): Float {
        return getGranularity() / 2f
    }

    fun xMin(): Float {
        val default = xMax() - xRange()
        return if (timeMin == null || timeMin!! > default) default
        else timeMin!!
    }

    fun xMax(): Float {
        return ChartUtil.secondToX(Date().time / 1000) + xMargin()
    }

    var upperLimit = ThresholdManager.DEFAULT_HYPER.toGlucoseValue()
        get() {
            return ThresholdManager.hyper.toGlucoseValue()
        }
        private set
    var lowerLimit = ThresholdManager.DEFAULT_HYPO.toGlucoseValue()
        get() {
            return ThresholdManager.hypo.toGlucoseValue()
        }
        private set

    //endregion


    private val nextPageCgmData = CopyOnWriteArrayList<RealCgmHistoryEntity>()
    private val nextPageBgData = CopyOnWriteArrayList<BloodGlucoseEntity>()
    private val nextPageCalData = CopyOnWriteArrayList<CalibrateEntity>()
    private val nextPageEventData = CopyOnWriteArrayList<BaseEventEntity>()
    /**
     * 加载下一页数据到图表集合
     * @return false-正在加载时 发现切换用户了
     */
    private suspend fun loadNextPageData(startDate: Date, endDate: Date, needApply: Boolean = true): Boolean =
        withContext(Dispatchers.IO) {
            LogUtil.d("===CHART=== loadNextPage start=${startDate.formatToYMdHm()} end=${endDate.formatToYMdHm()}")
            timeMin = ChartUtil.dateToX(startDate)
            var isSuccess = true

            withContext(Dispatchers.IO) {

                val loadTasks = listOf(
                    async {
                        getCgmPageData(startDate, endDate)?.let { d ->
                            if (d.size > 0 && d[0].userId != UserInfoManager.getCurShowUserId()) {
                                isSuccess = false
                                LogUtil.d("cgm 加载成功 size=${d.size} 或用户不对", TAG)
                            } else {
                                if (needApply) {
                                    LogUtil.d("cgm 加载成功 size=${d.size} 开始添加内存", TAG)
                                    addCgmData(d)
                                    LogUtil.d("cgm 加载成功 size=${d.size} 添加内存成功", TAG)
                                } else {
                                    LogUtil.d("===CHART=== 下一页数据 size=${d.size} 准备完毕")
                                    if (d.size > 2) {
                                        LogUtil.d("===CHART=== 下一页数据 first=${d.first().timestamp} last=${d.last().timestamp}")
                                    }
                                    nextPageCgmData.addAll(d)
                                }
                            }
                        }
                    },
                    async {
                        CgmCalibBgRepository.queryBgByPage(startDate, endDate, UserInfoManager.getCurShowUserId())?.let { d ->
                            if (d.size > 0 && d[0].userId != UserInfoManager.getCurShowUserId()) {
                                isSuccess = false
                            } else {
                                if (needApply) {
                                    addBgData(d)
                                } else {
                                    nextPageBgData.addAll(d)
                                }
                            }
                        }
                    },
                    async {
                        CgmCalibBgRepository.queryCalByPage(startDate, endDate, UserInfoManager.getCurShowUserId())?.let { d ->
                            if (d.size > 0 && d[0].userId != UserInfoManager.getCurShowUserId()) {
                                isSuccess = false
                            } else {
                                if (needApply) {
                                    addCalData(d)
                                } else {
                                    nextPageCalData.addAll(d)
                                }
                            }
                        }
                    },
                    async {
                        EventDbRepository.queryEventByPage(startDate, endDate, UserInfoManager.getCurShowUserId()).let { d ->
                            if (d.isNotEmpty() && d[0].userId != UserInfoManager.getCurShowUserId()) {
                                isSuccess = false
                            } else {
                                if (needApply) {
                                    addEvent(d)
                                } else {
                                    nextPageEventData.addAll(d)
                                }
                            }
                        }
                    }
                )
                loadTasks.awaitAll()
            }

            isSuccess
        }


    /**
     * 当前x轴显示的最小的日期 毫秒
     */
    private fun curXMinTimeMillis() = ChartUtil.xToSecond(timeMin?:0f) * 1000

    private suspend fun getCgmPageData(startDate: Date, endDate: Date) =
        CgmCalibBgRepository.queryCgmByPage(
            startDate,
            endDate,
            UserInfoManager.getCurShowUserId()
        )


    private fun getCurPageStartDate(curTime: Long = System.currentTimeMillis(), isFromInit: Boolean = false): Date =
        Date(curTime - TimeUtils.oneDayMillis * (if (isFromInit) 2 else 4))

    private fun clearEventSets() {
        bgSet.clear()
        eventSet.clear()
    }

    private fun clearGlucoseSets() {
        glucoseSets.clear()
        calSet.clear()
        timeMin = null
        timeMax = null
    }

    /**
     * 切换用户重置数据
     */
    private fun reset() {
        LogUtil.d("reset data1", TAG)
        //重置数据
        clearEventSets()
        clearGlucoseSets()
        combinedData.lineData.clearValues()
        combinedData.scatterData.clearValues()
        combinedData.clearValues()

        nextPageCgmData.clear()
        nextPageBgData.clear()
        nextPageCalData.clear()
        nextPageEventData.clear()

        //重置分页标记
        val ret = startLoadNextPage.compareAndSet(true, update = false)
        loadedMinDate = -Float.MAX_VALUE
        curPageMinDate = Date()
        LogUtil.d("reset data2", TAG)
    }

    //region cgm

    /**
     * 新增更新血糖数据
     */
    private suspend fun addCgmData(cgmHistories: List<RealCgmHistoryEntity>) {
        if (glucoseSets.isEmpty()) glucoseSets.add(GlucoseDataSet())
        if (cgmHistories.isEmpty()) {
            return
        }

        cgmHistories.forEach {history ->
            history.userId?.let {
                if (!canMergeData(it)) {
                    LogUtil.xLogE("正在把cgm数据合并到chart 发现换人了", TAG)
                    reset()
                    return
                }
                if (history.isGlucoseIsValid()) {
                    if (history.glucose != null && history.eventWarning != -1) {
                        val entry = history.toChartEntry()
                        if (glucoseSets.isEmpty()) glucoseSets.add(GlucoseDataSet())
                        glucoseSets.last().addEntryOrdered(entry)
                        xMaxMin(entry.x)
                        calDateMaxMin(Date(history.timestamp))
                    }
                }
            } ?: LogUtil.d("cgm data userid null", TAG)
        }
        LogUtil.d("glucoseSets last : ${glucoseSets.last().entries.size}")

        if (isGp) {
            val all: MutableList<CalibrateEntity> = CalibrateManager.getCalibrateHistorys()
            calSet.clear()
            for (item in all) {
                updateGpCalibrationSet(item)
            }
        }

    }

    private fun checkCgmHistory(cgm: RealCgmHistoryEntity) =
        cgm.isGlucoseIsValid()
                && (cgm.glucose != null && cgm.eventWarning != -1)

    //endregion

    //region 指血
    /**
     * 新增指血数据
     */
    private fun addBgData(bgs: List<BloodGlucoseEntity>) {
        for (bg in bgs) {
            bg.userId?.let {
                if (!canMergeData(it)) {
                    LogUtil.xLogE("正在把bg数据合并到chart 发现换人了", TAG)
                    reset()
                    return
                }
                val entry = bg.toChartEntry()
                bgSet.addEntryOrdered(entry)
                xMaxMin(entry.x)
                calDateMaxMin(Date(bg.timestamp))
            } ?: LogUtil.d("bg data userid null", TAG)
        }
    }

    //endregion

    //region 校准
    private fun addCalData(calEntityList: List<CalibrateEntity>) {
        calEntityList.forEach { calEntity ->
            calEntity.userId?.let {
                if (!canMergeData(it)) {
                    LogUtil.xLogE("正在把cal数据合并到chart 发现换人了", TAG)
                    reset()
                    return
                }
                val entry = calEntity.toChartEntry()
                calSet.addEntryOrdered(entry)
                xMaxMin(entry.x)
                calDateMaxMin(Date(calEntity.timestamp))
            } ?: LogUtil.d("cal data userid null", TAG)
        }
    }

    private fun canMergeData(dataUserId: String) =
        (UserInfoManager.shareUserInfo != null && UserInfoManager.instance().userId() != dataUserId)
                || (UserInfoManager.shareUserInfo == null
                    && UserInfoManager.instance().userId() == dataUserId)

    // 国际版用
    private fun updateGpCalibrationSet(history: CalibrateEntity) {
        val dateTime = ChartUtil.dateToX(history.calTime)
        val bg = BloodGlucoseEntity(history.calTime, history.referenceGlucose)
        bg.calibration = true
        val entry = Entry(dateTime, bg.bloodGlucoseMg.toGlucoseValue())
        entry.data = bg
        entry.icon = CalDataSet.icon
        calSet.addEntryOrdered(entry)
        xMaxMin(dateTime)
        // todo 国际版是直接从库里加载 应该也需要给个校准数据的最小日期标记 国内版是混合在cgm表中
//        calDateMaxMin(history.calTime)
    }

    // endregion

    //region 事件
    private fun <T : BaseEventEntity> addEvent(es: List<T>) {
        LogUtil.d("initIconSet")
        for (e in es) {

            e.userId?.let {
                if (!canMergeData(it)) {
                    LogUtil.xLogE("正在把event数据合并到chart 发现换人了", TAG)
                    reset()
                    return
                }
                val entry = e.toChartEntry {
                    (5f * 18).toGlucoseValue()
                }
                eventSet.addEntryOrdered(entry)
            } ?: LogUtil.d("event data $e userid null", TAG)
        }
        LogUtil.d("eventSet :" + eventSet.entries?.size)
    }

    suspend fun onEventDataChanged(changedInfo: EventDataChangedInfo) {
        withContext(Dispatchers.IO) {
            when (changedInfo.first) {
                DataChangedType.ADD -> {
                    val rets = changedInfo.second.filter {
                        it.timestamp >= curXMinTimeMillis()
                    }
                    if (rets.isNotEmpty()) {
                        addEvent(rets)
                        LogUtil.d("===CHART=== onEventDataChanged add")
                        mDataChangedFlow.emit(ChartChangedInfo(timeMin, false))
                    }
                }
                DataChangedType.DELETE -> {
                    val needRefresh = changedInfo.second.fold(false) { needRefresh, it ->
                        needRefresh || if (it.timestamp >= curXMinTimeMillis()){
                            eventSet.removeEntry(it.toChartEntry())
                        } else false
                    }
                    if (needRefresh) {
                        LogUtil.d("===CHART=== onEventDataChanged del")
                        mDataChangedFlow.emit(ChartChangedInfo(timeMin, false))
                    }
                }
                else -> {}
            }
        }
    }
    //endregion

    private fun xMaxMin(dateTime: Float) {
        if (timeMin == null || timeMin!! > dateTime) {
            timeMin = dateTime
            LogUtil.d("===CHART=== timeMin=$timeMin")
        }

        if (timeMax == null || timeMax!! < dateTime) {
            timeMax = dateTime
        }
    }

    private fun calDateMaxMin(dateTime: Date) {
        //记录最小时间
        if (curPageMinDate.time > dateTime.time) {
            curPageMinDate = dateTime
        }
    }

}