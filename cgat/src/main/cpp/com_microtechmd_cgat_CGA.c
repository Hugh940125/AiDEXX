#include <jni.h>
#include "com_microtechmd_cgat_CGA.h"

#include "rt_nonfinite.h"
#include "datools_terminate.h"
#include "datools_emxAPI.h"
#include "datools_initialize.h"
#include "neg2nan.h"
#include "SAFilter.h"
#include "SelectByHour.h"
#include "DailyTrendMean.h"
#include "DailyTrendPrctile.h"
#include "NUM.h"
#include "MAXBG.h"
#include "MINBG.h"
#include "LBGD.h"
#include "HBGD.h"
#include "MBG.h"
#include "SDBG.h"
#include "CV.h"
#include "JINDEX.h"
#include "MValue.h"
#include "IQR.h"
#include "PT.h"
#include "AAC.h"
#include "AUC.h"
#include "HbA1c.h"
#include "LBGI.h"
#include "HBGI.h"
#include "ADRR.h"
#include "GRADE.h"
#include "LAGE.h"
#include "MAGE.h"
#include "MAG.h"
#include "MODD.h"
#include "CONGA.h"
#include "Pentagon.h"

static jclass cgaCls;
static jfieldID fieldColSize;
static jfieldID fieldRowSize;
static jfieldID fieldDataPtr;
static jfieldID fieldSelectedDataPtr;
static jfieldID fieldMbgPtr;
static jfieldID fieldSdbgPtr;
static jfieldID fieldCvPtr;


static jdoubleArray outputArray(JNIEnv *env, emxArray_real_T *array)
{
  if (array == NULL)
    return NULL;
  int size = array->size[0];
  int i;
  for (i = 1; i < array->numDimensions; i++)
  {
    size *= array->size[i];
  }
  jdoubleArray result = (*env)->NewDoubleArray(env, size);
  (*env)->SetDoubleArrayRegion(env, result, 0, size, array->data);
  return result;
}

static jobjectArray outputMatrix(JNIEnv *env, emxArray_real_T *array)
{
  if (array == NULL || array->numDimensions != 2)
    return NULL;

  int colSize = array->size[0];
  int rowSize = array->size[1];

  jclass doubleArrCls = (*env)->FindClass(env, "[D");
  jdoubleArray result = (*env)->NewObjectArray(env, rowSize, doubleArrCls, NULL);
  int i;
  for (i = 0; i < rowSize; i++)
  {
    jdoubleArray colResult = (*env)->NewDoubleArray(env, colSize);
    (*env)->SetDoubleArrayRegion(env, colResult, 0, colSize, &array->data[i * colSize]);
    (*env)->SetObjectArrayElement(env, result, i, colResult);
  }
  return result;
}

JNIEXPORT jint JNI_OnLoad(JavaVM *vm, void *reserved) {
  JNIEnv *env = NULL;
  (*vm)->GetEnv(vm, (void **)&env, JNI_VERSION_1_4);

  cgaCls = (*env)->FindClass(env, "com/microtechmd/cgat/CGA");
  fieldColSize = (*env)->GetFieldID(env, cgaCls, "colSize", "I");
  fieldRowSize = (*env)->GetFieldID(env, cgaCls, "rowSize", "I");
  fieldDataPtr = (*env)->GetFieldID(env, cgaCls, "dataPtr", "J");
  fieldSelectedDataPtr = (*env)->GetFieldID(env, cgaCls, "selectedDataPtr", "J");
  fieldMbgPtr = (*env)->GetFieldID(env, cgaCls, "mbgPtr", "J");
  fieldSdbgPtr = (*env)->GetFieldID(env, cgaCls, "sdbgPtr", "J");
  fieldCvPtr = (*env)->GetFieldID(env, cgaCls, "cvPtr", "J");

  datools_initialize();

  return JNI_VERSION_1_4;
}

JNIEXPORT void JNI_OnUnload(JavaVM *vm, void *reserved) {
  datools_terminate();
}

