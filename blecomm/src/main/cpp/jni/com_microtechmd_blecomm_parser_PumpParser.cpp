#include <jni.h>
#include "jni_global.h"
#include "com_microtechmd_blecomm_parser_PumpParser.h"
#include "../parser/pump/pumpentities.h"
#include "../parser/pump/pumpbroadcastparser.h"
#include "../parser/pump/pumpbolusprofileparser.h"
#include "../pumpcomm.h"


static jclass deviceInfoCls;
static jmethodID deviceInfo_new;
static jmethodID deviceInfo_setSn;
static jmethodID deviceInfo_setEndian;
static jmethodID deviceInfo_setDeviceType;
static jmethodID deviceInfo_setModel;
static jmethodID deviceInfo_setEdition;
static jmethodID deviceInfo_setCapacity;

static jclass historyCls;
static jmethodID history_new;
static jmethodID history_setDatetime;
static jmethodID history_setRemainingCapacity;
static jmethodID history_setRemainingInsulin;
static jmethodID history_setBasal;
static jmethodID history_setBolus;
static jmethodID history_setEventIndex;
static jmethodID history_setEventPort;
static jmethodID history_setEventType;
static jmethodID history_setEventLevel;
static jmethodID history_setEventValue;
static jmethodID history_setEvent;
static jmethodID history_setAuto;
static jmethodID history_setBolusUnitPerHour;
static jmethodID history_setBasalUnitPerHour;

static jclass broadcastCls;
static jmethodID broadcast_new;
static jmethodID broadcast_setExpired;
static jmethodID broadcast_setHistory;


static jobject deviceAsJavaObject(JNIEnv *env, const PumpDeviceEntity *cdevice, bool isRaw) {
    jobject entity = env->NewObject(deviceInfoCls, deviceInfo_new);
    jstring snString = env->NewStringUTF(cdevice->sn.data());
    env->CallVoidMethod(entity, deviceInfo_setSn, snString);
    env->CallVoidMethod(entity, deviceInfo_setEndian, cdevice->endian);
    env->CallVoidMethod(entity, deviceInfo_setDeviceType, cdevice->deviceType);
    env->CallVoidMethod(entity, deviceInfo_setModel, cdevice->model);
    jstring editionString = env->NewStringUTF(cdevice->edition.data());
    env->CallVoidMethod(entity, deviceInfo_setEdition, editionString);
    env->CallVoidMethod(entity, deviceInfo_setCapacity, cdevice->capacity);
    return entity;
}

static jobject bytesToDeviceObject(JNIEnv *env, jbyteArray bytes) {
    const jbyte *data = env->GetByteArrayElements(bytes, JNI_FALSE);
    jint length = env->GetArrayLength(bytes);
    PumpDeviceParser pumpDeviceParser((const char *) data, length);

    const PumpDeviceEntity *cdevice = pumpDeviceParser.getDevice();
    return deviceAsJavaObject(env, cdevice, false);
}


static jobject historyAsJavaObject(JNIEnv *env, const PumpHistoryEntity *chistory, bool isRaw) {
    jobject entity = env->NewObject(historyCls, history_new);
    jstring dateTimeString = env->NewStringUTF(chistory->dateTime.data());
    env->CallVoidMethod(entity, history_setDatetime, dateTimeString);
    env->CallVoidMethod(entity, history_setRemainingCapacity, chistory->remainingCapacity);
    env->CallVoidMethod(entity, history_setRemainingInsulin, chistory->remainingInsulin);
    env->CallVoidMethod(entity, history_setBasal, chistory->basal);
    env->CallVoidMethod(entity, history_setBolus, chistory->bolus);
    env->CallVoidMethod(entity, history_setEventIndex, chistory->eventIndex);
    env->CallVoidMethod(entity, history_setEventPort, chistory->eventPort);
    env->CallVoidMethod(entity, history_setEventType, chistory->eventType);
    env->CallVoidMethod(entity, history_setEventLevel, chistory->eventLevel);
    env->CallVoidMethod(entity, history_setEventValue, chistory->eventValue);
    env->CallVoidMethod(entity, history_setEvent, chistory->event);
    env->CallVoidMethod(entity, history_setAuto, chistory->autoMode);
    env->CallVoidMethod(entity, history_setBasalUnitPerHour, chistory->basalUnitPerHour);
    env->CallVoidMethod(entity, history_setBolusUnitPerHour, chistory->bolusUnitPerHour);
    return entity;
}

static jobject bytesToHistoryObject(JNIEnv *env, jbyteArray bytes) {
    const jbyte *data = env->GetByteArrayElements(bytes, JNI_FALSE);
    jint length = env->GetArrayLength(bytes);
    PumpHistoryParser pumpHistoryParser((const char *) data, length);

    const PumpHistoryEntity *chistory = pumpHistoryParser.getHistory();
    return historyAsJavaObject(env, chistory, false);
}


