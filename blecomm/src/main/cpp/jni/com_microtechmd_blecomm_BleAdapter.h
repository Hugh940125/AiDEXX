/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class com_microtechmd_blecomm_BleAdapter */

#ifndef _Included_com_microtechmd_blecomm_BleAdapter
#define _Included_com_microtechmd_blecomm_BleAdapter
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     com_microtechmd_blecomm_BleAdapter
 * Method:    constructor
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_microtechmd_blecomm_BleAdapter_constructor
  (JNIEnv *, jobject);

/*
 * Class:     com_microtechmd_blecomm_BleAdapter
 * Method:    destructor
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_microtechmd_blecomm_BleAdapter_destructor
  (JNIEnv *, jobject);

/*
 * Class:     com_microtechmd_blecomm_BleAdapter
 * Method:    getCharacteristicUUID
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_com_microtechmd_blecomm_BleAdapter_getCharacteristicUUID
  (JNIEnv *, jobject);

/*
 * Class:     com_microtechmd_blecomm_BleAdapter
 * Method:    setDiscoverTimeoutSeconds
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_com_microtechmd_blecomm_BleAdapter_setDiscoverTimeoutSeconds
  (JNIEnv *, jobject, jint);

/*
 * Class:     com_microtechmd_blecomm_BleAdapter
 * Method:    onScanRespond
 * Signature: (Ljava/lang/String;I[B)V
 */
JNIEXPORT void JNICALL Java_com_microtechmd_blecomm_BleAdapter_onScanRespond
  (JNIEnv *, jobject, jstring, jint, jbyteArray);

/*
 * Class:     com_microtechmd_blecomm_BleAdapter
 * Method:    onAdvertise
 * Signature: (Ljava/lang/String;I[B)V
 */
JNIEXPORT void JNICALL Java_com_microtechmd_blecomm_BleAdapter_onAdvertise
  (JNIEnv *, jobject, jstring, jint, jbyteArray);

/*
 * Class:     com_microtechmd_blecomm_BleAdapter
 * Method:    onConnectSuccess
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_microtechmd_blecomm_BleAdapter_onConnectSuccess
  (JNIEnv *, jobject);

/*
 * Class:     com_microtechmd_blecomm_BleAdapter
 * Method:    onConnectFailure
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_microtechmd_blecomm_BleAdapter_onConnectFailure
  (JNIEnv *, jobject);

/*
 * Class:     com_microtechmd_blecomm_BleAdapter
 * Method:    onDisconnected
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_microtechmd_blecomm_BleAdapter_onDisconnected
  (JNIEnv *, jobject);

/*
 * Class:     com_microtechmd_blecomm_BleAdapter
 * Method:    onReceiveData
 * Signature: ([B)V
 */
JNIEXPORT void JNICALL Java_com_microtechmd_blecomm_BleAdapter_onReceiveData
  (JNIEnv *, jobject, jbyteArray);

#ifdef __cplusplus
}
#endif
#endif
