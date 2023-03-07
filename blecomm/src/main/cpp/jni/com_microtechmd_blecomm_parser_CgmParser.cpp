#include <jni.h>
#include "jni_global.h"
#include "com_microtechmd_blecomm_parser_CgmParser.h"
#include "../parser/cgm/cgmbroadcastparser.h"
#include "../parser/cgm/cgmhistoryparser.h"
#include "../parser/cgm/cgmhistoriesparser.h"
#include "../cgmscomm.h"


static jclass broadcastCls;
static jmethodID broadcast_new;
static jmethodID broadcast_setDatetime;
static jmethodID broadcast_setBattery;
static jmethodID broadcast_setState;
static jmethodID broadcast_setGlucose;
static jmethodID broadcast_setPrimary;
static jmethodID broadcast_setHistory;

static jclass historyCls;
static jmethodID history_new;
static jmethodID history_setDatetime;
static jmethodID history_setEventIndex;
static jmethodID history_setSensorIndex;
static jmethodID history_setEventType;
static jmethodID history_setEventValue;
static jmethodID history_setRawValue;
static const int rawValueSize = 9;


static jobject historyAsJavaObject(JNIEnv *env, const CgmHistoryEntity *chistory, bool isRaw) {
    jobject entity = env->NewObject(historyCls, history_new);
    env->CallVoidMethod(entity, history_setDatetime, chistory->dateTime);
    env->CallVoidMethod(entity, history_setEventIndex, chistory->eventIndex);
    env->CallVoidMethod(entity, history_setSensorIndex, chistory->sensorIndex);
    env->CallVoidMethod(entity, history_setEventType, chistory->eventType);
    env->CallVoidMethod(entity, history_setEventValue, chistory->eventValue);
    if (isRaw) {
        jfloatArray rawValue = env->NewFloatArray(rawValueSize);
        env->SetFloatArrayRegion(rawValue, 0, rawValueSize, chistory->rawValue);
        env->CallVoidMethod(entity, history_setRawValue, rawValue);
    }
    return entity;
}

static jobject bytesToHistoryObject(JNIEnv *env, jbyteArray bytes) {
    const jbyte *data = env->GetByteArrayElements(bytes, JNI_FALSE);
    jint length = env->GetArrayLength(bytes);
    CgmHistoryParser cgmHistoryParser((const char *) data, length);

    const CgmHistoryEntity *chistory = cgmHistoryParser.getHistory();
    return historyAsJavaObject(env, chistory, false);
}

static jobject bytesToHistoryList(JNIEnv *env, jbyteArray bytes, bool isRaw) {
    const jbyte *data = env->GetByteArrayElements(bytes, JNI_FALSE);
    jint length = env->GetArrayLength(bytes);
    CgmHistoriesParser cgmHistoriesParser((const char *) data, length);

    jobject listObj = newList(env);
    while (cgmHistoriesParser.hasNext()) {
        const CgmHistoryEntity *chistory = isRaw ? cgmHistoriesParser.getFullHistory()
                                                 : cgmHistoriesParser.getHistory();
        jobject history = historyAsJavaObject(env, chistory, isRaw);
        env->CallBooleanMethod(listObj, listAdd, history);
    }
    return listObj;
}


JNIEXPORT void JNICALL Java_com_microtechmd_blecomm_parser_CgmParser_setBroadcastClass
        (JNIEnv *env, jclass, jclass cls) {
    if (broadcastCls != nullptr) {
        env->DeleteWeakGlobalRef(broadcastCls);
    }
    broadcastCls = (jclass) env->NewWeakGlobalRef((jobject) cls);
    broadcast_new = env->GetMethodID(broadcastCls, "<init>", "()V");
    broadcast_setDatetime = env->GetMethodID(broadcastCls, "_setDatetime", "(J)V");
    broadcast_setBattery = env->GetMethodID(broadcastCls, "_setBattery", "(I)V");
    broadcast_setState = env->GetMethodID(broadcastCls, "_setState", "(I)V");
    broadcast_setGlucose = env->GetMethodID(broadcastCls, "_setGlucose", "(F)V");
    broadcast_setPrimary = env->GetMethodID(broadcastCls, "_setPrimary", "(I)V");
    broadcast_setHistory = env->GetMethodID(broadcastCls, "_setHistory",
                                            "(Lcom/microtechmd/blecomm/parser/CgmHistoryEntity;)V");
}


static jclass deviceCls;
static jmethodID device_new;
static jmethodID device_sn;
static jmethodID device_endian;
static jmethodID device_type;
static jmethodID device_mode;
static jmethodID device_edition;
static jmethodID device_capacity;


