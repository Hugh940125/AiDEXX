package com.microtech.aidexx.db.dao

import com.microtech.aidexx.db.ObjectBox
import com.microtech.aidexx.db.ObjectBox.awaitCallInTx
import com.microtech.aidexx.db.entity.CalibrateEntity
import io.objectbox.kotlin.boxFor
import io.objectbox.query.QueryBuilder

object CalibrateDao {

    private val box by lazy { ObjectBox.store.boxFor<CalibrateEntity>() }

    suspend fun queryByUid(id: String): MutableList<CalibrateEntity>? = null
//        awaitCallInTx {
//            box.query()
//                .equal(CalibrateEntity_.authorizationId, id, QueryBuilder.StringOrder.CASE_SENSITIVE)
//                .build()
//                .find()
//        }

}