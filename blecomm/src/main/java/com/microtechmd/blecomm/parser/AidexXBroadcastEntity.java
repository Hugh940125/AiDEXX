package com.microtechmd.blecomm.parser;

import java.util.List;

public class AidexXBroadcastEntity {
    public AidexXAbstractEntity abstractEntity;
    public List<AidexXHistoryEntity> history; //histories, max to 3
    public int historyCount;             // count of history entities in this broadcast

    public AidexXBroadcastEntity(AidexXAbstractEntity abstractEntity, List<AidexXHistoryEntity> history, int historyCount) {
        this.abstractEntity = abstractEntity;
        this.history = history;
        this.historyCount = historyCount;
    }

    @Override
    public String toString() {
        return "AidexXBroadcastEntity{" +
                "abstractEntity=" + abstractEntity +
                ", history=" + history +
                ", historyCount=" + historyCount +
                '}';
    }
}
