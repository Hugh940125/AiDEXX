#include <jni.h>
#include "jni_global.h"
#include "com_microtechmd_blecomm_controller_PumpController.h"
#include "../pumpcomm.h"


static PumpController *getPtr(JNIEnv *env, jobject obj) {
    jlong ptr = env->GetLongField(obj, fieldBleControllerPtr);
    return reinterpret_cast<PumpController *>(ptr);
}

JNIEXPORT void JNICALL Java_com_microtechmd_blecomm_controller_PumpController_constructor
        (JNIEnv *env, jobject obj) {
    auto ptr = reinterpret_cast<jlong>(new PumpController());
    env->SetLongField(obj, fieldBleControllerPtr, ptr);
}

JNIEXPORT void JNICALL Java_com_microtechmd_blecomm_controller_PumpController_destructor
        (JNIEnv *env, jobject obj) {
    PumpController *ptr = getPtr(env, obj);
    delete ptr;
    env->SetLongField(obj, fieldBleControllerPtr, reinterpret_cast<jlong>(nullptr));
}

JNIEXPORT jint JNICALL Java_com_microtechmd_blecomm_controller_PumpController_getDeviceInfo
        (JNIEnv *env, jobject obj) {
    PumpController *ptr = getPtr(env, obj);
    if (ptr) {
        return ptr->getDeviceInfo();
    } else {
        return PumpOperation::UNKNOWN;
    }
}

JNIEXPORT jint JNICALL Java_com_microtechmd_blecomm_controller_PumpController_getHistory
        (JNIEnv *env, jobject obj, jint index) {
    PumpController *ptr = getPtr(env, obj);
    if (ptr) {
        return ptr->getHistory(index);
    } else {
        return PumpOperation::UNKNOWN;
    }
}

JNIEXPORT jint JNICALL Java_com_microtechmd_blecomm_controller_PumpController_setDatetime
        (JNIEnv *env, jobject obj, jstring datetime) {
    PumpController *ptr = getPtr(env, obj);
    if (ptr) {

        const char *csn = env->GetStringUTFChars(datetime, JNI_FALSE);
        jint result = ptr->setDatetime(string(csn));
        env->ReleaseStringUTFChars(datetime, csn);
        return result;
    } else {
        return PumpOperation::UNKNOWN;
    }
}

JNIEXPORT jint JNICALL Java_com_microtechmd_blecomm_controller_PumpController_setCgmSn
        (JNIEnv *env, jobject obj, jstring datetime) {
    PumpController *ptr = getPtr(env, obj);
    if (ptr) {
        const char *csn = env->GetStringUTFChars(datetime, JNI_FALSE);
        jint result = ptr->setCgmSn(string(csn));
        env->ReleaseStringUTFChars(datetime, csn);
        return result;
    } else {
        return PumpOperation::UNKNOWN;
    }
}


JNIEXPORT jint JNICALL Java_com_microtechmd_blecomm_controller_PumpController_setAutoMode
        (JNIEnv *env, jobject obj, jboolean isOn) {
    PumpController *ptr = getPtr(env, obj);
    if (ptr) {
        return ptr->setAutoMode(isOn);
    } else {
        return PumpOperation::UNKNOWN;
    }
}


JNIEXPORT jint JNICALL Java_com_microtechmd_blecomm_controller_PumpController_getMode
        (JNIEnv *env, jobject obj) {

    PumpController *ptr = getPtr(env, obj);
    if (ptr) {
        return ptr->getMode();
    } else {
        return PumpOperation::UNKNOWN;
    }
}

JNIEXPORT jint JNICALL Java_com_microtechmd_blecomm_controller_PumpController_getBolusProfile
        (JNIEnv *env, jobject obj) {

    PumpController *ptr = getPtr(env, obj);
    if (ptr) {
        return ptr->getBolusProfile();
    } else {
        return PumpOperation::UNKNOWN;
    }
}

/**
 * 设置模式
 *
 * */
