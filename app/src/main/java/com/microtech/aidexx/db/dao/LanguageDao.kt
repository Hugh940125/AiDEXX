package com.microtech.aidexx.db.dao

import com.microtech.aidexx.db.ObjectBox
import com.microtech.aidexx.db.ObjectBox.awaitCallInTx
import com.microtech.aidexx.db.entity.LanguageEntity
import com.microtech.aidexx.db.entity.LanguageEntity_
import io.objectbox.kotlin.boxFor
import io.objectbox.kotlin.query
import io.objectbox.query.QueryBuilder

class LanguageDao {

    private val box by lazy { ObjectBox.store.boxFor<LanguageEntity>() }

    suspend fun insert(list: List<LanguageEntity>) = awaitCallInTx {
        box.put(list)
    }

    suspend fun query(language: String?, module: String?, version: String?): MutableList<LanguageEntity>? = awaitCallInTx {

        box.query {
            language?.let {
                equal(LanguageEntity_.language, it, QueryBuilder.StringOrder.CASE_SENSITIVE)
            }
            module?.let {
                equal(LanguageEntity_.module, it, QueryBuilder.StringOrder.CASE_SENSITIVE)
            }
            version?.let {
                equal(LanguageEntity_.version, it, QueryBuilder.StringOrder.CASE_SENSITIVE)
            }
        }.find()

    }

    suspend fun removeLanguageOfOtherVersion(exceptVersion: String) = awaitCallInTx {
        box.query {
            notEqual(LanguageEntity_.version, exceptVersion, QueryBuilder.StringOrder.CASE_INSENSITIVE)
        }.remove()
    }

    suspend fun removeAll() = awaitCallInTx {
        box.removeAll()
    }

    suspend fun querySupportLanguages() = awaitCallInTx {
        box.query().build()
            .property(LanguageEntity_.language)
            .distinct(QueryBuilder.StringOrder.CASE_SENSITIVE)
            .findStrings().toMutableList()
    }

}