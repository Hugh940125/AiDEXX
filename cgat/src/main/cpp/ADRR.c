/*
 * Academic License - for use in teaching, academic research, and meeting
 * course requirements at degree granting institutions only.  Not for
 * government, commercial, or other organizational use.
 * File: ADRR.c
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
#include "nanmean.h"
#include "power.h"
#include "datools_rtwutil.h"

/* Function Declarations */
static void T(const emxArray_real_T *sg, emxArray_real_T *tbg);

/* Function Definitions */

/*
 * Arguments    : const emxArray_real_T *sg
 *                emxArray_real_T *tbg
 * Return Type  : void
 */
static void T(const emxArray_real_T *sg, emxArray_real_T *tbg)
{
  emxArray_real_T *x;
  int k;
  int nx;
  emxInit_real_T1(&x, 2);
  k = x->size[0] * x->size[1];
  x->size[0] = 1;
  x->size[1] = sg->size[1];
  emxEnsureCapacity_real_T(x, k);
  nx = sg->size[0] * sg->size[1];
  for (k = 0; k < nx; k++) {
    x->data[k] = sg->data[k] * 18.0;
  }

  nx = x->size[1];
  for (k = 0; k + 1 <= nx; k++) {
    x->data[k] = log(x->data[k]);
  }

  nx = x->size[1];
  k = tbg->size[0] * tbg->size[1];
  tbg->size[0] = 1;
  tbg->size[1] = x->size[1];
  emxEnsureCapacity_real_T(tbg, k);
  for (k = 0; k + 1 <= nx; k++) {
    tbg->data[k] = rt_powd_snf(x->data[k], 1.084);
  }

  emxFree_real_T(&x);
  k = tbg->size[0] * tbg->size[1];
  tbg->size[0] = 1;
  emxEnsureCapacity_real_T(tbg, k);
  nx = tbg->size[0];
  k = tbg->size[1];
  nx *= k;
  for (k = 0; k < nx; k++) {
    tbg->data[k] = 1.509 * (tbg->data[k] - 5.381);
  }
}

/*
 * Arguments    : const emxArray_real_T *sg
 * Return Type  : double
 */