JNIEXPORT void JNICALL Java_com_microtechmd_blecomm_parser_CgmParser_setDeviceInfoClass
        (JNIEnv *env, jclass, jclass cls) {
    if (deviceCls != nullptr) {
        env->DeleteWeakGlobalRef(deviceCls);
    }
    deviceCls = (jclass) env->NewWeakGlobalRef((jobject) cls);
    device_new = env->GetMethodID(deviceCls, "<init>", "()V");
    device_sn = env->GetMethodID(deviceCls, "_setSn", "(Ljava/lang/String;)V");
    device_endian = env->GetMethodID(deviceCls, "_setEndian", "(I)V");
    device_type = env->GetMethodID(deviceCls, "_setDeviceType", "(I)V");
    device_mode = env->GetMethodID(deviceCls, "_setModel", "(I)V");
    device_edition = env->GetMethodID(deviceCls, "_setEdition", "(Ljava/lang/String;)V");
    device_capacity = env->GetMethodID(deviceCls, "_setCapacity", "(I)V");
}


static jclass configCls;
static jmethodID config_new;
static jmethodID device_et;

static jmethodID device_cf;
static jmethodID device_cf1;
static jmethodID device_cf2;
static jmethodID device_cf3;
static jmethodID device_cf4;
static jmethodID device_cf5;

static jmethodID device_cfh2;
static jmethodID device_cfh3;
static jmethodID device_cfh4;

static jmethodID device_ofs;
static jmethodID device_ofs1;
static jmethodID device_ofs2;
static jmethodID device_ofs3;
static jmethodID device_ofs4;
static jmethodID device_ofs5;

static jmethodID device_ofsh2;
static jmethodID device_ofsh3;
static jmethodID device_ofsh4;
static jmethodID device_ib;
static jmethodID device_ird;


static jmethodID device_inl1;
static jmethodID device_inl0;
static jmethodID device_cfls;
static jmethodID device_cfus;
static jmethodID device_sfl;
static jmethodID device_sfu;

static jmethodID device_rrcsh;
static jmethodID device_rrf;
static jmethodID device_rr;
static jmethodID device_rns;
static jmethodID device_rl;
static jmethodID device_ru;

static jmethodID device_rrcph;
static jmethodID device_rrsc;
static jmethodID device_il;
static jmethodID device_iu;
static jmethodID device_ir;
static jmethodID device_irf;
static jmethodID device_irsc;


JNIEXPORT jobject JNICALL
Java_com_microtechmd_blecomm_parser_CgmParser_getDeviceConfig

        (JNIEnv *env, jclass, jbyteArray bytes) {
    const jbyte *data = env->GetByteArrayElements(bytes, JNI_FALSE);
    jint length = env->GetArrayLength(bytes);
    CgmDefaultParamParser paramParser((const char *) data, length);

    const CgmDefaultParamEntity *config = paramParser.getCgmDefaultParam();
    jobject broadcast = env->NewObject(configCls, config_new);

    env->CallVoidMethod(broadcast, device_et, config->et);
    env->CallVoidMethod(broadcast, device_cf, config->cf);
    env->CallVoidMethod(broadcast, device_cf1, config->cf1);
    env->CallVoidMethod(broadcast, device_cf2, config->cf2);
    env->CallVoidMethod(broadcast, device_cf3, config->cf3);
    env->CallVoidMethod(broadcast, device_cf4, config->cf4);
    env->CallVoidMethod(broadcast, device_cf5, config->cf5);
    env->CallVoidMethod(broadcast, device_cfh2, config->cfh2);
    env->CallVoidMethod(broadcast, device_cfh3, config->cfh3);
    env->CallVoidMethod(broadcast, device_cfh4, config->cfh4);
    env->CallVoidMethod(broadcast, device_ofs, config->ofs);
    env->CallVoidMethod(broadcast, device_ofs1, config->ofs1);
    env->CallVoidMethod(broadcast, device_ofs2, config->ofs2);
    env->CallVoidMethod(broadcast, device_ofs3, config->ofs3);
    env->CallVoidMethod(broadcast, device_ofs4, config->ofs4);
    env->CallVoidMethod(broadcast, device_ofs5, config->ofs5);
    env->CallVoidMethod(broadcast, device_ofsh2, config->ofsh2);
    env->CallVoidMethod(broadcast, device_ofsh3, config->ofsh3);
    env->CallVoidMethod(broadcast, device_ofsh4, config->ofsh4);
    env->CallVoidMethod(broadcast, device_ib, config->ib);
    env->CallVoidMethod(broadcast, device_ird, config->ird);
    env->CallVoidMethod(broadcast, device_inl1, config->inl1);
    env->CallVoidMethod(broadcast, device_inl0, config->inl0);
    env->CallVoidMethod(broadcast, device_cfls, config->cfls);
    env->CallVoidMethod(broadcast, device_cfus, config->cfus);
    env->CallVoidMethod(broadcast, device_sfl, config->sfl);
    env->CallVoidMethod(broadcast, device_sfu, config->sfu);
    env->CallVoidMethod(broadcast, device_rrcsh, config->rrcsh);
    env->CallVoidMethod(broadcast, device_rrf, config->rrf);
    env->CallVoidMethod(broadcast, device_rr, config->rr);
    env->CallVoidMethod(broadcast, device_rns, config->rns);
    env->CallVoidMethod(broadcast, device_rl, config->rl);
    env->CallVoidMethod(broadcast, device_ru, config->ru);
    env->CallVoidMethod(broadcast, device_rrcph, config->rrcph);
    env->CallVoidMethod(broadcast, device_rrsc, config->rrsc);
    env->CallVoidMethod(broadcast, device_il, config->il);
    env->CallVoidMethod(broadcast, device_iu, config->iu);
    env->CallVoidMethod(broadcast, device_irsc, config->irsc);
    env->CallVoidMethod(broadcast, device_irf, config->irf);
    env->CallVoidMethod(broadcast, device_ir, config->ir);

    return broadcast;
}



