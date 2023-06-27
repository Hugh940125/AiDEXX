package com.microtech.aidexx.db.entity

import android.os.Parcel
import android.os.Parcelable
import com.microtech.aidexx.utils.ThresholdManager
import java.util.TimeZone

class SettingsEntity() : Parcelable {
    var language: String? = "zh-CN"
    var timeZone: String? = TimeZone.getDefault().id
    var userId: String? = null
    var unit = 0
    var theme = 1
    var userSettingId: String? = null
    var alertType: Int = 3
    var alertRate: Int = 30
    var lowAlertSwitch = 0
    var lowLimitMg = ThresholdManager.DEFAULT_HYPO
    var highAlertSwitch = 0
    var highLimitMg = ThresholdManager.DEFAULT_HYPER
    var fastUpSwitch = 0
    var isFastDownEnable = 0
    var fastDownSwitch = 0
    var urgentLowAlertSwitch = 0
    var urgentLowMg = ThresholdManager.URGENT_HYPO
    var urgentAlertType = 3
    var urgentAlertRate: Int = 5
    var signalMissingSwitch = 0
    var signalMissingAlertType = 3
    var signalMissingAlertRate = 15
    var version = 0

    constructor(parcel: Parcel) : this() {
        language = parcel.readString()
        timeZone = parcel.readString()
        unit = parcel.readInt()
        theme = parcel.readInt()
        userSettingId = parcel.readString()
        alertType = parcel.readInt()
        alertRate = parcel.readInt()
        lowAlertSwitch = parcel.readInt()
        lowLimitMg = parcel.readFloat()
        highAlertSwitch = parcel.readInt()
        highLimitMg = parcel.readFloat()
        fastUpSwitch = parcel.readInt()
        isFastDownEnable = parcel.readInt()
        fastDownSwitch = parcel.readInt()
        urgentLowAlertSwitch = parcel.readInt()
        urgentLowMg = parcel.readFloat()
        urgentAlertType = parcel.readInt()
        urgentAlertRate = parcel.readInt()
        signalMissingSwitch = parcel.readInt()
        signalMissingAlertType = parcel.readInt()
        signalMissingAlertRate = parcel.readInt()
        version = parcel.readInt()
    }


    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(language)
        parcel.writeString(timeZone)
        parcel.writeInt(unit)
        parcel.writeInt(theme)
        parcel.writeString(userSettingId)
        parcel.writeInt(alertType)
        parcel.writeInt(alertRate)
        parcel.writeInt(lowAlertSwitch)
        parcel.writeFloat(lowLimitMg)
        parcel.writeInt(highAlertSwitch)
        parcel.writeFloat(highLimitMg)
        parcel.writeInt(fastUpSwitch)
        parcel.writeInt(isFastDownEnable)
        parcel.writeInt(fastDownSwitch)
        parcel.writeInt(urgentLowAlertSwitch)
        parcel.writeFloat(urgentLowMg)
        parcel.writeInt(urgentAlertType)
        parcel.writeInt(urgentAlertRate)
        parcel.writeInt(signalMissingSwitch)
        parcel.writeInt(signalMissingAlertType)
        parcel.writeInt(signalMissingAlertRate)
        parcel.writeInt(version)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun toString(): String {
        return "SettingsEntity(language=$language, timeZone=$timeZone, userId=$userId, unit=$unit, theme=$theme, userSettingId=$userSettingId, alertType=$alertType, alertRate=$alertRate, lowAlertSwitch=$lowAlertSwitch, lowLimitMg=$lowLimitMg, highAlertSwitch=$highAlertSwitch, highLimitMg=$highLimitMg, fastUpSwitch=$fastUpSwitch, isFastDownEnable=$isFastDownEnable, fastDownSwitch=$fastDownSwitch, urgentLowAlertSwitch=$urgentLowAlertSwitch, urgentLowMg=$urgentLowMg, urgentAlertType=$urgentAlertType, urgentAlertRate=$urgentAlertRate, signalMissingSwitch=$signalMissingSwitch, signalMissingAlertType=$signalMissingAlertType, signalMissingAlertRate=$signalMissingAlertRate, version=$version)"
    }

    companion object CREATOR : Parcelable.Creator<SettingsEntity> {
        override fun createFromParcel(parcel: Parcel): SettingsEntity {
            return SettingsEntity(parcel)
        }

        override fun newArray(size: Int): Array<SettingsEntity?> {
            return arrayOfNulls(size)
        }
    }
}