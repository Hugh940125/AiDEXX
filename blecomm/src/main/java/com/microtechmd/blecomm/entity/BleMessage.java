package com.microtechmd.blecomm.entity;

import java.io.Serializable;

public class BleMessage implements Serializable {
    private int resCode;
    private int operation;
    private boolean success;
    private byte[] data;

    public BleMessage(int op) {
        this(op, true, null);
    }

    public BleMessage(int op, boolean success) {
        this(op, success, null);
    }

    public BleMessage(int op, boolean success, byte[] data) {
        this.operation = op;
        this.success = success;
        this.data = data;
    }

    public BleMessage(int op, boolean success, byte[] data, int resCode) {
        this.operation = op;
        this.success = success;
        this.data = data;
        this.resCode = resCode;
    }

    public int getResCode() {
        return resCode;
    }

    public void setResCode(int resCode) {
        this.resCode = resCode;
    }

    public int getOperation() {
        return operation;
    }

    public void setOperation(int operation) {
        this.operation = operation;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }
}