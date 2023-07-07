package com.microtech.aidexx.db.entity

import android.os.Parcel
import android.os.Parcelable
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.annotation.Unique

@Entity
class UserEntity() : Parcelable {


    @Id
    var idx: Long = 0
    @Unique
    var userId: String? = null //必须

    var userInformationId: String? = null //非必须

     var email: String? = null //非必须
     var phone: String? = null //非必须
     var avatar: String? = null //非必须 头像地址

     var name: String? = null //非必须
     var surname: String? = null //非必须
     var fullName: String? = null //非必须
     var middleName: String? = null //非必须
     var givenName: String? = null //非必须
     var gender: Int? = null // 非必须 1男2女
     var birthDate: String? = null// 非必须 UTC格式时间，生日
     var height: Int? = null // 非必须
     var bodyWeight: Int? = null //非必须
     var diabetesType: Int? = null // 非必须 疾病类型
     var diagnosisDate: String? = null //非必须 UTC格式时间
     var complications: String? = null // 非必须 并发症(多选); 0无,1心血管并发症,2视网膜病变,3神经病变,4肾病,5糖尿病足,99其他)

    fun getDisplayName() = name?.ifEmpty { null }
//        ?: "${surname?:""}${givenName?:""}".ifEmpty { null }
        ?: fullName
        ?: getMaskedPhone()
        ?: email
        ?: ""

    fun getMaskedPhone(): String? = phone?.let {
        if (it.length == 11) {
            it.replaceRange(3, 7, "****")
        } else it
    }

    override fun toString(): String {
        return buildString {
            append("UserEntity[")
            append("idx=$idx,")
            append("userId=$userId,")
            append("userInformationId=$userInformationId,")
            append("email=$email,")
            append("phone=$phone,")
            append("avatar=$avatar,")
            append("name=$name,")
            append("surname=$surname,")
            append("middleName=$middleName,")
            append("givenName=$givenName,")
            append("gender=$gender,")
            append("birthDate=$birthDate,")
            append("height=$height,")
            append("bodyWeight=$bodyWeight,")
            append("diabetesType=$diabetesType,")
            append("diagnosisDate=$diagnosisDate,")
            append("complications=$complications]")
        }
    }

    constructor(parcel: Parcel) : this() {
        idx = parcel.readLong()
        userId = parcel.readString()
        userInformationId = parcel.readString()
        email = parcel.readString()
        phone = parcel.readString()
        avatar = parcel.readString()
        name = parcel.readString()
        surname = parcel.readString()
        middleName = parcel.readString()
        givenName = parcel.readString()
        gender = parcel.readValue(Int::class.java.classLoader) as? Int
        birthDate = parcel.readString()
        height = parcel.readValue(Int::class.java.classLoader) as? Int
        bodyWeight = parcel.readValue(Int::class.java.classLoader) as? Int
        diabetesType = parcel.readValue(Int::class.java.classLoader) as? Int
        diagnosisDate = parcel.readString()
        complications = parcel.readString()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(idx)
        parcel.writeString(userId)
        parcel.writeString(userInformationId)
        parcel.writeString(email)
        parcel.writeString(phone)
        parcel.writeString(avatar)
        parcel.writeString(name)
        parcel.writeString(surname)
        parcel.writeString(middleName)
        parcel.writeString(givenName)
        parcel.writeValue(gender)
        parcel.writeString(birthDate)
        parcel.writeValue(height)
        parcel.writeValue(bodyWeight)
        parcel.writeValue(diabetesType)
        parcel.writeString(diagnosisDate)
        parcel.writeString(complications)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<UserEntity> {
        override fun createFromParcel(parcel: Parcel): UserEntity {
            return UserEntity(parcel)
        }

        override fun newArray(size: Int): Array<UserEntity?> {
            return arrayOfNulls(size)
        }
    }

}