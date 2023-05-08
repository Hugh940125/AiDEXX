package com.microtechmd.blecomm.parser;

public class AidexXCalibrationEntity {

    public int index;
    public int timeOffset;
    public float cf;
    public float offset;
    public float referenceGlucose;
    public int isValid;

    public AidexXCalibrationEntity(int index, int timeOffset, int cf, int offset, float referenceGlucose, int isValid) {
        this.index = index;
        this.timeOffset = timeOffset;
        this.referenceGlucose = referenceGlucose;
        this.isValid = isValid;
        this.cf = cf;
        this.offset = offset;
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
