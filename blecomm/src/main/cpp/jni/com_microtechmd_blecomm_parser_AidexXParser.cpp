#include <jni.h>
#include "jni_global.h"
#include "com_microtechmd_blecomm_parser_AidexXParser.h"
#include "../parser/cgm/aidexxbroadcastparser.h"
#include "../parser/cgm/aidexxhistoriesparser.h"
#include "../parser/cgm/aidexxentities.h"
#include "../parser/cgm/aidexxcalibrationsparser.h"
#include "../parser/cgm/aidexxdefaultparamsparser.h"
#include "../cgmscomm.h"
#include "../parser/cgm/aidexxinstanthistoryparser.h"
#include "../parser/cgm/aidexxscanresponseparser.h"


JNIEXPORT jobject JNICALL Java_com_microtechmd_blecomm_parser_AidexXParser_getBroadcast
        (JNIEnv *env, jclass, jbyteArray bytes) {
    const jbyte *data = env->GetByteArrayElements(bytes, JNI_FALSE);
    jint length = env->GetArrayLength(bytes);
    AidexXBroadcastParser cgmBroadcastParser((const char *) data, length);
    jclass broad_cls = env->FindClass("com/microtechmd/blecomm/parser/AidexXBroadcastEntity");
    jclass history_Class = env->FindClass("com/microtechmd/blecomm/parser/AidexXHistoryEntity");
    jclass basic_Class = env->FindClass("com/microtechmd/blecomm/parser/AidexXAbstractEntity");
    jmethodID broadConstructMId = env->GetMethodID(broad_cls, "<init>",
                                                   "(Lcom/microtechmd/blecomm/parser/AidexXAbstractEntity;Ljava/util/List;I)V");
    jmethodID historyConstructMId = env->GetMethodID(history_Class, "<init>", "(IIIII)V");
    jmethodID basicConstructMId = env->GetMethodID(basic_Class, "<init>", "(IIIII)V");
    const AidexXBroadcastEntity *cbroadcast = cgmBroadcastParser.getBroadcast();
    jobject listObj = newList(env);
    if (cbroadcast == NULL) {
        jobject basic = env->NewObject(basic_Class, basicConstructMId, 0, 0, 0, 0, 0);
        return env->NewObject(broad_cls, broadConstructMId,
                              basic,
                              listObj,
                              0);
    }
    jobject basic = env->NewObject(basic_Class, basicConstructMId,
                                   cbroadcast->abstract.timeOffset,
                                   cbroadcast->abstract.status, cbroadcast->abstract.calTemp,
                                   cbroadcast->abstract.trend, cbroadcast->abstract.calIndex);
    for (int i = 0; i < cbroadcast->historyCount; i++) {
        AidexXHistoryEntity history = cbroadcast->history[i];
        jobject historyObject = env->NewObject(history_Class, historyConstructMId,
                                               history.timeOffset, history.glucose, history.status,
                                               history.quality, history.isValid);
        env->CallBooleanMethod(listObj, listAdd, historyObject);
    }
    return env->NewObject(broad_cls, broadConstructMId,
                          basic,
                          listObj,
                          cbroadcast->historyCount);
}

JNIEXPORT jobject JNICALL Java_com_microtechmd_blecomm_parser_AidexXParser_getScanResponseInfo
        (JNIEnv *env, jclass, jbyteArray bytes) {
    const jbyte *data = env->GetByteArrayElements(bytes, JNI_FALSE);
    jint length = env->GetArrayLength(bytes);
    AidexXScanResponseParser aidexXScanResponseParser((const char *) data, length);
    jclass scan_response_cls = env->FindClass(
            "com/microtechmd/blecomm/controller/ScanResponseInfo");
    jmethodID scanResponseMId = env->GetMethodID(scan_response_cls, "<init>",
                                                 "(ZZ)V");
    const AidexXScanResponseEntity *scanResponseEntity = aidexXScanResponseParser.getScanResponse();
    jobject listObj = newList(env);
    if (scanResponseEntity == NULL) {
        return NULL;
    }
    return env->NewObject(scan_response_cls, scanResponseMId,
                          scanResponseEntity->isBleNativePaired,
                          scanResponseEntity->isAesInitialized);
}


JNIEXPORT jobject JNICALL Java_com_microtechmd_blecomm_parser_AidexXParser_getAidexXInstantHistory
        (JNIEnv *env, jclass, jbyteArray bytes) {
    const jbyte *data = env->GetByteArrayElements(bytes, JNI_FALSE);
    jint length = env->GetArrayLength(bytes);
    AidexXInstantHistoryParser instantHistoryParser((const char *) data, length);
    jclass result_class = env->FindClass(
            "com/microtechmd/blecomm/parser/AidexXInstantHistoryEntity");
    jclass history_class = env->FindClass(
            "com/microtechmd/blecomm/parser/AidexXHistoryEntity");
    jclass raw_history = env->FindClass(
            "com/microtechmd/blecomm/parser/AidexXRawHistoryEntity");
    jclass basic_history = env->FindClass(
            "com/microtechmd/blecomm/parser/AidexXAbstractEntity");

    jmethodID resultConstructMId = env->GetMethodID(result_class, "<init>",
                                                    "(Lcom/microtechmd/blecomm/parser/AidexXAbstractEntity;Lcom/microtechmd/blecomm/parser/AidexXHistoryEntity;Lcom/microtechmd/blecomm/parser/AidexXRawHistoryEntity;)V");

    jmethodID historyConstructMId = env->GetMethodID(history_class, "<init>",
                                                     "(IIIII)V");

    jmethodID rawConstructMId = env->GetMethodID(raw_history, "<init>",
                                                 "(IFFFI)V");

    jmethodID basicConstructMId = env->GetMethodID(basic_history, "<init>",
                                                   "(IIIII)V");
    const AidexXInstantHistoryEntity *historyEntity = instantHistoryParser.getInstantHistory();
    if (historyEntity == nullptr) {
        return nullptr;
    }
    AidexXHistoryEntity history = historyEntity->history;

    jobject basicObject = env->NewObject(basic_history, basicConstructMId,
                                         historyEntity->abstract.timeOffset,
                                         historyEntity->abstract.status,
                                         historyEntity->abstract.calTemp,
                                         historyEntity->abstract.trend,
                                         historyEntity->abstract.calIndex);

    jobject historyObject = env->NewObject(history_class, historyConstructMId,
                                           history.timeOffset,
                                           history.glucose,
                                           history.status,
                                           history.quality,
                                           history.isValid);

    AidexXRawHistoryEntity raw = historyEntity->raw;
    jobject rawObject = env->NewObject(raw_history, rawConstructMId,
                                       raw.timeOffset,
                                       raw.i1,
                                       raw.i2,
                                       raw.vc,
                                       raw.isValid);

    jobject result = env->NewObject(result_class, resultConstructMId,
                                    basicObject,
                                    historyObject,
                                    rawObject);
    return result;
}

