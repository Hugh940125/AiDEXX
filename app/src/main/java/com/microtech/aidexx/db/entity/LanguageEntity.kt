package com.microtech.aidexx.db.entity

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.annotation.Index

@Entity
data class LanguageEntity(
    @Id var id: Long? = null,
    val key: String? = null,
    val value: String? = null,
    @Index val language: String? = null,
    @Index val module: String? = null, // 哪个模块 或定义为优先级
    @Index var version: String? = null
)

@Entity
data class LanguageConfEntity(
    @Id var id: Long? = null,
    @Index val name: String? = null,
    val chineseName: String? = null,
    @Index val langId: String? = null, // zh-Hans-CN 三段格式
    @Index val langZoneId: String? = null,
    @Index var isDefault: Int? = 0
)