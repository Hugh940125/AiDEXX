#include <jni.h>
#include "jni_global.h"
#include "com_microtechmd_blecomm_controller_CgmController.h"
#include "../controller/cgm/cgmcontroller.h"
#include "../constant/cgm/cgmoperation.h"


static CgmController *getPtr(JNIEnv *env, jobject obj) {
    jlong ptr = env->GetLongField(obj, fieldBleControllerPtr);
    return reinterpret_cast<CgmController *>(ptr);
}

JNIEXPORT void JNICALL Java_com_microtechmd_blecomm_controller_CgmController_constructor
        (JNIEnv *env, jobject obj) {
    auto ptr = reinterpret_cast<jlong>(new CgmController());
    env->SetLongField(obj, fieldBleControllerPtr, ptr);
}

JNIEXPORT void JNICALL Java_com_microtechmd_blecomm_controller_CgmController_destructor
        (JNIEnv *env, jobject obj) {
    CgmController *ptr = getPtr(env, obj);
    delete ptr;
    env->SetLongField(obj, fieldBleControllerPtr, reinterpret_cast<jlong>(nullptr));
}

JNIEXPORT jint JNICALL Java_com_microtechmd_blecomm_controller_CgmController_getHistories
        (JNIEnv *env, jobject obj, jint index) {
    CgmController *ptr = getPtr(env, obj);
    if (ptr) {
        return ptr->getHistories(index);
    } else {
        return CgmOperation::UNKNOWN;
    }
}

JNIEXPORT jfloat JNICALL Java_com_microtechmd_blecomm_controller_CgmController_getHypo
        (JNIEnv *env, jobject obj) {
    CgmController *ptr = getPtr(env, obj);
    if (ptr) {
        return ptr->getHypo();
    } else {
        return CgmOperation::UNKNOWN;
    }
}

JNIEXPORT jfloat JNICALL Java_com_microtechmd_blecomm_controller_CgmController_getHyper
        (JNIEnv *env, jobject obj) {
    CgmController *ptr = getPtr(env, obj);
    if (ptr) {
        return ptr->getHyper();
    } else {
        return CgmOperation::UNKNOWN;
    }
}

JNIEXPORT jint JNICALL Java_com_microtechmd_blecomm_controller_CgmController_getDeviceInfo
        (JNIEnv *env, jobject obj) {
    CgmController *ptr = getPtr(env, obj);
    if (ptr) {
        return ptr->getDeviceInfo();
    } else {
        return CgmOperation::UNKNOWN;
    }
}



JNIEXPORT jint JNICALL Java_com_microtechmd_blecomm_controller_CgmController_getDefaultParamData
        (JNIEnv *env, jobject obj) {
    CgmController *ptr = getPtr(env, obj);
    if (ptr) {
        return ptr->getDefaultParamData();
    } else {
        return CgmOperation::UNKNOWN;
    }
}


JNIEXPORT jint JNICALL Java_com_microtechmd_blecomm_controller_CgmController_setDefaultParamData
        (JNIEnv *env, jobject obj, jfloatArray array) {
    CgmController *ptr = getPtr(env, obj);
    if (ptr) {

        jfloat *chost = env->GetFloatArrayElements(array, JNI_FALSE);
        jint result = ptr->setDefaultParamData(chost);
        env->ReleaseFloatArrayElements(array, chost, JNI_FALSE);
        return result;
    } else {
        return CgmOperation::UNKNOWN;
    }
}



JNIEXPORT jint JNICALL Java_com_microtechmd_blecomm_controller_CgmController_getFullHistories
        (JNIEnv *env, jobject obj, jint index) {
    CgmController *ptr = getPtr(env, obj);
    if (ptr) {
        return ptr->getFullHistories(index);
    } else {
        return CgmOperation::UNKNOWN;
    }
}

JNIEXPORT jint JNICALL Java_com_microtechmd_blecomm_controller_CgmController_newSensor
        (JNIEnv *env, jobject obj, jboolean isNew, jlong datetime) {
    CgmController *ptr = getPtr(env, obj);
    if (ptr) {
        return ptr->newSensor(isNew, datetime);
    } else {
        return CgmOperation::UNKNOWN;
    }
}

JNIEXPORT jint JNICALL Java_com_microtechmd_blecomm_controller_CgmController_setDatetime
        (JNIEnv *env, jobject obj, jlong datetime) {
    CgmController *ptr = getPtr(env, obj);
    if (ptr) {
        return ptr->setDatetime(datetime);
    } else {
        return CgmOperation::UNKNOWN;
    }
}

JNIEXPORT jint JNICALL Java_com_microtechmd_blecomm_controller_CgmController_recordBg
        (JNIEnv *env, jobject obj, jfloat glucose, jlong datetime) {
    CgmController *ptr = getPtr(env, obj);
    if (ptr) {
        return ptr->recordBg(glucose, datetime);
    } else {
        return CgmOperation::UNKNOWN;
    }
}

JNIEXPORT jint JNICALL Java_com_microtechmd_blecomm_controller_CgmController_calibration
        (JNIEnv *env, jobject obj, jfloat glucose, jlong datetime) {
    CgmController *ptr = getPtr(env, obj);
    if (ptr) {
        return ptr->calibration(glucose, datetime);
    } else {
        return CgmOperation::UNKNOWN;
    }
}

JNIEXPORT jint JNICALL Java_com_microtechmd_blecomm_controller_CgmController_setHyper
        (JNIEnv *env, jobject obj, jfloat hyper) {
    CgmController *ptr = getPtr(env, obj);
    if (ptr) {
        return ptr->setHyper(hyper);
    } else {
        return CgmOperation::UNKNOWN;
    }
}

JNIEXPORT jint JNICALL Java_com_microtechmd_blecomm_controller_CgmController_setHypo
        (JNIEnv *env, jobject obj, jfloat hypo) {
    CgmController *ptr = getPtr(env, obj);
    if (ptr) {
        return ptr->setHypo(hypo);
    } else {
        return CgmOperation::UNKNOWN;
    }
}

JNIEXPORT void JNICALL Java_com_microtechmd_blecomm_controller_CgmController_initialSettings
        (JNIEnv *env, jobject obj, jfloat hypo, jfloat hyper) {
    CgmController *ptr = getPtr(env, obj);
    if (ptr) {
        ptr->initialSettings(hypo, hyper);
    }
}