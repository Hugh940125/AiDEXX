package com.microtechmd.blecomm.parser;

import androidx.annotation.NonNull;

import java.util.List;

public class AidexXFullBroadcastEntity {
    public List<AidexXHistoryEntity> history;
    public int historyTimeOffset;       // minutes, based on Session Start Time
    public int calTimeOffset;           // minutes, based on Session Start Time
    public int historyCount;             // count of history entities in this broadcast
    public int status;                   // AidexxStatus in aidexxconstants.h
    public int calTemp;                  // AidexxCalTemp in aidexxconstants.h
    public int trend;

    public AidexXFullBroadcastEntity(int historyTimeOffset, int status, int calTemp, int trend, List<AidexXHistoryEntity> history, int historyCount, int calTimeOffset) {
        this.history = history;
        this.historyTimeOffset = historyTimeOffset;
        this.calTimeOffset = calTimeOffset;
        this.historyCount = historyCount;
        this.status = status;
        this.calTemp = calTemp;
        this.trend = trend;
    }

    @NonNull
    @Override
    public String toString() {
        return '{' +
                "history=" + history +
                ", historyTimeOffset=" + historyTimeOffset +
                ", calTimeOffset=" + calTimeOffset +
                ", historyCount=" + historyCount +
                ", status=" + status +
                ", calTemp=" + calTemp +
                ", trend=" + trend +
                '}';
    }
}
