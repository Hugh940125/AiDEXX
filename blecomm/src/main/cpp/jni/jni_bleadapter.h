#ifndef JNIBLEADAPTER_H
#define JNIBLEADAPTER_H

#include "../ble.h"

class JniBleAdapter : public Ble {
public:
    JniBleAdapter() : Ble() {}

    using Ble::getCharacteristicUUID;
    using Ble::setDiscoverTimeoutSeconds;
    using Ble::onScanRespond;
    using Ble::onAdvertise;
    using Ble::onConnectSuccess;
    using Ble::onConnectFailure;
    using Ble::onDisconnected;
    using Ble::onReceiveData;

protected:
    void executeStartScan();
    void executeStopScan();
    bool isReadyToConnect(string mac);
    void executeConnect(string mac);
    void executeDisconnect();
    void executeWrite(const char *data, uint16 length);
    void executeWriteCharacteristic(uint16 uuid, const char *data, uint16 length);
    void executeReadCharacteristic(uint16 uuid);
};

#endif //JNIBLEADAPTER_H
