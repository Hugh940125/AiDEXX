#include <jni.h>
#include "jni_global.h"
#include "com_microtechmd_blecomm_controller_AidexXController.h"
#include "../controller/cgm/aidexxcontroller.h"
#include "../constant/cgm/aidexxoperation.h"


static AidexXController *getPtr(JNIEnv *env, jobject obj) {
    jlong ptr = env->GetLongField(obj, fieldBleControllerPtr);
    return reinterpret_cast<AidexXController *>(ptr);
}

JNIEXPORT jint
Java_com_microtechmd_blecomm_controller_AidexXController_setDynamicAdvMode(JNIEnv *env, jobject obj,
                                                                           jint mode) {
    AidexXController *ptr = getPtr(env, obj);
    if (ptr) {
        return ptr->setDynamicAdvMode(mode);
    } else {
        return AidexXOperation::UNKNOWN;
    }
}

JNIEXPORT jint JNICALL
Java_com_microtechmd_blecomm_controller_AidexXController_setAutoUpdateStatus(JNIEnv *env,
                                                                             jobject obj) {
    AidexXController *ptr = getPtr(env, obj);
    if (ptr) {
        return ptr->setAutoUpdateStatus();
    } else {
        return AidexXOperation::UNKNOWN;
    }
}

JNIEXPORT void JNICALL Java_com_microtechmd_blecomm_controller_AidexXController_constructor
        (JNIEnv *env, jobject obj) {
    auto ptr = reinterpret_cast<jlong>(new AidexXController());
    env->SetLongField(obj, fieldBleControllerPtr, ptr);
}

JNIEXPORT void JNICALL Java_com_microtechmd_blecomm_controller_AidexXController_destructor
        (JNIEnv *env, jobject obj) {
    AidexXController *ptr = getPtr(env, obj);
    delete ptr;
    env->SetLongField(obj, fieldBleControllerPtr, reinterpret_cast<jlong>(nullptr));
}

JNIEXPORT jint JNICALL Java_com_microtechmd_blecomm_controller_AidexXController_pair
        (JNIEnv *env, jobject obj) {
    AidexXController *ptr = getPtr(env, obj);
    if (ptr) {
        return ptr->pair();
    } else {
        return AidexXOperation::UNKNOWN;
    }
}

JNIEXPORT jint JNICALL Java_com_microtechmd_blecomm_controller_AidexXController_unpair
        (JNIEnv *env, jobject obj) {
    AidexXController *ptr = getPtr(env, obj);
    if (ptr) {
        return ptr->unpair();
    } else {
        return AidexXOperation::UNKNOWN;
    }
}

JNIEXPORT jint JNICALL Java_com_microtechmd_blecomm_controller_AidexXController_getDeviceInfo
        (JNIEnv *env, jobject obj) {
    AidexXController *ptr = getPtr(env, obj);
    if (ptr) {
        return ptr->getDeviceInfo();
    } else {
        return AidexXOperation::UNKNOWN;
    }
}

JNIEXPORT jint JNICALL Java_com_microtechmd_blecomm_controller_AidexXController_getBroadcastData
        (JNIEnv *env, jobject obj) {
    AidexXController *ptr = getPtr(env, obj);
    if (ptr) {
        return ptr->getBroadcastData();
    } else {
        return AidexXOperation::UNKNOWN;
    }
}

JNIEXPORT jint JNICALL Java_com_microtechmd_blecomm_controller_AidexXController_newSensor
        (JNIEnv *env, jobject obj, jobject date) {

    AidexXController *ptr = getPtr(env, obj);
    jclass date_cls = env->GetObjectClass(date);
    if (NULL == date_cls) {
        LOGI("GetObjectClass failed");
        return AidexXOperation::UNKNOWN;
    }
    jfieldID yearFieldId = env->GetFieldID(date_cls, "year", "I"); //获得属性ID
    jfieldID monthFieldId = env->GetFieldID(date_cls, "month", "I"); //获得属性ID
    jfieldID dayFieldId = env->GetFieldID(date_cls, "day", "I"); //获得属性ID
    jfieldID hourFieldId = env->GetFieldID(date_cls, "hour", "I"); //获得属性ID
    jfieldID miniteFieldId = env->GetFieldID(date_cls, "minute", "I"); //获得属性ID
    jfieldID secondFieldId = env->GetFieldID(date_cls, "second", "I"); //获得属性ID
    jfieldID timeZoneFieldId = env->GetFieldID(date_cls, "timeZone", "I"); //获得属性ID
    jfieldID dstOffsetFieldId = env->GetFieldID(date_cls, "dstOffset", "I"); //获得属性ID
    AidexXDatetimeEntity entity;
    if (ptr) {
        entity.year = env->GetIntField(date, yearFieldId);
        entity.month = env->GetIntField(date, monthFieldId);
        entity.day = env->GetIntField(date, dayFieldId);
        entity.hour = env->GetIntField(date, hourFieldId);
        entity.minute = env->GetIntField(date, miniteFieldId);
        entity.second = env->GetIntField(date, secondFieldId);
        entity.timeZone = env->GetIntField(date, timeZoneFieldId);
        entity.dstOffset = env->GetIntField(date, dstOffsetFieldId);
        return ptr->newSensor(entity);
    } else {
        return AidexXOperation::UNKNOWN;
    }
}