JNIEXPORT void JNICALL Java_com_microtechmd_blecomm_parser_PumpParser_setDeviceInfoClass
        (JNIEnv *env, jclass, jclass cls) {
    if (deviceInfoCls != nullptr) {
        env->DeleteWeakGlobalRef(deviceInfoCls);
    }
    deviceInfoCls = (jclass) env->NewWeakGlobalRef((jobject) cls);
    deviceInfo_new = env->GetMethodID(deviceInfoCls, "<init>", "()V");
    deviceInfo_setSn = env->GetMethodID(deviceInfoCls, "_setSn", "(Ljava/lang/String;)V");
    deviceInfo_setEndian = env->GetMethodID(deviceInfoCls, "_setEndian", "(I)V");
    deviceInfo_setDeviceType = env->GetMethodID(deviceInfoCls, "_setDeviceType", "(I)V");
    deviceInfo_setModel = env->GetMethodID(deviceInfoCls, "_setModel", "(I)V");
    deviceInfo_setEdition = env->GetMethodID(deviceInfoCls, "_setEdition", "(Ljava/lang/String;)V");
    deviceInfo_setCapacity = env->GetMethodID(deviceInfoCls, "_setCapacity", "(I)V");
}


JNIEXPORT void JNICALL Java_com_microtechmd_blecomm_parser_PumpParser_setHistoryClass
        (JNIEnv *env, jclass, jclass cls) {
    if (historyCls != nullptr) {
        env->DeleteWeakGlobalRef(historyCls);
    }
    historyCls = (jclass) env->NewWeakGlobalRef((jobject) cls);
    history_new = env->GetMethodID(historyCls, "<init>", "()V");
    history_setDatetime = env->GetMethodID(historyCls, "_setDatetime", "(Ljava/lang/String;)V");
    history_setRemainingCapacity = env->GetMethodID(historyCls, "_setRemainingCapacity", "(I)V");
    history_setRemainingInsulin = env->GetMethodID(historyCls, "_setRemainingInsulin", "(I)V");
    history_setBasal = env->GetMethodID(historyCls, "_setBasal", "(I)V");
    history_setBolus = env->GetMethodID(historyCls, "_setBolus", "(I)V");
    history_setAuto = env->GetMethodID(historyCls, "_setAuto", "(Z)V");
    history_setEventIndex = env->GetMethodID(historyCls, "_setEventIndex", "(I)V");
    history_setEventPort = env->GetMethodID(historyCls, "_setEventPort", "(I)V");
    history_setEventType = env->GetMethodID(historyCls, "_setEventType", "(I)V");
    history_setEventLevel = env->GetMethodID(historyCls, "_setEventLevel", "(I)V");
    history_setEventValue = env->GetMethodID(historyCls, "_setEventValue", "(I)V");
    history_setBolusUnitPerHour = env->GetMethodID(historyCls, "_setBolusUnitPerHour", "(F)V");
    history_setBasalUnitPerHour = env->GetMethodID(historyCls, "_setBasalUnitPerHour", "(F)V");
    history_setEvent = env->GetMethodID(historyCls, "_setEvent", "(I)V");
}


JNIEXPORT jobject JNICALL Java_com_microtechmd_blecomm_parser_PumpParser_getDeviceInfo
        (JNIEnv *env, jclass, jbyteArray bytes) {
    return bytesToDeviceObject(env, bytes);
}


JNIEXPORT jobject JNICALL Java_com_microtechmd_blecomm_parser_PumpParser_getHistory
        (JNIEnv *env, jclass, jbyteArray bytes) {
    return bytesToHistoryObject(env, bytes);
}

JNIEXPORT void JNICALL
Java_com_microtechmd_blecomm_parser_PumpParser_setBroadcastClass(JNIEnv *env, jclass clazz,
                                                                 jclass broadcast_class) {

    if (broadcastCls != nullptr) {
        env->DeleteWeakGlobalRef(broadcastCls);
    }
    broadcastCls = (jclass) env->NewWeakGlobalRef((jobject) broadcast_class);
    broadcast_new = env->GetMethodID(broadcastCls, "<init>", "()V");
    broadcast_setExpired = env->GetMethodID(broadcastCls, "_setExpired", "(Z)V");
    broadcast_setHistory = env->GetMethodID(broadcastCls, "_setHistory",
                                            "(Lcom/microtechmd/blecomm/parser/PumpHistoryEntity;)V");
}


JNIEXPORT jobject JNICALL
Java_com_microtechmd_blecomm_parser_PumpParser_getBroadcast(JNIEnv *env, jclass clazz,
                                                            jbyteArray bytes) {

    const jbyte *data = env->GetByteArrayElements(bytes, JNI_FALSE);
    jint length = env->GetArrayLength(bytes);
    PumpBroadcastParser cgmBroadcastParser((const char *) data, length);

    const PumpBroadcastEntity *cbroadcast = cgmBroadcastParser.getBroadcast();
    jobject broadcast = env->NewObject(broadcastCls, broadcast_new);
    env->CallVoidMethod(broadcast, broadcast_setExpired, cbroadcast->historyExpired);
    env->CallVoidMethod(broadcast, broadcast_setHistory,
                        historyAsJavaObject(env, &(cbroadcast->history), false));
    return broadcast;

}

extern "C" JNIEXPORT jfloatArray JNICALL
Java_com_microtechmd_blecomm_parser_PumpParser_getBolus(JNIEnv *env, jclass clazz,
                                                        jbyteArray bytes) {
    const jbyte *data = env->GetByteArrayElements(bytes, JNI_FALSE);
    jint length = env->GetArrayLength(bytes);
    PumpBolusProfileParser cgmBroadcastParser((const char *) data, length);
    const float32 *blous = cgmBroadcastParser.getBolusProfile();
    jfloatArray result = env->NewFloatArray(4);
    env->SetFloatArrayRegion(result, 0, 4, blous);
    return result;
}