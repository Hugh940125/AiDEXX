package com.microtech.aidexx.utils;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class ByteUtils {

    /**
     * 截取byte数组   不改变原数组
     *
     * @param b      原数组
     * @param off    偏差值（索引）
     * @param length 长度
     * @return 截取后的数组
     */
    public static byte[] subByte(byte[] b, int off, int length) {
        byte[] b1 = new byte[length];
        System.arraycopy(b, off, b1, 0, length);
        return b1;
    }

    public static Date checkToDate(byte[] data) {
        int year = Byte.toUnsignedInt(data[1]) * 256 + Byte.toUnsignedInt(data[0]);
        int month = Byte.toUnsignedInt(data[2]);
        int day = Byte.toUnsignedInt(data[3]);
        int hour = Byte.toUnsignedInt(data[4]);
        int min = Byte.toUnsignedInt(data[5]);
        int second = Byte.toUnsignedInt(data[6]);
        int timeZoneOffset = Byte.toUnsignedInt(data[7]);
        LogUtils.eAiDex("timeZone " + timeZoneOffset);
        if (year != 0 || month != 0 || day != 0 || hour != 0 || min != 0 || second != 0) {
            Calendar ca = Calendar.getInstance(Locale.getDefault());
            ca.set(Calendar.YEAR, year);
            ca.set(Calendar.MONTH, month - 1);
            ca.set(Calendar.DAY_OF_MONTH, day);
            ca.set(Calendar.HOUR_OF_DAY, hour);
            ca.set(Calendar.MINUTE, min);
            ca.set(Calendar.SECOND, second);
            ca.set(Calendar.MILLISECOND, 0);
            TimeZone timeZone = getTimeZone(timeZoneOffset * 15 * 60 * 1000);
            ca.setTimeZone(timeZone);
            return ca.getTime();
        }
        return null;
    }

    public static TimeZone getTimeZone(int offset) {
        final TimeZone UTC_TIME_ZONE = TimeZone.getTimeZone("UTC");
        String[] ids = TimeZone.getAvailableIDs(offset);
        if (ids == null || ids.length == 0) {
            return UTC_TIME_ZONE;
        }

        String matchingZoneId = ids[0];
        return TimeZone.getTimeZone(matchingZoneId);
    }

    public static Date toDate(byte[] data) {
        int year = Byte.toUnsignedInt(data[1]) * 256 + Byte.toUnsignedInt(data[0]);
        int month = Byte.toUnsignedInt(data[2]);
        int day = Byte.toUnsignedInt(data[3]);
        int hour = Byte.toUnsignedInt(data[4]);
        int min = Byte.toUnsignedInt(data[5]);
        int s = Byte.toUnsignedInt(data[6]);
        Calendar ca = Calendar.getInstance();
        ca.set(Calendar.YEAR, year);
        ca.set(Calendar.MONTH, month - 1);
        ca.set(Calendar.DAY_OF_MONTH, day);
        ca.set(Calendar.HOUR_OF_DAY, hour);
        ca.set(Calendar.MINUTE, min);
        ca.set(Calendar.SECOND, s);
        ca.set(Calendar.MILLISECOND, 0);
        return ca.getTime();
    }

    public static String getDeviceSoftVersion(byte[] data) {
        int major = Byte.toUnsignedInt(data[2]);
        int minor = Byte.toUnsignedInt(data[3]);
        int revision = Byte.toUnsignedInt(data[4]);
        int build = Byte.toUnsignedInt(data[5]);
        return major + "." + minor + "." + revision + "." + build;
    }

    public static int getDeviceType(byte[] data) {
        return Byte.toUnsignedInt(data[1]);
    }
}
