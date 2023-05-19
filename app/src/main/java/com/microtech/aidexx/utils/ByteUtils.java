package com.microtech.aidexx.utils;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

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
        int s = Byte.toUnsignedInt(data[6]);
        if (year != 0 || month != 0 || day != 0 || hour != 0 || min != 0 || s != 0) {
            Calendar ca = Calendar.getInstance(Locale.getDefault());
            ca.set(Calendar.YEAR, year);
            ca.set(Calendar.MONTH, month - 1);
            ca.set(Calendar.DAY_OF_MONTH, day);
            ca.set(Calendar.HOUR_OF_DAY, hour);
            ca.set(Calendar.MINUTE, min);
            ca.set(Calendar.SECOND, s);
            ca.set(Calendar.MILLISECOND, 0);
            return ca.getTime();
        }
        return null;
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
}
