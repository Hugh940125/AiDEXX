#include <jni.h>
#include "jni_global.h"
#include "com_microtechmd_blecomm_controller_BleController.h"
#include "com_microtechmd_blecomm_BleAdapter.h"
#include "jni_bleadapter.h"
#include "../controller/blecontroller.h"
#include "../constant/bleoperation.h"
#include<android/log.h>

#define TAG "ble-jni" // 这个是自定义的LOG的标识
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG,TAG ,__VA_ARGS__) // 定义LOGD类型
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,TAG ,__VA_ARGS__) // 定义LOGI类型
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN,TAG ,__VA_ARGS__) // 定义LOGW类型
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR,TAG ,__VA_ARGS__) // 定义LOGE类型
#define LOGF(...) __android_log_print(ANDROID_LOG_FATAL,TAG ,__VA_ARGS__) // 定义LOGF类型

static jobject discoverCallback = nullptr;

static BleController *getPtr(JNIEnv *env, jobject obj) {
    jlong ptr = env->GetLongField(obj, fieldBleControllerPtr);
    return reinterpret_cast<BleController *>(ptr);
}


JNIEXPORT void JNICALL Java_com_microtechmd_blecomm_controller_BleController_setBleAdapter
        (JNIEnv *env, jclass cls, jobject adapter) {
    jlong ptr = env->GetLongField(adapter, fieldBleAdapterPtr);
    BleController::setBleAdapter(reinterpret_cast<JniBleAdapter *>(ptr));
}


JNIEXPORT void JNICALL Java_com_microtechmd_blecomm_controller_BleController_setDiscoveredCallback
        (JNIEnv *env, jclass, jobject callback) {
    jclass callbackCls = env->GetObjectClass(callback);
    jmethodID onDiscoveredMethod = env->GetMethodID(callbackCls, "onDiscovered",
                                                    "(Lcom/microtechmd/blecomm/controller/BleControllerInfo;)V");
    if (discoverCallback != nullptr)
        env->DeleteGlobalRef(discoverCallback);
    discoverCallback = env->NewGlobalRef(callback);

    BleController::setDiscoveredCallback([=](const BleControllerInfo &info) {
        if (discoverCallback != nullptr) {
            int status;
            JNIEnv *env;
            bool isAttached = false;
            status = m_jvm->GetEnv((void **) &env, JNI_VERSION_1_6);
            if (status < 0) {
                m_jvm->AttachCurrentThread(&env, NULL);//将当前线程注册到虚拟机中．
                isAttached = true;
            }

            jclass bleControllerInfoCls = env->FindClass(
                    "com/microtechmd/blecomm/controller/BleControllerInfo");
            jmethodID controllerInfo_new = env->GetMethodID(bleControllerInfoCls, "<init>", "()V");
            jobject controllerInfo = env->NewObject(bleControllerInfoCls, controllerInfo_new);
            jfieldID addressField = env->GetFieldID(bleControllerInfoCls, "address",
                                                    "Ljava/lang/String;");
            jfieldID nameField = env->GetFieldID(bleControllerInfoCls, "name",
                                                 "Ljava/lang/String;");
            jfieldID snField = env->GetFieldID(bleControllerInfoCls, "sn", "Ljava/lang/String;");
            jfieldID rssiField = env->GetFieldID(bleControllerInfoCls, "rssi", "I");
            jstring addressString = env->NewStringUTF(info.address.data());
            jstring nameString = env->NewStringUTF(info.name.data());

            jstring snString = env->NewStringUTF(info.sn.data());
            env->SetObjectField(controllerInfo, addressField, addressString);
            env->SetObjectField(controllerInfo, nameField, nameString);
            env->SetObjectField(controllerInfo, snField, snString);
            env->SetIntField(controllerInfo, rssiField, info.rssi);
            env->CallVoidMethod(discoverCallback, onDiscoveredMethod, controllerInfo);

            if (isAttached)
                m_jvm->DetachCurrentThread();
        }
    });
}


JNIEXPORT jstring JNICALL Java_com_microtechmd_blecomm_controller_BleController_getMac
        (JNIEnv *env, jobject obj) {
    BleController *ptr = getPtr(env, obj);
    return ptr ? env->NewStringUTF(ptr->getMac().data()) : nullptr;
}


JNIEXPORT jstring JNICALL Java_com_microtechmd_blecomm_controller_BleController_getName
        (JNIEnv *env, jobject obj) {
    BleController *ptr = getPtr(env, obj);
    return ptr ? env->NewStringUTF(ptr->getName().data()) : nullptr;
}


JNIEXPORT jstring JNICALL Java_com_microtechmd_blecomm_controller_BleController_getSn
        (JNIEnv *env, jobject obj) {
    BleController *ptr = getPtr(env, obj);
    return ptr ? env->NewStringUTF(ptr->getSn().data()) : nullptr;
}


