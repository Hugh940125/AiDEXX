#include <jni.h>
#include "com_microtechmd_blecomm_BleAdapter.h"
#include "jni_bleadapter.h"
#include "jni_global.h"

static jobject m_obj;
jmethodID m_executeStartScan;
jmethodID m_executeStopScan;
jmethodID m_isReadyToConnect;
jmethodID m_executeConnect;
jmethodID m_executeDisconnect;
jmethodID m_executeWrite;
jmethodID m_executeWriteCharacteristic;
jmethodID m_executeReadCharacteristic;

static JniBleAdapter *getPtr(JNIEnv *env, jobject obj) {
    jlong ptr = env->GetLongField(obj, fieldBleAdapterPtr);
    return reinterpret_cast<JniBleAdapter *>(ptr);
}

void JniBleAdapter::executeStartScan() {
//    bool attached;
//    try {
//        attached = AttachCurrentThread();
//    } catch (int) {
//        return;
//    }

    int status;
    JNIEnv *env;
    bool isAttached = false;
    status = m_jvm->GetEnv((void **) &env, JNI_VERSION_1_6);
    if (status < 0) {
        　　m_jvm->AttachCurrentThread(&env, NULL);//将当前线程注册到虚拟机中．
        　　isAttached = true;
    }

    jint type = env->GetObjectRefType(m_obj);

    if (type != 1) {
        env->CallVoidMethod(m_obj, m_executeStartScan);
    }
    if (isAttached)
        m_jvm->DetachCurrentThread();
//    DetachCurrentThread(attached);
}

void JniBleAdapter::executeStopScan() {
//    bool attached;
//    try {
//        attached = AttachCurrentThread();
//
//    } catch (int) {
//        return;
//    }

    int status;
    JNIEnv *env;
    bool isAttached = false;
    status = m_jvm->GetEnv((void **) &env, JNI_VERSION_1_6);
    if (status < 0) {
        　　m_jvm->AttachCurrentThread(&env, NULL);//将当前线程注册到虚拟机中．
        　　isAttached = true;
    }

    jint type = env->GetObjectRefType(m_obj);
    if (type != 1) {
        env->CallVoidMethod(m_obj, m_executeStopScan);
    }
    if (isAttached)
        m_jvm->DetachCurrentThread();
//    DetachCurrentThread(attached);
}

bool JniBleAdapter::isReadyToConnect(string mac) {
    bool ret = false;

    int status;
    JNIEnv *env;
    bool isAttached = false;
    status = m_jvm->GetEnv((void **) &env, JNI_VERSION_1_6);
    if (status < 0) {
        　　m_jvm->AttachCurrentThread(&env, NULL);//将当前线程注册到虚拟机中．
        　　isAttached = true;
    }

    jstring jmac = env->NewStringUTF(mac.data());
    ret = env->CallBooleanMethod(m_obj, m_isReadyToConnect, jmac);
//    m_env->DeleteLocalRef(jmac);
    if (isAttached)
        m_jvm->DetachCurrentThread();
    return ret;
}

void JniBleAdapter::executeConnect(string mac) {

    int status;
    JNIEnv *env;
    bool isAttached = false;
    status = m_jvm->GetEnv((void **) &env, JNI_VERSION_1_6);
    if (status < 0) {
        　　m_jvm->AttachCurrentThread(&env, NULL);//将当前线程注册到虚拟机中．
        　　isAttached = true;
    }

    jstring jmac = env->NewStringUTF(mac.data());
    env->CallVoidMethod(m_obj, m_executeConnect, jmac);
//    m_env->DeleteLocalRef(jmac);
    if (isAttached)
        m_jvm->DetachCurrentThread();
}

void JniBleAdapter::executeDisconnect() {

    int status;
    JNIEnv *env;
    bool isAttached = false;
    status = m_jvm->GetEnv((void **) &env, JNI_VERSION_1_6);
    if (status < 0) {
        　　m_jvm->AttachCurrentThread(&env, NULL);//将当前线程注册到虚拟机中．
        　　isAttached = true;
    }

    env->CallVoidMethod(m_obj, m_executeDisconnect);

    if (isAttached)
        m_jvm->DetachCurrentThread();
}