JNIEXPORT void JNICALL Java_com_microtechmd_blecomm_parser_CgmParser_setDeviceConfigClass
        (JNIEnv *env, jclass, jclass cls) {
    if (configCls != nullptr) {
        env->DeleteWeakGlobalRef(configCls);
    }
    configCls = (jclass) env->NewWeakGlobalRef((jobject) cls);
    config_new = env->GetMethodID(configCls, "<init>", "()V");
    device_et = env->GetMethodID(configCls, "setEt", "(F)V");
    device_cf = env->GetMethodID(configCls, "setCf", "(F)V");
    device_cf1 = env->GetMethodID(configCls, "setCf1", "(F)V");
    device_cf2 = env->GetMethodID(configCls, "setCf2", "(F)V");
    device_cf3 = env->GetMethodID(configCls, "setCf3", "(F)V");
    device_cf4 = env->GetMethodID(configCls, "setCf4", "(F)V");
    device_cf5 = env->GetMethodID(configCls, "setCf5", "(F)V");
    device_cfh2 = env->GetMethodID(configCls, "setCfh2", "(F)V");
    device_cfh3 = env->GetMethodID(configCls, "setCfh3", "(F)V");
    device_cfh4 = env->GetMethodID(configCls, "setCfh4", "(F)V");

    device_ofs = env->GetMethodID(configCls, "setOfs", "(F)V");
    device_ofs1 = env->GetMethodID(configCls, "setOfs1", "(F)V");
    device_ofs2 = env->GetMethodID(configCls, "setOfs2", "(F)V");
    device_ofs3 = env->GetMethodID(configCls, "setOfs3", "(F)V");
    device_ofs4 = env->GetMethodID(configCls, "setOfs4", "(F)V");
    device_ofs5 = env->GetMethodID(configCls, "setOfs5", "(F)V");

    device_ofsh2 = env->GetMethodID(configCls, "setOfsh2", "(F)V");
    device_ofsh3 = env->GetMethodID(configCls, "setOfsh3", "(F)V");
    device_ofsh4 = env->GetMethodID(configCls, "setOfsh4", "(F)V");
    device_ib = env->GetMethodID(configCls, "setIb", "(F)V");
    device_ird = env->GetMethodID(configCls, "setIrd", "(F)V");
    device_inl1 = env->GetMethodID(configCls, "setInl1", "(F)V");
    device_inl0 = env->GetMethodID(configCls, "setInl0", "(F)V");

    device_cfls = env->GetMethodID(configCls, "setCfls", "(F)V");
    device_cfus = env->GetMethodID(configCls, "setCfus", "(F)V");
    device_sfl = env->GetMethodID(configCls, "setSfl", "(F)V");
    device_sfu = env->GetMethodID(configCls, "setSfu", "(F)V");

    device_rl = env->GetMethodID(configCls, "setRl", "(F)V");
    device_ru = env->GetMethodID(configCls, "setRu", "(F)V");
    device_rns = env->GetMethodID(configCls, "setRns", "(F)V");
    device_rr = env->GetMethodID(configCls, "setRr", "(F)V");
    device_rrf = env->GetMethodID(configCls, "setRrf", "(F)V");
    device_rrcsh = env->GetMethodID(configCls, "setRrcsh", "(F)V");

    device_rrcph = env->GetMethodID(configCls, "setRrcph", "(F)V");
    device_rrsc = env->GetMethodID(configCls, "setRrsc", "(F)V");
    device_il = env->GetMethodID(configCls, "setIl", "(F)V");
    device_iu = env->GetMethodID(configCls, "setIu", "(F)V");
    device_ir = env->GetMethodID(configCls, "setIr", "(F)V");
    device_irf = env->GetMethodID(configCls, "setIrf", "(F)V");
    device_irsc = env->GetMethodID(configCls, "setIrsc", "(F)V");

}


