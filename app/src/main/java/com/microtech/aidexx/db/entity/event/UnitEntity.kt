package com.microtech.aidexx.db.entity.event

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id

@Entity
data class UnitEntity(
    @Id var id: Long = 0,

    var eventType: Int, // 事件类型  1,饮食，2用药，3运动，4胰岛素
    var value: Int, // 单位代码 之前的枚举
    var isDefault: Int, //是否是默认单位
    var language: String, // 语言代码
    var name: String, // 单位名称
    var ratio: Double, // 该单位对克的转换比率 M / 克 = ratio
    var version: String, // 该单位当前版本
) {

    override fun toString(): String = "UnitEntity:(id=$id, isDefault=$isDefault eventType=$eventType, language=$language, value=$value, name=$name, ratio=$ratio, version=$version)"

}



data class UnitBaseInfo(
    var eventType: Int, // 事件类型  1,饮食，2用药，3运动，4胰岛素
    var value: Int, // 单位代码 之前的枚举
    var default: Int, // 是否默认
    var name: String, // 单位名称
    var ratio: Double, // 该单位对克的转换比率 M / 克 = ratio
    var status: Int, // 该单位当前状态 0-可用  1-不可用
)

data class UnitWithLangInfo(
    var language: String,
    var unitList: MutableList<UnitBaseInfo>
)

data class UnitConfigWithType(
    var type: String,
    var languageList: MutableList<UnitWithLangInfo>
)

data class UnitConfig(
    var version: String,
    var content: MutableList<UnitConfigWithType>
)