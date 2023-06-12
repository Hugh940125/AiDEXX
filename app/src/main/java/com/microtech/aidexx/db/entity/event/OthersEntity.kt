package com.microtech.aidexx.db.entity.event

import android.content.res.Resources
import com.microtech.aidexx.data.LocalManager
import com.microtech.aidexx.db.entity.BaseEventEntity
import com.microtech.aidexx.utils.LanguageUnitManager
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Index
import io.objectbox.annotation.IndexType
import java.util.UUID


@Entity
class OthersEntity : BaseEventEntity, EventActions {


    @Index(type = IndexType.HASH)
    var otherId: String? = UUID.randomUUID().toString().replace("-", "")

    var content: String = ""
    var deleteFlag: Int? = 0
        set(value) {
            field = value
            if (1 == field) {
                deleteStatus = 2
            }
        }

    constructor() {
        this.language = LocalManager.getCurLanguageTag()
    }

    override fun getEventDescription(res: Resources): String {
        return ""
    }

    override fun getValueDescription(res: Resources): String {
        return if (content.length > 35) {
            content.substring(0, 35) + "..."
        } else {
            content
        }
    }

    override fun getEventDesc(spliter: String?): String = content.replace("\n", " ")

    override fun toString(): String {
        return "OthersEntity[idx=$idx, state=$state, id=$id, recordIndex=$recordIndex, " +
                "deleteStatus=$deleteStatus, otherId=$otherId, startTime=$createTime, content=$content, authorizationId=$userId]"
    }

    override fun hashCode(): Int {
        return otherId.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return other?.let {
            it is OthersEntity && it.otherId == this.otherId
        } ?: false
    }

}