void JniBleAdapter::executeWrite(const char *data, uint16 length) {

    int status;
    JNIEnv *env;
    bool isAttached = false;
    status = m_jvm->GetEnv((void **) &env, JNI_VERSION_1_6);
    if (status < 0) {
        　　m_jvm->AttachCurrentThread(&env, NULL);//将当前线程注册到虚拟机中．
        　　isAttached = true;
    }

    jbyteArray dataBytes = env->NewByteArray(length);
    env->SetByteArrayRegion(dataBytes, 0, length, (const jbyte *) data);
    if (m_obj != NULL && m_executeWrite != NULL) {
        env->CallVoidMethod(m_obj, m_executeWrite, dataBytes);
    }
    if (isAttached)
        m_jvm->DetachCurrentThread();
////    m_env->DeleteLocalRef(dataBytes);
//    DetachCurrentThread(attached);
}

void JniBleAdapter::executeWriteCharacteristic(uint16 uuid, const char *data, uint16 length) {
    int status;
    JNIEnv *env;
    bool isAttached = false;
    status = m_jvm->GetEnv((void **) &env, JNI_VERSION_1_6);
    if (status < 0) {
        　　m_jvm->AttachCurrentThread(&env, NULL);//将当前线程注册到虚拟机中．
        　　isAttached = true;
    }

    jbyteArray dataBytes = env->NewByteArray(length);
    env->SetByteArrayRegion(dataBytes, 0, length, (const jbyte *) data);
    if (m_obj != NULL && m_executeWrite != NULL) {
        env->CallVoidMethod(m_obj, m_executeWriteCharacteristic, uuid, dataBytes);
    }
    if (isAttached)
        m_jvm->DetachCurrentThread();
}

void JniBleAdapter::executeReadCharacteristic(uint16 uuid) {
    int status;
    JNIEnv *env;
    bool isAttached = false;
    status = m_jvm->GetEnv((void **) &env, JNI_VERSION_1_6);
    if (status < 0) {
        　　m_jvm->AttachCurrentThread(&env, NULL);//将当前线程注册到虚拟机中．
        　　isAttached = true;
    }
    env->CallVoidMethod(m_obj, m_executeReadCharacteristic, uuid);
    if (isAttached)
        m_jvm->DetachCurrentThread();
}


JNIEXPORT void JNICALL Java_com_microtechmd_blecomm_BleAdapter_constructor
        (JNIEnv *env, jobject obj) {
    jclass cls = env->GetObjectClass(obj);
    m_executeStartScan = env->GetMethodID(cls, "executeStartScan", "()V");
    m_executeStopScan = env->GetMethodID(cls, "executeStopScan", "()V");
    m_isReadyToConnect = env->GetMethodID(cls, "isReadyToConnect", "(Ljava/lang/String;)Z");
    m_executeConnect = env->GetMethodID(cls, "executeConnect", "(Ljava/lang/String;)V");
    m_executeDisconnect = env->GetMethodID(cls, "executeDisconnect", "()V");
    m_executeWriteCharacteristic = env->GetMethodID(cls, "executeWriteCharacteristic", "(I[B)V");
    m_executeReadCharacteristic = env->GetMethodID(cls, "executeReadCharacteristic", "(I)V");

    auto ptr = reinterpret_cast<jlong>(new JniBleAdapter());
    env->SetLongField(obj, fieldBleAdapterPtr, ptr);

    m_obj = env->NewGlobalRef(obj);
}


JNIEXPORT void JNICALL Java_com_microtechmd_blecomm_BleAdapter_destructor
        (JNIEnv *env, jobject obj) {
    JniBleAdapter *ptr = getPtr(env, obj);
    delete ptr;
    env->SetLongField(obj, fieldBleAdapterPtr, reinterpret_cast<jlong>(nullptr));
    env->DeleteGlobalRef(m_obj);
}

JNIEXPORT jint JNICALL Java_com_microtechmd_blecomm_BleAdapter_getServiceUUID
        (JNIEnv *env, jobject obj) {
    JniBleAdapter *ptr = getPtr(env, obj);
    jint ret = 0;
    if (ptr) {
        ret = ptr->getServiceUUID();
    }
    return ret;
}