JNIEXPORT jint JNICALL Java_com_microtechmd_blecomm_controller_PumpController_setMode
        (JNIEnv *env, jobject obj, jint mode) {

    PumpController *ptr = getPtr(env, obj);
    if (ptr) {
        return ptr->setMode(mode);
    } else {
        return PumpOperation::UNKNOWN;
    }
}


/**
 *
 *
 * 设置基础率
 *
 * **/
JNIEXPORT jint JNICALL Java_com_microtechmd_blecomm_controller_PumpController_setBasalProfile
        (JNIEnv *env, jobject obj, jfloatArray array) {

    PumpController *ptr = getPtr(env, obj);
    if (ptr) {

        jfloat *chost = env->GetFloatArrayElements(array, JNI_FALSE);
        jint result = ptr->setBasalProfile(chost);
        env->ReleaseFloatArrayElements(array, chost, JNI_FALSE);
        return result;
    } else {
        return PumpOperation::UNKNOWN;
    }

}

/**
 *
 *
 * 设置大剂量
 *
 * ***/
JNIEXPORT jint JNICALL Java_com_microtechmd_blecomm_controller_PumpController_setBolusProfile
        (JNIEnv *env, jobject obj, jfloat amountTotal, jfloat bolusRatio, jfloat amountExtended,
         jint intervalExtended) {

    PumpController *ptr = getPtr(env, obj);
    if (ptr) {
        return ptr->setBolusProfile(amountTotal, bolusRatio, amountExtended, intervalExtended);
    } else {
        return PumpOperation::UNKNOWN;
    }
}

/***
 *
 * 设置临时基础率
 *
 * **/
JNIEXPORT jint JNICALL Java_com_microtechmd_blecomm_controller_PumpController_setTemporaryProfile
        (JNIEnv *env, jobject obj, jfloat tempBasal, jint interval) {

    PumpController *ptr = getPtr(env, obj);
    if (ptr) {
        return ptr->setTemporaryProfile(tempBasal, interval);
    } else {
        return PumpOperation::UNKNOWN;
    }
}

/***
 *
 * 设置
 *
 * */
JNIEXPORT jint JNICALL Java_com_microtechmd_blecomm_controller_PumpController_setSetting
        (JNIEnv *env, jobject obj, jfloatArray array) {

    PumpController *ptr = getPtr(env, obj);
    if (ptr) {

        jfloat *chost = env->GetFloatArrayElements(array, JNI_FALSE);
        jint result = ptr->setSetting(chost);
        env->ReleaseFloatArrayElements(array, chost, JNI_FALSE);
        return result;
    } else {
        return PumpOperation::UNKNOWN;
    }

}


/**
 * 推杆回退
 *
 * */
JNIEXPORT jint JNICALL Java_com_microtechmd_blecomm_controller_PumpController_setRewinding
        (JNIEnv *env, jobject obj, jfloat amount) {

    PumpController *ptr = getPtr(env, obj);
    if (ptr) {
        return ptr->setRewinding(amount);
    } else {
        return PumpOperation::UNKNOWN;
    }

}

/**
 * 推杆定位
 *
 * */
JNIEXPORT jint JNICALL Java_com_microtechmd_blecomm_controller_PumpController_setPriming
        (JNIEnv *env, jobject obj, jfloat amount) {

    PumpController *ptr = getPtr(env, obj);
    if (ptr) {
        return ptr->setPriming(amount);
    } else {
        return PumpOperation::UNKNOWN;
    }
}

JNIEXPORT jint JNICALL Java_com_microtechmd_blecomm_controller_PumpController_setGlucoseTarget
        (JNIEnv *env, jobject obj, jfloat targetLower,jfloat targetUpper) {

    PumpController *ptr = getPtr(env, obj);
    if (ptr) {
        return ptr->setGlucoseTarget(targetLower,targetUpper);
    } else {
        return PumpOperation::UNKNOWN;
    }
}


