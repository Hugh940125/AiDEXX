package com.microtech.aidexx.utils;

import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Log;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DigitFilter implements InputFilter {
    private static final String TAG = "GlucoseInputFilter";
    Pattern mPattern;
   private boolean isIntOnly = false;

   private boolean isGlucoseValue = false;
    //输入的最大金额
    private float maxValue = Float.MAX_VALUE;


    public int getPointerLength() {
        return pointerLength;
    }

    public void setPointerLength(int pointerLength) {
        this.pointerLength = pointerLength;
    }

    //小数点后的位数
    int pointerLength = 1;

    public int getMaxLength() {
        return maxLength;
    }

    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
    }

    int maxLength = 1000;

    private String POINTER = ".";

    private static final String ZERO = "0";

    public DigitFilter(String pointer) {
        POINTER = pointer;
        if (POINTER.equals(".")) {
            mPattern = Pattern.compile("([0-9]|\\.)*");
        } else {
            mPattern = Pattern.compile("([0-9]|,)*");
        }
    }

    public boolean isIntOnly() {
        return isIntOnly;
    }

    public void setIntOnly(boolean intOnly) {
        isIntOnly = intOnly;
        if (isIntOnly) {
            mPattern = Pattern.compile("([0-9]|)*");
        } else {
            if (POINTER.equals(".")) {
                mPattern = Pattern.compile("([0-9]|\\.)*");
            } else {
                mPattern = Pattern.compile("([0-9]|,)*");
            }

        }
    }

    public float getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(float maxValue) {
        this.maxValue = maxValue;
    }

    public void setGlucoseValue(boolean isIgnoreDigitlCount) {
        this.isGlucoseValue = isIgnoreDigitlCount;
    }

    public boolean isGlucoseValue() {
        return isGlucoseValue;
    }

    /**
     * @param source 新输入的字符串
     * @param start  新输入的字符串起始下标，一般为0
     * @param end    新输入的字符串终点下标，一般为source长度-1
     * @param dest   输入之前文本框内容
     * @param dstart 原内容起始坐标，一般为0
     * @param dend   原内容终点坐标，一般为dest长度-1
     * @return 输入内容
     */
    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        String sourceText = source.toString();
        String destText = dest.toString();
        Log.d(TAG, "filter: isdigitOnly " + isIntOnly);
        Log.d(TAG, "filter: " + dest + "   " + source);
        Log.d(TAG, "filter: mPattern " + mPattern);
        Log.d(TAG, "filter: POINTER " + POINTER);
        if (RomUtils.INSTANCE.isSamsung() && sourceText.contains(destText) && sourceText.contains(".")) {
            sourceText = sourceText.replace(destText, "");
        }
        //验证删除等按键
        if (TextUtils.isEmpty(sourceText)) {
            Log.d(TAG, "filter返回0");
            return "";
        }


        Matcher matcher = mPattern.matcher(source);
        //已经输入小数点的情况下，只能输入数字
        if (destText.contains(POINTER)) {
            if (!matcher.matches()) {
                Log.d(TAG, "filter返回1");
                return "";
            } else {
                if (POINTER.contentEquals(source)) {  //只能输入一个小数点
                    Log.d(TAG, "filter返回2");

                    return "";
                }
            }

            if (ZERO.contentEquals(source) && (ZERO + POINTER).contentEquals(dest)) {
                Log.d(TAG, "filter返回3");
                return "";
            }


            if (isGlucoseValue) {
                //葡萄糖数值mmol单位不低于0.6
                for (int i = 0; i < 6; i++) {
                    if ((i + "").contentEquals(source) && (ZERO + POINTER).contentEquals(dest)) {
                        Log.d(TAG, "filter返回4");

                        return "";
                    }
                }
                //验证小数点精度，保证小数点后只能输入两位
                int index = destText.indexOf(POINTER);
                int length = dend - index;

                if (length > pointerLength) {
                    return dest.subSequence(dstart, dend);
                }
            }

        } else {
            //没有输入小数点的情况下，只能输入小数点和数字，但首位不能输入小数点和0
            if (!matcher.matches()) {
                return "";
            } else {
                if ((POINTER.contentEquals(source)) && TextUtils.isEmpty(destText)) {
                    return "";
                }
                if ((ZERO.contentEquals(source)) && (ZERO.contentEquals(dest))) {
                    return "";
                }
//                if (!(POINTER.contentEquals(source)) && (ZERO.contentEquals(dest))) {
//                    return "";
//                }
            }
        }


        if ((destText + sourceText).length() > maxLength) {
            Log.d(TAG, "filter返回8");
            return "";
        }
        //验证输入数值的大小
//        double sumText = Double.parseDouble(destText + sourceText);
//        if (sumText > maxValue) {
//            return dest.subSequence(dstart, dend);
//        }
//        if (sumText >= 1000) {
//            return "";
//        }

        return dest.subSequence(dstart, dend) + sourceText;
    }
}