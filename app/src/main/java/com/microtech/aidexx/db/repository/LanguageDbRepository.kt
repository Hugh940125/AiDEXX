package com.microtech.aidexx.db.repository

import com.microtech.aidexx.db.dao.LanguageDao
import com.microtech.aidexx.db.entity.LanguageEntity
import com.microtech.aidexx.utils.mmkv.MmkvManager

class LanguageDbRepository {

    private val dao by lazy { LanguageDao() }

    suspend fun insert(list: MutableList<LanguageEntity>) = dao.insert(list)

    suspend fun query(
        language: String?,
        module: String?,
        version: String = MmkvManager.getLanguageVersion()
    ): MutableList<LanguageEntity>? =
        dao.query(language, module, version)

    suspend fun removeLanguageOfOtherVersion(exceptVersion: String) =
        dao.removeLanguageOfOtherVersion(exceptVersion)

    suspend fun removeAll() = dao.removeAll()

    suspend fun querySupportLanguages() = dao.querySupportLanguages()

}