/*
 * Academic License - for use in teaching, academic research, and meeting
 * course requirements at degree granting institutions only.  Not for
 * government, commercial, or other organizational use.
 * File: prctile.c
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
#include "prctile.h"
#include "datools_emxutil.h"

/* Function Declarations */
static double rt_roundd_snf(double u);

/* Function Definitions */

/*
 * Arguments    : double u
 * Return Type  : double
 */
static double rt_roundd_snf(double u)
{
  double y;
  if (fabs(u) < 4.503599627370496E+15) {
    if (u >= 0.5) {
      y = floor(u + 0.5);
    } else if (u > -0.5) {
      y = u * 0.0;
    } else {
      y = ceil(u - 0.5);
    }
  } else {
    y = u;
  }

  return y;
}

/*
 * Arguments    : const emxArray_real_T *x
 *                double p
 *                emxArray_real_T *pct
 * Return Type  : void
 */
void percentile_array(const emxArray_real_T *x, double p, emxArray_real_T *pct)
{
  int i2;
  emxArray_real_T *wk;
  emxArray_int32_T *idx;
  emxArray_int32_T *iwork;
  int ix;
  int vstride;
  int ixstart;
  int iystart;
  int xj;
  int k;
  int n;
  unsigned int unnamed_idx_0;
  boolean_T b_p;
  int j;
  int pEnd;
  double r;
  int c_p;
  double i;
  int q;
  int qEnd;
  int kEnd;
  i2 = pct->size[0];
  pct->size[0] = x->size[0];
  emxEnsureCapacity_real_T1(pct, i2);
  emxInit_real_T(&wk, 1);
  emxInit_int32_T1(&idx, 1);
  emxInit_int32_T1(&iwork, 1);
  if ((x->size[0] == 0) || (x->size[1] == 0) || (pct->size[0] == 0)) {
    ix = pct->size[0];
    i2 = pct->size[0];
    pct->size[0] = ix;
    emxEnsureCapacity_real_T1(pct, i2);
    for (i2 = 0; i2 < ix; i2++) {
      pct->data[i2] = rtNaN;
    }
  } else {
    i2 = wk->size[0];
    wk->size[0] = x->size[1];
    emxEnsureCapacity_real_T1(wk, i2);
    vstride = x->size[0];
    ixstart = -1;
    iystart = -1;
    for (xj = 1; xj <= vstride; xj++) {
      ixstart++;
      iystart++;
      ix = ixstart;
      wk->data[0] = x->data[ixstart];
      for (k = 2; k <= x->size[1]; k++) {
        ix += vstride;
        wk->data[k - 1] = x->data[ix];
      }

      n = wk->size[0] + 1;
      unnamed_idx_0 = (unsigned int)wk->size[0];
      i2 = idx->size[0];
      idx->size[0] = (int)unnamed_idx_0;
      emxEnsureCapacity_int32_T1(idx, i2);
      ix = (int)unnamed_idx_0;
      for (i2 = 0; i2 < ix; i2++) {
        idx->data[i2] = 0;
      }

      i2 = iwork->size[0];
      iwork->size[0] = (int)unnamed_idx_0;
      emxEnsureCapacity_int32_T1(iwork, i2);
      for (k = 1; k <= n - 2; k += 2) {
        if ((wk->data[k - 1] <= wk->data[k]) || rtIsNaN(wk->data[k])) {
          b_p = true;
        } else {
          b_p = false;
        }

        if (b_p) {
          idx->data[k - 1] = k;
          idx->data[k] = k + 1;
        } else {
          idx->data[k - 1] = k + 1;
          idx->data[k] = k;
        }
      }

      if ((wk->size[0] & 1) != 0) {
        idx->data[wk->size[0] - 1] = wk->size[0];
      }

      ix = 2;
      while (ix < n - 1) {
        i2 = ix << 1;
        j = 1;
        for (pEnd = 1 + ix; pEnd < n; pEnd = qEnd + ix) {
          c_p = j;
          q = pEnd - 1;
          qEnd = j + i2;
          if (qEnd > n) {
            qEnd = n;
          }

          k = 0;
          kEnd = qEnd - j;
          while (k + 1 <= kEnd) {
            if ((wk->data[idx->data[c_p - 1] - 1] <= wk->data[idx->data[q] - 1])
                || rtIsNaN(wk->data[idx->data[q] - 1])) {
              b_p = true;
            } else {
              b_p = false;
            }

            if (b_p) {
              iwork->data[k] = idx->data[c_p - 1];
              c_p++;
              if (c_p == pEnd) {
                while (q + 1 < qEnd) {
                  k++;
                  iwork->data[k] = idx->data[q];
                  q++;
                }
              }
            } else {
              iwork->data[k] = idx->data[q];
              q++;
              if (q + 1 == qEnd) {
                while (c_p < pEnd) {
                  k++;
                  iwork->data[k] = idx->data[c_p - 1];
                  c_p++;
                }
              }
            }

            k++;
          }

          for (k = 0; k + 1 <= kEnd; k++) {
            idx->data[(j + k) - 1] = iwork->data[k];
          }

          j = qEnd;
        }

        ix = i2;
      }

      ix = wk->size[0];
      while ((ix > 0) && rtIsNaN(wk->data[idx->data[ix - 1] - 1])) {
        ix--;
      }

      if (ix < 1) {
        r = rtNaN;
      } else if (ix == 1) {
        r = wk->data[idx->data[0] - 1];
      } else {
        r = p / 100.0 * (double)ix;
        i = rt_roundd_snf(r);
        if (i < 1.0) {
          r = wk->data[idx->data[0] - 1];
        } else if (ix <= i) {
          r = wk->data[idx->data[ix - 1] - 1];
        } else {
          r -= i;
          r = (0.5 - r) * wk->data[idx->data[(int)i - 1] - 1] + (0.5 + r) *
            wk->data[idx->data[(int)(i + 1.0) - 1] - 1];
        }
      }

      pct->data[iystart] = r;
    }
  }

  emxFree_int32_T(&iwork);
  emxFree_int32_T(&idx);
  emxFree_real_T(&wk);
}

/*
 * File trailer for prctile.c
 *
 * [EOF]
 */
