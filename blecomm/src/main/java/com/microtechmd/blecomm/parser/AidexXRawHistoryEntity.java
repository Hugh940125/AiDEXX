package com.microtechmd.blecomm.parser;

public class AidexXRawHistoryEntity {

    public int timeOffset;
    public float i1;
    public float i2;
    public float vc;
    public boolean isValid;

    public AidexXRawHistoryEntity(int timeOffset, float i1, float i2, float vc, boolean isValid) {
        this.timeOffset = timeOffset;
        this.i1 = i1;
        this.i2 = i2;
        this.vc = vc;
        this.isValid = isValid;
    }
}
