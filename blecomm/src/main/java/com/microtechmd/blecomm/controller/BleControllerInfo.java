package com.microtechmd.blecomm.controller;

import androidx.annotation.NonNull;

import java.util.Objects;

public class BleControllerInfo {
    public String address;
    public String name;
    public String sn;
    public int rssi;

    public BleControllerInfo(String address, String name, String sn, int rssi) {
        this.address = address;
        this.name = name;
        this.sn = sn;
        this.rssi = rssi;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BleControllerInfo)) return false;
        BleControllerInfo that = (BleControllerInfo) o;
        return rssi == that.rssi && Objects.equals(address, that.address) && Objects.equals(name, that.name) && Objects.equals(sn, that.sn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(address, name, sn, rssi);
    }

    @NonNull
    @Override
    public String toString() {
        return "BleControllerInfo{" +
                "address='" + address + '\'' +
                ", name='" + name + '\'' +
                ", sn='" + sn + '\'' +
                ", rssi=" + rssi +
                '}';
    }
}
