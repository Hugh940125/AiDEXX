package com.microtech.aidexx.db.entity.event.preset

import com.microtech.aidexx.db.entity.event.EventActions
import io.objectbox.annotation.BaseEntity
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.annotation.Index
import java.util.Date
import java.util.UUID

interface BaseSysPreset

@BaseEntity
open class BasePresetEntity: EventActions {

    @Id
    open var idx: Long = 0 // 本地id
    @Index
    open var name: String = ""
    var userId: String = "" // 登录用户id 为空时系统预设
    open var deleteFlag: Int = 0  //是否删除，0：未删除，1：已删除

    @Transient
    var isUserInputType = false //是否是用户输入的待选项
    @Transient
    open var createTime: Date = Date()
    @Transient
    open var updateTime: Date = Date()

    @Index
    var language: String = ""

    override fun toString(): String {
        return "idx=$idx, name=$name, delete_flag=$deleteFlag, create_time=$createTime, update_time=$updateTime"
    }

    fun genUuid() = UUID.randomUUID().toString().replace("-", "")

    fun isUserPreset() = this !is BaseSysPreset

    open fun getServerPresetId(): Long? = null
    open fun setServerPresetId(serverId: Long?){}

}

//region 饮食
@BaseEntity
open class DietPresetEntity: BasePresetEntity() {

    var protein: Double = 0.0 //number 非必须 0.111111 蛋白质， 克 mock: @float
    var fat: Double = 0.0 //number 非必须 0.111111 脂肪， 克 mock: @float
    var carbohydrate: Double = 0.0 //number 非必须 0.111111 碳水， 克 mock: @float
    var quantity: Double = 0.0 //number 非必须 0.111111 单位数量 mock: @float
    var unit: Int = 0 //integer 非必须 1 单位，0：克，1：千克，2：毫升，3：升，4：两，5：斤 mock: @integer

    override fun toString(): String {
        return "[protein=$protein,fat=$fat, carbohydrate=$carbohydrate, quantity=$quantity, unit=$unit, ${super.toString()}]"
    }
    override fun getEventDesc(splitter: String?): String {
        return name
    }
}

@Entity
class DietSysPresetEntity: DietPresetEntity(), BaseSysPreset {

    var energyKcal: Double = 0.0	//number 非必须 0.111111 能量，千卡 mock: @float
    var foodSysPresetId: Long? = null	 //integer 必须 1 mock: @integer
    override fun getServerPresetId(): Long? = foodSysPresetId
    override fun toString(): String {
        return "${this.javaClass.simpleName}${super.toString()}"
    }
}

@Entity
class DietUsrPresetEntity: DietPresetEntity() {

    var foodUserPresetId: String = genUuid()	//string 必须 string 饮食用户预设id，前端生成 mock: @string

    var autoIncrementColumn: Long? = null	 //integer 非必须 1 自增列 mock: @integer
    override fun getServerPresetId(): Long? = autoIncrementColumn

    override fun setServerPresetId(serverId: Long?) {
        autoIncrementColumn = serverId
    }

    override fun toString(): String {
        return "${this.javaClass.simpleName}${super.toString()}"
    }

    override fun hashCode(): Int {
        return foodUserPresetId.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return other?.let {
            it is DietUsrPresetEntity && it.foodUserPresetId == this.foodUserPresetId
        } ?: false
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
class SportSysPresetEntity: SportPresetEntity(), BaseSysPreset {

    var exerciseSysPresetId: Long? = null	//number 必须 表id mock: @integer
    override fun getServerPresetId(): Long? = exerciseSysPresetId
    override fun toString(): String {
        return "${this.javaClass.simpleName}${super.toString()}"
    }
}
@Entity
class SportUsrPresetEntity: SportPresetEntity() {

    var autoIncrementColumn: Long? = null	 //integer 非必须 1
    var exerciseUserPresetId: String = genUuid()//	string 必须 string 这个id前端生成
    override fun getServerPresetId(): Long? = autoIncrementColumn

    override fun setServerPresetId(serverId: Long?) {
        autoIncrementColumn = serverId
    }

    override fun toString(): String {
        return "${this.javaClass.simpleName}${super.toString()}"
    }

    override fun hashCode(): Int {
        return exerciseUserPresetId.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return other?.let {
            it is SportUsrPresetEntity && it.exerciseUserPresetId == this.exerciseUserPresetId
        } ?: false
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
class MedicineSysPresetEntity: MedicinePresetEntity(), BaseSysPreset {

    var medicationSysPresetId: Long? = null	//integer 非必须 1 mock: @integer
    override fun getServerPresetId(): Long? = medicationSysPresetId
    override fun toString(): String {
        return "${this.javaClass.simpleName}${super.toString()}"
    }
}
@Entity
class MedicineUsrPresetEntity: MedicinePresetEntity() {

    var medicationUserPresetId: String = genUuid()	//string 必须 string 前端生成 mock: @string

    var autoIncrementColumn: Long? = null	//integer 非必须 1 mock: @integer
    override fun getServerPresetId(): Long? = autoIncrementColumn

    override fun setServerPresetId(serverId: Long?) {
        autoIncrementColumn = serverId
    }

    override fun toString(): String {
        return "${this.javaClass.simpleName}${super.toString()}"
    }

    override fun hashCode(): Int {
        return medicationUserPresetId.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return other?.let {
            it is MedicineUsrPresetEntity && it.medicationUserPresetId == this.medicationUserPresetId
        } ?: false
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
class InsulinSysPresetEntity: InsulinPresetEntity(), BaseSysPreset {

    var insulinSysPresetId: Long? = null	// integer 非必须 1 自增主键 mock: @integer

    override fun getServerPresetId(): Long? = insulinSysPresetId
    override fun toString(): String {
        return "${this.javaClass.simpleName}${super.toString()}"
    }
}

@Entity
class InsulinUsrPresetEntity: InsulinPresetEntity() {

    var insulinUserPresetId: String = genUuid() //	string 必须 string mock: @string
    var autoIncrementColumn: Long? = null	//integer 非必须 1 mock: @integer
    override fun getServerPresetId(): Long? = autoIncrementColumn

    override fun setServerPresetId(serverId: Long?) {
        autoIncrementColumn = serverId
    }

    override fun toString(): String {
        return "${this.javaClass.simpleName}${super.toString()}"
    }

    override fun hashCode(): Int {
        return insulinUserPresetId.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return other?.let {
            it is InsulinUsrPresetEntity && it.insulinUserPresetId == this.insulinUserPresetId
        } ?: false
    }

}
//endregion
