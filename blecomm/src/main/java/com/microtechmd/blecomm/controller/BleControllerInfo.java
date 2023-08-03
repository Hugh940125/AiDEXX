package com.microtechmd.blecomm.controller;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;

import java.util.Arrays;
import java.util.Objects;

public class BleControllerInfo implements Parcelable {
    public int type;
    public String address;
    public String name;
    public String sn;
    public int rssi;
    public byte[] params;
    public boolean isPaired;

    public BleControllerInfo() {
    }

    public BleControllerInfo(int type, String address, String name, String sn) {
        this.type = type;
        this.address = address;
        this.name = name;
        this.sn = sn;
    }

    public BleControllerInfo(int type, String address, String name, String sn, int rssi, byte[] params) {
        this.type = type;
        this.address = address;
        this.name = name;
        this.sn = sn;
        this.rssi = rssi;
        this.params = params;
    }

    protected BleControllerInfo(Parcel in) {
        address = in.readString();
        name = in.readString();
        sn = in.readString();
        rssi = in.readInt();
        params = in.createByteArray();
    }

    public static final Creator<BleControllerInfo> CREATOR = new Creator<BleControllerInfo>() {
        @Override
        public BleControllerInfo createFromParcel(Parcel in) {
            return new BleControllerInfo(in);
        }

        @Override
        public BleControllerInfo[] newArray(int size) {
            return new BleControllerInfo[size];
        }
    };

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BleControllerInfo)) return false;
        BleControllerInfo that = (BleControllerInfo) o;
        return Objects.equals(address, that.address) && Objects.equals(name, that.name) && Objects.equals(sn, that.sn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(address, name, sn);
    }

    @Override
    public String toString() {
        return "BleControllerInfo{" +
                "type=" + type +
                ", address='" + address + '\'' +
                ", name='" + name + '\'' +
                ", sn='" + sn + '\'' +
                ", rssi=" + rssi +
                ", params=" + Arrays.toString(params) +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(address);
        dest.writeString(name);
        dest.writeString(sn);
        dest.writeInt(rssi);
        dest.writeByteArray(params);
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSn() {
        return sn;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }

    public int getRssi() {
        return rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }

    public byte[] getParams() {
        return params;
    }

    public void setParams(byte[] params) {
        this.params = params;
    }
}
