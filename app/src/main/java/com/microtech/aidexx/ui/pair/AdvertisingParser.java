package com.microtech.aidexx.ui.pair;

import java.nio.charset.StandardCharsets;

public class AdvertisingParser {

    public static String getName(byte[] data) {
        int length = data.length;
        String name = "";
        for (int i = 0; i < length; ) {
            byte len = data[i++];
            if (len == 0 || len + 1 > length) {
                break;
            }
            byte flag = data[i++];

            if ((flag & 0xFF) == 0x09) {
                byte[] bytes = subBytes(data, i, len - 1);
                name = new String(bytes, StandardCharsets.UTF_8);
            }
            i += len - 1;
        }
        return name;
    }

    public static String getSN(byte[] data) {
        int length = data.length;
        String sn = "";
        for (int i = 0; i < length; ) {
            byte len = data[i++];

            if (len == 0 || len + 1 > length) {
                break;
            }
            byte flag = data[i++];
            if ((flag & 0xFF) == 0xFF) {
                if (len == 7) {
                    byte[] bytes = subBytes(data, i, 6);
                    bytesToSn(bytes, bytes.length);
                    sn = new String(bytes);
                } else if (len >= 9) {
                    byte[] bytes = subBytes(data, i + 2, 6);
                    bytesToSn(bytes, bytes.length);
                    sn = new String(bytes);
                }
            }
            i += len - 1;
        }
        return sn;
    }


    static void bytesToSn(byte[] address, int length) {
        for (int i = 0; i < length; i++) {
            byte value = address[i];
            if (value > 9) {
                value += 64 - 9;
            } else {
                value += 48;
            }
            address[i] = value;
        }
    }

    public static byte[] subBytes(byte[] src, int begin, int count) {
        byte[] bs = new byte[count];
        System.arraycopy(src, begin, bs, 0, count);
        return bs;
    }
}
