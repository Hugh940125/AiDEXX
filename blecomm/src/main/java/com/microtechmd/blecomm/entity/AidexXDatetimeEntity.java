package com.microtechmd.blecomm.entity;

import java.util.Calendar;
import java.util.Date;

public class AidexXDatetimeEntity {

    int year;
    int month;
    int day;
    int hour;
    int minute;
    int second;
    int timeZone;
    int dstOffset;

    public AidexXDatetimeEntity(Calendar calendar) {
        Date date = new Date();
        timeZone = calendar.getTimeZone().getOffset(date.getTime()) / 1000 / 60 / 60 * 4;
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH) + 1;
        day = calendar.get(Calendar.DATE);
        hour = calendar.get(Calendar.HOUR_OF_DAY);
        minute = calendar.get(Calendar.MINUTE);
        second = calendar.get(Calendar.SECOND);
    }
}
