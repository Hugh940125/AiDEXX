/*
 * Academic License - for use in teaching, academic research, and meeting
 * course requirements at degree granting institutions only.  Not for
 * government, commercial, or other organizational use.
 * File: AAC.c
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
#include "sum.h"
#include "datools_emxutil.h"
#include "nansum.h"

/* Function Definitions */

/*
 * Arguments    : const emxArray_real_T *sg
 *                double target
 *                emxArray_real_T *aac
 * Return Type  : void
 */
void AAC(const emxArray_real_T *sg, double target, emxArray_real_T *aac)
{
  int i0;
  int loop_ub;
  int i;
  emxArray_real_T *sgi;
  emxArray_real_T *sgd;
  emxArray_boolean_T *r0;
  emxArray_boolean_T *r1;
  emxArray_real_T *b_sgd;
  double x;
  double y;
  i0 = aac->size[0] * aac->size[1];
  aac->size[0] = 1;
  aac->size[1] = sg->size[1] + 1;
  emxEnsureCapacity_real_T(aac, i0);
  loop_ub = sg->size[1] + 1;
  for (i0 = 0; i0 < loop_ub; i0++) {
    aac->data[i0] = rtNaN;
  }

  i = 0;
  emxInit_real_T(&sgi, 1);
  emxInit_real_T(&sgd, 1);
  emxInit_boolean_T(&r0, 1);
  emxInit_boolean_T(&r1, 1);
  emxInit_real_T(&b_sgd, 1);
  while (i <= sg->size[1]) {
    if (1.0 + (double)i > sg->size[1]) {
      i0 = sgi->size[0];
      sgi->size[0] = sg->size[0] * sg->size[1];
      emxEnsureCapacity_real_T1(sgi, i0);
      loop_ub = sg->size[0] * sg->size[1];
      for (i0 = 0; i0 < loop_ub; i0++) {
        sgi->data[i0] = sg->data[i0];
      }
    } else {
      loop_ub = sg->size[0];
      i0 = sgi->size[0];
      sgi->size[0] = loop_ub;
      emxEnsureCapacity_real_T1(sgi, i0);
      for (i0 = 0; i0 < loop_ub; i0++) {
        sgi->data[i0] = sg->data[i0 + sg->size[0] * i];
      }
    }

    i0 = sgd->size[0];
    sgd->size[0] = sgi->size[0];
    emxEnsureCapacity_real_T1(sgd, i0);
    loop_ub = sgi->size[0];
    for (i0 = 0; i0 < loop_ub; i0++) {
      sgd->data[i0] = target - sgi->data[i0];
    }

    i0 = b_sgd->size[0];
    b_sgd->size[0] = sgd->size[0];
    emxEnsureCapacity_real_T1(b_sgd, i0);
    loop_ub = sgd->size[0];
    for (i0 = 0; i0 < loop_ub; i0++) {
      b_sgd->data[i0] = sgd->data[i0] * (double)(sgd->data[i0] > 0.0);
    }

    x = nansum(b_sgd);
    i0 = r0->size[0];
    r0->size[0] = sgi->size[0];
    emxEnsureCapacity_boolean_T(r0, i0);
    loop_ub = sgi->size[0];
    for (i0 = 0; i0 < loop_ub; i0++) {
      r0->data[i0] = rtIsNaN(sgi->data[i0]);
    }

    i0 = r1->size[0];
    r1->size[0] = r0->size[0];
    emxEnsureCapacity_boolean_T(r1, i0);
    loop_ub = r0->size[0];
    for (i0 = 0; i0 < loop_ub; i0++) {
      r1->data[i0] = !r0->data[i0];
    }

    y = sum(r1);
    aac->data[i] = x / y;
    i++;
  }

  emxFree_real_T(&b_sgd);
  emxFree_boolean_T(&r1);
  emxFree_boolean_T(&r0);
  emxFree_real_T(&sgd);
  emxFree_real_T(&sgi);
}

/*
 * Arguments    : const emxArray_real_T *sg
 *                emxArray_real_T *aac
 * Return Type  : void
 */
void b_AAC(const emxArray_real_T *sg, emxArray_real_T *aac)
{
  int i17;
  int loop_ub;
  int i;
  emxArray_real_T *sgi;
  emxArray_real_T *sgd;
  emxArray_boolean_T *r34;
  emxArray_boolean_T *r35;
  emxArray_real_T *b_sgd;
  double x;
  double y;
  i17 = aac->size[0] * aac->size[1];
  aac->size[0] = 1;
  aac->size[1] = sg->size[1] + 1;
  emxEnsureCapacity_real_T(aac, i17);
  loop_ub = sg->size[1] + 1;
  for (i17 = 0; i17 < loop_ub; i17++) {
    aac->data[i17] = rtNaN;
  }

  i = 0;
  emxInit_real_T(&sgi, 1);
  emxInit_real_T(&sgd, 1);
  emxInit_boolean_T(&r34, 1);
  emxInit_boolean_T(&r35, 1);
  emxInit_real_T(&b_sgd, 1);
  while (i <= sg->size[1]) {
    if (1.0 + (double)i > sg->size[1]) {
      i17 = sgi->size[0];
      sgi->size[0] = sg->size[0] * sg->size[1];
      emxEnsureCapacity_real_T1(sgi, i17);
      loop_ub = sg->size[0] * sg->size[1];
      for (i17 = 0; i17 < loop_ub; i17++) {
        sgi->data[i17] = sg->data[i17];
      }
    } else {
      loop_ub = sg->size[0];
      i17 = sgi->size[0];
      sgi->size[0] = loop_ub;
      emxEnsureCapacity_real_T1(sgi, i17);
      for (i17 = 0; i17 < loop_ub; i17++) {
        sgi->data[i17] = sg->data[i17 + sg->size[0] * i];
      }
    }

    i17 = sgd->size[0];
    sgd->size[0] = sgi->size[0];
    emxEnsureCapacity_real_T1(sgd, i17);
    loop_ub = sgi->size[0];
    for (i17 = 0; i17 < loop_ub; i17++) {
      sgd->data[i17] = 3.9 - sgi->data[i17];
    }

    i17 = b_sgd->size[0];
    b_sgd->size[0] = sgd->size[0];
    emxEnsureCapacity_real_T1(b_sgd, i17);
    loop_ub = sgd->size[0];
    for (i17 = 0; i17 < loop_ub; i17++) {
      b_sgd->data[i17] = sgd->data[i17] * (double)(sgd->data[i17] > 0.0);
    }

    x = nansum(b_sgd);
    i17 = r34->size[0];
    r34->size[0] = sgi->size[0];
    emxEnsureCapacity_boolean_T(r34, i17);
    loop_ub = sgi->size[0];
    for (i17 = 0; i17 < loop_ub; i17++) {
      r34->data[i17] = rtIsNaN(sgi->data[i17]);
    }

    i17 = r35->size[0];
    r35->size[0] = r34->size[0];
    emxEnsureCapacity_boolean_T(r35, i17);
    loop_ub = r34->size[0];
    for (i17 = 0; i17 < loop_ub; i17++) {
      r35->data[i17] = !r34->data[i17];
    }

    y = sum(r35);
    aac->data[i] = x / y;
    i++;
  }

  emxFree_real_T(&b_sgd);
  emxFree_boolean_T(&r35);
  emxFree_boolean_T(&r34);
  emxFree_real_T(&sgd);
  emxFree_real_T(&sgi);
}

/*
 * File trailer for AAC.c
 *
 * [EOF]
 */
