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
    var id: String? = null
    var phoneNumber: String? = null
    var emailAddress: String? = null
    var registerTime: String? = null
    var authorizedUserAlias: String? = null
    var avatar: String? = null

    constructor(parcel: Parcel) : this() {
        id = parcel.readString()
        phoneNumber = parcel.readString()
        emailAddress = parcel.readString()
        registerTime = parcel.readString()
        authorizedUserAlias = parcel.readString()
        avatar = parcel.readString()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(phoneNumber)
        parcel.writeString(emailAddress)
        parcel.writeString(registerTime)
        parcel.writeString(authorizedUserAlias)
        parcel.writeString(avatar)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun toString(): String {
        return "UserEntity(idx=$idx, id=$id, phoneNumber=$phoneNumber, emailAddress=$emailAddress, registerTime=$registerTime, authorizedUserAlias=$authorizedUserAlias)"
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