package com.microtechmd.blecomm.parser;

public interface BgmHistoryEntity {
    void _setDatetime(String datetime);

    void _setTemperature(int temperature);

    void _setFlag(int flag);

    void _setBgValue(int bgValue);

    void _setReserved(int reserved);

    void _setHypo(boolean hypo);

    void _setHyper(boolean hyper);

    void _setKetone(boolean ketone);

    void _setPreMeal(boolean preMeal);

    void _setPostMeal(boolean postMeal);

    void _setInvalid(boolean invalid);

    void _setControlSolution(boolean controlSolution);

    void _setEventIndex(int eventIndex);

    void _setEventPort(int eventPort);

    void _setEventType(int eventType);

    void _setEventLevel(int eventLevel);

    void _setEventValue(int eventValue);
}