JNIEXPORT jbyteArray JNICALL Java_com_microtechmd_blecomm_controller_BleController_getHostAddress
        (JNIEnv *env, jobject obj) {
    BleController *ptr = getPtr(env, obj);
    jbyteArray jhost = nullptr;
    if (ptr) {
        const char *chost = ptr->getHostAddress();
        int length = ptr->getHostAddressLength();
        jhost = env->NewByteArray(length);
        env->SetByteArrayRegion(jhost, 0, length, (const jbyte *) chost);
    }
    return jhost;
}


JNIEXPORT jbyteArray JNICALL Java_com_microtechmd_blecomm_controller_BleController_getId
        (JNIEnv *env, jobject obj) {
    BleController *ptr = getPtr(env, obj);
    jbyteArray jid = nullptr;
    if (ptr) {
        const char *cid = ptr->getId();
        int length = ptr->getIdLength();
        jid = env->NewByteArray(length);
        env->SetByteArrayRegion(jid, 0, length, (const jbyte *) cid);
    }
    return jid;
}


JNIEXPORT jbyteArray JNICALL Java_com_microtechmd_blecomm_controller_BleController_getKey
        (JNIEnv *env, jobject obj) {
    BleController *ptr = getPtr(env, obj);
    jbyteArray jkey = nullptr;
    if (ptr) {
        const char *ckey = ptr->getKey();
        int length = ptr->getKeyLength();
        jkey = env->NewByteArray(length);
        env->SetByteArrayRegion(jkey, 0, length, (const jbyte *) ckey);
    }
    return jkey;
}


JNIEXPORT jint JNICALL Java_com_microtechmd_blecomm_controller_BleController_getRssi
        (JNIEnv *env, jobject obj) {
    BleController *ptr = getPtr(env, obj);
    jint rssi = 0;
    if (ptr) {
        rssi = ptr->getRssi();
    }
    return rssi;
}


JNIEXPORT void JNICALL Java_com_microtechmd_blecomm_controller_BleController_setMac
        (JNIEnv *env, jobject obj, jstring mac) {
    BleController *ptr = getPtr(env, obj);
    if (ptr && mac) {
        const char *cmac = env->GetStringUTFChars(mac, JNI_FALSE);
        ptr->setMac(string(cmac));
        env->ReleaseStringUTFChars(mac, cmac);
    }
}


JNIEXPORT void JNICALL Java_com_microtechmd_blecomm_controller_BleController_setName
        (JNIEnv *env, jobject obj, jstring name) {
    BleController *ptr = getPtr(env, obj);
    if (ptr && name) {
        const char *cname = env->GetStringUTFChars(name, JNI_FALSE);
        ptr->setName(string(cname));
        env->ReleaseStringUTFChars(name, cname);
    }
}


JNIEXPORT void JNICALL Java_com_microtechmd_blecomm_controller_BleController_setSn
        (JNIEnv *env, jobject obj, jstring sn) {
    BleController *ptr = getPtr(env, obj);
    if (ptr && sn) {
        const char *csn = env->GetStringUTFChars(sn, JNI_FALSE);
        ptr->setSn(string(csn));
        env->ReleaseStringUTFChars(sn, csn);
    }
}


JNIEXPORT void JNICALL Java_com_microtechmd_blecomm_controller_BleController_setHostAddress
        (JNIEnv *env, jobject obj, jbyteArray host) {
    BleController *ptr = getPtr(env, obj);
    if (ptr && host) {
        jbyte *chost = env->GetByteArrayElements(host, JNI_FALSE);
        ptr->setHostAddress((const char *) chost);
        env->ReleaseByteArrayElements(host, chost, JNI_FALSE);
    }
}


JNIEXPORT void JNICALL Java_com_microtechmd_blecomm_controller_BleController_setId
        (JNIEnv *env, jobject obj, jbyteArray id) {
    BleController *ptr = getPtr(env, obj);
    if (ptr && id) {
        jbyte *cid = env->GetByteArrayElements(id, JNI_FALSE);
        ptr->setId((const char *) cid);
        env->ReleaseByteArrayElements(id, cid, JNI_FALSE);
    }
}


JNIEXPORT void JNICALL Java_com_microtechmd_blecomm_controller_BleController_setKey
        (JNIEnv *env, jobject obj, jbyteArray key) {
    BleController *ptr = getPtr(env, obj);
    if (ptr && key) {
        jbyte *ckey = env->GetByteArrayElements(key, JNI_FALSE);
        ptr->setKey((const char *) ckey);
        env->ReleaseByteArrayElements(key, ckey, JNI_FALSE);
    }
}


JNIEXPORT void JNICALL Java_com_microtechmd_blecomm_controller_BleController_setRssi
        (JNIEnv *env, jobject obj, jint rssi) {
    BleController *ptr = getPtr(env, obj);
    if (ptr) {
        ptr->setRssi(rssi);
    }
}


