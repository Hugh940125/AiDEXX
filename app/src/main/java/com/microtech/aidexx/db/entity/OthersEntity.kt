package com.microtech.aidexx.db.entity

import android.content.res.Resources
import com.microtech.aidexx.utils.LanguageUnitManager
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.annotation.Index
import io.objectbox.annotation.IndexType
import java.util.*


@Entity
class OthersEntity : EventEntity, EventActions {
    @Id
    override var idx: Long? = null

    @Index
    override var state: Int = 0
    override var id: String? = null

    @Index
    override var recordIndex: Long? = null

    @Index
    override var deleteStatus: Int = 0

    @Index(type = IndexType.HASH)
    var recordUuid: String? = UUID.randomUUID().toString().replace("-", "")

    override var recordId: String? = null

    override var createTime: Date = Date()

    var content: String = ""
    var deleteFlag: Int? = 0
        set(value) {
            field = value
            if (1 == field) {
                deleteStatus = 2
            }
        }

    @Index(type = IndexType.HASH)
    override var userId: String? = null

    override var language: String = ""
    override var uploadState: Int = 0

    constructor() {
        this.language = LanguageUnitManager.getCurrentLanguageCode()
    }

    @Transient
    override var time: Date = createTime
        get() {
            return createTime
        }
        set(time) {
            field = time
            createTime = time
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
                "deleteStatus=$deleteStatus, recordUuid=$recordUuid, startTime=$createTime, content=$content, authorizationId=$userId]"
    }

}