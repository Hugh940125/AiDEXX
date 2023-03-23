package com.microtech.aidexx.db.dao

import com.microtech.aidexx.db.ObjectBox
import com.microtech.aidexx.db.entity.CgmHistoryEntity
import com.microtech.aidexx.db.entity.CgmHistoryEntity_
import com.microtechmd.blecomm.constant.History
import io.objectbox.kotlin.boxFor
import io.objectbox.kotlin.equal
import io.objectbox.query.QueryBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object CgmHistoryDao {

    private val boxStore by lazy { ObjectBox.store.boxFor<CgmHistoryEntity>() }

    suspend fun queryByUid(id: String): List<CgmHistoryEntity> =
        withContext(Dispatchers.IO) {
            boxStore.query()
                .equal(CgmHistoryEntity_.eventType, History.HISTORY_CALIBRATION)
                .equal(CgmHistoryEntity_.authorizationId, id, QueryBuilder.StringOrder.CASE_SENSITIVE)
                .build()
                .find()
        }

}