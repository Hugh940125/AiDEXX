/*
 * Academic License - for use in teaching, academic research, and meeting
 * course requirements at degree granting institutions only.  Not for
 * government, commercial, or other organizational use.
 * File: MValue.c
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
#include "nanmean.h"
#include "datools_emxutil.h"
#include "abs.h"
#include "log10.h"
#include "rdivide.h"
#include "datools_rtwutil.h"

/* Function Definitions */

/*
 * Arguments    : const emxArray_real_T *sg
 *                double target
 *                emxArray_real_T *m
 * Return Type  : void
 */
void MValue(const emxArray_real_T *sg, double target, emxArray_real_T *m)
{
  int k;
  int loop_ub;
  int i;
  emxArray_real_T *sgi;
  emxArray_real_T *b;
  unsigned int b_idx_0;
  unsigned int b_b_idx_0;
  k = m->size[0] * m->size[1];
  m->size[0] = 1;
  m->size[1] = sg->size[1] + 1;
  emxEnsureCapacity_real_T(m, k);
  loop_ub = sg->size[1] + 1;
  for (k = 0; k < loop_ub; k++) {
    m->data[k] = rtNaN;
  }

  i = 0;
  emxInit_real_T(&sgi, 1);
  emxInit_real_T(&b, 1);
  while (i <= sg->size[1]) {
    if (1.0 + (double)i > sg->size[1]) {
      k = sgi->size[0];
      sgi->size[0] = sg->size[0] * sg->size[1];
      emxEnsureCapacity_real_T1(sgi, k);
      loop_ub = sg->size[0] * sg->size[1];
      for (k = 0; k < loop_ub; k++) {
        sgi->data[k] = sg->data[k];
      }
    } else {
      loop_ub = sg->size[0];
      k = sgi->size[0];
      sgi->size[0] = loop_ub;
      emxEnsureCapacity_real_T1(sgi, k);
      for (k = 0; k < loop_ub; k++) {
        sgi->data[k] = sg->data[k + sg->size[0] * i];
      }
    }

    rdivide(sgi, target, b);
    b_log10(b);
    k = sgi->size[0];
    sgi->size[0] = b->size[0];
    emxEnsureCapacity_real_T1(sgi, k);
    loop_ub = b->size[0];
    for (k = 0; k < loop_ub; k++) {
      sgi->data[k] = 10.0 * b->data[k];
    }

    b_abs(sgi, b);
    b_idx_0 = (unsigned int)b->size[0];
    b_b_idx_0 = (unsigned int)b->size[0];
    k = sgi->size[0];
    sgi->size[0] = (int)b_b_idx_0;
    emxEnsureCapacity_real_T1(sgi, k);
    for (k = 0; k + 1 <= (int)b_idx_0; k++) {
      sgi->data[k] = rt_powd_snf(b->data[k], 3.0);
    }

    m->data[i] = c_nanmean(sgi);
    i++;
  }

  emxFree_real_T(&b);
  emxFree_real_T(&sgi);
}

/*
 * File trailer for MValue.c
 *
 * [EOF]
 */