JNIEXPORT void JNICALL Java_com_microtechmd_cgat_CGA_constructor
  (JNIEnv *env, jobject this, jobjectArray sg)
{
  jsize length = (*env)->GetArrayLength(env, sg);
  if (length == 0)
    return;

  jdoubleArray firstCol = (*env)->GetObjectArrayElement(env, sg, 0);
  int rowSize = (*env)->GetArrayLength(env, sg);
  int colSize = (*env)->GetArrayLength(env, firstCol);

  int iv0[2] = {colSize, rowSize};
  emxArray_real_T *m_sg0;
  m_sg0 = emxCreateND_real_T(2, iv0);

  int i, j;
  double value;
  for (i = 0; i < rowSize; i++)
  {
    jdoubleArray rowI = (*env)->GetObjectArrayElement(env, sg, i);
    jdouble *value = (*env)->GetDoubleArrayElements(env, rowI, 0);
    for (j = 0; j < colSize; j++)
        m_sg0->data[colSize * i + j] = value[j];
    (*env)->ReleaseDoubleArrayElements(env, rowI, value, JNI_ABORT);
  }
  neg2nan(m_sg0);

  emxArray_real_T *m_sg = emxCreateND_real_T(2, iv0);
  SAFilter(m_sg0, 1, 24, m_sg);
  emxDestroyArray_real_T(m_sg0);
  (*env)->SetLongField(env, this, fieldDataPtr, (jlong)m_sg);

  Java_com_microtechmd_cgat_CGA_selectPeriod(env, this, 0, 24);
}

JNIEXPORT void JNICALL Java_com_microtechmd_cgat_CGA_destructor
  (JNIEnv *env, jobject this)
{
  emxArray_real_T *m_sg = (emxArray_real_T *)((*env)->GetLongField(env, this, fieldDataPtr));
  emxArray_real_T *m_ssg = (emxArray_real_T *)((*env)->GetLongField(env, this, fieldSelectedDataPtr));
  emxArray_real_T *m_mbg = (emxArray_real_T *)((*env)->GetLongField(env, this, fieldMbgPtr));
  emxArray_real_T *m_sdbg = (emxArray_real_T *)((*env)->GetLongField(env, this, fieldSdbgPtr));
  emxArray_real_T *m_cv = (emxArray_real_T *)((*env)->GetLongField(env, this, fieldCvPtr));

  emxDestroyArray_real_T(m_mbg);
  emxDestroyArray_real_T(m_sdbg);
  emxDestroyArray_real_T(m_cv);
  emxDestroyArray_real_T(m_ssg);
  emxDestroyArray_real_T(m_sg);
  (*env)->SetLongField(env, this, fieldMbgPtr, (jlong)NULL);
  (*env)->SetLongField(env, this, fieldSdbgPtr, (jlong)NULL);
  (*env)->SetLongField(env, this, fieldCvPtr, (jlong)NULL);
  (*env)->SetLongField(env, this, fieldSelectedDataPtr, (jlong)NULL);
  (*env)->SetLongField(env, this, fieldDataPtr, (jlong)NULL);
}


JNIEXPORT jdoubleArray JNICALL Java_com_microtechmd_cgat_CGA_selectPeriod
  (JNIEnv *env, jobject this, jdouble startHour, jdouble endHour)
{
  emxArray_real_T *m_sg = (emxArray_real_T *)((*env)->GetLongField(env, this, fieldDataPtr));
  if (m_sg == NULL)
    return NULL;
  emxArray_real_T *m_ssg = (emxArray_real_T *)((*env)->GetLongField(env, this, fieldSelectedDataPtr));
  emxArray_real_T *m_mbg = (emxArray_real_T *)((*env)->GetLongField(env, this, fieldMbgPtr));
  emxArray_real_T *m_sdbg = (emxArray_real_T *)((*env)->GetLongField(env, this, fieldSdbgPtr));
  emxArray_real_T *m_cv = (emxArray_real_T *)((*env)->GetLongField(env, this, fieldCvPtr));

  emxDestroyArray_real_T(m_ssg);
  emxArray_real_T *ssg;
  emxInitArray_real_T(&ssg, 2);
  SelectByHour(m_sg, startHour, endHour, ssg);
  (*env)->SetLongField(env, this, fieldSelectedDataPtr, (jlong)ssg);

  emxDestroyArray_real_T(m_mbg);
  emxArray_real_T *mbg;
  emxInitArray_real_T(&mbg, 2);
  MBG(ssg, mbg);
  (*env)->SetLongField(env, this, fieldMbgPtr, (jlong)mbg);

  emxDestroyArray_real_T(m_sdbg);
  emxArray_real_T *sdbg;
  emxInitArray_real_T(&sdbg, 2);
  SDBG(ssg, sdbg);
  (*env)->SetLongField(env, this, fieldSdbgPtr, (jlong)sdbg);

  emxDestroyArray_real_T(m_cv);
  emxArray_real_T *cv;
  emxInitArray_real_T(&cv, 2);
  CV(mbg, sdbg, cv);
  (*env)->SetLongField(env, this, fieldCvPtr, (jlong)cv);
}


