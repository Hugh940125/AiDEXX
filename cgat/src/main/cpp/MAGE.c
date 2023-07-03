/*
 * Academic License - for use in teaching, academic research, and meeting
 * course requirements at degree granting institutions only.  Not for
 * government, commercial, or other organizational use.
 * File: MAGE.c
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
#include "diff.h"
#include "sign.h"
#include "nullAssignment.h"
#include "nanstd.h"

/* Function Declarations */
static int div_s32_floor(int numerator, int denominator);
static void peaks(emxArray_real_T *tm, emxArray_real_T *sg, emxArray_boolean_T
                  *e, emxArray_real_T *tm1, emxArray_real_T *sg1);

/* Function Definitions */

/*
 * Arguments    : int numerator
 *                int denominator
 * Return Type  : int
 */
static int div_s32_floor(int numerator, int denominator)
{
  int quotient;
  unsigned int absNumerator;
  unsigned int absDenominator;
  boolean_T quotientNeedsNegation;
  unsigned int tempAbsQuotient;
  if (denominator == 0) {
    if (numerator >= 0) {
      quotient = MAX_int32_T;
    } else {
      quotient = MIN_int32_T;
    }
  } else {
    if (numerator < 0) {
      absNumerator = ~(unsigned int)numerator + 1U;
    } else {
      absNumerator = (unsigned int)numerator;
    }

    if (denominator < 0) {
      absDenominator = ~(unsigned int)denominator + 1U;
    } else {
      absDenominator = (unsigned int)denominator;
    }

    quotientNeedsNegation = ((numerator < 0) != (denominator < 0));
    tempAbsQuotient = absNumerator / absDenominator;
    if (quotientNeedsNegation) {
      absNumerator %= absDenominator;
      if (absNumerator > 0U) {
        tempAbsQuotient++;
      }

      quotient = -(int)tempAbsQuotient;
    } else {
      quotient = (int)tempAbsQuotient;
    }
  }

  return quotient;
}

/*
 * Arguments    : emxArray_real_T *tm
 *                emxArray_real_T *sg
 *                emxArray_boolean_T *e
 *                emxArray_real_T *tm1
 *                emxArray_real_T *sg1
 * Return Type  : void
 */
