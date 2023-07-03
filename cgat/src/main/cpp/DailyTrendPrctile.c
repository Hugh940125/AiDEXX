/*
 * Academic License - for use in teaching, academic research, and meeting
 * course requirements at degree granting institutions only.  Not for
 * government, commercial, or other organizational use.
 * File: DailyTrendPrctile.c
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
#include "prctile.h"

/* Function Definitions */

/*
 * Arguments    : const emxArray_real_T *sg
 *                double pct
 *                emxArray_real_T *sgp
 * Return Type  : void
 */
void DailyTrendPrctile(const emxArray_real_T *sg, double pct, emxArray_real_T
  *sgp)
{
  emxArray_real_T *sgp0;
  int L;
  int i5;
  int nm1d2;
  double dv0[2];
  emxArray_int32_T *r7;
  emxArray_boolean_T *r8;
  emxArray_real_T *y;
  emxArray_real_T *b_y;
  emxArray_real_T *b_sgp0;
  emxArray_int32_T *c_y;
  int N;
  double b_N;
  int i;
  double a;
  double ndbl;
  double apnd;
  int end;
  double cdiff;
  double absa;
  double u1;
  int n;
  emxInit_real_T(&sgp0, 1);
  L = sg->size[0];
  percentile_array(sg, pct, sgp0);
  i5 = sgp->size[0];
  sgp->size[0] = sgp0->size[0];
  emxEnsureCapacity_real_T1(sgp, i5);
  nm1d2 = sgp0->size[0];
  for (i5 = 0; i5 < nm1d2; i5++) {
    sgp->data[i5] = sgp0->data[i5];
  }

  dv0[0] = (double)sg->size[0] / 48.0;
  dv0[1] = 3.0;
  emxInit_int32_T1(&r7, 1);
  emxInit_boolean_T(&r8, 1);
  emxInit_real_T1(&y, 2);
  emxInit_real_T1(&b_y, 2);
  emxInit_real_T(&b_sgp0, 1);
  emxInit_int32_T1(&c_y, 1);
  for (N = 0; N < 2; N++) {
    b_N = dv0[N];
    for (i = 0; i < L; i++) {
      if (1.0 + (double)i <= b_N) {
        a = (double)(((unsigned int)i + L) + 1U) - b_N;
        if (L < a) {
          i5 = y->size[0] * y->size[1];
          y->size[0] = 1;
          y->size[1] = 0;
          emxEnsureCapacity_real_T(y, i5);
        } else if (floor(a) == a) {
          i5 = y->size[0] * y->size[1];
          y->size[0] = 1;
          y->size[1] = (int)floor((double)L - a) + 1;
          emxEnsureCapacity_real_T(y, i5);
          nm1d2 = (int)floor((double)L - a);
          for (i5 = 0; i5 <= nm1d2; i5++) {
            y->data[y->size[0] * i5] = a + (double)i5;
          }
        } else {
          ndbl = floor(((double)L - a) + 0.5);
          apnd = a + ndbl;
          cdiff = apnd - (double)L;
          absa = fabs(a);
          u1 = L;
          if (absa > u1) {
            u1 = absa;
          }

          if (fabs(cdiff) < 4.4408920985006262E-16 * u1) {
            ndbl++;
            apnd = L;
          } else if (cdiff > 0.0) {
            apnd = a + (ndbl - 1.0);
          } else {
            ndbl++;
          }

          if (ndbl >= 0.0) {
            n = (int)ndbl;
          } else {
            n = 0;
          }

          i5 = y->size[0] * y->size[1];
          y->size[0] = 1;
          y->size[1] = n;
          emxEnsureCapacity_real_T(y, i5);
          if (n > 0) {
            y->data[0] = a;
            if (n > 1) {
              y->data[n - 1] = apnd;
              nm1d2 = (n - 1) / 2;
              for (end = 1; end < nm1d2; end++) {
                y->data[end] = a + (double)end;
                y->data[(n - end) - 1] = apnd - (double)end;
              }

              if (nm1d2 << 1 == n - 1) {
                y->data[nm1d2] = (a + apnd) / 2.0;
              } else {
                y->data[nm1d2] = a + (double)nm1d2;
                y->data[nm1d2 + 1] = apnd - (double)nm1d2;
              }
            }
          }
        }

        a = (1.0 + (double)i) + b_N;
        i5 = b_y->size[0] * b_y->size[1];
        b_y->size[0] = 1;
        b_y->size[1] = (int)floor(a - 1.0) + 1;
        emxEnsureCapacity_real_T(b_y, i5);
        nm1d2 = (int)floor(a - 1.0);
        for (i5 = 0; i5 <= nm1d2; i5++) {
          b_y->data[b_y->size[0] * i5] = 1.0 + (double)i5;
        }

        i5 = c_y->size[0];
        c_y->size[0] = y->size[1] + b_y->size[1];
        emxEnsureCapacity_int32_T1(c_y, i5);
        nm1d2 = y->size[1];
        for (i5 = 0; i5 < nm1d2; i5++) {
          c_y->data[i5] = (int)y->data[y->size[0] * i5] - 1;
        }

        nm1d2 = b_y->size[1];
        for (i5 = 0; i5 < nm1d2; i5++) {
          c_y->data[i5 + y->size[1]] = (int)b_y->data[b_y->size[0] * i5] - 1;
        }

        i5 = b_sgp0->size[0];
        b_sgp0->size[0] = y->size[1] + b_y->size[1];
        emxEnsureCapacity_real_T1(b_sgp0, i5);
        nm1d2 = y->size[1] + b_y->size[1];
        for (i5 = 0; i5 < nm1d2; i5++) {
          b_sgp0->data[i5] = sgp0->data[c_y->data[i5]];
        }

        sgp->data[i] = c_nanmean(b_sgp0);
      } else if (1.0 + (double)i > (double)L - b_N) {
        a = (1.0 + (double)i) - b_N;
        if (L < a) {
          i5 = y->size[0] * y->size[1];
          y->size[0] = 1;
          y->size[1] = 0;
          emxEnsureCapacity_real_T(y, i5);
        } else if (floor(a) == a) {
          i5 = y->size[0] * y->size[1];
          y->size[0] = 1;
          y->size[1] = (int)floor((double)L - a) + 1;
          emxEnsureCapacity_real_T(y, i5);
          nm1d2 = (int)floor((double)L - a);
          for (i5 = 0; i5 <= nm1d2; i5++) {
            y->data[y->size[0] * i5] = a + (double)i5;
          }
        } else {
          ndbl = floor(((double)L - a) + 0.5);
          apnd = a + ndbl;
          cdiff = apnd - (double)L;
          absa = fabs(a);
          u1 = L;
          if (absa > u1) {
            u1 = absa;
          }

          if (fabs(cdiff) < 4.4408920985006262E-16 * u1) {
            ndbl++;
            apnd = L;
          } else if (cdiff > 0.0) {
            apnd = a + (ndbl - 1.0);
          } else {
            ndbl++;
          }

          if (ndbl >= 0.0) {
            n = (int)ndbl;
          } else {
            n = 0;
          }

          i5 = y->size[0] * y->size[1];
          y->size[0] = 1;
          y->size[1] = n;
          emxEnsureCapacity_real_T(y, i5);
          if (n > 0) {
            y->data[0] = a;
            if (n > 1) {
              y->data[n - 1] = apnd;
              nm1d2 = (n - 1) / 2;
              for (end = 1; end < nm1d2; end++) {
                y->data[end] = a + (double)end;
                y->data[(n - end) - 1] = apnd - (double)end;
              }

              if (nm1d2 << 1 == n - 1) {
                y->data[nm1d2] = (a + apnd) / 2.0;
              } else {
                y->data[nm1d2] = a + (double)nm1d2;
                y->data[nm1d2 + 1] = apnd - (double)nm1d2;
              }
            }
          }
        }

        a = ((1.0 + (double)i) + b_N) - (double)L;
        if (a < 1.0) {
          i5 = b_y->size[0] * b_y->size[1];
          b_y->size[0] = 1;
          b_y->size[1] = 0;
          emxEnsureCapacity_real_T(b_y, i5);
        } else {
          i5 = b_y->size[0] * b_y->size[1];
          b_y->size[0] = 1;
          b_y->size[1] = (int)floor(a - 1.0) + 1;
          emxEnsureCapacity_real_T(b_y, i5);
          nm1d2 = (int)floor(a - 1.0);
          for (i5 = 0; i5 <= nm1d2; i5++) {
            b_y->data[b_y->size[0] * i5] = 1.0 + (double)i5;
          }
        }

        i5 = c_y->size[0];
        c_y->size[0] = y->size[1] + b_y->size[1];
        emxEnsureCapacity_int32_T1(c_y, i5);
        nm1d2 = y->size[1];
        for (i5 = 0; i5 < nm1d2; i5++) {
          c_y->data[i5] = (int)y->data[y->size[0] * i5] - 1;
        }

        nm1d2 = b_y->size[1];
        for (i5 = 0; i5 < nm1d2; i5++) {
          c_y->data[i5 + y->size[1]] = (int)b_y->data[b_y->size[0] * i5] - 1;
        }

        i5 = b_sgp0->size[0];
        b_sgp0->size[0] = y->size[1] + b_y->size[1];
        emxEnsureCapacity_real_T1(b_sgp0, i5);
        nm1d2 = y->size[1] + b_y->size[1];
        for (i5 = 0; i5 < nm1d2; i5++) {
          b_sgp0->data[i5] = sgp0->data[c_y->data[i5]];
        }

        sgp->data[i] = c_nanmean(b_sgp0);
      } else {
        a = (1.0 + (double)i) - b_N;
        ndbl = (1.0 + (double)i) + b_N;
        if (a > ndbl) {
          i5 = 0;
          end = 0;
        } else {
          i5 = (int)a - 1;
          end = (int)ndbl;
        }

        nm1d2 = b_sgp0->size[0];
        b_sgp0->size[0] = end - i5;
        emxEnsureCapacity_real_T1(b_sgp0, nm1d2);
        nm1d2 = end - i5;
        for (end = 0; end < nm1d2; end++) {
          b_sgp0->data[end] = sgp0->data[i5 + end];
        }

        sgp->data[i] = c_nanmean(b_sgp0);
      }
    }

    i5 = r8->size[0];
    r8->size[0] = sgp0->size[0];
    emxEnsureCapacity_boolean_T(r8, i5);
    nm1d2 = sgp0->size[0];
    for (i5 = 0; i5 < nm1d2; i5++) {
      r8->data[i5] = rtIsNaN(sgp0->data[i5]);
    }

    end = r8->size[0] - 1;
    nm1d2 = 0;
    for (i = 0; i <= end; i++) {
      if (r8->data[i]) {
        nm1d2++;
      }
    }

    i5 = r7->size[0];
    r7->size[0] = nm1d2;
    emxEnsureCapacity_int32_T1(r7, i5);
    nm1d2 = 0;
    for (i = 0; i <= end; i++) {
      if (r8->data[i]) {
        r7->data[nm1d2] = i + 1;
        nm1d2++;
      }
    }

    nm1d2 = r7->size[0];
    for (i5 = 0; i5 < nm1d2; i5++) {
      sgp->data[r7->data[i5] - 1] = rtNaN;
    }

    i5 = sgp0->size[0];
    sgp0->size[0] = sgp->size[0];
    emxEnsureCapacity_real_T1(sgp0, i5);
    nm1d2 = sgp->size[0];
    for (i5 = 0; i5 < nm1d2; i5++) {
      sgp0->data[i5] = sgp->data[i5];
    }
  }

  emxFree_int32_T(&c_y);
  emxFree_real_T(&b_sgp0);
  emxFree_real_T(&b_y);
  emxFree_real_T(&y);
  emxFree_boolean_T(&r8);
  emxFree_int32_T(&r7);
  emxFree_real_T(&sgp0);
}

/*
 * File trailer for DailyTrendPrctile.c
 *
 * [EOF]
 */
