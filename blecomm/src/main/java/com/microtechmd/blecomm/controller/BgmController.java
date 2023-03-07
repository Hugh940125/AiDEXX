package com.microtechmd.blecomm.controller;

public class BgmController extends BleController {

    public BgmController() {
        constructor();
    }

    @Override
    protected void finalize() throws Throwable {
        destructor();
        super.finalize();
    }

    private native void constructor();
    private native void destructor();

    public native int getDeviceInfo();
    public native int getHistory(int index);
}
