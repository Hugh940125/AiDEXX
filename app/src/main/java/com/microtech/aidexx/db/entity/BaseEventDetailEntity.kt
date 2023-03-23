package com.microtech.aidexx.db.entity

import com.google.gson.Gson
import io.objectbox.converter.PropertyConverter
import java.lang.reflect.Type

abstract class BaseEventDetailEntity: EventActions,
    PropertyConverter<MutableList<in BaseEventDetailEntity>, String> {

    var id: Long = 0
    var presetType: Int = 0 // 类型，0：系统，1：用户
    var name: String = ""
    var unitStr: String = ""
    override fun toString(): String {
        return "id=$id, type=$presetType, name='$name'"
    }

    abstract fun getCurrClassMutableListType(): Type

    override fun convertToEntityProperty(databaseValue: String?): MutableList<in BaseEventDetailEntity> {

        return if(databaseValue.isNullOrEmpty())
            ArrayList()
        else
            Gson().fromJson(databaseValue, getCurrClassMutableListType())
    }

    override fun convertToDatabaseValue(entityProperty: MutableList<in BaseEventDetailEntity>): String {
        return entityProperty.let {
            Gson().toJson(entityProperty)
        } ?: ""
    }


}