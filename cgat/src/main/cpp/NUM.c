/*
 * Academic License - for use in teaching, academic research, and meeting
 * course requirements at degree granting institutions only.  Not for
 * government, commercial, or other organizational use.
 * File: NUM.c
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
#include "datools_emxutil.h"

/* Function Definitions */

/*
 * Arguments    : const emxArray_real_T *sg
 *                emxArray_real_T *num
 * Return Type  : void
 */
void NUM(const emxArray_real_T *sg, emxArray_real_T *num)
{
  emxArray_boolean_T *x;
  int xpageoffset;
  int loop_ub;
  unsigned int sz[2];
  int k;
  double y;
  emxInit_boolean_T1(&x, 2);
  xpageoffset = x->size[0] * x->size[1];
  x->size[0] = sg->size[0];
  x->size[1] = sg->size[1];
  emxEnsureCapacity_boolean_T1(x, xpageoffset);
  loop_ub = sg->size[0] * sg->size[1];
  for (xpageoffset = 0; xpageoffset < loop_ub; xpageoffset++) {
    x->data[xpageoffset] = rtIsNaN(sg->data[xpageoffset]);
  }

  xpageoffset = x->size[0] * x->size[1];
  emxEnsureCapacity_boolean_T1(x, xpageoffset);
  loop_ub = x->size[0];
  xpageoffset = x->size[1];
  loop_ub *= xpageoffset;
  for (xpageoffset = 0; xpageoffset < loop_ub; xpageoffset++) {
    x->data[xpageoffset] = !x->data[xpageoffset];
  }

  if ((x->size[0] == 0) || (x->size[1] == 0)) {
    for (xpageoffset = 0; xpageoffset < 2; xpageoffset++) {
      sz[xpageoffset] = (unsigned int)x->size[xpageoffset];
    }

    xpageoffset = num->size[0] * num->size[1];
    num->size[0] = 1;
    num->size[1] = (int)sz[1];
    emxEnsureCapacity_real_T(num, xpageoffset);
    loop_ub = (int)sz[1];
    for (xpageoffset = 0; xpageoffset < loop_ub; xpageoffset++) {
      num->data[xpageoffset] = 0.0;
    }
  } else {
    xpageoffset = num->size[0] * num->size[1];
    num->size[0] = 1;
    num->size[1] = x->size[1];
    emxEnsureCapacity_real_T(num, xpageoffset);
    for (loop_ub = 0; loop_ub + 1 <= x->size[1]; loop_ub++) {
      xpageoffset = loop_ub * x->size[0];
      num->data[loop_ub] = x->data[xpageoffset];
      for (k = 2; k <= x->size[0]; k++) {
        num->data[loop_ub] += (double)x->data[(xpageoffset + k) - 1];
      }
    }
  }

  emxFree_boolean_T(&x);
  if (num->size[1] == 0) {
    y = 0.0;
  } else {
    y = num->data[0];
    for (k = 2; k <= num->size[1]; k++) {
      y += num->data[k - 1];
    }
  }

  loop_ub = num->size[1];
  xpageoffset = num->size[0] * num->size[1];
  num->size[1] = loop_ub + 1;
  emxEnsureCapacity_real_T(num, xpageoffset);
  num->data[loop_ub] = y;
}

/*
 * File trailer for NUM.c
 *
 * [EOF]
 */
