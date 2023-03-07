package com.microtechmd.blecomm.parser;

public interface BgmDeviceEntity {
    void _setSn(String sn);
    void _setEndian(int endian);
    void _setDeviceType(int deviceType);
    void _setModel(int model);
    void _setEdition(String edition);
    void _setCapacity(int capacity);
}
