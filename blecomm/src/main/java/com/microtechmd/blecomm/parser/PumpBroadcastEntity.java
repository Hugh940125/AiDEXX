package com.microtechmd.blecomm.parser;

public interface PumpBroadcastEntity {

    void _setExpired(boolean historyExpired);


    void _setHistory(PumpHistoryEntity history);
}