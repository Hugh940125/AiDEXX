package com.microtech.aidexx.db.entity

import android.os.Build
import android.os.Parcel
import android.os.Parcelable

class ShareUserEntity() : Parcelable {

    var lastData: RealCgmHistoryEntity? = null

//    var authorizedUser: UserEntity? = null
    var user: UserEntity? = null

//    var authorizedUserAlias: String? = null
    var userAlias: String? = null

    var id: String? = null
    var hide: Boolean = false

    var pushState = false
    var emergeState = false
    var isLooking = false
    var sn: String? = null
    var startUp: Long? = null
    var et: Int? = null
    var source: Int = 0

    fun getDisplayName() = userAlias?.ifEmpty { null }
        ?: user?.emailAddress?.ifBlank { null }
        ?: user?.phoneNumber

    constructor(parcel: Parcel) : this() {
        userAlias = parcel.readString()
        id = parcel.readString()
        hide = parcel.readByte() != 0.toByte()
        pushState = parcel.readByte() != 0.toByte()
        emergeState = parcel.readByte() != 0.toByte()
        user = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            parcel.readParcelable(
                UserEntity::javaClass.javaClass.classLoader,
                UserEntity::class.java
            )
        } else {
            parcel.readParcelable(UserEntity::javaClass.javaClass.classLoader)
        }
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(userAlias)
        parcel.writeString(id)
        parcel.writeByte(if (hide) 1 else 0)
        parcel.writeByte(if (pushState) 1 else 0)
        parcel.writeByte(if (emergeState) 1 else 0)
        parcel.writeParcelable(user, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ShareUserEntity> {
        override fun createFromParcel(parcel: Parcel): ShareUserEntity {
            return ShareUserEntity(parcel)
        }

        override fun newArray(size: Int): Array<ShareUserEntity?> {
            return arrayOfNulls(size)
        }
    }
}