JNIEXPORT jdoubleArray JNICALL Java_com_microtechmd_cgat_CGA_getDailyTrendMean
  (JNIEnv *env, jobject this)
{
  emxArray_real_T *m_sg = (emxArray_real_T *)((*env)->GetLongField(env, this, fieldDataPtr));
  if (m_sg == NULL)
    return NULL;
  emxArray_real_T *m_sgm;
  emxInitArray_real_T(&m_sgm, 1);
  DailyTrendMean(m_sg, m_sgm);
  jdoubleArray sgm = outputArray(env, m_sgm);
  emxDestroyArray_real_T(m_sgm);
  return sgm;
}


JNIEXPORT jdoubleArray JNICALL Java_com_microtechmd_cgat_CGA_getDailyTrendPrctile
  (JNIEnv *env, jobject this, jdouble pct)
{
  emxArray_real_T *m_sg = (emxArray_real_T *)((*env)->GetLongField(env, this, fieldDataPtr));
  if (m_sg == NULL)
    return NULL;
  emxArray_real_T *m_sgp;
  emxInitArray_real_T(&m_sgp, 1);
  DailyTrendPrctile(m_sg, pct, m_sgp);
  jdoubleArray sgp = outputArray(env, m_sgp);
  emxDestroyArray_real_T(m_sgp);
  return sgp;
}


JNIEXPORT jobjectArray JNICALL Java_com_microtechmd_cgat_CGA_getPeriodLBGD
  (JNIEnv *env, jobject this, jdouble hypo)
{
  emxArray_real_T *m_ssg = (emxArray_real_T *)((*env)->GetLongField(env, this, fieldSelectedDataPtr));
  if (m_ssg == NULL)
    return NULL;
  emxArray_real_T *m_lbgd;
  emxInitArray_real_T(&m_lbgd, 2);
  LBGD(m_ssg, 1.0 / 12, hypo, m_lbgd);
  jobjectArray lbgd = outputMatrix(env, m_lbgd);
  emxDestroyArray_real_T(m_lbgd);
  return lbgd;
}


JNIEXPORT jobjectArray JNICALL Java_com_microtechmd_cgat_CGA_getPeriodHBGD
  (JNIEnv *env, jobject this, jdouble hyper)
{
  emxArray_real_T *m_ssg = (emxArray_real_T *)((*env)->GetLongField(env, this, fieldSelectedDataPtr));
  if (m_ssg == NULL)
    return NULL;
  emxArray_real_T *m_hbgd;
  emxInitArray_real_T(&m_hbgd, 2);
  HBGD(m_ssg, 1.0 / 12, hyper, m_hbgd);
  jobjectArray hbgd = outputMatrix(env, m_hbgd);
  emxDestroyArray_real_T(m_hbgd);
  return hbgd;
}


JNIEXPORT jdoubleArray JNICALL Java_com_microtechmd_cgat_CGA_getPeriodNUM
  (JNIEnv *env, jobject this)
{
  emxArray_real_T *m_ssg = (emxArray_real_T *)((*env)->GetLongField(env, this, fieldSelectedDataPtr));
  if (m_ssg == NULL)
    return NULL;
  emxArray_real_T *m_num;
  emxInitArray_real_T(&m_num, 2);
  NUM(m_ssg, m_num);
  jdoubleArray num = outputArray(env, m_num);
  emxDestroyArray_real_T(m_num);
  return num;
}


