/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class com_microtechmd_cgat_CGA */

#ifndef _Included_com_microtechmd_cgat_CGA
#define _Included_com_microtechmd_cgat_CGA
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     com_microtechmd_cgat_CGA
 * Method:    constructor
 * Signature: ([[D)V
 */
JNIEXPORT void JNICALL Java_com_microtechmd_cgat_CGA_constructor
  (JNIEnv *, jobject, jobjectArray);

/*
 * Class:     com_microtechmd_cgat_CGA
 * Method:    destructor
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_microtechmd_cgat_CGA_destructor
  (JNIEnv *, jobject);

/*
 * Class:     com_microtechmd_cgat_CGA
 * Method:    selectPeriod
 * Signature: (DD)[D
 */
JNIEXPORT jdoubleArray JNICALL Java_com_microtechmd_cgat_CGA_selectPeriod
  (JNIEnv *, jobject, jdouble, jdouble);

/*
 * Class:     com_microtechmd_cgat_CGA
 * Method:    getDailyTrendMean
 * Signature: ()[D
 */
JNIEXPORT jdoubleArray JNICALL Java_com_microtechmd_cgat_CGA_getDailyTrendMean
  (JNIEnv *, jobject);

/*
 * Class:     com_microtechmd_cgat_CGA
 * Method:    getDailyTrendPrctile
 * Signature: (D)[D
 */
JNIEXPORT jdoubleArray JNICALL Java_com_microtechmd_cgat_CGA_getDailyTrendPrctile
  (JNIEnv *, jobject, jdouble);

/*
 * Class:     com_microtechmd_cgat_CGA
 * Method:    getPeriodLBGD
 * Signature: (D)[[D
 */
JNIEXPORT jobjectArray JNICALL Java_com_microtechmd_cgat_CGA_getPeriodLBGD
  (JNIEnv *, jobject, jdouble);

/*
 * Class:     com_microtechmd_cgat_CGA
 * Method:    getPeriodHBGD
 * Signature: (D)[[D
 */
JNIEXPORT jobjectArray JNICALL Java_com_microtechmd_cgat_CGA_getPeriodHBGD
  (JNIEnv *, jobject, jdouble);

/*
 * Class:     com_microtechmd_cgat_CGA
 * Method:    getPeriodNUM
 * Signature: ()[D
 */
JNIEXPORT jdoubleArray JNICALL Java_com_microtechmd_cgat_CGA_getPeriodNUM
  (JNIEnv *, jobject);

/*
 * Class:     com_microtechmd_cgat_CGA
 * Method:    getPeriodMAXBG
 * Signature: ()[D
 */
JNIEXPORT jdoubleArray JNICALL Java_com_microtechmd_cgat_CGA_getPeriodMAXBG
  (JNIEnv *, jobject);

/*
 * Class:     com_microtechmd_cgat_CGA
 * Method:    getPeriodMINBG
 * Signature: ()[D
 */
JNIEXPORT jdoubleArray JNICALL Java_com_microtechmd_cgat_CGA_getPeriodMINBG
  (JNIEnv *, jobject);

/*
 * Class:     com_microtechmd_cgat_CGA
 * Method:    getPeriodMBG
 * Signature: ()[D
 */
JNIEXPORT jdoubleArray JNICALL Java_com_microtechmd_cgat_CGA_getPeriodMBG
  (JNIEnv *, jobject);

/*
 * Class:     com_microtechmd_cgat_CGA
 * Method:    getPeriodMValue
 * Signature: (D)[D
 */
JNIEXPORT jdoubleArray JNICALL Java_com_microtechmd_cgat_CGA_getPeriodMValue
  (JNIEnv *, jobject, jdouble);

/*
 * Class:     com_microtechmd_cgat_CGA
 * Method:    getPeriodSDBG
 * Signature: ()[D
 */
JNIEXPORT jdoubleArray JNICALL Java_com_microtechmd_cgat_CGA_getPeriodSDBG
  (JNIEnv *, jobject);

/*
 * Class:     com_microtechmd_cgat_CGA
 * Method:    getPeriodCV
 * Signature: ()[D
 */
JNIEXPORT jdoubleArray JNICALL Java_com_microtechmd_cgat_CGA_getPeriodCV
  (JNIEnv *, jobject);

/*
 * Class:     com_microtechmd_cgat_CGA
 * Method:    getPeriodJIndex
 * Signature: ()[D
 */
JNIEXPORT jdoubleArray JNICALL Java_com_microtechmd_cgat_CGA_getPeriodJIndex
  (JNIEnv *, jobject);

/*
 * Class:     com_microtechmd_cgat_CGA
 * Method:    getPeriodIQR
 * Signature: ()[D
 */
JNIEXPORT jdoubleArray JNICALL Java_com_microtechmd_cgat_CGA_getPeriodIQR
  (JNIEnv *, jobject);

/*
 * Class:     com_microtechmd_cgat_CGA
 * Method:    getPeriodPT
 * Signature: ([D)[[D
 */
JNIEXPORT jobjectArray JNICALL Java_com_microtechmd_cgat_CGA_getPeriodPT
  (JNIEnv *, jobject, jdoubleArray);

/*
 * Class:     com_microtechmd_cgat_CGA
 * Method:    getPeriodAAC
 * Signature: (D)[D
 */
JNIEXPORT jdoubleArray JNICALL Java_com_microtechmd_cgat_CGA_getPeriodAAC
  (JNIEnv *, jobject, jdouble);

/*
 * Class:     com_microtechmd_cgat_CGA
 * Method:    getPeriodAUC
 * Signature: (D)[D
 */
JNIEXPORT jdoubleArray JNICALL Java_com_microtechmd_cgat_CGA_getPeriodAUC
  (JNIEnv *, jobject, jdouble);

/*
 * Class:     com_microtechmd_cgat_CGA
 * Method:    getHBA1C
 * Signature: ()D
 */
JNIEXPORT jdouble JNICALL Java_com_microtechmd_cgat_CGA_getHBA1C
  (JNIEnv *, jobject);

/*
 * Class:     com_microtechmd_cgat_CGA
 * Method:    getLBGI
 * Signature: ()D
 */
JNIEXPORT jdouble JNICALL Java_com_microtechmd_cgat_CGA_getLBGI
  (JNIEnv *, jobject);

/*
 * Class:     com_microtechmd_cgat_CGA
 * Method:    getHBGI
 * Signature: ()D
 */
JNIEXPORT jdouble JNICALL Java_com_microtechmd_cgat_CGA_getHBGI
  (JNIEnv *, jobject);

/*
 * Class:     com_microtechmd_cgat_CGA
 * Method:    getADRR
 * Signature: ()D
 */
JNIEXPORT jdouble JNICALL Java_com_microtechmd_cgat_CGA_getADRR
  (JNIEnv *, jobject);

/*
 * Class:     com_microtechmd_cgat_CGA
 * Method:    getGRADE
 * Signature: (DD)[D
 */
JNIEXPORT jdoubleArray JNICALL Java_com_microtechmd_cgat_CGA_getGRADE
  (JNIEnv *, jobject, jdouble, jdouble);

/*
 * Class:     com_microtechmd_cgat_CGA
 * Method:    getLAGE
 * Signature: ()[D
 */
JNIEXPORT jdoubleArray JNICALL Java_com_microtechmd_cgat_CGA_getLAGE
  (JNIEnv *, jobject);

/*
 * Class:     com_microtechmd_cgat_CGA
 * Method:    getMAGE
 * Signature: (D)[[D
 */
JNIEXPORT jobjectArray JNICALL Java_com_microtechmd_cgat_CGA_getMAGE
  (JNIEnv *, jobject, jdouble);

/*
 * Class:     com_microtechmd_cgat_CGA
 * Method:    getMAG
 * Signature: (D)[D
 */
JNIEXPORT jdoubleArray JNICALL Java_com_microtechmd_cgat_CGA_getMAG
  (JNIEnv *, jobject, jdouble);

/*
 * Class:     com_microtechmd_cgat_CGA
 * Method:    getMODD
 * Signature: ()[D
 */
JNIEXPORT jdoubleArray JNICALL Java_com_microtechmd_cgat_CGA_getMODD
  (JNIEnv *, jobject);

/*
 * Class:     com_microtechmd_cgat_CGA
 * Method:    getCONGA
 * Signature: (D)[D
 */
JNIEXPORT jdoubleArray JNICALL Java_com_microtechmd_cgat_CGA_getCONGA
  (JNIEnv *, jobject, jdouble);

/*
 * Class:     com_microtechmd_cgat_CGA
 * Method:    getPentagon
 * Signature: ()[[D
 */
JNIEXPORT jobjectArray JNICALL Java_com_microtechmd_cgat_CGA_getPentagon
  (JNIEnv *, jobject);

#ifdef __cplusplus
}
#endif
#endif