JNIEXPORT jint JNICALL Java_com_microtechmd_blecomm_controller_AidexXController_getStartTime
        (JNIEnv *env, jobject obj) {
    AidexXController *ptr = getPtr(env, obj);
    if (ptr) {
        return ptr->getStartTime();
    } else {
        return AidexXOperation::UNKNOWN;
    }
}

JNIEXPORT jint JNICALL Java_com_microtechmd_blecomm_controller_AidexXController_getHistoryRange
        (JNIEnv *env, jobject obj) {
    AidexXController *ptr = getPtr(env, obj);
    if (ptr) {
        return ptr->getHistoryRange();
    } else {
        return AidexXOperation::UNKNOWN;
    }
}


JNIEXPORT jint JNICALL Java_com_microtechmd_blecomm_controller_AidexXController_getHistories
        (JNIEnv *env, jobject obj, jint index) {
    AidexXController *ptr = getPtr(env, obj);
    if (ptr) {
        return ptr->getHistories(index);
    } else {
        return AidexXOperation::UNKNOWN;
    }
}


JNIEXPORT jint JNICALL Java_com_microtechmd_blecomm_controller_AidexXController_getRawHistories
        (JNIEnv *env, jobject obj, jint index) {
    AidexXController *ptr = getPtr(env, obj);
    if (ptr) {
        return ptr->getRawHistories(index);
    } else {
        return AidexXOperation::UNKNOWN;
    }
}

JNIEXPORT jint JNICALL Java_com_microtechmd_blecomm_controller_AidexXController_calibration
        (JNIEnv *env, jobject obj, jint glucose, jint timeOffset) {
    AidexXController *ptr = getPtr(env, obj);
    if (ptr) {
        return ptr->calibration(glucose, timeOffset);
    } else {
        return AidexXOperation::UNKNOWN;
    }
}

JNIEXPORT jint JNICALL Java_com_microtechmd_blecomm_controller_AidexXController_getCalibrationRange
        (JNIEnv *env, jobject obj) {
    AidexXController *ptr = getPtr(env, obj);
    if (ptr) {
        return ptr->getCalibrationRange();
    } else {
        return AidexXOperation::UNKNOWN;
    }
}

JNIEXPORT jint JNICALL Java_com_microtechmd_blecomm_controller_AidexXController_getCalibration
        (JNIEnv *env, jobject obj, jint index) {
    AidexXController *ptr = getPtr(env, obj);
    if (ptr) {
        return ptr->getCalibration(index);
    } else {
        return AidexXOperation::UNKNOWN;
    }
}

JNIEXPORT jint JNICALL Java_com_microtechmd_blecomm_controller_AidexXController_getDefaultParamData
        (JNIEnv *env, jobject obj) {
    AidexXController *ptr = getPtr(env, obj);
    if (ptr) {
        return ptr->getDefaultParamData();
    } else {
        return AidexXOperation::UNKNOWN;
    }
}


JNIEXPORT jint JNICALL Java_com_microtechmd_blecomm_controller_AidexXController_setDefaultParamData
        (JNIEnv *env, jobject obj, jfloatArray array) {
    AidexXController *ptr = getPtr(env, obj);
    if (ptr) {
        jfloat *chost = env->GetFloatArrayElements(array, JNI_FALSE);
        jint result = ptr->setDefaultParamData(chost);
        env->ReleaseFloatArrayElements(array, chost, JNI_FALSE);
        return result;
    } else {
        return AidexXOperation::UNKNOWN;
    }
}

JNIEXPORT jint JNICALL Java_com_microtechmd_blecomm_controller_AidexXController_getSensorCheck
        (JNIEnv *env, jobject obj) {
    AidexXController *ptr = getPtr(env, obj);
    if (ptr) {
        return ptr->getSensorCheck();
    } else {
        return AidexXOperation::UNKNOWN;
    }
}

JNIEXPORT jint JNICALL Java_com_microtechmd_blecomm_controller_AidexXController_reset
        (JNIEnv *env, jobject obj) {
    AidexXController *ptr = getPtr(env, obj);
    if (ptr) {
        return ptr->reset();
    } else {
        return AidexXOperation::UNKNOWN;
    }
}

