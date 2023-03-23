package com.microtech.aidexx.utils;

import android.content.Context;
import android.content.res.Configuration;

//import com.github.jokar.multilanguages.library.MultiLanguage;

import java.util.Locale;

public class LocalManageUtil {


    private static final String TAG = "LocalManageUtil";

    /**
     * 获取系统的locale
     *
     * @return Locale对象
     */
    public static Locale getSystemLocale(Context context) {
        return LanguageUtil.getInstance(context).getSystemCurrentLocal();
    }

    /**
     * 获取选择的语言设置
     *
     * @param context
     * @return
     */
    public static Locale getSetLanguageLocale(Context context) {

        switch (LanguageUtil.getInstance(context).getSelectLanguage()) {
            case 0:
                return Locale.ENGLISH;
            case 1:
                return Locale.SIMPLIFIED_CHINESE;
            case 2:
                return new Locale("cs");
            case 3:
                return new Locale("sk");
            case 4:
                return Locale.FRANCE;
            case 5:
                if(isArabicNotSupported()) {
                    return new Locale("en");
                }
                return new Locale("ar");
            case 6:
                return new Locale("it");
            case 7:
                return new Locale("mn");
            case 8:
                return new Locale("ro");
            case 9:
                return new Locale("tr");
            case 10:
                return new Locale("es");
            case 11:
                return new Locale("ru");
            case 12:
                return new Locale("de");
            case 13:
                return new Locale("sv");
            default:
                Locale systemLocale = getSystemLocale(context);
                if ("en".equals(systemLocale.getLanguage()) ||
                        "zh".equals(systemLocale.getLanguage()) ||
                        "cs".equals(systemLocale.getLanguage()) ||
                        "sk".equals(systemLocale.getLanguage()) ||
                        "fr".equals(systemLocale.getLanguage()) ||
                        "ar".equals(systemLocale.getLanguage()) ||
                        "mn".equals(systemLocale.getLanguage()) ||
                        "it".equals(systemLocale.getLanguage()) ||
                        "ro".equals(systemLocale.getLanguage()) ||
                        "tr".equals(systemLocale.getLanguage()) ||
                        "es".equals(systemLocale.getLanguage()) ||
                        "ru".equals(systemLocale.getLanguage()) ||
                        "de".equals(systemLocale.getLanguage()) ||
                        "sv".equals(systemLocale.getLanguage())

                ) {
                    int select = 0;
                    if ("zh".equals(systemLocale.getLanguage())) {
                        select = 1;
                    } else if ("cs".equals(systemLocale.getLanguage())) {
                        select = 2;
                    } else if ("sk".equals(systemLocale.getLanguage())) {
                        select = 3;
                    } else if ("fr".equals(systemLocale.getLanguage())) {
                        select = 4;
                    } else if ("ar".equals(systemLocale.getLanguage())) {
                        if (isArabicNotSupported()) {
                            select = 0;
                        }else {
                            select = 5;
                        }
                    } else if ("it".equals(systemLocale.getLanguage())) {
                        select = 6;
                    } else if ("mn".equals(systemLocale.getLanguage())) {
                        select = 7;
                    } else if ("ro".equals(systemLocale.getLanguage())) {
                        select = 8;
                    } else if ("tr".equals(systemLocale.getLanguage())) {
                        select = 9;
                    } else if ("es".equals(systemLocale.getLanguage())) {
                        select = 10;
                    } else if ("ru".equals(systemLocale.getLanguage())) {
                        select = 11;
                    } else if ("de".equals(systemLocale.getLanguage())) {
                        select = 12;
                    } else if ("sv".equals(systemLocale.getLanguage())) {
                        select = 13;
                    }
                    LanguageUtil.getInstance(context).saveLanguage(select);
                    return systemLocale;
                } else {
                    LanguageUtil.getInstance(context).saveLanguage(0);
                    return Locale.ENGLISH;
                }
        }
    }


    public static boolean isSameWithSetting(Context context) {
        Locale current = context.getResources().getConfiguration().locale;
        return current.equals(getSetLanguageLocale(context));
    }


    public static void saveSystemCurrentLanguage(Context context) {
//     todo   LanguageUtil.getInstance(context).setSystemCurrentLocal(MultiLanguage.getSystemLocal(context));
    }

    /**
     * 保存系统语言
     *
     * @param newConfig
     */
    public static void saveSystemCurrentLanguage(Context context, Configuration newConfig) {

//   todo     LanguageUtil.getInstance(context).setSystemCurrentLocal(MultiLanguage.getSystemLocal(newConfig));
    }

    public static void saveSelectLanguage(Context context, int select) {
        LanguageUtil.getInstance(context).saveLanguage(select);
//   todo     MultiLanguage.setApplicationLanguage(context);

    }

    /** 是否不支持阿拉伯语言 */
    public static boolean isArabicNotSupported() {
//        return BuildConfig.FLAVOR_BRAND == "medmmoltrust" || BuildConfig.FLAVOR_BRAND == "medmgtrust";
        return true;
    }
}
