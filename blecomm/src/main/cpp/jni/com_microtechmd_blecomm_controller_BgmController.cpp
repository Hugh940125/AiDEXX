#include <jni.h>
#include "jni_global.h"
#include "com_microtechmd_blecomm_controller_BgmController.h"
#include "../controller/bgm/bgmcontroller.h"
#include "../constant/bleoperation.h"


static BgmController *getPtr(JNIEnv *env, jobject obj) {
    jlong ptr = env->GetLongField(obj, fieldBleControllerPtr);
    return reinterpret_cast<BgmController *>(ptr);
}

JNIEXPORT void JNICALL Java_com_microtechmd_blecomm_controller_BgmController_constructor
        (JNIEnv *env, jobject obj) {
    auto ptr = reinterpret_cast<jlong>(new BgmController());
    env->SetLongField(obj, fieldBleControllerPtr, ptr);
}

JNIEXPORT void JNICALL Java_com_microtechmd_blecomm_controller_BgmController_destructor
        (JNIEnv *env, jobject obj) {
    BgmController *ptr = getPtr(env, obj);
    delete ptr;
    env->SetLongField(obj, fieldBleControllerPtr, reinterpret_cast<jlong>(nullptr));
}

JNIEXPORT jint JNICALL Java_com_microtechmd_blecomm_controller_BgmController_getDeviceInfo
        (JNIEnv *env, jobject obj) {
    BgmController *ptr = getPtr(env, obj);
    if (ptr) {
        return ptr->getDeviceInfo();
    } else {
        return BleOperation::UNKNOWN;
    }
}

JNIEXPORT jint JNICALL Java_com_microtechmd_blecomm_controller_BgmController_getHistory
        (JNIEnv *env, jobject obj, jint index) {
    BgmController *ptr = getPtr(env, obj);
    if (ptr) {
        return ptr->getHistory(index);
    } else {
        return BleOperation::UNKNOWN;
    }
}