JNIEXPORT jint JNICALL Java_com_microtechmd_blecomm_BleAdapter_getCharacteristicUUID
        (JNIEnv *env, jobject obj) {
    JniBleAdapter *ptr = getPtr(env, obj);
    jint ret = 0;
    if (ptr) {
        ret = ptr->getCharacteristicUUID();
    }
    return ret;
}


JNIEXPORT void JNICALL Java_com_microtechmd_blecomm_BleAdapter_setDiscoverTimeoutSeconds
        (JNIEnv *env, jobject obj, jint seconds) {
    JniBleAdapter *ptr = getPtr(env, obj);
    if (ptr) {
        ptr->setDiscoverTimeoutSeconds(seconds);
    }
}


JNIEXPORT void JNICALL Java_com_microtechmd_blecomm_BleAdapter_onScanRespond
        (JNIEnv *env, jobject obj, jstring address, jint rssi, jbyteArray data) {
    JniBleAdapter *ptr = getPtr(env, obj);
    if (ptr) {


        const char *caddress = env->GetStringUTFChars(address, JNI_FALSE);

        jbyte *cdata = env->GetByteArrayElements(data, JNI_FALSE);
        int length = env->GetArrayLength(data);

        ptr->onScanRespond(string(caddress), rssi, (const char *) cdata, length);

        env->ReleaseStringUTFChars(address, caddress);
        env->ReleaseByteArrayElements(data, cdata, JNI_FALSE);

    }
}


JNIEXPORT void JNICALL Java_com_microtechmd_blecomm_BleAdapter_onAdvertise
        (JNIEnv *env, jobject obj, jstring address, jint rssi, jbyteArray data) {


    JniBleAdapter *ptr = getPtr(env, obj);
    if (ptr) {

        const char *caddress = env->GetStringUTFChars(address, JNI_FALSE);

        jbyte *cdata = env->GetByteArrayElements(data, JNI_FALSE);
        int length = env->GetArrayLength(data);

        ptr->onAdvertise(string(caddress), rssi, (const char *) cdata, length);

        env->ReleaseStringUTFChars(address, caddress);
        env->ReleaseByteArrayElements(data, cdata, JNI_FALSE);

    }

}


JNIEXPORT void JNICALL Java_com_microtechmd_blecomm_BleAdapter_onConnectSuccess
        (JNIEnv *env, jobject obj) {
    JniBleAdapter *ptr = getPtr(env, obj);
    if (ptr) {
        ptr->onConnectSuccess();
    }
}


JNIEXPORT void JNICALL Java_com_microtechmd_blecomm_BleAdapter_onConnectFailure
        (JNIEnv *env, jobject obj) {
    JniBleAdapter *ptr = getPtr(env, obj);
    if (ptr) {
        ptr->onConnectFailure();
    }
}

JNIEXPORT void JNICALL Java_com_microtechmd_blecomm_BleAdapter_onDisconnected
        (JNIEnv *env, jobject obj) {
    JniBleAdapter *ptr = getPtr(env, obj);
    if (ptr) {
        ptr->onDisconnected();
    }
}


JNIEXPORT void JNICALL Java_com_microtechmd_blecomm_BleAdapter_onReceiveData___3B
        (JNIEnv *env, jobject obj, jbyteArray data) {
    JniBleAdapter *ptr = getPtr(env, obj);
    if (ptr) {

        jbyte *cdata = env->GetByteArrayElements(data, JNI_FALSE);
        jint length = env->GetArrayLength(data);
        ptr->onReceiveData((const char *) cdata, length);
        env->ReleaseByteArrayElements(data, cdata, JNI_FALSE);

    }
}

JNIEXPORT void JNICALL Java_com_microtechmd_blecomm_BleAdapter_onReceiveData__I_3B
        (JNIEnv *env, jobject obj, jint uuid, jbyteArray data) {
    JniBleAdapter *ptr = getPtr(env, obj);
    if (ptr) {

        jbyte *cdata = env->GetByteArrayElements(data, JNI_FALSE);
        jint length = env->GetArrayLength(data);
        ptr->onReceiveData(uuid, (const char *) cdata, length);
        env->ReleaseByteArrayElements(data, cdata, JNI_FALSE);
    }
}