static void peaks(emxArray_real_T *tm, emxArray_real_T *sg, emxArray_boolean_T
                  *e, emxArray_real_T *tm1, emxArray_real_T *sg1)
{
  emxArray_boolean_T *p;
  emxArray_real_T *r23;
  emxArray_real_T *r24;
  int i;
  int loop_ub;
  emxArray_boolean_T *v;
  emxArray_boolean_T *r25;
  int end;
  emxInit_boolean_T(&p, 1);
  emxInit_real_T(&r23, 1);
  emxInit_real_T(&r24, 1);

  /*  mageu = nanmean(mageu); */
  /*  maged = nanmean(maged); */
  e->data[0] = false;
  e->data[e->size[0] - 1] = false;
  nullAssignment(tm, e);
  nullAssignment(sg, e);
  diff(sg, r23);
  b_sign(r23);
  diff(r23, r24);
  i = p->size[0];
  p->size[0] = r24->size[0];
  emxEnsureCapacity_boolean_T(p, i);
  loop_ub = r24->size[0];
  for (i = 0; i < loop_ub; i++) {
    p->data[i] = (r24->data[i] == -2.0);
  }

  emxInit_boolean_T(&v, 1);
  diff(sg, r23);
  b_sign(r23);
  diff(r23, r24);
  i = v->size[0];
  v->size[0] = r24->size[0];
  emxEnsureCapacity_boolean_T(v, i);
  loop_ub = r24->size[0];
  emxFree_real_T(&r23);
  for (i = 0; i < loop_ub; i++) {
    v->data[i] = (r24->data[i] == 2.0);
  }

  emxFree_real_T(&r24);
  emxInit_boolean_T(&r25, 1);
  i = r25->size[0];
  r25->size[0] = 2 + p->size[0];
  emxEnsureCapacity_boolean_T(r25, i);
  r25->data[0] = true;
  loop_ub = p->size[0];
  for (i = 0; i < loop_ub; i++) {
    r25->data[i + 1] = (p->data[i] || v->data[i]);
  }

  r25->data[1 + p->size[0]] = true;
  end = r25->size[0] - 1;
  loop_ub = 0;
  for (i = 0; i <= end; i++) {
    if (r25->data[i]) {
      loop_ub++;
    }
  }

  i = sg1->size[0];
  sg1->size[0] = loop_ub;
  emxEnsureCapacity_real_T1(sg1, i);
  loop_ub = 0;
  for (i = 0; i <= end; i++) {
    if (r25->data[i]) {
      sg1->data[loop_ub] = sg->data[i];
      loop_ub++;
    }
  }

  i = r25->size[0];
  r25->size[0] = 2 + p->size[0];
  emxEnsureCapacity_boolean_T(r25, i);
  r25->data[0] = true;
  loop_ub = p->size[0];
  for (i = 0; i < loop_ub; i++) {
    r25->data[i + 1] = (p->data[i] || v->data[i]);
  }

  emxFree_boolean_T(&v);
  r25->data[1 + p->size[0]] = true;
  end = r25->size[0] - 1;
  loop_ub = 0;
  emxFree_boolean_T(&p);
  for (i = 0; i <= end; i++) {
    if (r25->data[i]) {
      loop_ub++;
    }
  }

  i = tm1->size[0];
  tm1->size[0] = loop_ub;
  emxEnsureCapacity_real_T1(tm1, i);
  loop_ub = 0;
  for (i = 0; i <= end; i++) {
    if (r25->data[i]) {
      tm1->data[loop_ub] = tm->data[i];
      loop_ub++;
    }
  }

  emxFree_boolean_T(&r25);
}

/*
 * Arguments    : const emxArray_real_T *sg
 *                double nv
 *                emxArray_real_T *mage
 * Return Type  : void
 */
