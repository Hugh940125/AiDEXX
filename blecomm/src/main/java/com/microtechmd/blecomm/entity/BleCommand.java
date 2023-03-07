package com.microtechmd.blecomm.entity;

public class BleCommand {
    private int port;
    private int op;
    private int param;
    private byte[] data;

    public BleCommand(int port, int op, int param, byte[] data) {
        this.port = port;
        this.op = op;
        this.param = param;
        if (data == null) {
            this.data = new byte[]{};
        } else {
            this.data = data;
        }
    }

    public int getPort() {
        return port;
    }

    public int getOp() {
        return op;
    }

    public int getParam() {
        return param;
    }

    public byte[] getData() {
        return data;
    }
}
