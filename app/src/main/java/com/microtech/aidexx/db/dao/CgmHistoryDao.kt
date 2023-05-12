package com.microtech.aidexx.db.dao

import com.microtech.aidexx.db.ObjectBox
import com.microtech.aidexx.db.ObjectBox.awaitCallInTx
import com.microtech.aidexx.db.entity.RealCgmHistoryEntity
import com.microtech.aidexx.db.entity.RealCgmHistoryEntity_
import com.microtechmd.blecomm.constant.History
import io.objectbox.kotlin.boxFor
import io.objectbox.kotlin.equal
import io.objectbox.query.QueryBuilder.StringOrder
import java.util.Date

object CgmHistoryDao {

    private val box by lazy { ObjectBox.store.boxFor<RealCgmHistoryEntity>() }

    suspend fun queryByUid(authorId: String): MutableList<RealCgmHistoryEntity>? =
        awaitCallInTx {
            box.query()
                .equal(RealCgmHistoryEntity_.eventType, History.HISTORY_CALIBRATION)
                .equal(RealCgmHistoryEntity_.userId, authorId, StringOrder.CASE_SENSITIVE)
                .build()
                .find()
        }

    /**
     * 查 某用户 一段时间内 有效的 (血糖有效 或者 校准有效) 的数据
     */
    suspend fun query(
        startDate: Date,
        endDate: Date,
        authorId: String
    ): MutableList<RealCgmHistoryEntity>? =
        awaitCallInTx {
            box.query()
                .equal(RealCgmHistoryEntity_.userId, authorId, StringOrder.CASE_SENSITIVE)
                .between(
                    RealCgmHistoryEntity_.deviceTime,
                    startDate,
                    endDate
                )
                .equal(RealCgmHistoryEntity_.deleteStatus, 0)

                .equal(RealCgmHistoryEntity_.glucoseIsValid, 1)
                .and()
                .equal(RealCgmHistoryEntity_.status, 0)

                .or()
                .equal(RealCgmHistoryEntity_.calibrationIsValid, 1)

                .build().find()
        }

    /**
     * 查 某用户 给定时间点的前一条 有效的 (血糖有效 或者 校准有效) 的数据
     */
    suspend fun queryNextByTargetDate(authorId: String, targetDate: Date): RealCgmHistoryEntity? =
        awaitCallInTx {
            box.query()
                .equal( RealCgmHistoryEntity_.userId, authorId, StringOrder.CASE_SENSITIVE )
                .equal(RealCgmHistoryEntity_.deleteStatus, 0)
                .less(RealCgmHistoryEntity_.deviceTime, targetDate)

                .equal(RealCgmHistoryEntity_.glucoseIsValid, 1)
                .and()
                .equal(RealCgmHistoryEntity_.status, 0)

                .or()
                .equal(RealCgmHistoryEntity_.calibrationIsValid, 1)

                .orderDesc(RealCgmHistoryEntity_.deviceTime)
                .build()
                .findFirst()
        }

    suspend fun insert(list: List<RealCgmHistoryEntity>) =
        awaitCallInTx {
            box.put(list)
        }
}