void MAGE(const emxArray_real_T *sg, double nv, emxArray_real_T *mage)
{
  emxArray_real_T *ssg;
  double duration;
  int i10;
  int n;
  int i;
  emxArray_real_T *sg1;
  emxArray_boolean_T *e;
  emxArray_real_T *i1;
  emxArray_real_T *i2;
  emxArray_real_T *tm1;
  emxArray_real_T *b_sg1;
  emxArray_boolean_T *r16;
  emxArray_uint32_T *y;
  emxArray_boolean_T *x;
  emxArray_int32_T *ii;
  emxArray_int32_T *r17;
  emxArray_int32_T *r18;
  emxArray_int32_T *r19;
  emxArray_real_T *b_y;
  emxArray_boolean_T *r20;
  emxArray_boolean_T *b_e;
  emxArray_boolean_T *c_e;
  unsigned int u0;
  double v;
  int nx;
  int idx;
  boolean_T exitg1;
  int i11;
  double b_v;
  int itmp;
  int ix;
  int b_itmp;
  int c_itmp[2];
  double d0;
  double d1;
  emxInit_real_T1(&ssg, 2);
  duration = (double)sg->size[0] / 24.0;
  b_SAFilter(sg, ssg);
  i10 = mage->size[0] * mage->size[1];
  mage->size[0] = 3;
  mage->size[1] = sg->size[1] + 1;
  emxEnsureCapacity_real_T(mage, i10);
  n = 3 * (sg->size[1] + 1);
  for (i10 = 0; i10 < n; i10++) {
    mage->data[i10] = rtNaN;
  }

  i = 0;
  emxInit_real_T(&sg1, 1);
  emxInit_boolean_T(&e, 1);
  emxInit_real_T(&i1, 1);
  emxInit_real_T(&i2, 1);
  emxInit_real_T(&tm1, 1);
  emxInit_real_T(&b_sg1, 1);
  emxInit_boolean_T(&r16, 1);
  emxInit_uint32_T(&y, 2);
  emxInit_boolean_T(&x, 1);
  emxInit_int32_T1(&ii, 1);
  emxInit_int32_T1(&r17, 1);
  emxInit_int32_T1(&r18, 1);
  emxInit_int32_T1(&r19, 1);
  emxInit_real_T(&b_y, 1);
  emxInit_boolean_T(&r20, 1);
  emxInit_boolean_T(&b_e, 1);
  emxInit_boolean_T(&c_e, 1);
  while (i <= sg->size[1]) {
    if (1.0 + (double)i > sg->size[1]) {
      i10 = sg1->size[0];
      sg1->size[0] = ssg->size[0] * ssg->size[1];
      emxEnsureCapacity_real_T1(sg1, i10);
      n = ssg->size[0] * ssg->size[1];
      for (i10 = 0; i10 < n; i10++) {
        sg1->data[i10] = ssg->data[i10];
      }
    } else {
      n = ssg->size[0];
      i10 = sg1->size[0];
      sg1->size[0] = n;
      emxEnsureCapacity_real_T1(sg1, i10);
      for (i10 = 0; i10 < n; i10++) {
        sg1->data[i10] = ssg->data[i10 + ssg->size[0] * i];
      }
    }

    if (sg1->size[0] < 1) {
      i10 = y->size[0] * y->size[1];
      y->size[0] = 1;
      y->size[1] = 0;
      emxEnsureCapacity_uint32_T(y, i10);
    } else {
      u0 = sg1->size[0] + MAX_uint32_T;
      i10 = y->size[0] * y->size[1];
      y->size[0] = 1;
      y->size[1] = (int)u0 + 1;
      emxEnsureCapacity_uint32_T(y, i10);
      n = (int)u0;
      for (i10 = 0; i10 <= n; i10++) {
        y->data[y->size[0] * i10] = 1U + i10;
      }
    }

    v = nanstd(sg1) * nv;

    /*      figure; */
    /*      hold on */
    /*      plot(tm,sg1,'linewidth',1); */
    diff(sg1, b_y);
    i10 = r16->size[0];
    r16->size[0] = b_y->size[0] + 1;
    emxEnsureCapacity_boolean_T(r16, i10);
    n = b_y->size[0];
    for (i10 = 0; i10 < n; i10++) {
      r16->data[i10] = (b_y->data[i10] == 0.0);
    }

    r16->data[b_y->size[0]] = false;
    i10 = x->size[0];
    x->size[0] = sg1->size[0];
    emxEnsureCapacity_boolean_T(x, i10);
    n = sg1->size[0];
    for (i10 = 0; i10 < n; i10++) {
      x->data[i10] = rtIsNaN(sg1->data[i10]);
    }

    i10 = b_y->size[0];
    b_y->size[0] = y->size[1];
    emxEnsureCapacity_real_T1(b_y, i10);
    n = y->size[1];
    for (i10 = 0; i10 < n; i10++) {
      b_y->data[i10] = y->data[y->size[0] * i10];
    }

    i10 = r20->size[0];
    r20->size[0] = r16->size[0];
    emxEnsureCapacity_boolean_T(r20, i10);
    n = r16->size[0];
    for (i10 = 0; i10 < n; i10++) {
      r20->data[i10] = (r16->data[i10] || x->data[i10]);
    }

    peaks(b_y, sg1, r20, tm1, b_sg1);

    /*      plot(tm1,sg1,':o','markersize',2,'linewidth',2); */
    diff(b_sg1, b_y);
    b_abs(b_y, i2);
    i10 = e->size[0];
    e->size[0] = i2->size[0];
    emxEnsureCapacity_boolean_T(e, i10);
    n = i2->size[0];
    for (i10 = 0; i10 < n; i10++) {
      e->data[i10] = (i2->data[i10] < v);
    }

    i10 = b_e->size[0];
    b_e->size[0] = e->size[0] + 1;
    emxEnsureCapacity_boolean_T(b_e, i10);
    n = e->size[0];
    for (i10 = 0; i10 < n; i10++) {
      b_e->data[i10] = e->data[i10];
    }

    b_e->data[e->size[0]] = false;
    i10 = r20->size[0];
    r20->size[0] = 1 + e->size[0];
    emxEnsureCapacity_boolean_T(r20, i10);
    r20->data[0] = false;
    n = e->size[0];
    for (i10 = 0; i10 < n; i10++) {
      r20->data[i10 + 1] = e->data[i10];
    }

    i10 = e->size[0];
    e->size[0] = b_e->size[0];
    emxEnsureCapacity_boolean_T(e, i10);
    n = b_e->size[0];
    for (i10 = 0; i10 < n; i10++) {
      e->data[i10] = (b_e->data[i10] && r20->data[i10]);
    }

    b_diff(e, b_y);
    i10 = x->size[0];
    x->size[0] = b_y->size[0];
    emxEnsureCapacity_boolean_T(x, i10);
    n = b_y->size[0];
    for (i10 = 0; i10 < n; i10++) {
      x->data[i10] = (b_y->data[i10] == 1.0);
    }

    nx = x->size[0];
    idx = 0;
    i10 = ii->size[0];
    ii->size[0] = x->size[0];
    emxEnsureCapacity_int32_T1(ii, i10);
    n = 1;
    exitg1 = false;
    while ((!exitg1) && (n <= nx)) {
      if (x->data[n - 1]) {
        idx++;
        ii->data[idx - 1] = n;
        if (idx >= nx) {
          exitg1 = true;
        } else {
          n++;
        }
      } else {
        n++;
      }
    }

    if (x->size[0] == 1) {
      if (idx == 0) {
        i10 = ii->size[0];
        ii->size[0] = 0;
        emxEnsureCapacity_int32_T1(ii, i10);
      }
    } else {
      i10 = ii->size[0];
      if (1 > idx) {
        ii->size[0] = 0;
      } else {
        ii->size[0] = idx;
      }

      emxEnsureCapacity_int32_T1(ii, i10);
    }

    i10 = i1->size[0];
    i1->size[0] = ii->size[0];
    emxEnsureCapacity_real_T1(i1, i10);
    n = ii->size[0];
    for (i10 = 0; i10 < n; i10++) {
      i1->data[i10] = (double)ii->data[i10] + 1.0;
    }

    b_diff(e, b_y);
    i10 = x->size[0];
    x->size[0] = b_y->size[0];
    emxEnsureCapacity_boolean_T(x, i10);
    n = b_y->size[0];
    for (i10 = 0; i10 < n; i10++) {
      x->data[i10] = (b_y->data[i10] == -1.0);
    }

    nx = x->size[0];
    idx = 0;
    i10 = ii->size[0];
    ii->size[0] = x->size[0];
    emxEnsureCapacity_int32_T1(ii, i10);
    n = 1;
    exitg1 = false;
    while ((!exitg1) && (n <= nx)) {
      if (x->data[n - 1]) {
        idx++;
        ii->data[idx - 1] = n;
        if (idx >= nx) {
          exitg1 = true;
        } else {
          n++;
        }
      } else {
        n++;
      }
    }

    if (x->size[0] == 1) {
      if (idx == 0) {
        i10 = ii->size[0];
        ii->size[0] = 0;
        emxEnsureCapacity_int32_T1(ii, i10);
      }
    } else {
      i10 = ii->size[0];
      if (1 > idx) {
        ii->size[0] = 0;
      } else {
        ii->size[0] = idx;
      }

      emxEnsureCapacity_int32_T1(ii, i10);
    }

    i10 = i2->size[0];
    i2->size[0] = ii->size[0];
    emxEnsureCapacity_real_T1(i2, i10);
    n = ii->size[0];
    for (i10 = 0; i10 < n; i10++) {
      i2->data[i10] = ii->data[i10];
    }

    for (nx = 0; nx < i1->size[0]; nx++) {
      if (i1->data[nx] > i2->data[nx]) {
        i10 = 0;
        i11 = 0;
      } else {
        i10 = (int)i1->data[nx] - 1;
        i11 = (int)i2->data[nx];
      }

      idx = 1;
      n = i11 - i10;
      b_v = b_sg1->data[i10];
      itmp = 1;
      if (i11 - i10 > 1) {
        if (rtIsNaN(b_v)) {
          ix = 2;
          exitg1 = false;
          while ((!exitg1) && (ix <= n)) {
            idx = ix;
            if (!rtIsNaN(b_sg1->data[(i10 + ix) - 1])) {
              b_v = b_sg1->data[(i10 + ix) - 1];
              itmp = ix;
              exitg1 = true;
            } else {
              ix++;
            }
          }
        }

        if (idx < i11 - i10) {
          for (ix = idx + 1; ix <= n; ix++) {
            if (b_sg1->data[(i10 + ix) - 1] > b_v) {
              b_v = b_sg1->data[(i10 + ix) - 1];
              itmp = ix;
            }
          }
        }
      }

      if (i1->data[nx] > i2->data[nx]) {
        i10 = 0;
        i11 = 0;
      } else {
        i10 = (int)i1->data[nx] - 1;
        i11 = (int)i2->data[nx];
      }

      idx = 1;
      n = i11 - i10;
      b_v = b_sg1->data[i10];
      b_itmp = 1;
      if (i11 - i10 > 1) {
        if (rtIsNaN(b_v)) {
          ix = 2;
          exitg1 = false;
          while ((!exitg1) && (ix <= n)) {
            idx = ix;
            if (!rtIsNaN(b_sg1->data[(i10 + ix) - 1])) {
              b_v = b_sg1->data[(i10 + ix) - 1];
              b_itmp = ix;
              exitg1 = true;
            } else {
              ix++;
            }
          }
        }

        if (idx < i11 - i10) {
          for (ix = idx + 1; ix <= n; ix++) {
            if (b_sg1->data[(i10 + ix) - 1] < b_v) {
              b_v = b_sg1->data[(i10 + ix) - 1];
              b_itmp = ix;
            }
          }
        }
      }

      c_itmp[0] = (int)(((double)itmp + i1->data[nx]) - 1.0) - 1;
      c_itmp[1] = (int)(((double)b_itmp + i1->data[nx]) - 1.0) - 1;
      for (i10 = 0; i10 < 2; i10++) {
        e->data[c_itmp[i10]] = false;
      }
    }

    peaks(tm1, b_sg1, e, i1, sg1);

    /*  plot(tm1,sg1,':o','markersize',2,'linewidth',2); */
    diff(sg1, b_y);
    b_abs(b_y, i2);
    b_v = v * 0.5;
    i10 = e->size[0];
    e->size[0] = i2->size[0];
    emxEnsureCapacity_boolean_T(e, i10);
    n = i2->size[0];
    for (i10 = 0; i10 < n; i10++) {
      e->data[i10] = (i2->data[i10] < b_v);
    }

    diff(i1, b_y);
    b_abs(b_y, i2);
    i10 = r16->size[0];
    r16->size[0] = i2->size[0];
    emxEnsureCapacity_boolean_T(r16, i10);
    n = i2->size[0];
    for (i10 = 0; i10 < n; i10++) {
      r16->data[i10] = (i2->data[i10] < duration);
    }

    i10 = e->size[0];
    emxEnsureCapacity_boolean_T(e, i10);
    n = e->size[0];
    for (i10 = 0; i10 < n; i10++) {
      e->data[i10] = (e->data[i10] && r16->data[i10]);
    }

    i10 = c_e->size[0];
    c_e->size[0] = e->size[0] + 1;
    emxEnsureCapacity_boolean_T(c_e, i10);
    n = e->size[0];
    for (i10 = 0; i10 < n; i10++) {
      c_e->data[i10] = e->data[i10];
    }

    c_e->data[e->size[0]] = false;
    i10 = r20->size[0];
    r20->size[0] = 1 + e->size[0];
    emxEnsureCapacity_boolean_T(r20, i10);
    r20->data[0] = false;
    n = e->size[0];
    for (i10 = 0; i10 < n; i10++) {
      r20->data[i10 + 1] = e->data[i10];
    }

    i10 = b_e->size[0];
    b_e->size[0] = c_e->size[0];
    emxEnsureCapacity_boolean_T(b_e, i10);
    n = c_e->size[0];
    for (i10 = 0; i10 < n; i10++) {
      b_e->data[i10] = (c_e->data[i10] && r20->data[i10]);
    }

    peaks(i1, sg1, b_e, tm1, b_sg1);

    /*  plot(tm1,sg1,':o','markersize',2,'linewidth',2); */
    diff(b_sg1, b_y);
    b_abs(b_y, i2);
    b_v = v * 0.5;
    i10 = e->size[0];
    e->size[0] = i2->size[0];
    emxEnsureCapacity_boolean_T(e, i10);
    n = i2->size[0];
    for (i10 = 0; i10 < n; i10++) {
      e->data[i10] = (i2->data[i10] < b_v);
    }

    diff(tm1, b_y);
    b_abs(b_y, i2);
    i10 = r16->size[0];
    r16->size[0] = i2->size[0];
    emxEnsureCapacity_boolean_T(r16, i10);
    n = i2->size[0];
    for (i10 = 0; i10 < n; i10++) {
      r16->data[i10] = (i2->data[i10] < duration);
    }

    i10 = e->size[0];
    emxEnsureCapacity_boolean_T(e, i10);
    n = e->size[0];
    for (i10 = 0; i10 < n; i10++) {
      e->data[i10] = (e->data[i10] && r16->data[i10]);
    }

    i10 = c_e->size[0];
    c_e->size[0] = e->size[0] + 1;
    emxEnsureCapacity_boolean_T(c_e, i10);
    n = e->size[0];
    for (i10 = 0; i10 < n; i10++) {
      c_e->data[i10] = e->data[i10];
    }

    c_e->data[e->size[0]] = false;
    i10 = r20->size[0];
    r20->size[0] = 1 + e->size[0];
    emxEnsureCapacity_boolean_T(r20, i10);
    r20->data[0] = false;
    n = e->size[0];
    for (i10 = 0; i10 < n; i10++) {
      r20->data[i10 + 1] = e->data[i10];
    }

    i10 = b_e->size[0];
    b_e->size[0] = c_e->size[0];
    emxEnsureCapacity_boolean_T(b_e, i10);
    n = c_e->size[0];
    for (i10 = 0; i10 < n; i10++) {
      b_e->data[i10] = (c_e->data[i10] || r20->data[i10]);
    }

    peaks(tm1, b_sg1, b_e, i1, sg1);

    /*      plot(tm1,sg1,':o','markersize',2,'linewidth',2); */
    diff(sg1, i1);
    b_abs(i1, i2);
    if (1 > i2->size[0]) {
      i10 = 1;
      i11 = -1;
    } else {
      i10 = 2;
      i11 = i2->size[0] - 1;
    }

    if (2 > i2->size[0]) {
      itmp = 1;
      ix = 1;
      b_itmp = 0;
    } else {
      itmp = 2;
      ix = 2;
      b_itmp = i2->size[0];
    }

    n = i2->size[0] - 1;
    nx = 0;
    for (idx = 0; idx <= n; idx++) {
      if (i2->data[idx] > v) {
        nx++;
      }
    }

    idx = r17->size[0];
    r17->size[0] = nx;
    emxEnsureCapacity_int32_T1(r17, idx);
    nx = 0;
    for (idx = 0; idx <= n; idx++) {
      if (i2->data[idx] > v) {
        r17->data[nx] = idx + 1;
        nx++;
      }
    }

    idx = b_y->size[0];
    b_y->size[0] = r17->size[0];
    emxEnsureCapacity_real_T1(b_y, idx);
    n = r17->size[0];
    for (idx = 0; idx < n; idx++) {
      b_y->data[idx] = i2->data[r17->data[idx] - 1];
    }

    d0 = c_nanmean(b_y);
    n = div_s32_floor(i11, i10);
    nx = 0;
    for (idx = 0; idx <= n; idx++) {
      if (i2->data[i10 * idx] > v) {
        nx++;
      }
    }

    i11 = r18->size[0];
    r18->size[0] = nx;
    emxEnsureCapacity_int32_T1(r18, i11);
    nx = 0;
    for (idx = 0; idx <= n; idx++) {
      if (i2->data[i10 * idx] > v) {
        r18->data[nx] = idx + 1;
        nx++;
      }
    }

    i11 = tm1->size[0];
    tm1->size[0] = r18->size[0];
    emxEnsureCapacity_real_T1(tm1, i11);
    n = r18->size[0];
    for (i11 = 0; i11 < n; i11++) {
      tm1->data[i11] = i2->data[i10 * (r18->data[i11] - 1)];
    }

    i10 = b_y->size[0];
    b_y->size[0] = tm1->size[0];
    emxEnsureCapacity_real_T1(b_y, i10);
    n = tm1->size[0];
    for (i10 = 0; i10 < n; i10++) {
      b_y->data[i10] = tm1->data[i10];
    }

    d1 = c_nanmean(b_y);
    n = div_s32_floor(b_itmp - itmp, ix);
    nx = 0;
    for (idx = 0; idx <= n; idx++) {
      if (i2->data[(itmp + ix * idx) - 1] > v) {
        nx++;
      }
    }

    i10 = r19->size[0];
    r19->size[0] = nx;
    emxEnsureCapacity_int32_T1(r19, i10);
    nx = 0;
    for (idx = 0; idx <= n; idx++) {
      if (i2->data[(itmp + ix * idx) - 1] > v) {
        r19->data[nx] = idx + 1;
        nx++;
      }
    }

    i10 = b_y->size[0];
    b_y->size[0] = r19->size[0];
    emxEnsureCapacity_real_T1(b_y, i10);
    n = r19->size[0];
    for (i10 = 0; i10 < n; i10++) {
      b_y->data[i10] = i2->data[(itmp + ix * (r19->data[i10] - 1)) - 1];
    }

    i10 = i2->size[0];
    i2->size[0] = b_y->size[0];
    emxEnsureCapacity_real_T1(i2, i10);
    n = b_y->size[0];
    for (i10 = 0; i10 < n; i10++) {
      i2->data[i10] = b_y->data[i10];
    }

    b_v = c_nanmean(i2);
    if (i1->data[0] < 0.0) {
      v = d1;
      d1 = b_v;
      b_v = v;
    }

    mage->data[mage->size[0] * i] = d0;
    mage->data[1 + mage->size[0] * i] = d1;
    mage->data[2 + mage->size[0] * i] = b_v;
    i++;
  }

  emxFree_boolean_T(&c_e);
  emxFree_boolean_T(&b_e);
  emxFree_boolean_T(&r20);
  emxFree_real_T(&b_y);
  emxFree_int32_T(&r19);
  emxFree_int32_T(&r18);
  emxFree_int32_T(&r17);
  emxFree_int32_T(&ii);
  emxFree_boolean_T(&x);
  emxFree_uint32_T(&y);
  emxFree_boolean_T(&r16);
  emxFree_real_T(&b_sg1);
  emxFree_real_T(&tm1);
  emxFree_real_T(&i2);
  emxFree_real_T(&i1);
  emxFree_boolean_T(&e);
  emxFree_real_T(&sg1);
  emxFree_real_T(&ssg);
}

/*
 * File trailer for MAGE.c
 *
 * [EOF]
 */
