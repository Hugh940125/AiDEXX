/*
 * Academic License - for use in teaching, academic research, and meeting
 * course requirements at degree granting institutions only.  Not for
 * government, commercial, or other organizational use.
 * File: power.c
 *
 * MATLAB Coder version            : 3.4
 * C/C++ source code generated on  : 30-Jun-2022 13:42:41
 */

/* Include Files */
#include "rt_nonfinite.h"
#include "AAC.h"
#include "ADRR.h"
#include "AUC.h"
#include "CONGA.h"
#include "CV.h"
#include "DailyTrendMean.h"
#include "DailyTrendPrctile.h"
#include "GRADE.h"
#include "HBGD.h"
#include "HBGI.h"
#include "HbA1c.h"
#include "IQR.h"
#include "JINDEX.h"
#include "LAGE.h"
#include "LBGD.h"
#include "LBGI.h"
#include "MAG.h"
#include "MAGE.h"
#include "MAXBG.h"
#include "MBG.h"
#include "MINBG.h"
#include "MODD.h"
#include "MValue.h"
#include "NUM.h"
#include "PT.h"
#include "Pentagon.h"
#include "SAFilter.h"
#include "SDBG.h"
#include "SelectByHour.h"
#include "neg2nan.h"
#include "power.h"
#include "datools_emxutil.h"
#include "datools_rtwutil.h"

/* Function Definitions */

/*
 * Arguments    : const emxArray_real_T *a
 *                emxArray_real_T *y
 * Return Type  : void
 */
void b_power(const emxArray_real_T *a, emxArray_real_T *y)
{
  unsigned int a_idx_0;
  unsigned int b_a_idx_0;
  int k;
  a_idx_0 = (unsigned int)a->size[0];
  b_a_idx_0 = (unsigned int)a->size[0];
  k = y->size[0];
  y->size[0] = (int)b_a_idx_0;
  emxEnsureCapacity_real_T1(y, k);
  for (k = 0; k + 1 <= (int)a_idx_0; k++) {
    y->data[k] = a->data[k] * a->data[k];
  }
}

/*
 * Arguments    : const emxArray_real_T *a
 *                emxArray_real_T *y
 * Return Type  : void
 */
void c_power(const emxArray_real_T *a, emxArray_real_T *y)
{
  unsigned int a_idx_0;
  unsigned int b_a_idx_0;
  int k;
  a_idx_0 = (unsigned int)a->size[0];
  b_a_idx_0 = (unsigned int)a->size[0];
  k = y->size[0];
  y->size[0] = (int)b_a_idx_0;
  emxEnsureCapacity_real_T1(y, k);
  for (k = 0; k + 1 <= (int)a_idx_0; k++) {
    y->data[k] = rt_powd_snf(a->data[k], 1.026);
  }
}

/*
 * Arguments    : const emxArray_real_T *a
 *                emxArray_real_T *y
 * Return Type  : void
 */
void d_power(const emxArray_real_T *a, emxArray_real_T *y)
{
  int unnamed_idx_1;
  int k;
  unnamed_idx_1 = a->size[1];
  k = y->size[0] * y->size[1];
  y->size[0] = 1;
  y->size[1] = a->size[1];
  emxEnsureCapacity_real_T(y, k);
  for (k = 0; k + 1 <= unnamed_idx_1; k++) {
    y->data[k] = rt_powd_snf(a->data[k], 1.581);
  }
}

/*
 * Arguments    : const emxArray_real_T *a
 *                emxArray_real_T *y
 * Return Type  : void
 */
void e_power(const emxArray_real_T *a, emxArray_real_T *y)
{
  int unnamed_idx_1;
  int k;
  unnamed_idx_1 = a->size[1];
  k = y->size[0] * y->size[1];
  y->size[0] = 1;
  y->size[1] = a->size[1];
  emxEnsureCapacity_real_T(y, k);
  for (k = 0; k + 1 <= unnamed_idx_1; k++) {
    y->data[k] = rt_powd_snf(a->data[k], 1.51);
  }
}

/*
 * Arguments    : const emxArray_real_T *a
 *                emxArray_real_T *y
 * Return Type  : void
 */
void f_power(const emxArray_real_T *a, emxArray_real_T *y)
{
  int unnamed_idx_1;
  int k;
  unnamed_idx_1 = a->size[1];
  k = y->size[0] * y->size[1];
  y->size[0] = 1;
  y->size[1] = a->size[1];
  emxEnsureCapacity_real_T(y, k);
  for (k = 0; k + 1 <= unnamed_idx_1; k++) {
    y->data[k] = rt_powd_snf(a->data[k], 2.63);
  }
}

/*
 * Arguments    : const emxArray_real_T *a
 *                emxArray_real_T *y
 * Return Type  : void
 */
void power(const emxArray_real_T *a, emxArray_real_T *y)
{
  int unnamed_idx_1;
  int k;
  unnamed_idx_1 = a->size[1];
  k = y->size[0] * y->size[1];
  y->size[0] = 1;
  y->size[1] = a->size[1];
  emxEnsureCapacity_real_T(y, k);
  for (k = 0; k + 1 <= unnamed_idx_1; k++) {
    y->data[k] = a->data[k] * a->data[k];
  }
}

/*
 * File trailer for power.c
 *
 * [EOF]
 */
