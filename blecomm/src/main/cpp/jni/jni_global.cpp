#include <jni.h>
#include "jni_global.h"
#include "../devcomm/CLibrary/global.h"

JavaVM *m_jvm;
JNIEnv *m_env;
static bool m_attached;

jmethodID listAdd;
static jclass listCls;
static jmethodID listInit;

jfieldID fieldBleAdapterPtr;
jfieldID fieldBleControllerPtr;
jfieldID fieldMessageCallbackPtr;

jobject newList(JNIEnv *env) {
    return env->NewObject(listCls, listInit);
}

JNIEXPORT jint JNI_OnLoad(JavaVM *vm, void *reserved) {
    LOGE("Load JNI library");

    m_jvm = vm;
    JNIEnv *env;
    vm->GetEnv((void **)&env, JNI_VERSION_1_4);

    listCls = env->FindClass("java/util/ArrayList");
    listCls = (jclass)env->NewWeakGlobalRef((jobject)listCls);
    listInit = env->GetMethodID(listCls, "<init>", "()V");
    listAdd = env->GetMethodID(listCls, "add", "(Ljava/lang/Object;)Z");

    jclass bleAdapterCls = env->FindClass("com/microtechmd/blecomm/BleAdapter");
    fieldBleAdapterPtr = env->GetFieldID(bleAdapterCls, "ptr", "J");

    jclass bleControllerCls = env->FindClass("com/microtechmd/blecomm/controller/BleController");
    fieldBleControllerPtr = env->GetFieldID(bleControllerCls, "ptr", "J");
    fieldMessageCallbackPtr = env->GetFieldID(bleControllerCls, "messageCallbackPtr","J");

    return JNI_VERSION_1_4;
}

JNIEXPORT void JNI_OnUnload(JavaVM *vm, void *reserved) {
    LOGE("Unload JNI library");

    JNIEnv *env;
    vm->GetEnv((void **)&env, JNI_VERSION_1_4);

    env->DeleteWeakGlobalRef(listCls);
}

void clearException(){
    jthrowable ex = m_env->ExceptionOccurred();
    if (ex != NULL) {
        // 打印异常信息等
        LOG_I("BleController ex");
        // 必须清除异常
        m_env->ExceptionClear();
    }
}

bool AttachCurrentThread() {
    while(m_attached) {
        LOG_I("wait");
    }
    if (m_jvm->GetEnv((void **)&m_env, JNI_VERSION_1_4) == JNI_OK) {
        LOG_I("AttachCurrentThread 1 ");
        return false;
    }

    while(m_attached || m_env) {
        LOG_I("wait");
    }
    if (m_jvm->AttachCurrentThread(&m_env, nullptr) == JNI_OK) {
        LOG_I("AttachCurrentThread 2");
        m_attached = true;
        return true;
    } else {
        m_env = nullptr;
        LOG_I("Attaching Thread Failed");
        throw JNI_ERR;
    }
}

void DetachCurrentThread(bool attached) {
    LOG_I("DetachCurrentThread b  b");

    if (attached) {
        LOG_I("DetachCurrentThread b");
        if (m_attached) {
            LOG_I("DetachCurrentThread 1");
            m_jvm->DetachCurrentThread();
            LOG_I("DetachCurrentThread 2");
            m_attached = false;
        }
    }
    m_env = nullptr;
}
