package com.microtechmd.blecomm.parser;

public class AidexXCalibrationEntity {

    public int index;
    public int timeOffset;
    public float cf;
    public float offset;
    public int referenceGlucose;
    public int isValid;

    public AidexXCalibrationEntity(int index, int timeOffset, float cf, float offset, int referenceGlucose, int isValid) {
        this.index = index;
        this.timeOffset = timeOffset;
        this.cf = cf;
        this.offset = offset;
        this.referenceGlucose = referenceGlucose;
        this.isValid = isValid;
    }

    @Override
    public String toString() {
        return "index=" + index +
                ", timeOffset=" + timeOffset +
                ", referenceGlucose=" + referenceGlucose +
                ", cf=" + cf +
                ", offset=" + offset;
    }
}