JNIEXPORT jint JNICALL Java_com_microtechmd_blecomm_controller_PumpController_setIsf
        (JNIEnv *env, jobject obj, jfloat amount) {

    PumpController *ptr = getPtr(env, obj);
    if (ptr) {
        return ptr->setIsf(amount);
    } else {
        return PumpOperation::UNKNOWN;
    }
}


//JNIEXPORT jint JNICALL Java_com_microtechmd_blecomm_controller_PumpController_setGlucose
//        (JNIEnv *env, jobject obj, jfloat amount) {
//
//    PumpController *ptr = getPtr(env, obj);
//    if (ptr) {
//        return ptr->setGlucose(amount);
//    } else {
//        return PumpOperation::UNKNOWN;
//    }
//}



JNIEXPORT jint JNICALL Java_com_microtechmd_blecomm_controller_PumpController_getOcclusion
        (JNIEnv *env, jobject obj) {

    PumpController *ptr = getPtr(env, obj);
    if (ptr) {
        return ptr->getOcclusion();
    } else {
        return PumpOperation::UNKNOWN;
    }

}

JNIEXPORT jint JNICALL Java_com_microtechmd_blecomm_controller_PumpController_setAddress
        (JNIEnv *env, jobject obj) {

    PumpController *ptr = getPtr(env, obj);
    if (ptr) {
        return ptr->setAddress();
    } else {
        return PumpOperation::UNKNOWN;
    }
}

JNIEXPORT jint JNICALL Java_com_microtechmd_blecomm_controller_PumpController_getBasalProfile
        (JNIEnv *env, jobject obj) {

    PumpController *ptr = getPtr(env, obj);
    if (ptr) {
        return ptr->getBasalProfile();
    } else {
        return PumpOperation::UNKNOWN;
    }
}

JNIEXPORT jint JNICALL Java_com_microtechmd_blecomm_controller_PumpController_getSetting
        (JNIEnv *env, jobject obj) {

    PumpController *ptr = getPtr(env, obj);
    if (ptr) {
        return ptr->getSetting();
    } else {
        return PumpOperation::UNKNOWN;
    }
}


//JNIEXPORT jint JNICALL Java_com_microtechmd_blecomm_controller_PumpController_getPumpCapacityStatus
//        (JNIEnv *env, jobject obj) {
//
//    PumpController *ptr = getPtr(env, obj);
//    if (ptr) {
//        return ptr->getPumpCapacityStatus();
//    } else {
//        return PumpOperation::UNKNOWN;
//    }
//}




JNIEXPORT jint JNICALL Java_com_microtechmd_blecomm_controller_PumpController_clearAddress
        (JNIEnv *env, jobject obj) {

    PumpController *ptr = getPtr(env, obj);
    if (ptr) {
        return ptr->clearAddress();
    } else {
        return PumpOperation::UNKNOWN;
    }
}


JNIEXPORT jint JNICALL Java_com_microtechmd_blecomm_controller_PumpController_setEventConfirmed
        (JNIEnv *env, jobject obj, jint eventIndex, jint event, jint value) {

    PumpController *ptr = getPtr(env, obj);
    if (ptr) {
        return ptr->setEventConfirmed(eventIndex, event, value);
    } else {
        return PumpOperation::UNKNOWN;
    }
}

JNIEXPORT jint JNICALL Java_com_microtechmd_blecomm_controller_PumpController_setBolusRatio
        (JNIEnv *env, jobject obj, jint multiple, jint division) {

    PumpController *ptr = getPtr(env, obj);
    if (ptr) {
        return ptr->setBolusRatio(multiple, division);
    } else {
        return PumpOperation::UNKNOWN;
    }
}


JNIEXPORT jint JNICALL
Java_com_microtechmd_blecomm_controller_PumpController_setTemporaryPercentProfile
        (JNIEnv *env, jobject obj, jint tempBasalPercent, jint interval) {

    PumpController *ptr = getPtr(env, obj);
    if (ptr) {
        return ptr->setTemporaryPercentProfile(tempBasalPercent, interval);
    } else {
        return PumpOperation::UNKNOWN;
    }
}

