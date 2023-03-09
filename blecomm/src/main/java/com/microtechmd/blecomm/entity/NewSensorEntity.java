package com.microtechmd.blecomm.entity;/**
 * @Description:
 * @Author: Hugh
 * @CreateDate: 2023/3/9 17:52
 */

/**
 *@date 2023/3/9
 *@author Hugh
 *@desc
 */
public class NewSensorEntity {
    AidexXDatetimeEntity aidexXDatetimeEntity;
    Boolean isNew;
    Long dateTime;

    public NewSensorEntity(AidexXDatetimeEntity aidexXDatetimeEntity) {
        this.aidexXDatetimeEntity = aidexXDatetimeEntity;
    }

    public NewSensorEntity(Boolean isNew, Long dateTime) {
        this.isNew = isNew;
        this.dateTime = dateTime;
    }

    public AidexXDatetimeEntity getAidexXDatetimeEntity() {
        return aidexXDatetimeEntity;
    }

    public Boolean getNew() {
        return isNew;
    }

    public Long getDateTime() {
        return dateTime;
    }
}
