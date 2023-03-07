package com.microtechmd.blecomm.parser;

public interface PumpHistoryEntity {
    void _setDatetime(String datetime);
    void _setRemainingCapacity(int remainingCapacity);
    void _setRemainingInsulin(int remainingInsulin);
    void _setBasal(int basal);
    void _setBolus(int bolus);
    void _setEventIndex(int eventIndex);
    void _setEventPort(int eventPort);
    void _setEventType(int eventType);
    void _setEventLevel(int eventLevel);
    void _setEventValue(int eventValue);
    void _setEvent(int eventValue);
    void _setAuto(boolean autoMode);
    void _setBasalUnitPerHour(float basalUnitPerHour);
    void _setBolusUnitPerHour(float bolusUnitPerHour);
}