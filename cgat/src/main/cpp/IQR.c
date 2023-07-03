/*
 * Academic License - for use in teaching, academic research, and meeting
 * course requirements at degree granting institutions only.  Not for
 * government, commercial, or other organizational use.
 * File: IQR.c
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
#include "sort1.h"
#include "nullAssignment.h"
#include "datools_emxutil.h"

/* Function Definitions */

/*
 * Arguments    : const emxArray_real_T *sg
 *                emxArray_real_T *iqr
 * Return Type  : void
 */
void IQR(const emxArray_real_T *sg, emxArray_real_T *iqr)
{
  int n;
  int loop_ub;
  int i;
  emxArray_real_T *bgi;
  emxArray_boolean_T *r14;
  double A;
  double B;
  n = iqr->size[0] * iqr->size[1];
  iqr->size[0] = 1;
  iqr->size[1] = sg->size[1] + 1;
  emxEnsureCapacity_real_T(iqr, n);
  loop_ub = sg->size[1] + 1;
  for (n = 0; n < loop_ub; n++) {
    iqr->data[n] = rtNaN;
  }

  i = 0;
  emxInit_real_T(&bgi, 1);
  emxInit_boolean_T(&r14, 1);
  while (i <= sg->size[1]) {
    if (1.0 + (double)i > sg->size[1]) {
      n = bgi->size[0];
      bgi->size[0] = sg->size[0] * sg->size[1];
      emxEnsureCapacity_real_T1(bgi, n);
      loop_ub = sg->size[0] * sg->size[1];
      for (n = 0; n < loop_ub; n++) {
        bgi->data[n] = sg->data[n];
      }
    } else {
      loop_ub = sg->size[0];
      n = bgi->size[0];
      bgi->size[0] = loop_ub;
      emxEnsureCapacity_real_T1(bgi, n);
      for (n = 0; n < loop_ub; n++) {
        bgi->data[n] = sg->data[n + sg->size[0] * i];
      }
    }

    n = r14->size[0];
    r14->size[0] = bgi->size[0];
    emxEnsureCapacity_boolean_T(r14, n);
    loop_ub = bgi->size[0];
    for (n = 0; n < loop_ub; n++) {
      r14->data[n] = rtIsNaN(bgi->data[n]);
    }

    nullAssignment(bgi, r14);
    n = bgi->size[0];
    if (!(bgi->size[0] < 8)) {
      sort(bgi);
      A = ((double)n + 1.0) / 4.0;
      B = 3.0 * A;
      iqr->data[i] = (bgi->data[(int)floor(B) - 1] + (B - floor(B)) * (bgi->
        data[(int)floor(B) + 1] - bgi->data[(int)floor(B)])) - (bgi->data[(int)
        floor(A) - 1] + (A - floor(A)) * (bgi->data[(int)floor(A) + 1] -
        bgi->data[(int)floor(A)]));
    }

    i++;
  }

  emxFree_boolean_T(&r14);
  emxFree_real_T(&bgi);
}

/*
 * File trailer for IQR.c
 *
 * [EOF]
 */
