package com.microtechmd.blecomm.parser;


public class AidexXInstantHistoryEntity {

    public int status;                   // AidexxStatus in aidexxconstants.h
    public int calTemp;                  // AidexxCalTemp in aidexxconstants.h
    public int trend;
    public int timeOffset;
    public int calIndex;
    public AidexXHistoryEntity history;
    public AidexXRawHistoryEntity raw;

    public AidexXInstantHistoryEntity(int status, int calTemp, int trend, int timeOffset, int calIndex, AidexXHistoryEntity history, AidexXRawHistoryEntity raw) {
        this.status = status;
        this.calTemp = calTemp;
        this.trend = trend;
        this.timeOffset = timeOffset;
        this.calIndex = calIndex;
        this.history = history;
        this.raw = raw;
    }

    @Override
    public String toString() {
        return "{" +
                "status=" + status +
                ", calTemp=" + calTemp +
                ", trend=" + trend +
                ", timeOffset=" + timeOffset +
                ", calIndex=" + calIndex +
                ", history=" + history +
                ", raw=" + raw +
                '}';
    }
}