JNIEXPORT jdoubleArray JNICALL Java_com_microtechmd_cgat_CGA_getPeriodMAXBG
  (JNIEnv *env, jobject this)
{
  emxArray_real_T *m_ssg = (emxArray_real_T *)((*env)->GetLongField(env, this, fieldSelectedDataPtr));
  if (m_ssg == NULL)
    return NULL;
  emxArray_real_T *m_maxbg;
  emxInitArray_real_T(&m_maxbg, 2);
  MAXBG(m_ssg, m_maxbg);
  jdoubleArray maxbg = outputArray(env, m_maxbg);
  emxDestroyArray_real_T(m_maxbg);
  return maxbg;
}


JNIEXPORT jdoubleArray JNICALL Java_com_microtechmd_cgat_CGA_getPeriodMINBG
  (JNIEnv *env, jobject this)
{
  emxArray_real_T *m_ssg = (emxArray_real_T *)((*env)->GetLongField(env, this, fieldSelectedDataPtr));
  if (m_ssg == NULL)
    return NULL;
  emxArray_real_T *m_minbg;
  emxInitArray_real_T(&m_minbg, 2);
  MINBG(m_ssg, m_minbg);
  jdoubleArray minbg = outputArray(env, m_minbg);
  emxDestroyArray_real_T(m_minbg);
  return minbg;
}


JNIEXPORT jdoubleArray JNICALL Java_com_microtechmd_cgat_CGA_getPeriodMBG
  (JNIEnv *env, jobject this)
{
  emxArray_real_T *m_mbg = (emxArray_real_T *)((*env)->GetLongField(env, this, fieldMbgPtr));
  return outputArray(env, m_mbg);
}


JNIEXPORT jdoubleArray JNICALL Java_com_microtechmd_cgat_CGA_getPeriodMValue
  (JNIEnv *env, jobject this, jdouble target)
{
  emxArray_real_T *m_ssg = (emxArray_real_T *)((*env)->GetLongField(env, this, fieldSelectedDataPtr));
  if (m_ssg == NULL)
    return NULL;
  emxArray_real_T *m_mvalue;
  emxInitArray_real_T(&m_mvalue, 2);
  MValue(m_ssg, target, m_mvalue);
  jdoubleArray mvalue = outputArray(env, m_mvalue);
  emxDestroyArray_real_T(m_mvalue);
  return mvalue;
}


JNIEXPORT jdoubleArray JNICALL Java_com_microtechmd_cgat_CGA_getPeriodSDBG
  (JNIEnv *env, jobject this)
{
  emxArray_real_T *m_sdbg = (emxArray_real_T *)((*env)->GetLongField(env, this, fieldSdbgPtr));
  return outputArray(env, m_sdbg);
}


JNIEXPORT jdoubleArray JNICALL Java_com_microtechmd_cgat_CGA_getPeriodCV
  (JNIEnv *env, jobject this)
{
  emxArray_real_T *m_cv = (emxArray_real_T *)((*env)->GetLongField(env, this, fieldCvPtr));
  return outputArray(env, m_cv);
}


JNIEXPORT jdoubleArray JNICALL Java_com_microtechmd_cgat_CGA_getPeriodJIndex
  (JNIEnv *env, jobject this)
{
  emxArray_real_T *m_mbg = (emxArray_real_T *)((*env)->GetLongField(env, this, fieldMbgPtr));
  if (m_mbg == NULL)
    return NULL;
  emxArray_real_T *m_sdbg = (emxArray_real_T *)((*env)->GetLongField(env, this, fieldSdbgPtr));
  if (m_sdbg == NULL)
    return NULL;
  emxArray_real_T *m_jindex;
  emxInitArray_real_T(&m_jindex, 2);
  JINDEX(m_mbg, m_sdbg, m_jindex);
  jdoubleArray jindex = outputArray(env, m_jindex);
  emxDestroyArray_real_T(m_jindex);
  return jindex;
}


JNIEXPORT jdoubleArray JNICALL Java_com_microtechmd_cgat_CGA_getPeriodIQR
  (JNIEnv *env, jobject this)
{
  emxArray_real_T *m_ssg = (emxArray_real_T *)((*env)->GetLongField(env, this, fieldSelectedDataPtr));
  if (m_ssg == NULL)
    return NULL;
  emxArray_real_T *m_iqr;
  emxInitArray_real_T(&m_iqr, 2);
  IQR(m_ssg, m_iqr);
  jdoubleArray iqr = outputArray(env, m_iqr);
  emxDestroyArray_real_T(m_iqr);
  return iqr;
}


