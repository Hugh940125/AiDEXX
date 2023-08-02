package com.microtechmd.blecomm.parser;


public class AidexXInstantHistoryEntity {
    public AidexXAbstractEntity abstractEntity;
    public AidexXHistoryEntity history;
    public AidexXRawHistoryEntity raw;

    public AidexXInstantHistoryEntity(AidexXAbstractEntity abstractEntity, AidexXHistoryEntity history, AidexXRawHistoryEntity raw) {
        this.abstractEntity = abstractEntity;
        this.history = history;
        this.raw = raw;
    }

    @Override
    public String toString() {
        return "AidexXInstantHistoryEntity{" +
                "abstractEntity=" + abstractEntity +
                ", history=" + history +
                ", raw=" + raw +
                '}';
    }
}