JNIEXPORT jobject JNICALL Java_com_microtechmd_blecomm_parser_AidexXParser_getHistories
        (JNIEnv *env, jclass, jbyteArray bytes) {
    const jbyte *data = env->GetByteArrayElements(bytes, JNI_FALSE);
    jint length = env->GetArrayLength(bytes);
    AidexXHistoriesParser cgmHistoriesParser((const char *) data, length);
    jclass history_Class = env->FindClass("com/microtechmd/blecomm/parser/AidexXHistoryEntity");
    jmethodID historyConstructMId = env->GetMethodID(history_Class, "<init>", "(IIIII)V");
    jobject listObj = newList(env);
    while (cgmHistoriesParser.hasNext()) {
        const AidexXHistoryEntity *history = cgmHistoriesParser.getHistory();
        if (history != nullptr) {
            jobject historyObject = env->NewObject(history_Class, historyConstructMId,
                                                   history->timeOffset, history->glucose,
                                                   history->status, history->quality,
                                                   history->isValid);
            env->CallBooleanMethod(listObj, listAdd, historyObject);
        }
    }
    return listObj;
}

JNIEXPORT jobject JNICALL Java_com_microtechmd_blecomm_parser_AidexXParser_getRawHistory
        (JNIEnv *env, jclass, jbyteArray bytes) {
    const jbyte *data = env->GetByteArrayElements(bytes, JNI_FALSE);
    jint length = env->GetArrayLength(bytes);
    AidexXHistoriesParser rawHistoryParser((const char *) data, length);
    jclass history_Class = env->FindClass(
            "com/microtechmd/blecomm/parser/AidexXRawHistoryEntity");
    jmethodID historyConstructMId = env->GetMethodID(history_Class, "<init>", "(IFFFI)V");
    jobject listObj = newList(env);
    while (rawHistoryParser.hasNext()) {
        const AidexXRawHistoryEntity *history = rawHistoryParser.getRawHistory();
        if (history != nullptr) {
            jobject historyObject = env->NewObject(history_Class, historyConstructMId,
                                                   history->timeOffset, history->i1,
                                                   history->i2,
                                                   history->vc, history->isValid);
            env->CallBooleanMethod(listObj, listAdd, historyObject);
        }
    }
    return listObj;
}


JNIEXPORT jobject JNICALL Java_com_microtechmd_blecomm_parser_AidexXParser_getAidexXCalibration
        (JNIEnv *env, jclass, jbyteArray bytes) {
    const jbyte *data = env->GetByteArrayElements(bytes, JNI_FALSE);
    jint length = env->GetArrayLength(bytes);
    AidexXCalibrationsParser calibrationsParser((const char *) data, length);
    jclass history_Class = env->FindClass(
            "com/microtechmd/blecomm/parser/AidexXCalibrationEntity");
    jmethodID caliConstructMId = env->GetMethodID(history_Class, "<init>", "(IIFFII)V");

    jobject listObj = newList(env);
    while (calibrationsParser.hasNext()) {
        const AidexXCalibrationEntity *calbration = calibrationsParser.getCalibration();
        if (calbration != nullptr) {
//            LOGE("calbration : %d",calbration->offset);
            jobject historyObject = env->NewObject(history_Class, caliConstructMId,
                                                   calbration->index,
                                                   calbration->timeOffset,
                                                   calbration->cf,
                                                   calbration->offset,
                                                   calbration->referenceGlucose,
                                                   calbration->isValid);
            env->CallBooleanMethod(listObj, listAdd, historyObject);
        }
    }
    return listObj;
}

JNIEXPORT jfloatArray JNICALL Java_com_microtechmd_blecomm_parser_AidexXParser_getParam
        (JNIEnv *env, jclass, jbyteArray bytes) {
    const jbyte *data = env->GetByteArrayElements(bytes, JNI_FALSE);
    jint length = env->GetArrayLength(bytes);
    AidexXDefaultParamsParser defaultParamsParser((const char *) data, length);
    jfloatArray result = env->NewFloatArray(40);
    jfloat *elements = env->GetFloatArrayElements(result, NULL);
    int i = 0;
    while (defaultParamsParser.hasNext()) {
        float params = defaultParamsParser.getParam();
        elements[i] = params;
        i++;
    }
    env->ReleaseFloatArrayElements(result, elements, 0);
    return result;
}



