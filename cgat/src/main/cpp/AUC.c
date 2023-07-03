/*
 * Academic License - for use in teaching, academic research, and meeting
 * course requirements at degree granting institutions only.  Not for
 * government, commercial, or other organizational use.
 * File: AUC.c
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
 *                emxArray_real_T *auc
 * Return Type  : void
 */
void AUC(const emxArray_real_T *sg, double target, emxArray_real_T *auc)
{
  int i1;
  int loop_ub;
  int i;
  emxArray_real_T *sgi;
  emxArray_real_T *sgd;
  emxArray_boolean_T *r5;
  emxArray_boolean_T *r6;
  emxArray_real_T *b_sgd;
  double x;
  double y;
  i1 = auc->size[0] * auc->size[1];
  auc->size[0] = 1;
  auc->size[1] = sg->size[1] + 1;
  emxEnsureCapacity_real_T(auc, i1);
  loop_ub = sg->size[1] + 1;
  for (i1 = 0; i1 < loop_ub; i1++) {
    auc->data[i1] = rtNaN;
  }

  i = 0;
  emxInit_real_T(&sgi, 1);
  emxInit_real_T(&sgd, 1);
  emxInit_boolean_T(&r5, 1);
  emxInit_boolean_T(&r6, 1);
  emxInit_real_T(&b_sgd, 1);
  while (i <= sg->size[1]) {
    if (1.0 + (double)i > sg->size[1]) {
      i1 = sgi->size[0];
      sgi->size[0] = sg->size[0] * sg->size[1];
      emxEnsureCapacity_real_T1(sgi, i1);
      loop_ub = sg->size[0] * sg->size[1];
      for (i1 = 0; i1 < loop_ub; i1++) {
        sgi->data[i1] = sg->data[i1];
      }
    } else {
      loop_ub = sg->size[0];
      i1 = sgi->size[0];
      sgi->size[0] = loop_ub;
      emxEnsureCapacity_real_T1(sgi, i1);
      for (i1 = 0; i1 < loop_ub; i1++) {
        sgi->data[i1] = sg->data[i1 + sg->size[0] * i];
      }
    }

    i1 = sgd->size[0];
    sgd->size[0] = sgi->size[0];
    emxEnsureCapacity_real_T1(sgd, i1);
    loop_ub = sgi->size[0];
    for (i1 = 0; i1 < loop_ub; i1++) {
      sgd->data[i1] = sgi->data[i1] - target;
    }

    i1 = b_sgd->size[0];
    b_sgd->size[0] = sgd->size[0];
    emxEnsureCapacity_real_T1(b_sgd, i1);
    loop_ub = sgd->size[0];
    for (i1 = 0; i1 < loop_ub; i1++) {
      b_sgd->data[i1] = sgd->data[i1] * (double)(sgd->data[i1] > 0.0);
    }

    x = nansum(b_sgd);
    i1 = r5->size[0];
    r5->size[0] = sgi->size[0];
    emxEnsureCapacity_boolean_T(r5, i1);
    loop_ub = sgi->size[0];
    for (i1 = 0; i1 < loop_ub; i1++) {
      r5->data[i1] = rtIsNaN(sgi->data[i1]);
    }

    i1 = r6->size[0];
    r6->size[0] = r5->size[0];
    emxEnsureCapacity_boolean_T(r6, i1);
    loop_ub = r5->size[0];
    for (i1 = 0; i1 < loop_ub; i1++) {
      r6->data[i1] = !r5->data[i1];
    }

    y = sum(r6);
    auc->data[i] = x / y;
    i++;
  }

  emxFree_real_T(&b_sgd);
  emxFree_boolean_T(&r6);
  emxFree_boolean_T(&r5);
  emxFree_real_T(&sgd);
  emxFree_real_T(&sgi);
}

/*
 * Arguments    : const emxArray_real_T *sg
 *                emxArray_real_T *auc
 * Return Type  : void
 */
void b_AUC(const emxArray_real_T *sg, emxArray_real_T *auc)
{
  int i18;
  int loop_ub;
  int i;
  emxArray_real_T *sgi;
  emxArray_real_T *sgd;
  emxArray_boolean_T *r36;
  emxArray_boolean_T *r37;
  emxArray_real_T *b_sgd;
  double x;
  double y;
  i18 = auc->size[0] * auc->size[1];
  auc->size[0] = 1;
  auc->size[1] = sg->size[1] + 1;
  emxEnsureCapacity_real_T(auc, i18);
  loop_ub = sg->size[1] + 1;
  for (i18 = 0; i18 < loop_ub; i18++) {
    auc->data[i18] = rtNaN;
  }

  i = 0;
  emxInit_real_T(&sgi, 1);
  emxInit_real_T(&sgd, 1);
  emxInit_boolean_T(&r36, 1);
  emxInit_boolean_T(&r37, 1);
  emxInit_real_T(&b_sgd, 1);
  while (i <= sg->size[1]) {
    if (1.0 + (double)i > sg->size[1]) {
      i18 = sgi->size[0];
      sgi->size[0] = sg->size[0] * sg->size[1];
      emxEnsureCapacity_real_T1(sgi, i18);
      loop_ub = sg->size[0] * sg->size[1];
      for (i18 = 0; i18 < loop_ub; i18++) {
        sgi->data[i18] = sg->data[i18];
      }
    } else {
      loop_ub = sg->size[0];
      i18 = sgi->size[0];
      sgi->size[0] = loop_ub;
      emxEnsureCapacity_real_T1(sgi, i18);
      for (i18 = 0; i18 < loop_ub; i18++) {
        sgi->data[i18] = sg->data[i18 + sg->size[0] * i];
      }
    }

    i18 = sgd->size[0];
    sgd->size[0] = sgi->size[0];
    emxEnsureCapacity_real_T1(sgd, i18);
    loop_ub = sgi->size[0];
    for (i18 = 0; i18 < loop_ub; i18++) {
      sgd->data[i18] = sgi->data[i18] - 8.9;
    }

    i18 = b_sgd->size[0];
    b_sgd->size[0] = sgd->size[0];
    emxEnsureCapacity_real_T1(b_sgd, i18);
    loop_ub = sgd->size[0];
    for (i18 = 0; i18 < loop_ub; i18++) {
      b_sgd->data[i18] = sgd->data[i18] * (double)(sgd->data[i18] > 0.0);
    }

    x = nansum(b_sgd);
    i18 = r36->size[0];
    r36->size[0] = sgi->size[0];
    emxEnsureCapacity_boolean_T(r36, i18);
    loop_ub = sgi->size[0];
    for (i18 = 0; i18 < loop_ub; i18++) {
      r36->data[i18] = rtIsNaN(sgi->data[i18]);
    }

    i18 = r37->size[0];
    r37->size[0] = r36->size[0];
    emxEnsureCapacity_boolean_T(r37, i18);
    loop_ub = r36->size[0];
    for (i18 = 0; i18 < loop_ub; i18++) {
      r37->data[i18] = !r36->data[i18];
    }

    y = sum(r37);
    auc->data[i] = x / y;
    i++;
  }

  emxFree_real_T(&b_sgd);
  emxFree_boolean_T(&r37);
  emxFree_boolean_T(&r36);
  emxFree_real_T(&sgd);
  emxFree_real_T(&sgi);
}

/*
 * File trailer for AUC.c
 *
 * [EOF]
 */
