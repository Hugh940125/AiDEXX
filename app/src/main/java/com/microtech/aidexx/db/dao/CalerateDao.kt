package com.microtech.aidexx.db.dao

import com.microtech.aidexx.db.ObjectBox
import com.microtech.aidexx.db.entity.CalerateEntity
import com.microtech.aidexx.db.entity.CalerateEntity_
import io.objectbox.kotlin.boxFor
import io.objectbox.query.QueryBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object CalerateDao {

    private val boxStore by lazy { ObjectBox.store.boxFor<CalerateEntity>() }

    suspend fun queryByUid(id: String): List<CalerateEntity> =
        withContext(Dispatchers.IO) {
            boxStore.query()
                .equal(CalerateEntity_.authorizationId, id, QueryBuilder.StringOrder.CASE_SENSITIVE)
                .build()
                .find()
        }
}