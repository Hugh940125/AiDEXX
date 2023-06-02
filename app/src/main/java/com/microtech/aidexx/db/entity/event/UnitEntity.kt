package com.microtech.aidexx.db.entity.event

import io.objectbox.annotation.ConflictStrategy
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.annotation.Unique

@Entity
data class UnitEntity(
    @Id var id: Long = 0,

    @Unique(onConflict = ConflictStrategy.REPLACE)
    var key: String, // 自定义的唯一标记 用于全量更新自动替换 type+language+code

    var isDefault: Int, //是否是默认单位
    var type: String, // 类型 food或其他
    var language: String, // 语言代码
    var code: Int, // 单位代码 之前的枚举
    var name: String, // 单位名称
    var ratio: Double, // 该单位对克的转换比率 M / 克 = ratio
    var status: Int, // 该单位当前状态 0-可用  1-不可用
) {

    override fun toString(): String = "UnitEntity:(id=$id, type=$type, language=$language, code=$code, name=$name, ratio=$ratio, status=$status)"

}

/**
 * 各语言配置的单位
 *
 [
    {
        "type": "food",
        "languageList": [
            {
                "language": "en",
                "unitList": [{
                    "code": 0,
                    "default": 1,
                    "name": "g",
                    "ratio": 1,
                    "status": 0 // 0为当前版本可用，1为当前版本不可用
                },
                {
                    "code": 1,
                    "default": 0,
                    "name": "kg",
                    "ratio": 1000,
                    "status": 0
                }]
            }
        ]
    }
]
 * */

data class UnitBaseInfo(
    var code: Int, // 单位代码 之前的枚举
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
    var version: Int,
    var content: MutableList<UnitConfigWithType>
)