JNIEXPORT jint JNICALL Java_com_microtechmd_blecomm_controller_AidexXController_shelfMode
        (JNIEnv *env, jobject obj) {
    AidexXController *ptr = getPtr(env, obj);
    if (ptr) {
        return ptr->shelfMode();
    } else {
        return AidexXOperation::UNKNOWN;
    }
}


JNIEXPORT jint JNICALL Java_com_microtechmd_blecomm_controller_AidexXController_deleteBond
        (JNIEnv *env, jobject obj) {
    AidexXController *ptr = getPtr(env, obj);
    if (ptr) {
        return ptr->DeleteBond();
    } else {
        return AidexXOperation::UNKNOWN;
    }
}


JNIEXPORT jint JNICALL Java_com_microtechmd_blecomm_controller_AidexXController_clearStorage
        (JNIEnv *env, jobject obj) {
    AidexXController *ptr = getPtr(env, obj);
    if (ptr) {
        return ptr->ClearStorage();
    } else {
        return AidexXOperation::UNKNOWN;
    }
}

JNIEXPORT jint JNICALL Java_com_microtechmd_blecomm_controller_AidexXController_setGcBiasTrimming
        (JNIEnv *env, jobject obj, jint value) {
    AidexXController *ptr = getPtr(env, obj);
    if (ptr) {
        return ptr->setGcBiasTrimming(value);
    } else {
        return AidexXOperation::UNKNOWN;
    }
}

JNIEXPORT jint JNICALL Java_com_microtechmd_blecomm_controller_AidexXController_setGcImeasTrimming
        (JNIEnv *env, jobject obj, jint zero, jint scale) {
    AidexXController *ptr = getPtr(env, obj);
    if (ptr) {
        return ptr->setGcImeasTrimming(zero, scale);
    } else {
        return AidexXOperation::UNKNOWN;
    }
}

JNIEXPORT jint Java_com_microtechmd_blecomm_controller_AidexXController_isBleNativePaired
        (JNIEnv *env, jobject obj) {
    AidexXController *ptr = getPtr(env, obj);
    if (ptr) {
        return ptr->isBleNativePaired();
    } else {
        return AidexXOperation::UNKNOWN;
    }
}

JNIEXPORT jint Java_com_microtechmd_blecomm_controller_AidexXController_isAesInitialized
        (JNIEnv *env, jobject obj) {
    AidexXController *ptr = getPtr(env, obj);
    if (ptr) {
        return ptr->isAesInitialized();
    } else {
        return AidexXOperation::UNKNOWN;
    }
}

extern "C"
JNIEXPORT void JNICALL Java_com_microtechmd_blecomm_controller_AidexXController_constructorWithInfo
        (JNIEnv *env, jobject obj, jobject info) {
    jclass info_cls = env->GetObjectClass(info);
    jlong ptr;
    if (NULL == info_cls) {
        ptr = reinterpret_cast<jlong>(new AidexXController());
    } else {
        BleControllerInfo bleControllerInfo;
        jfieldID typeField = env->GetFieldID(info_cls, "type", "I");
        jfieldID addressField = env->GetFieldID(info_cls, "address", "Ljava/lang/String;");
        jfieldID nameField = env->GetFieldID(info_cls, "name", "Ljava/lang/String;");
        jfieldID snField = env->GetFieldID(info_cls, "sn", "Ljava/lang/String;");
        jfieldID rssiField = env->GetFieldID(info_cls, "rssi", "I");
        jfieldID paramsField = env->GetFieldID(info_cls, "params", "[B");
        bleControllerInfo.rssi = dev_type(env->GetIntField(info, rssiField));
        bleControllerInfo.type = dev_type(env->GetIntField(info, typeField));
        auto jAdd = (jstring) env->GetObjectField(info, addressField);
        bleControllerInfo.address = env->GetStringUTFChars(jAdd, JNI_FALSE);
        auto jName = (jstring) env->GetObjectField(info, nameField);
        bleControllerInfo.name = env->GetStringUTFChars(jName, JNI_FALSE);
        auto jSn = (jstring) env->GetObjectField(info, snField);
        bleControllerInfo.sn = env->GetStringUTFChars(jSn, JNI_FALSE);
        auto params = (jbyteArray) (env->GetObjectField(info, paramsField));
        jbyte *paramsBytes = env->GetByteArrayElements(params, JNI_FALSE);
        jsize paramsLength = env->GetArrayLength(params);
        std::vector<uint8_t> paramsVec(paramsBytes, paramsBytes + paramsLength);
        bleControllerInfo.params = paramsVec;
        ptr = reinterpret_cast<jlong>(new AidexXController(bleControllerInfo));
    }

    env->SetLongField(obj, fieldBleControllerPtr, ptr);
}