double ADRR(const emxArray_real_T *sg)
{
  double adrr;
  emxArray_real_T *minbg;
  int ixstart;
  int n;
  int i;
  emxArray_real_T *maxbg;
  int ix;
  int ixstop;
  double u0;
  boolean_T exitg1;
  emxArray_real_T *tmaxbg0;
  emxArray_real_T *tmaxbg;
  emxArray_boolean_T *r2;
  emxArray_int32_T *r3;
  emxArray_int32_T *r4;
  emxInit_real_T1(&minbg, 2);
  ixstart = minbg->size[0] * minbg->size[1];
  minbg->size[0] = 1;
  minbg->size[1] = sg->size[1];
  emxEnsureCapacity_real_T(minbg, ixstart);
  n = sg->size[0];
  for (i = 0; i + 1 <= sg->size[1]; i++) {
    ix = i * n;
    ixstart = i * n + 1;
    ixstop = ix + n;
    u0 = sg->data[ix];
    if (n > 1) {
      if (rtIsNaN(sg->data[ix])) {
        ix = ixstart + 1;
        exitg1 = false;
        while ((!exitg1) && (ix <= ixstop)) {
          ixstart = ix;
          if (!rtIsNaN(sg->data[ix - 1])) {
            u0 = sg->data[ix - 1];
            exitg1 = true;
          } else {
            ix++;
          }
        }
      }

      if (ixstart < ixstop) {
        while (ixstart + 1 <= ixstop) {
          if (sg->data[ixstart] < u0) {
            u0 = sg->data[ixstart];
          }

          ixstart++;
        }
      }
    }

    minbg->data[i] = u0;
  }

  emxInit_real_T1(&maxbg, 2);
  ixstart = maxbg->size[0] * maxbg->size[1];
  maxbg->size[0] = 1;
  maxbg->size[1] = sg->size[1];
  emxEnsureCapacity_real_T(maxbg, ixstart);
  n = sg->size[0];
  for (i = 0; i + 1 <= sg->size[1]; i++) {
    ix = i * n;
    ixstart = i * n + 1;
    ixstop = ix + n;
    u0 = sg->data[ix];
    if (n > 1) {
      if (rtIsNaN(sg->data[ix])) {
        ix = ixstart + 1;
        exitg1 = false;
        while ((!exitg1) && (ix <= ixstop)) {
          ixstart = ix;
          if (!rtIsNaN(sg->data[ix - 1])) {
            u0 = sg->data[ix - 1];
            exitg1 = true;
          } else {
            ix++;
          }
        }
      }

      if (ixstart < ixstop) {
        while (ixstart + 1 <= ixstop) {
          if (sg->data[ixstart] > u0) {
            u0 = sg->data[ixstart];
          }

          ixstart++;
        }
      }
    }

    maxbg->data[i] = u0;
  }

  emxInit_real_T1(&tmaxbg0, 2);
  emxInit_real_T1(&tmaxbg, 2);
  T(maxbg, tmaxbg0);
  ix = tmaxbg0->size[1];
  ixstart = tmaxbg->size[0] * tmaxbg->size[1];
  tmaxbg->size[0] = 1;
  tmaxbg->size[1] = tmaxbg0->size[1];
  emxEnsureCapacity_real_T(tmaxbg, ixstart);
  for (n = 0; n + 1 <= ix; n++) {
    u0 = tmaxbg0->data[n];
    if (!(u0 > 0.0)) {
      u0 = 0.0;
    }

    tmaxbg->data[n] = u0;
  }

  emxInit_boolean_T1(&r2, 2);
  ixstart = r2->size[0] * r2->size[1];
  r2->size[0] = 1;
  r2->size[1] = tmaxbg0->size[1];
  emxEnsureCapacity_boolean_T1(r2, ixstart);
  n = tmaxbg0->size[0] * tmaxbg0->size[1];
  for (ixstart = 0; ixstart < n; ixstart++) {
    r2->data[ixstart] = rtIsNaN(tmaxbg0->data[ixstart]);
  }

  ix = r2->size[1] - 1;
  n = 0;
  for (i = 0; i <= ix; i++) {
    if (r2->data[i]) {
      n++;
    }
  }

  emxInit_int32_T(&r3, 2);
  ixstart = r3->size[0] * r3->size[1];
  r3->size[0] = 1;
  r3->size[1] = n;
  emxEnsureCapacity_int32_T(r3, ixstart);
  n = 0;
  for (i = 0; i <= ix; i++) {
    if (r2->data[i]) {
      r3->data[n] = i + 1;
      n++;
    }
  }

  n = r3->size[0] * r3->size[1];
  for (ixstart = 0; ixstart < n; ixstart++) {
    tmaxbg->data[r3->data[ixstart] - 1] = rtNaN;
  }

  emxFree_int32_T(&r3);
  T(minbg, maxbg);
  ix = maxbg->size[1];
  ixstart = minbg->size[0] * minbg->size[1];
  minbg->size[0] = 1;
  minbg->size[1] = maxbg->size[1];
  emxEnsureCapacity_real_T(minbg, ixstart);
  for (n = 0; n + 1 <= ix; n++) {
    u0 = maxbg->data[n];
    if (!(u0 < 0.0)) {
      u0 = 0.0;
    }

    minbg->data[n] = u0;
  }

  ixstart = r2->size[0] * r2->size[1];
  r2->size[0] = 1;
  r2->size[1] = maxbg->size[1];
  emxEnsureCapacity_boolean_T1(r2, ixstart);
  n = maxbg->size[0] * maxbg->size[1];
  for (ixstart = 0; ixstart < n; ixstart++) {
    r2->data[ixstart] = rtIsNaN(maxbg->data[ixstart]);
  }

  ix = r2->size[1] - 1;
  n = 0;
  for (i = 0; i <= ix; i++) {
    if (r2->data[i]) {
      n++;
    }
  }

  emxInit_int32_T(&r4, 2);
  ixstart = r4->size[0] * r4->size[1];
  r4->size[0] = 1;
  r4->size[1] = n;
  emxEnsureCapacity_int32_T(r4, ixstart);
  n = 0;
  for (i = 0; i <= ix; i++) {
    if (r2->data[i]) {
      r4->data[n] = i + 1;
      n++;
    }
  }

  emxFree_boolean_T(&r2);
  n = r4->size[0] * r4->size[1];
  for (ixstart = 0; ixstart < n; ixstart++) {
    minbg->data[r4->data[ixstart] - 1] = rtNaN;
  }

  emxFree_int32_T(&r4);
  power(tmaxbg, tmaxbg0);
  power(minbg, maxbg);
  ixstart = minbg->size[0] * minbg->size[1];
  minbg->size[0] = 1;
  minbg->size[1] = tmaxbg0->size[1];
  emxEnsureCapacity_real_T(minbg, ixstart);
  n = tmaxbg0->size[0] * tmaxbg0->size[1];
  emxFree_real_T(&tmaxbg);
  for (ixstart = 0; ixstart < n; ixstart++) {
    minbg->data[ixstart] = 10.0 * (tmaxbg0->data[ixstart] + maxbg->data[ixstart]);
  }

  emxFree_real_T(&tmaxbg0);
  emxFree_real_T(&maxbg);
  adrr = nanmean(minbg);
  emxFree_real_T(&minbg);
  return adrr;
}

/*
 * File trailer for ADRR.c
 *
 * [EOF]
 */
