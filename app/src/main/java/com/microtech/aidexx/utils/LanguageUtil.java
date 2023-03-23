package com.microtech.aidexx.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Locale;

public class LanguageUtil {


    private final String SP_NAME = "sp_setting";
    private final String TAG_SYSTEM_LANGUAGE = "system_language";
    private final String LANGUAGE = "language";

    private static volatile LanguageUtil instance;

    private Locale systemCurrentLocal = Locale.ENGLISH;

    public void saveLanguage(int select) {
        SharedPreferences.Editor edit = mSharedPreferences.edit();
        edit.putInt(LANGUAGE, select);
        edit.apply();
    }

    public int getSelectLanguage() {
        return mSharedPreferences.getInt(LANGUAGE, -1);
    }

    public Locale getSystemCurrentLocal() {
        return systemCurrentLocal;
    }

    public void setSystemCurrentLocal(Locale local) {
        systemCurrentLocal = local;
    }

    private final SharedPreferences mSharedPreferences;


    public LanguageUtil(Context context) {
        mSharedPreferences = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
    }


    public static LanguageUtil getInstance(Context context) {
        if (instance == null) {
            synchronized (LanguageUtil.class) {
                if (instance == null) {
                    instance = new LanguageUtil(context);
                }
            }
        }
        return instance;
    }
}
