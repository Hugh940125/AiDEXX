#include <jni.h>
#include "jni_global.h"
#include "com_microtechmd_blecomm_parser_AidexXParser.h"
#include "../parser/cgm/aidexxbroadcastparser.h"
#include "../parser/cgm/aidexxhistoriesparser.h"
#include "../parser/cgm/aidexxentities.h"
#include "../parser/cgm/aidexxcalibrationsparser.h"
#include "../parser/cgm/aidexxdefaultparamsparser.h"
#include "../cgmscomm.h"


JNIEXPORT jobject JNICALL Java_com_microtechmd_blecomm_parser_AidexXParser_getBroadcast
        (JNIEnv *env, jclass, jbyteArray bytes) {
    const jbyte *data = env->GetByteArrayElements(bytes, JNI_FALSE);
    jint length = env->GetArrayLength(bytes);
    AidexXBroadcastParser cgmBroadcastParser((const char *) data, length);
    jclass broad_cls = env->FindClass("com/microtechmd/blecomm/parser/AidexXBroadcastEntity");
    jclass history_Class = env->FindClass("com/microtechmd/blecomm/parser/AidexXHistoryEntity");

    jmethodID broadConstructMId = env->GetMethodID(broad_cls, "<init>", "(IIIIILjava/util/List;)V");
    jmethodID historyConstructMId = env->GetMethodID(history_Class, "<init>", "(IIIII)V");
    const AidexXBroadcastEntity *cbroadcast = cgmBroadcastParser.getBroadcast();

    jobject listObj = newList(env);
    for (int i = 0; i < cbroadcast->historyCount; i++) {
        AidexXHistoryEntity history = cbroadcast->history[i];
        jobject historyObject = env->NewObject(history_Class, historyConstructMId,
                                               history.timeOffset, history.glucose, history.status,
                                               history.quality, history.isValid);
        env->CallBooleanMethod(listObj, listAdd, historyObject);
    }

    return env->NewObject(broad_cls, broadConstructMId,
                          cbroadcast->timeOffset,
                          cbroadcast->historyCount,
                          cbroadcast->status,
                          cbroadcast->calTemp,
                          cbroadcast->trend,
                          listObj);
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
    jclass history_Class = env->FindClass("com/microtechmd/blecomm/parser/AidexXRawHistoryEntity");
    jmethodID historyConstructMId = env->GetMethodID(history_Class, "<init>", "(IFFFZ)V");
    jobject listObj = newList(env);
    while (rawHistoryParser.hasNext()) {
        const AidexXRawHistoryEntity *history = rawHistoryParser.getRawHistory();
        if (history != nullptr) {
            jobject historyObject = env->NewObject(history_Class, historyConstructMId,
                                                   history->timeOffset, history->i1, history->i2,
                                                   history->vc, history->isValid);
            env->CallBooleanMethod(listObj, listAdd, historyObject);
        }
    }
    return listObj;
}


JNIEXPORT jobject JNICALL Java_com_microtechmd_blecomm_parser_AidexXParser_getAidexXCalbration
        (JNIEnv *env, jclass, jbyteArray bytes) {
    const jbyte *data = env->GetByteArrayElements(bytes, JNI_FALSE);
    jint length = env->GetArrayLength(bytes);
    AidexXCalibrationsParser calibrationsParser((const char *) data, length);
    jclass history_Class = env->FindClass("com/microtechmd/blecomm/parser/AidexXCalibrationEntity");
    jmethodID caliConstructMId = env->GetMethodID(history_Class, "<init>", "(IIFZ)V");

    jobject listObj = newList(env);
    while (calibrationsParser.hasNext()) {
        const AidexXCalibrationEntity *calbration = calibrationsParser.getCalibration();
        if (calbration != nullptr) {
            jobject historyObject = env->NewObject(history_Class, caliConstructMId,
                                                   calbration->index,
                                                   calbration->timeOffset,
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