JNIEXPORT jobject JNICALL Java_com_microtechmd_blecomm_parser_CgmParser_getDeviceInfo
        (JNIEnv *env, jclass, jbyteArray bytes) {
    const jbyte *data = env->GetByteArrayElements(bytes, JNI_FALSE);
    jint length = env->GetArrayLength(bytes);
    CgmDeviceParser cgmDeviceParser((const char *) data, length);

    const CgmDeviceEntity *cbroadcast = cgmDeviceParser.getDevice();
    jobject broadcast = env->NewObject(deviceCls, device_new);
    jstring snString = env->NewStringUTF(cbroadcast->sn.data());
    env->CallVoidMethod(broadcast, device_sn, snString);
    env->CallVoidMethod(broadcast, device_endian, cbroadcast->endian);
    env->CallVoidMethod(broadcast, device_type, cbroadcast->deviceType);
    env->CallVoidMethod(broadcast, device_mode, cbroadcast->model);
    jstring editionString = env->NewStringUTF(cbroadcast->edition.data());
    env->CallVoidMethod(broadcast, device_edition, editionString);
    env->CallVoidMethod(broadcast, device_capacity, cbroadcast->capacity);

    return broadcast;
}


JNIEXPORT void JNICALL Java_com_microtechmd_blecomm_parser_CgmParser_setHistoryClass
        (JNIEnv *env, jclass, jclass cls) {
    if (historyCls != nullptr) {
        env->DeleteWeakGlobalRef(historyCls);
    }
    historyCls = (jclass) env->NewWeakGlobalRef((jobject) cls);
    history_new = env->GetMethodID(historyCls, "<init>", "()V");
    history_setDatetime = env->GetMethodID(historyCls, "_setDatetime", "(J)V");
    history_setEventIndex = env->GetMethodID(historyCls, "_setEventIndex", "(I)V");
    history_setSensorIndex = env->GetMethodID(historyCls, "_setSensorIndex", "(I)V");
    history_setEventType = env->GetMethodID(historyCls, "_setEventType", "(I)V");
    history_setEventValue = env->GetMethodID(historyCls, "_setEventValue", "(F)V");
    history_setRawValue = env->GetMethodID(historyCls, "_setRawValue", "([F)V");
}


JNIEXPORT jobject JNICALL Java_com_microtechmd_blecomm_parser_CgmParser_getBroadcast
        (JNIEnv *env, jclass, jbyteArray bytes) {
    const jbyte *data = env->GetByteArrayElements(bytes, JNI_FALSE);
    jint length = env->GetArrayLength(bytes);
    CgmBroadcastParser cgmBroadcastParser((const char *) data, length);

    const CgmBroadcastEntity *cbroadcast = cgmBroadcastParser.getBroadcast();
    jobject broadcast = env->NewObject(broadcastCls, broadcast_new);
    env->CallVoidMethod(broadcast, broadcast_setDatetime, cbroadcast->dateTime);
    env->CallVoidMethod(broadcast, broadcast_setBattery, cbroadcast->bat);
    env->CallVoidMethod(broadcast, broadcast_setState, cbroadcast->state);
    env->CallVoidMethod(broadcast, broadcast_setGlucose, cbroadcast->glucose);
    env->CallVoidMethod(broadcast, broadcast_setPrimary, cbroadcast->primary);
    env->CallVoidMethod(broadcast, broadcast_setHistory,
                        historyAsJavaObject(env, &(cbroadcast->history), false));
    return broadcast;
}


JNIEXPORT jobject JNICALL Java_com_microtechmd_blecomm_parser_CgmParser_getHistory
        (JNIEnv *env, jclass, jbyteArray bytes) {
    return bytesToHistoryObject(env, bytes);
}


JNIEXPORT jobject JNICALL Java_com_microtechmd_blecomm_parser_CgmParser_getHistories
        (JNIEnv *env, jclass, jbyteArray bytes) {
    return bytesToHistoryList(env, bytes, false);
}


JNIEXPORT jobject JNICALL Java_com_microtechmd_blecomm_parser_CgmParser_getFullHistories
        (JNIEnv *env, jclass, jbyteArray bytes) {
    return bytesToHistoryList(env, bytes, true);
}

