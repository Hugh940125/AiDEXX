package com.microtech.aidexx.db.dao

import com.microtech.aidexx.db.ObjectBox
import com.microtech.aidexx.db.ObjectBox.awaitCallInTx
import com.microtech.aidexx.db.entity.LanguageConfEntity
import com.microtech.aidexx.db.entity.LanguageConfEntity_
import com.microtech.aidexx.db.entity.LanguageEntity
import com.microtech.aidexx.db.entity.LanguageEntity_
import io.objectbox.kotlin.boxFor
import io.objectbox.kotlin.query
import io.objectbox.query.QueryBuilder

class LanguageDao {

    private val box by lazy { ObjectBox.store.boxFor<LanguageEntity>() }
    private val confBox by lazy { ObjectBox.store.boxFor<LanguageConfEntity>() }

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
            .findStrings()
    }

    //===================
    //LanguageConf
    //===================
    suspend fun removeAllConf() = awaitCallInTx {
        confBox.removeAll()
    }
    suspend fun insertConf(list: List<LanguageConfEntity>) = awaitCallInTx {
        confBox.put(list)
    }
    suspend fun queryConfs(languageIds: Array<out String>) = awaitCallInTx {
        confBox.query {
            `in`(LanguageConfEntity_.langId, languageIds, QueryBuilder.StringOrder.CASE_SENSITIVE)
        }.find().toMutableList()
    }
    suspend fun queryConf(languageId: String) = awaitCallInTx {
        confBox.query {
            equal(LanguageConfEntity_.langId, languageId, QueryBuilder.StringOrder.CASE_SENSITIVE)
        }.findFirst()
    }
}