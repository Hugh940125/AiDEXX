package com.microtechmd.blecomm.parser;/**
 * @Description:
 * @Author: Hugh
 * @CreateDate: 2023/8/2 17:38
 */

/**
 *@date 2023/8/2
 *@author Hugh
 *@desc
 */
public class AidexXAbstractEntity {
    public int timeOffset;              // minutes, based on Session Start Time
    public int status;                   // AidexxStatus in aidexxconstants.h
    public int calTemp;                  // AidexxCalTemp in aidexxconstants.h
    public int trend;                     // mg/dL/min; -128: Unknow
    public int calIndex;

    public AidexXAbstractEntity(int timeOffset, int status, int calTemp, int trend, int calIndex) {
        this.timeOffset = timeOffset;
        this.status = status;
        this.calTemp = calTemp;
        this.trend = trend;
        this.calIndex = calIndex;
    }

    @Override
    public String toString() {
        return "AidexXAbstractEntity{" +
                "timeOffset=" + timeOffset +
                ", status=" + status +
                ", calTemp=" + calTemp +
                ", trend=" + trend +
                ", calIndex=" + calIndex +
                '}';
    }
}
