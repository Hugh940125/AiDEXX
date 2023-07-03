/*
 * Academic License - for use in teaching, academic research, and meeting
 * course requirements at degree granting institutions only.  Not for
 * government, commercial, or other organizational use.
 * File: Pentagon.c
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
#include "exp.h"
#include "power.h"
#include "sqrt.h"
#include "sum.h"

/* Function Definitions */

/*
 * Arguments    : const emxArray_real_T *sg
 *                emxArray_real_T *mbg
 *                emxArray_real_T *cv
 *                emxArray_real_T *pentagon
 * Return Type  : void
 */
void Pentagon(const emxArray_real_T *sg, emxArray_real_T *mbg, emxArray_real_T
              *cv, emxArray_real_T *pentagon)
{
  emxArray_boolean_T *mbg_nan;
  int i15;
  int n;
  emxArray_real_T *tor_mm;
  int k;
  double u0;
  int i;
  emxArray_real_T *pt;
  emxArray_real_T *b_pt;
  emxArray_real_T *tor;
  int i16;
  emxArray_real_T *intHyper_mm;
  emxArray_real_T *b_tor_mm;
  emxArray_real_T *intHypo;
  emxArray_real_T *r28;
  emxArray_real_T *intHyper;
  emxArray_real_T *mbg_mm;
  emxArray_real_T *axes;
  emxArray_boolean_T *axes_nan;
  emxArray_real_T *x;
  emxArray_real_T *ztemp;
  emxArray_int32_T *r29;
  int result;
  boolean_T empty_non_axis_sizes;
  int b_result;
  int c_result;
  int d_result;
  emxInit_boolean_T1(&mbg_nan, 2);
  i15 = mbg_nan->size[0] * mbg_nan->size[1];
  mbg_nan->size[0] = 1;
  mbg_nan->size[1] = mbg->size[1];
  emxEnsureCapacity_boolean_T1(mbg_nan, i15);
  n = mbg->size[0] * mbg->size[1];
  for (i15 = 0; i15 < n; i15++) {
    mbg_nan->data[i15] = rtIsNaN(mbg->data[i15]);
  }

  emxInit_real_T1(&tor_mm, 2);
  i15 = tor_mm->size[0] * tor_mm->size[1];
  tor_mm->size[0] = 1;
  tor_mm->size[1] = mbg->size[1];
  emxEnsureCapacity_real_T(tor_mm, i15);
  n = mbg->size[0] * mbg->size[1];
  for (i15 = 0; i15 < n; i15++) {
    tor_mm->data[i15] = mbg->data[i15] * 18.0;
  }

  n = tor_mm->size[1];
  i15 = mbg->size[0] * mbg->size[1];
  mbg->size[0] = 1;
  mbg->size[1] = tor_mm->size[1];
  emxEnsureCapacity_real_T(mbg, i15);
  for (k = 0; k + 1 <= n; k++) {
    u0 = tor_mm->data[k];
    if (!(u0 > 90.0)) {
      u0 = 90.0;
    }

    mbg->data[k] = u0;
  }

  k = mbg_nan->size[1];
  for (i = 0; i < k; i++) {
    if (mbg_nan->data[i]) {
      mbg->data[i] = rtNaN;
    }
  }

  i15 = mbg_nan->size[0] * mbg_nan->size[1];
  mbg_nan->size[0] = 1;
  mbg_nan->size[1] = cv->size[1];
  emxEnsureCapacity_boolean_T1(mbg_nan, i15);
  n = cv->size[0] * cv->size[1];
  for (i15 = 0; i15 < n; i15++) {
    mbg_nan->data[i15] = rtIsNaN(cv->data[i15]);
  }

  i15 = tor_mm->size[0] * tor_mm->size[1];
  tor_mm->size[0] = 1;
  tor_mm->size[1] = cv->size[1];
  emxEnsureCapacity_real_T(tor_mm, i15);
  n = cv->size[0] * cv->size[1];
  for (i15 = 0; i15 < n; i15++) {
    tor_mm->data[i15] = cv->data[i15];
  }

  n = cv->size[1];
  k = cv->size[1];
  i15 = cv->size[0] * cv->size[1];
  cv->size[0] = 1;
  cv->size[1] = k;
  emxEnsureCapacity_real_T(cv, i15);
  for (k = 0; k + 1 <= n; k++) {
    u0 = tor_mm->data[k];
    if (!(u0 > 17.0)) {
      u0 = 17.0;
    }

    cv->data[k] = u0;
  }

  k = mbg_nan->size[1];
  for (i = 0; i < k; i++) {
    if (mbg_nan->data[i]) {
      cv->data[i] = rtNaN;
    }
  }

  emxFree_boolean_T(&mbg_nan);
  emxInit_real_T1(&pt, 2);
  b_PT(sg, pt);
  i15 = pt->size[0] * pt->size[1];
  pt->size[0] = 4;
  emxEnsureCapacity_real_T(pt, i15);
  n = pt->size[0];
  k = pt->size[1];
  n *= k;
  for (i15 = 0; i15 < n; i15++) {
    pt->data[i15] = pt->data[i15] / 100.0 * 1440.0;
  }

  emxInit_real_T1(&b_pt, 2);
  n = pt->size[1];
  i15 = b_pt->size[0] * b_pt->size[1];
  b_pt->size[0] = 2;
  b_pt->size[1] = n;
  emxEnsureCapacity_real_T(b_pt, i15);
  for (i15 = 0; i15 < n; i15++) {
    for (i16 = 0; i16 < 2; i16++) {
      b_pt->data[i16 + b_pt->size[0] * i15] = pt->data[(i16 + pt->size[0] * i15)
        + 1];
    }
  }

  emxInit_real_T1(&tor, 2);
  d_sum(b_pt, tor);
  i15 = tor->size[0] * tor->size[1];
  tor->size[0] = 1;
  emxEnsureCapacity_real_T(tor, i15);
  n = tor->size[0];
  k = tor->size[1];
  n *= k;
  for (i15 = 0; i15 < n; i15++) {
    tor->data[i15] = 1440.0 - tor->data[i15];
  }

  emxInit_real_T1(&intHyper_mm, 2);
  emxInit_real_T1(&b_tor_mm, 2);
  b_AAC(sg, tor_mm);
  b_AUC(sg, intHyper_mm);
  i15 = b_tor_mm->size[0] * b_tor_mm->size[1];
  b_tor_mm->size[0] = 1;
  b_tor_mm->size[1] = tor_mm->size[1];
  emxEnsureCapacity_real_T(b_tor_mm, i15);
  n = tor_mm->size[0] * tor_mm->size[1];
  for (i15 = 0; i15 < n; i15++) {
    b_tor_mm->data[i15] = tor_mm->data[i15] * 18.0 * 1440.0;
  }

  emxInit_real_T1(&intHypo, 2);
  power(b_tor_mm, intHypo);
  n = pt->size[1];
  i15 = b_tor_mm->size[0] * b_tor_mm->size[1];
  b_tor_mm->size[0] = 1;
  b_tor_mm->size[1] = n;
  emxEnsureCapacity_real_T(b_tor_mm, i15);
  for (i15 = 0; i15 < n; i15++) {
    b_tor_mm->data[b_tor_mm->size[0] * i15] = pt->data[pt->size[0] * i15];
  }

  emxInit_real_T1(&r28, 2);
  power(b_tor_mm, r28);
  i15 = intHypo->size[0] * intHypo->size[1];
  intHypo->size[0] = 1;
  emxEnsureCapacity_real_T(intHypo, i15);
  n = intHypo->size[0];
  k = intHypo->size[1];
  n *= k;
  for (i15 = 0; i15 < n; i15++) {
    intHypo->data[i15] += r28->data[i15];
  }

  b_sqrt(intHypo);
  i15 = b_tor_mm->size[0] * b_tor_mm->size[1];
  b_tor_mm->size[0] = 1;
  b_tor_mm->size[1] = intHyper_mm->size[1];
  emxEnsureCapacity_real_T(b_tor_mm, i15);
  n = intHyper_mm->size[0] * intHyper_mm->size[1];
  for (i15 = 0; i15 < n; i15++) {
    b_tor_mm->data[i15] = intHyper_mm->data[i15] * 18.0 * 1440.0;
  }

  emxInit_real_T1(&intHyper, 2);
  power(b_tor_mm, intHyper);
  n = pt->size[1];
  i15 = b_pt->size[0] * b_pt->size[1];
  b_pt->size[0] = 2;
  b_pt->size[1] = n;
  emxEnsureCapacity_real_T(b_pt, i15);
  for (i15 = 0; i15 < n; i15++) {
    for (i16 = 0; i16 < 2; i16++) {
      b_pt->data[i16 + b_pt->size[0] * i15] = pt->data[(i16 + pt->size[0] * i15)
        + 2];
    }
  }

  d_sum(b_pt, r28);
  power(r28, tor_mm);
  i15 = intHyper->size[0] * intHyper->size[1];
  intHyper->size[0] = 1;
  emxEnsureCapacity_real_T(intHyper, i15);
  n = intHyper->size[0];
  k = intHyper->size[1];
  n *= k;
  emxFree_real_T(&b_pt);
  for (i15 = 0; i15 < n; i15++) {
    intHyper->data[i15] += tor_mm->data[i15];
  }

  b_sqrt(intHyper);
  i15 = b_tor_mm->size[0] * b_tor_mm->size[1];
  b_tor_mm->size[0] = 1;
  b_tor_mm->size[1] = tor->size[1];
  emxEnsureCapacity_real_T(b_tor_mm, i15);
  n = tor->size[0] * tor->size[1];
  for (i15 = 0; i15 < n; i15++) {
    b_tor_mm->data[i15] = tor->data[i15] * 0.00614;
  }

  d_power(b_tor_mm, tor_mm);
  i15 = tor_mm->size[0] * tor_mm->size[1];
  tor_mm->size[0] = 1;
  emxEnsureCapacity_real_T(tor_mm, i15);
  n = tor_mm->size[0];
  k = tor_mm->size[1];
  n *= k;
  for (i15 = 0; i15 < n; i15++) {
    tor_mm->data[i15] += 14.0;
  }

  i15 = b_tor_mm->size[0] * b_tor_mm->size[1];
  b_tor_mm->size[0] = 1;
  b_tor_mm->size[1] = intHyper->size[1];
  emxEnsureCapacity_real_T(b_tor_mm, i15);
  n = intHyper->size[0] * intHyper->size[1];
  for (i15 = 0; i15 < n; i15++) {
    b_tor_mm->data[i15] = intHyper->data[i15] * 0.000115;
  }

  e_power(b_tor_mm, intHyper_mm);
  i15 = intHyper_mm->size[0] * intHyper_mm->size[1];
  intHyper_mm->size[0] = 1;
  emxEnsureCapacity_real_T(intHyper_mm, i15);
  n = intHyper_mm->size[0];
  k = intHyper_mm->size[1];
  n *= k;
  for (i15 = 0; i15 < n; i15++) {
    intHyper_mm->data[i15] += 14.0;
  }

  i15 = b_tor_mm->size[0] * b_tor_mm->size[1];
  b_tor_mm->size[0] = 1;
  b_tor_mm->size[1] = mbg->size[1];
  emxEnsureCapacity_real_T(b_tor_mm, i15);
  n = mbg->size[0] * mbg->size[1];
  for (i15 = 0; i15 < n; i15++) {
    b_tor_mm->data[i15] = (mbg->data[i15] - 90.0) * 0.0217;
  }

  emxInit_real_T1(&mbg_mm, 2);
  f_power(b_tor_mm, mbg_mm);
  i15 = mbg_mm->size[0] * mbg_mm->size[1];
  mbg_mm->size[0] = 1;
  emxEnsureCapacity_real_T(mbg_mm, i15);
  n = mbg_mm->size[0];
  k = mbg_mm->size[1];
  n *= k;
  for (i15 = 0; i15 < n; i15++) {
    mbg_mm->data[i15] += 14.0;
  }

  i15 = r28->size[0] * r28->size[1];
  r28->size[0] = 1;
  r28->size[1] = intHypo->size[1];
  emxEnsureCapacity_real_T(r28, i15);
  n = intHypo->size[0] * intHypo->size[1];
  for (i15 = 0; i15 < n; i15++) {
    r28->data[i15] = intHypo->data[i15] * 0.00057;
  }

  emxInit_real_T1(&axes, 2);
  b_exp(r28);
  i15 = axes->size[0] * axes->size[1];
  axes->size[0] = 5;
  axes->size[1] = tor_mm->size[1];
  emxEnsureCapacity_real_T(axes, i15);
  n = tor_mm->size[1];
  for (i15 = 0; i15 < n; i15++) {
    axes->data[axes->size[0] * i15] = tor_mm->data[tor_mm->size[0] * i15];
  }

  n = cv->size[1];
  for (i15 = 0; i15 < n; i15++) {
    axes->data[1 + axes->size[0] * i15] = (cv->data[cv->size[0] * i15] - 17.0) *
      0.92 + 14.0;
  }

  n = r28->size[1];
  for (i15 = 0; i15 < n; i15++) {
    axes->data[2 + axes->size[0] * i15] = r28->data[r28->size[0] * i15] + 13.0;
  }

  emxFree_real_T(&r28);
  n = intHyper_mm->size[1];
  for (i15 = 0; i15 < n; i15++) {
    axes->data[3 + axes->size[0] * i15] = intHyper_mm->data[intHyper_mm->size[0]
      * i15];
  }

  emxFree_real_T(&intHyper_mm);
  n = mbg_mm->size[1];
  for (i15 = 0; i15 < n; i15++) {
    axes->data[4 + axes->size[0] * i15] = mbg_mm->data[mbg_mm->size[0] * i15];
  }

  emxFree_real_T(&mbg_mm);
  emxInit_boolean_T1(&axes_nan, 2);
  i15 = axes_nan->size[0] * axes_nan->size[1];
  axes_nan->size[0] = 5;
  axes_nan->size[1] = axes->size[1];
  emxEnsureCapacity_boolean_T1(axes_nan, i15);
  n = axes->size[0] * axes->size[1];
  for (i15 = 0; i15 < n; i15++) {
    axes_nan->data[i15] = rtIsNaN(axes->data[i15]);
  }

  emxInit_real_T1(&x, 2);
  i15 = x->size[0] * x->size[1];
  x->size[0] = 5;
  x->size[1] = axes->size[1];
  emxEnsureCapacity_real_T(x, i15);
  n = axes->size[0] * axes->size[1];
  for (i15 = 0; i15 < n; i15++) {
    x->data[i15] = axes->data[i15];
  }

  emxInit_real_T1(&ztemp, 2);
  i15 = ztemp->size[0] * ztemp->size[1];
  ztemp->size[1] = axes->size[1];
  emxEnsureCapacity_real_T(ztemp, i15);
  n = axes->size[1];
  i15 = axes->size[0] * axes->size[1];
  axes->size[0] = 5;
  axes->size[1] = n;
  emxEnsureCapacity_real_T(axes, i15);
  n = 5 * ztemp->size[1];
  for (k = 0; k + 1 <= n; k++) {
    u0 = x->data[k];
    if (!(u0 < 76.0)) {
      u0 = 76.0;
    }

    axes->data[k] = u0;
  }

  k = 5 * axes_nan->size[1] - 1;
  n = 0;
  for (i = 0; i <= k; i++) {
    if (axes_nan->data[i]) {
      n++;
    }
  }

  emxInit_int32_T1(&r29, 1);
  i15 = r29->size[0];
  r29->size[0] = n;
  emxEnsureCapacity_int32_T1(r29, i15);
  n = 0;
  for (i = 0; i <= k; i++) {
    if (axes_nan->data[i]) {
      r29->data[n] = i + 1;
      n++;
    }
  }

  emxFree_boolean_T(&axes_nan);
  n = r29->size[0];
  for (i15 = 0; i15 < n; i15++) {
    axes->data[r29->data[i15] - 1] = rtNaN;
  }

  emxFree_int32_T(&r29);
  i15 = axes->size[1];
  if (i15 == 0) {
    result = 1;
  } else {
    i15 = axes->size[1];
    if (!(i15 == 0)) {
      result = 1;
    } else {
      result = 0;
    }
  }

  n = axes->size[1];
  k = axes->size[1];
  i = axes->size[1];
  i15 = pt->size[0] * pt->size[1];
  pt->size[0] = 4;
  pt->size[1] = i;
  emxEnsureCapacity_real_T(pt, i15);
  for (i15 = 0; i15 < i; i15++) {
    for (i16 = 0; i16 < 4; i16++) {
      pt->data[i16 + pt->size[0] * i15] = axes->data[(i16 + axes->size[0] * i15)
        + 1];
    }
  }

  i = axes->size[1];
  i15 = b_tor_mm->size[0] * b_tor_mm->size[1];
  b_tor_mm->size[0] = 1;
  b_tor_mm->size[1] = i;
  emxEnsureCapacity_real_T(b_tor_mm, i15);
  for (i15 = 0; i15 < i; i15++) {
    b_tor_mm->data[b_tor_mm->size[0] * i15] = axes->data[axes->size[0] * i15];
  }

  i15 = ztemp->size[0] * ztemp->size[1];
  ztemp->size[0] = 4 + result;
  ztemp->size[1] = n;
  emxEnsureCapacity_real_T(ztemp, i15);
  for (i15 = 0; i15 < n; i15++) {
    for (i16 = 0; i16 < 4; i16++) {
      ztemp->data[i16 + ztemp->size[0] * i15] = pt->data[i16 + (i15 << 2)];
    }
  }

  emxFree_real_T(&pt);
  for (i15 = 0; i15 < k; i15++) {
    for (i16 = 0; i16 < result; i16++) {
      ztemp->data[(i16 + ztemp->size[0] * i15) + 4] = b_tor_mm->data[i16 +
        result * i15];
    }
  }

  emxFree_real_T(&b_tor_mm);
  i15 = x->size[0] * x->size[1];
  x->size[0] = 5;
  x->size[1] = axes->size[1];
  emxEnsureCapacity_real_T(x, i15);
  n = axes->size[1];
  for (i15 = 0; i15 < n; i15++) {
    for (i16 = 0; i16 < 5; i16++) {
      x->data[i16 + x->size[0] * i15] = 0.5 * axes->data[i16 + axes->size[0] *
        i15] * ztemp->data[i16 + ztemp->size[0] * i15] * 0.9510565;
    }
  }

  emxFree_real_T(&ztemp);
  i15 = tor_mm->size[0] * tor_mm->size[1];
  tor_mm->size[0] = 1;
  tor_mm->size[1] = x->size[1];
  emxEnsureCapacity_real_T(tor_mm, i15);
  for (i = 0; i + 1 <= x->size[1]; i++) {
    n = i * 5;
    tor_mm->data[i] = x->data[n];
    for (k = 0; k < 4; k++) {
      tor_mm->data[i] += x->data[(n + k) + 1];
    }
  }

  emxFree_real_T(&x);
  if (!(tor->size[1] == 0)) {
    result = tor->size[1];
  } else if (!(cv->size[1] == 0)) {
    result = cv->size[1];
  } else if (!(intHypo->size[1] == 0)) {
    result = intHypo->size[1];
  } else if (!(intHyper->size[1] == 0)) {
    result = intHyper->size[1];
  } else if (!(mbg->size[1] == 0)) {
    result = mbg->size[1];
  } else {
    result = axes->size[1];
  }

  empty_non_axis_sizes = (result == 0);
  if (empty_non_axis_sizes || (!(tor->size[1] == 0))) {
    n = 1;
  } else {
    n = 0;
  }

  if (empty_non_axis_sizes || (!(cv->size[1] == 0))) {
    k = 1;
  } else {
    k = 0;
  }

  if (empty_non_axis_sizes || (!(intHypo->size[1] == 0))) {
    i = 1;
  } else {
    i = 0;
  }

  if (empty_non_axis_sizes || (!(intHyper->size[1] == 0))) {
    b_result = 1;
  } else {
    b_result = 0;
  }

  if (empty_non_axis_sizes || (!(mbg->size[1] == 0))) {
    c_result = 1;
  } else {
    c_result = 0;
  }

  if (empty_non_axis_sizes || (!(tor_mm->size[1] == 0))) {
    d_result = 1;
  } else {
    d_result = 0;
  }

  i15 = pentagon->size[0] * pentagon->size[1];
  pentagon->size[0] = (((((n + k) + i) + b_result) + c_result) + d_result) + 5;
  pentagon->size[1] = result;
  emxEnsureCapacity_real_T(pentagon, i15);
  for (i15 = 0; i15 < result; i15++) {
    for (i16 = 0; i16 < n; i16++) {
      pentagon->data[i16 + pentagon->size[0] * i15] = tor->data[i16 + n * i15];
    }
  }

  emxFree_real_T(&tor);
  for (i15 = 0; i15 < result; i15++) {
    for (i16 = 0; i16 < k; i16++) {
      pentagon->data[(i16 + n) + pentagon->size[0] * i15] = cv->data[i16 + k *
        i15];
    }
  }

  for (i15 = 0; i15 < result; i15++) {
    for (i16 = 0; i16 < i; i16++) {
      pentagon->data[((i16 + n) + k) + pentagon->size[0] * i15] = intHypo->
        data[i16 + i * i15];
    }
  }

  emxFree_real_T(&intHypo);
  for (i15 = 0; i15 < result; i15++) {
    for (i16 = 0; i16 < b_result; i16++) {
      pentagon->data[(((i16 + n) + k) + i) + pentagon->size[0] * i15] =
        intHyper->data[i16 + b_result * i15];
    }
  }

  emxFree_real_T(&intHyper);
  for (i15 = 0; i15 < result; i15++) {
    for (i16 = 0; i16 < c_result; i16++) {
      pentagon->data[((((i16 + n) + k) + i) + b_result) + pentagon->size[0] *
        i15] = mbg->data[i16 + c_result * i15];
    }
  }

  for (i15 = 0; i15 < result; i15++) {
    for (i16 = 0; i16 < 5; i16++) {
      pentagon->data[(((((i16 + n) + k) + i) + b_result) + c_result) +
        pentagon->size[0] * i15] = axes->data[i16 + 5 * i15];
    }
  }

  emxFree_real_T(&axes);
  for (i15 = 0; i15 < result; i15++) {
    for (i16 = 0; i16 < d_result; i16++) {
      pentagon->data[((((((i16 + n) + k) + i) + b_result) + c_result) +
                      pentagon->size[0] * i15) + 5] = tor_mm->data[i16 +
        d_result * i15];
    }
  }

  emxFree_real_T(&tor_mm);
}

/*
 * File trailer for Pentagon.c
 *
 * [EOF]
 */
