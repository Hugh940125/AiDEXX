package com.microtechmd.blecomm.parser;

public class AidexXCalibrationEntity {

    int index;
    int timeOffset;
    float referenceGlucose;
    boolean isValid;

    public AidexXCalibrationEntity(int index, int timeOffset, float referenceGlucose, boolean isValid) {
        this.index = index;
        this.timeOffset = timeOffset;
        this.referenceGlucose = referenceGlucose;
        this.isValid = isValid;
    }
}
