package com.microtechmd.blecomm;

import java.util.Timer;
import java.util.TimerTask;

public abstract class NoConnectionBleAdapter extends BleAdapter {

    @Override
    public void executeStartScan() {}

    @Override
    public void executeStopScan() {}

    @Override
    public boolean isReadyToConnect(String mac) {
        return true;
    }

    @Override
    public void executeConnect(String mac) {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                onConnectSuccess();
            }
        },200);
    }

    @Override
    public void executeDisconnect() {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                onDisconnected();
            }
        },200);
    }
}