JNIEXPORT jobjectArray JNICALL Java_com_microtechmd_cgat_CGA_getPeriodPT
  (JNIEnv *env, jobject this, jdoubleArray v)
{
  emxArray_real_T *m_ssg = (emxArray_real_T *)((*env)->GetLongField(env, this, fieldSelectedDataPtr));
  if (m_ssg == NULL)
    return NULL;
  int n = (*env)->GetArrayLength(env, v);
  jdouble *value = (*env)->GetDoubleArrayElements(env, v, 0);
  int size[2] = {n, 1};
  emxArray_real_T *m_v = emxCreateWrapperND_real_T(value, 2, size);
  emxArray_real_T *m_pt;
  emxInitArray_real_T(&m_pt, 2);
  PT(m_ssg, m_v, m_pt);
  jobjectArray pt = outputMatrix(env, m_pt);
  emxDestroyArray_real_T(m_pt);
  emxDestroyArray_real_T(m_v);
  return pt;
}


JNIEXPORT jdoubleArray JNICALL Java_com_microtechmd_cgat_CGA_getPeriodAAC
  (JNIEnv *env, jobject this, jdouble target)
{
  emxArray_real_T *m_ssg = (emxArray_real_T *)((*env)->GetLongField(env, this, fieldSelectedDataPtr));
  if (m_ssg == NULL)
    return NULL;
  emxArray_real_T *m_aac;
  emxInitArray_real_T(&m_aac, 2);
  AAC(m_ssg, target, m_aac);
  jdoubleArray aac = outputArray(env, m_aac);
  emxDestroyArray_real_T(m_aac);
  return aac;
}


JNIEXPORT jdoubleArray JNICALL Java_com_microtechmd_cgat_CGA_getPeriodAUC
  (JNIEnv *env, jobject this, jdouble target)
{
  emxArray_real_T *m_ssg = (emxArray_real_T *)((*env)->GetLongField(env, this, fieldSelectedDataPtr));
  if (m_ssg == NULL)
    return NULL;
  emxArray_real_T *m_auc;
  emxInitArray_real_T(&m_auc, 2);
  AUC(m_ssg, target, m_auc);
  jdoubleArray auc = outputArray(env, m_auc);
  emxDestroyArray_real_T(m_auc);
  return auc;
}


JNIEXPORT jdouble JNICALL Java_com_microtechmd_cgat_CGA_getHBA1C
  (JNIEnv *env, jobject this)
{
  emxArray_real_T *m_sg = (emxArray_real_T *)((*env)->GetLongField(env, this, fieldDataPtr));
  if (m_sg == NULL)
    return rtNaN;
  return HbA1c(m_sg);
}


JNIEXPORT jdouble JNICALL Java_com_microtechmd_cgat_CGA_getLBGI
  (JNIEnv *env, jobject this)
{
  emxArray_real_T *m_sg = (emxArray_real_T *)((*env)->GetLongField(env, this, fieldDataPtr));
  if (m_sg == NULL)
    return rtNaN;
  return LBGI(m_sg);
}


JNIEXPORT jdouble JNICALL Java_com_microtechmd_cgat_CGA_getHBGI
  (JNIEnv *env, jobject this)
{
  emxArray_real_T *m_sg = (emxArray_real_T *)((*env)->GetLongField(env, this, fieldDataPtr));
  if (m_sg == NULL)
    return rtNaN;
  return HBGI(m_sg);
}


JNIEXPORT jdouble JNICALL Java_com_microtechmd_cgat_CGA_getADRR
  (JNIEnv *env, jobject this)
{
  emxArray_real_T *m_sg = (emxArray_real_T *)((*env)->GetLongField(env, this, fieldDataPtr));
  if (m_sg == NULL)
    return rtNaN;
  return ADRR(m_sg);
}


JNIEXPORT jdoubleArray JNICALL Java_com_microtechmd_cgat_CGA_getGRADE
  (JNIEnv *env, jobject this, jdouble hypo, jdouble hyper)
{
  emxArray_real_T *m_sg = (emxArray_real_T *)((*env)->GetLongField(env, this, fieldDataPtr));
  if (m_sg == NULL)
    return NULL;
  double m_grade[4] = {0};
  GRADE(m_sg, hypo, hyper, m_grade);
  jdoubleArray grade = (*env)->NewDoubleArray(env, 4);
  (*env)->SetDoubleArrayRegion(env, grade, 0, 4, m_grade);
  return grade;
}