JNIEXPORT void JNICALL Java_com_microtechmd_blecomm_controller_BleController_setMessageCallback
        (JNIEnv *env, jobject obj, jobject callback) {
    BleController *ptr = getPtr(env, obj);
    if (ptr && callback) {
        jlong callbackPtr = env->GetLongField(obj, fieldMessageCallbackPtr);
        if (callbackPtr) {
            env->DeleteGlobalRef(reinterpret_cast<jobject>(callbackPtr));
        }
        jobject g_callback = env->NewGlobalRef(callback);

        ptr->setMessageCallback([=](uint16 op, bool success, const char *data, uint16 length) {

            int status;
            JNIEnv *env;
            bool isAttached = false;
            status = m_jvm->GetEnv((void **) &env, JNI_VERSION_1_6);
            if (status < 0) {
                　　m_jvm->AttachCurrentThread(&env, NULL);//将当前线程注册到虚拟机中．
                　　isAttached = true;
            }

            jbyteArray bytes = env->NewByteArray(length);
            bytes = (jbyteArray) env->NewWeakGlobalRef((jobject) bytes);
            env->SetByteArrayRegion(bytes, 0, length, (const jbyte *) data);
            if (env->ExceptionCheck()) {
                LOGI("BleController begin 41");
                env->ExceptionDescribe();
                LOGI("BleController begin 42");
                env->ExceptionClear();
                LOGI("BleController begin 43");
                return;
            }

            jint type = env->GetObjectRefType(g_callback);

            if (type == 1) {

                LOGI("BleController type 无效");
                if (isAttached)
                    m_jvm->DetachCurrentThread();
            } else {

                jclass cls = env->GetObjectClass(g_callback);

                if (env->ExceptionCheck()) {
                    LOGI("BleController begin Exception 45");
                    env->ExceptionDescribe();
                    env->ExceptionClear();
                    LOGI("BleController begin Exception 46");
                    if (isAttached)
                        m_jvm->DetachCurrentThread();
                    LOGI("BleController begin Exception 47");
                    return;
                }

                jmethodID method = env->GetMethodID(cls, "onReceive", "(IZ[B)V");

                if (env->ExceptionCheck()) {
                    LOGI("BleController begin Exception 51");
                    env->ExceptionDescribe();
                    env->ExceptionClear();
                    LOGI("BleController begin Exception 53");
                    if (isAttached)
                        m_jvm->DetachCurrentThread();
                    LOGI("BleController begin Exception 52");
                    return;
                }

                if (env->ExceptionCheck()) {

                    LOGI("BleController begin Exception 6e");

                    env->ExceptionDescribe();
                    env->ExceptionClear();//清除引发的异常，在Java层不会打印异常堆栈信息，如果不清除，后面的调用ThrowNew抛出的异常堆栈信息会
//覆盖前面的异常信息
                    if (isAttached)
                        m_jvm->DetachCurrentThread();
                    return;
                }
                env->CallVoidMethod(g_callback, method, op, success, bytes);


                if (isAttached)
                    m_jvm->DetachCurrentThread();
            }
        });

        env->SetLongField(obj, fieldMessageCallbackPtr, reinterpret_cast<jlong>(g_callback));
    }
}


JNIEXPORT void JNICALL Java_com_microtechmd_blecomm_controller_BleController_startScan
        (JNIEnv *, jclass) {
    BleController::startScan();
}


JNIEXPORT void JNICALL Java_com_microtechmd_blecomm_controller_BleController_stopScan
        (JNIEnv *, jclass) {
    BleController::stopScan();
}


JNIEXPORT void JNICALL Java_com_microtechmd_blecomm_controller_BleController_register
        (JNIEnv *env, jobject obj) {
    BleController *ptr = getPtr(env, obj);
    if (ptr) {
        return ptr->doregister();
    }
}


JNIEXPORT void JNICALL Java_com_microtechmd_blecomm_controller_BleController_unregister
        (JNIEnv *env, jobject obj) {
    BleController *ptr = getPtr(env, obj);
    if (ptr) {
        return ptr->unregister();
    }
}


JNIEXPORT void JNICALL Java_com_microtechmd_blecomm_controller_BleController_disconnect
        (JNIEnv *env, jobject obj) {
    BleController *ptr = getPtr(env, obj);
    if (ptr) {
        ptr->disconnect();
    }
}


JNIEXPORT jint JNICALL Java_com_microtechmd_blecomm_controller_BleController_pair
        (JNIEnv *env, jobject obj) {
    BleController *ptr = getPtr(env, obj);
    if (ptr) {
        return ptr->pair();
    } else {
        return BleOperation::UNKNOWN;
    }
}


JNIEXPORT jint JNICALL Java_com_microtechmd_blecomm_controller_BleController_unpair
        (JNIEnv *env, jobject obj) {
    BleController *ptr = getPtr(env, obj);
    if (ptr) {
        return ptr->unpair();
    } else {
        return BleOperation::UNKNOWN;
    }
}

