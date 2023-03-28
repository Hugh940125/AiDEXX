package com.microtechmd.blecomm.controller;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.Objects;

public class BleControllerInfo implements Parcelable {
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

    protected BleControllerInfo(Parcel in) {
        address = in.readString();
        name = in.readString();
        sn = in.readString();
        rssi = in.readInt();
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
    }
}
