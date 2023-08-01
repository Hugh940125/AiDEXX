package com.microtechmd.blecomm.parser;

import java.util.List;

public class AidexXBroadcastEntity {
    public int timeOffset;              // minutes, based on Session Start Time
    public int status;                   // AidexxStatus in aidexxconstants.h
    public int calTemp;                  // AidexxCalTemp in aidexxconstants.h
    public int trend;                     // mg/dL/min; -128: Unknow
    public List<AidexXHistoryEntity> history; //histories, max to 3
    public int historyCount;             // count of history entities in this broadcast
    public int calIndex;

    public AidexXBroadcastEntity(int timeOffset, int status, int calTemp, int trend, List<AidexXHistoryEntity> history, int historyCount, int calIndex) {
        this.timeOffset = timeOffset;
        this.status = status;
        this.calTemp = calTemp;
        this.trend = trend;
        this.history = history;
        this.historyCount = historyCount;
        this.calIndex = calIndex;
    }

    @Override
    public String toString() {
        return "AidexXBroadcastEntity{" +
                "timeOffset=" + timeOffset +
                ", status=" + status +
                ", calTemp=" + calTemp +
                ", trend=" + trend +
                ", history=" + history +
                ", historyCount=" + historyCount +
                ", calIndex=" + calIndex +
                '}';
    }
}
