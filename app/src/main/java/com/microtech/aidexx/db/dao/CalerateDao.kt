package com.microtech.aidexx.db.dao

import com.microtech.aidexx.db.ObjectBox
import com.microtech.aidexx.db.ObjectBox.awaitCallInTx
import com.microtech.aidexx.db.entity.CalerateEntity
import com.microtech.aidexx.db.entity.CalerateEntity_
import io.objectbox.kotlin.boxFor
import io.objectbox.query.QueryBuilder

object CalerateDao {

    private val box by lazy { ObjectBox.store.boxFor<CalerateEntity>() }

    suspend fun queryByUid(id: String): MutableList<CalerateEntity>? =
        awaitCallInTx {
            box.query()
                .equal(CalerateEntity_.authorizationId, id, QueryBuilder.StringOrder.CASE_SENSITIVE)
                .build()
                .find()
        }

}