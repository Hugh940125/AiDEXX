#include <jni.h>
#include "jni_global.h"
#include "com_microtechmd_blecomm_parser_BgmParser.h"
#include "../parser/bgm/bgmdeviceparser.h"
#include "../parser/bgm/bgmhistoryparser.h"


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
static jmethodID history_setTemperature;
static jmethodID history_setFlag;
static jmethodID history_setBgValue;
static jmethodID history_setReserved;
static jmethodID history_setHypo;
static jmethodID history_setHyper;
static jmethodID history_setKetone;
static jmethodID history_setPreMeal;
static jmethodID history_setPostMeal;
static jmethodID history_setInvalid;
static jmethodID history_setControlSolution;

static jmethodID history_setEventIndex;
static jmethodID history_setEventPort;
static jmethodID history_setEventType;
static jmethodID history_setEventLevel;
static jmethodID history_setEventValue;


JNIEXPORT void JNICALL Java_com_microtechmd_blecomm_parser_BgmParser_setHistoryClass
        (JNIEnv *env, jclass, jclass cls) {
    if (historyCls != nullptr) {
        env->DeleteWeakGlobalRef(historyCls);
    }
    historyCls = (jclass) env->NewWeakGlobalRef((jobject) cls);
    history_new = env->GetMethodID(historyCls, "<init>", "()V");
    history_setDatetime = env->GetMethodID(historyCls, "_setDatetime", "(Ljava/lang/String;)V");


    history_setTemperature = env->GetMethodID(historyCls, "_setTemperature", "(I)V");
    history_setFlag = env->GetMethodID(historyCls, "_setFlag", "(I)V");
    history_setBgValue = env->GetMethodID(historyCls, "_setBgValue", "(I)V");
    history_setReserved = env->GetMethodID(historyCls, "_setReserved", "(I)V");

    history_setHypo = env->GetMethodID(historyCls, "_setHypo", "(Z)V");
    history_setHyper = env->GetMethodID(historyCls, "_setHyper", "(Z)V");
    history_setKetone = env->GetMethodID(historyCls, "_setKetone", "(Z)V");
    history_setPreMeal = env->GetMethodID(historyCls, "_setPreMeal", "(Z)V");
    history_setPostMeal = env->GetMethodID(historyCls, "_setPostMeal", "(Z)V");
    history_setInvalid = env->GetMethodID(historyCls, "_setInvalid", "(Z)V");
    history_setControlSolution = env->GetMethodID(historyCls, "_setControlSolution", "(Z)V");

    history_setEventIndex = env->GetMethodID(historyCls, "_setEventIndex", "(I)V");
    history_setEventPort = env->GetMethodID(historyCls, "_setEventPort", "(I)V");
    history_setEventType = env->GetMethodID(historyCls, "_setEventType", "(I)V");
    history_setEventLevel = env->GetMethodID(historyCls, "_setEventLevel", "(I)V");
    history_setEventValue = env->GetMethodID(historyCls, "_setEventValue", "(I)V");
}


static jobject historyAsJavaObject(JNIEnv *env, const BgmHistoryEntity *chistory, bool isRaw) {
    jobject entity = env->NewObject(historyCls, history_new);
    jstring dateTimeString = env->NewStringUTF(chistory->dateTime.data());
    env->CallVoidMethod(entity, history_setDatetime, dateTimeString);
    env->CallVoidMethod(entity, history_setTemperature, chistory->temperature);
    env->CallVoidMethod(entity, history_setFlag, chistory->flag);
    env->CallVoidMethod(entity, history_setBgValue, chistory->bgValue);
    env->CallVoidMethod(entity, history_setReserved, chistory->reserved);

    env->CallVoidMethod(entity, history_setHypo, chistory->hypo);
    env->CallVoidMethod(entity, history_setHyper, chistory->hyper);
    env->CallVoidMethod(entity, history_setKetone, chistory->ketone);
    env->CallVoidMethod(entity, history_setPreMeal, chistory->preMeal);
    env->CallVoidMethod(entity, history_setPostMeal, chistory->postMeal);
    env->CallVoidMethod(entity, history_setInvalid, chistory->invalid);
    env->CallVoidMethod(entity, history_setControlSolution, chistory->controlSolution);

    env->CallVoidMethod(entity, history_setEventIndex, chistory->eventIndex);
    env->CallVoidMethod(entity, history_setEventPort, chistory->eventPort);
    env->CallVoidMethod(entity, history_setEventType, chistory->eventType);
    env->CallVoidMethod(entity, history_setEventLevel, chistory->eventLevel);
    env->CallVoidMethod(entity, history_setEventValue, chistory->eventValue);
    return entity;
}


static jobject deviceAsJavaObject(JNIEnv *env, const BgmDeviceEntity *cdevice, bool isRaw) {
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
    BgmDeviceParser pumpDeviceParser((const char *) data, length);

    const BgmDeviceEntity *cdevice = pumpDeviceParser.getDevice();
    return deviceAsJavaObject(env, cdevice, false);
}


static jobject bytesToHistoryObject(JNIEnv *env, jbyteArray bytes) {
    const jbyte *data = env->GetByteArrayElements(bytes, JNI_FALSE);
    jint length = env->GetArrayLength(bytes);
    BgmHistoryParser pumpHistoryParser((const char *) data, length);

    const BgmHistoryEntity *chistory = pumpHistoryParser.getHistory();
    return historyAsJavaObject(env, chistory, false);
}


JNIEXPORT void JNICALL Java_com_microtechmd_blecomm_parser_BgmParser_setDeviceInfoClass
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


JNIEXPORT jobject JNICALL Java_com_microtechmd_blecomm_parser_BgmParser_getDeviceInfo
        (JNIEnv *env, jclass, jbyteArray bytes) {
    return bytesToDeviceObject(env, bytes);
}


JNIEXPORT jobject JNICALL Java_com_microtechmd_blecomm_parser_BgmParser_getHistory
        (JNIEnv *env, jclass, jbyteArray bytes) {
    return bytesToHistoryObject(env, bytes);
}
