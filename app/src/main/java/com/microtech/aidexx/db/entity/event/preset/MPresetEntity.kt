package com.microtech.aidexx.db.entity.event.preset

import com.microtech.aidexx.db.entity.event.EventActions
import io.objectbox.annotation.BaseEntity
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.annotation.Index
import java.util.Date
import java.util.UUID


@BaseEntity
open class BasePresetEntity: EventActions {

    @Id
    open var idx: Long = 0 // 本地id

    var id: Long? = null // 天上的id

    @Index
    open var presetUuid: String = UUID.randomUUID().toString().replace("-", "")

    var fkUser: String = "" // 登录用户id 为空时系统预设

    @Index
    open var name: String = ""

    open var delete_flag: Int = 0  //是否删除，0：未删除，1：已删除


    @Transient
    var isUserInputType = false //是否是用户输入的待选项

    @Transient
    open var createTime: Date = Date()

    @Transient
    open var updateTime: Date = Date()

    override fun toString(): String {
        return "id=$id, name=$name, delete_flag=$delete_flag, create_time=$createTime, update_time=$updateTime"
    }

}

//region 饮食
@BaseEntity
open class DietPresetEntity: BasePresetEntity() {

    var energy_kcal: Double = 0.0
    var protein: Double = 0.0
    var fat: Double = 0.0
    var carbohydrate: Double = 0.0
    var quantity: Double = 0.0
    var unit: Int = 0

    override fun toString(): String {
        return "[energy_kcal=$energy_kcal, protein=$protein,fat=$fat, carbohydrate=$carbohydrate, quantity=$quantity, unit=$unit, ${super.toString()}]"
    }
    override fun getEventDesc(splitter: String?): String {
        return name
    }
}

@Entity
class DietSystemPresetEntity: DietPresetEntity() {
    override fun toString(): String {
        return "${this.javaClass.simpleName}${super.toString()}"
    }
}

@Entity
class DietUserPresetEntity: DietPresetEntity() {
    override fun toString(): String {
        return "${this.javaClass.simpleName}${super.toString()}"
    }
}

//endregion

//region 运动
@BaseEntity
open class SportPresetEntity: BasePresetEntity() {
    var intensityCategoryName: String? = null //强度分类
    var hourKcalPerKg: Double = 0.0 // 每小时单位公斤消费千卡数

    override fun toString(): String {
        return "[intensity_category_name=$intensityCategoryName, hour_kcal_per_kg=$hourKcalPerKg, ${super.toString()}]"
    }
    override fun getEventDesc(splitter: String?): String {
        return name
    }
}
@Entity
class SportSysPresetEntity: SportPresetEntity() {
    override fun toString(): String {
        return "${this.javaClass.simpleName}${super.toString()}"
    }
}
@Entity
class SportUsrPresetEntity: SportPresetEntity() {
    override fun toString(): String {
        return "${this.javaClass.simpleName}${super.toString()}"
    }
}

//endregion

//region 用药
@BaseEntity
open class MedicinePresetEntity: BasePresetEntity() {

    var categoryName: String? = null //类别名称
    var tradeName: String? = null //商品名
    var manufacturer: String? = null //厂商
    var englishName: String? = null //英文名称

    override fun toString(): String {
        return "[${super.toString()}, category_name='$categoryName', " +
                "trade_name='$tradeName', manufacturer='$manufacturer', english_name='$englishName']"
    }

    override fun getEventDesc(splitter: String?): String {
        val ext = tradeName?:manufacturer?:""
        return name.plus(if(ext.isNotEmpty()) "(${ext})" else "")
    }

}

@Entity
class MedicineSysPresetEntity: MedicinePresetEntity() {
    override fun toString(): String {
        return "${this.javaClass.simpleName}${super.toString()}"
    }
}
@Entity
class MedicineUsrPresetEntity: MedicinePresetEntity() {
    override fun toString(): String {
        return "${this.javaClass.simpleName}${super.toString()}"
    }
}
//endregion

//region 胰岛素
@BaseEntity
open class InsulinPresetEntity: BasePresetEntity() {

    var categoryName: String? = null //类别名称
    var tradeName: String? = null //商品名
    var manufacturer: String? = null //厂商
    var comment: String = ""

    override fun toString(): String {
        return "[${super.toString()}, category_name='$categoryName', " +
                "trade_name='$tradeName', manufacturer='$manufacturer', comment='$comment']"
    }

    override fun getEventDesc(splitter: String?): String {
        val ext = tradeName?.ifEmpty { manufacturer?.ifEmpty { "" } }
        return name.plus(if (ext?.isNotEmpty() == true) "(${ext})" else "")
    }

}

@Entity
class InsulinSystemPresetEntity: InsulinPresetEntity() {
    override fun toString(): String {
        return "${this.javaClass.simpleName}${super.toString()}"
    }
}

@Entity
class InsulinUserPresetEntity: InsulinPresetEntity() {
    override fun toString(): String {
        return "${this.javaClass.simpleName}${super.toString()}"
    }
}
//endregion
