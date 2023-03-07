package com.microtechmd.blecomm.parser;

import java.util.Date;
import java.util.List;

public class AidexXBroadcastEntity {

    public int timeOffset;
    public int historyCount;
    public int status;
    public int calTemp;
    public int trend;
    public List<AidexXHistoryEntity> history;

    public long datetime;
    public long receivedTime;

    public AidexXBroadcastEntity(int timeOffset, int historyCount, int status, int calTemp, int trend, List<AidexXHistoryEntity> history) {
        this.timeOffset = timeOffset;
        this.historyCount = historyCount;
        this.status = status;
        this.calTemp = calTemp;
        this.trend = trend;
        this.history = history;
    }

    @Override
    public String toString() {
        return "" +
                "historyCount=" + historyCount +
                ", status=" + status +
                ", calTemp=" + calTemp +
                ", trend=" + trend +
                ", history=" + history +
                '}';
    }
}
