/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class com_microtechmd_blecomm_parser_CgmParser */

#ifndef _Included_com_microtechmd_blecomm_parser_CgmParser
#define _Included_com_microtechmd_blecomm_parser_CgmParser
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     com_microtechmd_blecomm_parser_CgmParser
 * Method:    setBroadcastClass
 * Signature: (Ljava/lang/Class;)V
 */
JNIEXPORT void JNICALL Java_com_microtechmd_blecomm_parser_CgmParser_setBroadcastClass
        (JNIEnv *, jclass, jclass);

JNIEXPORT void JNICALL Java_com_microtechmd_blecomm_parser_CgmParser_setDeviceInfoClass
        (JNIEnv *, jclass, jclass);

JNIEXPORT void JNICALL Java_com_microtechmd_blecomm_parser_CgmParser_setDeviceConfigClass
        (JNIEnv *, jclass, jclass);


/*
 * Class:     com_microtechmd_blecomm_parser_CgmParser
 * Method:    setHistoryClass
 * Signature: (Ljava/lang/Class;)V
 */
JNIEXPORT void JNICALL Java_com_microtechmd_blecomm_parser_CgmParser_setHistoryClass
        (JNIEnv *, jclass, jclass);

/*
 * Class:     com_microtechmd_blecomm_parser_CgmParser
 * Method:    getBroadcast
 * Signature: ([B)Lcom/microtechmd/blecomm/parser/CgmBroadcastEntity;
 */
JNIEXPORT jobject JNICALL Java_com_microtechmd_blecomm_parser_CgmParser_getBroadcast
        (JNIEnv *, jclass, jbyteArray);

JNIEXPORT jobject JNICALL Java_com_microtechmd_blecomm_parser_CgmParser_getDeviceInfo
        (JNIEnv *, jclass, jbyteArray);

JNIEXPORT jobject JNICALL Java_com_microtechmd_blecomm_parser_CgmParser_getDeviceConfig
        (JNIEnv *, jclass, jbyteArray);


/*
 * Class:     com_microtechmd_blecomm_parser_CgmParser
 * Method:    getHistory
 * Signature: ([B)Lcom/microtechmd/blecomm/parser/CgmHistoryEntity;
 */
JNIEXPORT jobject JNICALL Java_com_microtechmd_blecomm_parser_CgmParser_getHistory
        (JNIEnv *, jclass, jbyteArray);

/*
 * Class:     com_microtechmd_blecomm_parser_CgmParser
 * Method:    getHistories
 * Signature: ([B)Ljava/util/List;
 */
JNIEXPORT jobject JNICALL Java_com_microtechmd_blecomm_parser_CgmParser_getHistories
        (JNIEnv *, jclass, jbyteArray);

/*
 * Class:     com_microtechmd_blecomm_parser_CgmParser
 * Method:    getFullHistories
 * Signature: ([B)Ljava/util/List;
 */
JNIEXPORT jobject JNICALL Java_com_microtechmd_blecomm_parser_CgmParser_getFullHistories
        (JNIEnv *, jclass, jbyteArray);

#ifdef __cplusplus
}
#endif
#endif