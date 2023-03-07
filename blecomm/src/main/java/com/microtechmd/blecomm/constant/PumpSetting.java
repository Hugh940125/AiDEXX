package com.microtechmd.blecomm.constant;

public class PumpSetting {

    public static final int SETTING_INDEX_EXPIRATION_TIME = 0; //已使用时间
    public static final int SETTING_INDEX_AUTO_OFF_TIME = 1;   //设置自动关机时间
    public static final int SETTING_INDEX_RESERVOIR_LOW_LIMIT = 2; //设置低药量报警阈值  0.001U 为单位
    public static final int SETTING_INDEX_QUICK_BOLUS_STEP = 3; // 设置快速大剂量增长量   0.001U 为单位
    public static final int SETTING_INDEX_OCCLUSION_LIMIT = 4;  // 设置堵塞报警阈值
    public static final int SETTING_INDEX_UNIT_AMOUNT = 5; //单位输注量   0.001U
    public static final int SETTING_INDEX_BASAL_RATE_LIMIT = 6; //设置最大基础率
    public static final int SETTING_INDEX_BOLUS_AMOUNT_LIMIT = 7; //大剂量阈值
    public static final int SETTING_INDEX_BOLUS_STEP = 8; //设置大剂量增长步数  √
    public static final int SETTING_INDEX_BOLUS_RATIO = 9; //设置大剂量速率


    public static final int MODE_SUSPEND = 0; //暂停模式
    public static final int MODE_DELIVER = 1; //输注模式
    public static final int MODE_STOP = 2; //通知模式


    public static final int ALERT_HIGH = 2; //高优先级报警
    public static final int ALERT_NORMAL = 0; //通知模式
    public static final int ALERT_LOW = 1; //通知模式


    public static final float STEP_BASERATE_MIN = 0.025f; //
    public static final float PUMP_MAX_OCLUSION = 220f; //胰岛素堵塞阈值
}
