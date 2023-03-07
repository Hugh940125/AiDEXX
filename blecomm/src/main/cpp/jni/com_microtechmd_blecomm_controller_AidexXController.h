/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class com_microtechmd_blecomm_controller_CgmController */

#ifndef _Included_com_microtechmd_blecomm_controller_AidexXController
#define _Included_com_microtechmd_blecomm_controller_AidexXController
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     com_microtechmd_blecomm_controller_CgmController
 * Method:    constructor
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_microtechmd_blecomm_controller_AidexXController_constructor
        (JNIEnv *, jobject);

/*
 * Class:     com_microtechmd_blecomm_controller_CgmController
 * Method:    destructor
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_microtechmd_blecomm_controller_AidexXController_destructor
        (JNIEnv *, jobject);


JNIEXPORT jint JNICALL Java_com_microtechmd_blecomm_controller_AidexXController_getDeviceInfo
        (JNIEnv *, jobject);

JNIEXPORT jint JNICALL Java_com_microtechmd_blecomm_controller_AidexXController_getBroadcastData
        (JNIEnv *, jobject);

JNIEXPORT jint JNICALL Java_com_microtechmd_blecomm_controller_AidexXController_newSensor
        (JNIEnv *, jobject, jobject);

JNIEXPORT jint JNICALL Java_com_microtechmd_blecomm_controller_AidexXController_getStartTime
        (JNIEnv *, jobject);

JNIEXPORT jint JNICALL Java_com_microtechmd_blecomm_controller_AidexXController_getHistoryRange
        (JNIEnv *, jobject);

JNIEXPORT jint JNICALL Java_com_microtechmd_blecomm_controller_AidexXController_getHistories
        (JNIEnv *, jobject, jint);

JNIEXPORT jint JNICALL Java_com_microtechmd_blecomm_controller_AidexXController_getRawHistories
        (JNIEnv *, jobject, jint);

JNIEXPORT jint JNICALL Java_com_microtechmd_blecomm_controller_AidexXController_getSensorCheck
        (JNIEnv *, jobject);

JNIEXPORT jint JNICALL Java_com_microtechmd_blecomm_controller_AidexXController_calibration
        (JNIEnv *, jobject, jint, jint);

JNIEXPORT jint JNICALL Java_com_microtechmd_blecomm_controller_AidexXController_getCalibrationRange
        (JNIEnv *, jobject);

JNIEXPORT jint JNICALL Java_com_microtechmd_blecomm_controller_AidexXController_getCalibration
        (JNIEnv *, jobject, jint);

JNIEXPORT jint JNICALL Java_com_microtechmd_blecomm_controller_AidexXController_getDefaultParamData
        (JNIEnv *, jobject);

JNIEXPORT jint JNICALL Java_com_microtechmd_blecomm_controller_AidexXController_setDefaultParamData
        (JNIEnv *, jobject, jfloatArray);

JNIEXPORT jint JNICALL Java_com_microtechmd_blecomm_controller_AidexXController_reset
        (JNIEnv *, jobject);

JNIEXPORT jint JNICALL Java_com_microtechmd_blecomm_controller_AidexXController_shelfMode
        (JNIEnv *, jobject);

JNIEXPORT jint JNICALL Java_com_microtechmd_blecomm_controller_AidexXController_deleteBond
        (JNIEnv *, jobject);

JNIEXPORT jint JNICALL Java_com_microtechmd_blecomm_controller_AidexXController_clearStorage
        (JNIEnv *, jobject);

JNIEXPORT jint JNICALL Java_com_microtechmd_blecomm_controller_AidexXController_setGcBiasTrimming
        (JNIEnv *, jobject, jint);

JNIEXPORT jint JNICALL Java_com_microtechmd_blecomm_controller_AidexXController_setGcImeasTrimming
        (JNIEnv *, jobject, jint);


#ifdef __cplusplus
}
#endif
#endif