JNIEXPORT jdoubleArray JNICALL Java_com_microtechmd_cgat_CGA_getLAGE
  (JNIEnv *env, jobject this)
{
  emxArray_real_T *m_sg = (emxArray_real_T *)((*env)->GetLongField(env, this, fieldDataPtr));
  if (m_sg == NULL)
    return NULL;
  emxArray_real_T *m_lage;
  emxInitArray_real_T(&m_lage, 2);
  LAGE(m_sg, m_lage);
  jdoubleArray lage = outputArray(env, m_lage);
  emxDestroyArray_real_T(m_lage);
  return lage;
}


JNIEXPORT jobjectArray JNICALL Java_com_microtechmd_cgat_CGA_getMAGE
  (JNIEnv *env, jobject this, jdouble nv)
{
  emxArray_real_T *m_sg = (emxArray_real_T *)((*env)->GetLongField(env, this, fieldDataPtr));
  if (m_sg == NULL)
    return NULL;
  emxArray_real_T *m_mage;
  emxInitArray_real_T(&m_mage, 2);
  MAGE(m_sg, nv, m_mage);
  jobjectArray mage = outputMatrix(env, m_mage);
  emxDestroyArray_real_T(m_mage);
  return mage;
}


JNIEXPORT jdoubleArray JNICALL Java_com_microtechmd_cgat_CGA_getMAG
  (JNIEnv *env, jobject this, jdouble hour)
{
  emxArray_real_T *m_sg = (emxArray_real_T *)((*env)->GetLongField(env, this, fieldDataPtr));
  if (m_sg == NULL)
    return NULL;
  emxArray_real_T *m_mag;
  emxInitArray_real_T(&m_mag, 2);
  MAG(m_sg, hour, m_mag);
  jdoubleArray mag = outputArray(env, m_mag);
  emxDestroyArray_real_T(m_mag);
  return mag;
}


JNIEXPORT jdoubleArray JNICALL Java_com_microtechmd_cgat_CGA_getMODD
  (JNIEnv *env, jobject this)
{
  emxArray_real_T *m_sg = (emxArray_real_T *)((*env)->GetLongField(env, this, fieldDataPtr));
  if (m_sg == NULL)
    return NULL;
  emxArray_real_T *m_modd;
  emxInitArray_real_T(&m_modd, 2);
  MODD(m_sg, m_modd);
  jdoubleArray modd = outputArray(env, m_modd);
  emxDestroyArray_real_T(m_modd);
  return modd;
}


JNIEXPORT jdoubleArray JNICALL Java_com_microtechmd_cgat_CGA_getCONGA
  (JNIEnv *env, jobject this, jdouble hour)
{
  emxArray_real_T *m_sg = (emxArray_real_T *)((*env)->GetLongField(env, this, fieldDataPtr));
  if (m_sg == NULL)
    return NULL;
  emxArray_real_T *m_conga;
  emxInitArray_real_T(&m_conga, 2);
  CONGA(m_sg, hour, m_conga);
  jdoubleArray conga = outputArray(env, m_conga);
  emxDestroyArray_real_T(m_conga);
  return conga;
}


JNIEXPORT jobjectArray JNICALL Java_com_microtechmd_cgat_CGA_getPentagon
  (JNIEnv *env, jobject this)
{
  emxArray_real_T *m_sg = (emxArray_real_T *)((*env)->GetLongField(env, this, fieldDataPtr));
  if (m_sg == NULL)
    return NULL;
  emxArray_real_T *m_mbg = (emxArray_real_T *)((*env)->GetLongField(env, this, fieldMbgPtr));
  if (m_mbg == NULL)
    return NULL;
  emxArray_real_T *m_cv = (emxArray_real_T *)((*env)->GetLongField(env, this, fieldCvPtr));
  if (m_cv == NULL)
    return NULL;
  emxArray_real_T *m_pentagon;
  emxInitArray_real_T(&m_pentagon, 2);
  Pentagon(m_sg, m_mbg, m_cv, m_pentagon);
  jobjectArray pentagon = outputMatrix(env, m_pentagon);
  emxDestroyArray_real_T(m_pentagon);
  return pentagon;
}
