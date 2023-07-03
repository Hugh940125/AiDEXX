/*
 * Academic License - for use in teaching, academic research, and meeting
 * course requirements at degree granting institutions only.  Not for
 * government, commercial, or other organizational use.
 * File: MAG.c
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
#include "abs.h"
#include "datools_emxutil.h"

/* Function Definitions */

/*
 * Arguments    : const emxArray_real_T *sg
 *                double n
 *                emxArray_real_T *mag
 * Return Type  : void
 */
void MAG(const emxArray_real_T *sg, double n, emxArray_real_T *mag)
{
  int i8;
  int loop_ub;
  int i;
  emxArray_real_T *sgi;
  emxArray_real_T *b_sgi;
  int i9;
  n = floor(n * (double)sg->size[0] / 24.0);
  i8 = mag->size[0] * mag->size[1];
  mag->size[0] = 1;
  mag->size[1] = sg->size[1] + 1;
  emxEnsureCapacity_real_T(mag, i8);
  loop_ub = sg->size[1] + 1;
  for (i8 = 0; i8 < loop_ub; i8++) {
    mag->data[i8] = rtNaN;
  }

  i = 0;
  emxInit_real_T(&sgi, 1);
  emxInit_real_T(&b_sgi, 1);
  while (i <= sg->size[1]) {
    if (1.0 + (double)i > sg->size[1]) {
      i8 = sgi->size[0];
      sgi->size[0] = sg->size[0] * sg->size[1];
      emxEnsureCapacity_real_T1(sgi, i8);
      loop_ub = sg->size[0] * sg->size[1];
      for (i8 = 0; i8 < loop_ub; i8++) {
        sgi->data[i8] = sg->data[i8];
      }
    } else {
      loop_ub = sg->size[0];
      i8 = sgi->size[0];
      sgi->size[0] = loop_ub;
      emxEnsureCapacity_real_T1(sgi, i8);
      for (i8 = 0; i8 < loop_ub; i8++) {
        sgi->data[i8] = sg->data[i8 + sg->size[0] * i];
      }
    }

    if (1.0 + n > sgi->size[0]) {
      i8 = 0;
      i9 = 0;
    } else {
      i8 = (int)(1.0 + n) - 1;
      i9 = sgi->size[0];
    }

    loop_ub = b_sgi->size[0];
    b_sgi->size[0] = i9 - i8;
    emxEnsureCapacity_real_T1(b_sgi, loop_ub);
    loop_ub = i9 - i8;
    for (i9 = 0; i9 < loop_ub; i9++) {
      b_sgi->data[i9] = sgi->data[i8 + i9] - sgi->data[i9];
    }

    b_abs(b_sgi, sgi);
    mag->data[i] = c_nanmean(sgi);
    i++;
  }

  emxFree_real_T(&b_sgi);
  emxFree_real_T(&sgi);
}

/*
 * File trailer for MAG.c
 *
 * [EOF]
 */
