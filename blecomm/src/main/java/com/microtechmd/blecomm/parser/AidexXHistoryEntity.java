package com.microtechmd.blecomm.parser;



public class AidexXHistoryEntity {
    public int timeOffset;
    public int glucose;
    public int status;
    public int quality;
    public int isValid;

    public AidexXHistoryEntity() {
    }

    public AidexXHistoryEntity(int timeOffset, int glucose, int status, int quality, int isValid) {
        this.timeOffset = timeOffset;
        this.glucose = glucose;
        this.status = status;
        this.quality = quality;
        this.isValid = isValid;
    }

    @Override
    public String toString() {
        return "history:[" +
                "timeOffset=" + timeOffset +
                ", glucose=" + glucose +
                ", status=" + status +
                ", quality=" + quality +
                ", isValid=" + isValid +
                ']';
    }
}
