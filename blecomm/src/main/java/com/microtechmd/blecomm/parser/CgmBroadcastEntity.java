package com.microtechmd.blecomm.parser;

public interface CgmBroadcastEntity {
    void _setDatetime(long datetime);
    void _setBattery(int battery);
    void _setState(int state);
    void _setGlucose(float glucose);
    void _setPrimary(int primary);
    void _setHistory(CgmHistoryEntity history);
}
