package com.microtech.aidexx.db.entity.event

import com.google.gson.Gson
import io.objectbox.converter.PropertyConverter
import java.lang.reflect.Type

abstract class BaseEventDetail: EventActions,
    PropertyConverter<MutableList<in BaseEventDetail>, String> {

    var id: Long = 0
    var presetType: Int = 0 // 类型，0：系统，1：用户
    var name: String = ""
    var unitStr: String = ""
    override fun toString(): String {
        return "id=$id, type=$presetType, name='$name'"
    }

    abstract fun getCurrClassMutableListType(): Type

    override fun convertToEntityProperty(databaseValue: String?): MutableList<in BaseEventDetail> {

        return if(databaseValue.isNullOrEmpty())
            ArrayList()
        else
            Gson().fromJson(databaseValue, getCurrClassMutableListType())
    }

    override fun convertToDatabaseValue(entityProperty: MutableList<in BaseEventDetail>): String {
        return entityProperty.let {
            Gson().toJson(entityProperty)
        } ?: ""
    }


}