/*
 * Academic License - for use in teaching, academic research, and meeting
 * course requirements at degree granting institutions only.  Not for
 * government, commercial, or other organizational use.
 * File: nanmean.c
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

/* Function Definitions */

/*
 * Arguments    : const emxArray_real_T *varargin_1
 *                emxArray_real_T *y
 * Return Type  : void
 */
void b_nanmean(const emxArray_real_T *varargin_1, emxArray_real_T *y)
{
  int vstride;
  unsigned int sz[2];
  int iy;
  int ixstart;
  int j;
  int ix;
  double s;
  int n;
  int k;
  for (vstride = 0; vstride < 2; vstride++) {
    sz[vstride] = (unsigned int)varargin_1->size[vstride];
  }

  vstride = y->size[0];
  y->size[0] = (int)sz[0];
  emxEnsureCapacity_real_T1(y, vstride);
  if ((varargin_1->size[0] == 0) || (varargin_1->size[1] == 0)) {
    iy = y->size[0];
    vstride = y->size[0];
    y->size[0] = iy;
    emxEnsureCapacity_real_T1(y, vstride);
    for (vstride = 0; vstride < iy; vstride++) {
      y->data[vstride] = rtNaN;
    }
  } else {
    vstride = varargin_1->size[0];
    iy = -1;
    ixstart = -1;
    for (j = 1; j <= vstride; j++) {
      ixstart++;
      ix = ixstart;
      if (!rtIsNaN(varargin_1->data[ixstart])) {
        s = varargin_1->data[ixstart];
        n = 1;
      } else {
        s = 0.0;
        n = 0;
      }

      for (k = 2; k <= varargin_1->size[1]; k++) {
        ix += vstride;
        if (!rtIsNaN(varargin_1->data[ix])) {
          s += varargin_1->data[ix];
          n++;
        }
      }

      if (n == 0) {
        s = rtNaN;
      } else {
        s /= (double)n;
      }

      iy++;
      y->data[iy] = s;
    }
  }
}

/*
 * Arguments    : const emxArray_real_T *varargin_1
 * Return Type  : double
 */
double c_nanmean(const emxArray_real_T *varargin_1)
{
  double y;
  int c;
  int k;
  if (varargin_1->size[0] == 0) {
    y = rtNaN;
  } else {
    y = 0.0;
    c = 0;
    for (k = 0; k + 1 <= varargin_1->size[0]; k++) {
      if (!rtIsNaN(varargin_1->data[k])) {
        y += varargin_1->data[k];
        c++;
      }
    }

    if (c == 0) {
      y = rtNaN;
    } else {
      y /= (double)c;
    }
  }

  return y;
}

/*
 * Arguments    : const emxArray_real_T *varargin_1
 *                emxArray_real_T *y
 * Return Type  : void
 */
void d_nanmean(const emxArray_real_T *varargin_1, emxArray_real_T *y)
{
  int ixstart;
  unsigned int sz[2];
  int ix;
  int iy;
  int i;
  int n;
  double s;
  for (ixstart = 0; ixstart < 2; ixstart++) {
    sz[ixstart] = (unsigned int)varargin_1->size[ixstart];
  }

  ixstart = y->size[0] * y->size[1];
  y->size[0] = 1;
  y->size[1] = (int)sz[1];
  emxEnsureCapacity_real_T(y, ixstart);
  if ((varargin_1->size[0] == 0) || (varargin_1->size[1] == 0)) {
    ixstart = y->size[0] * y->size[1];
    y->size[0] = 1;
    emxEnsureCapacity_real_T(y, ixstart);
    n = y->size[1];
    for (ixstart = 0; ixstart < n; ixstart++) {
      y->data[y->size[0] * ixstart] = rtNaN;
    }
  } else {
    ix = -1;
    iy = -1;
    for (i = 1; i <= varargin_1->size[1]; i++) {
      ixstart = ix + 1;
      ix++;
      if (!rtIsNaN(varargin_1->data[ixstart])) {
        s = varargin_1->data[ixstart];
        n = 1;
      } else {
        s = 0.0;
        n = 0;
      }

      for (ixstart = 2; ixstart <= varargin_1->size[0]; ixstart++) {
        ix++;
        if (!rtIsNaN(varargin_1->data[ix])) {
          s += varargin_1->data[ix];
          n++;
        }
      }

      if (n == 0) {
        s = rtNaN;
      } else {
        s /= (double)n;
      }

      iy++;
      y->data[iy] = s;
    }
  }
}

/*
 * Arguments    : const emxArray_real_T *varargin_1
 * Return Type  : double
 */
double nanmean(const emxArray_real_T *varargin_1)
{
  double y;
  int c;
  int k;
  if (varargin_1->size[1] == 0) {
    y = rtNaN;
  } else {
    y = 0.0;
    c = 0;
    for (k = 0; k + 1 <= varargin_1->size[1]; k++) {
      if (!rtIsNaN(varargin_1->data[k])) {
        y += varargin_1->data[k];
        c++;
      }
    }

    if (c == 0) {
      y = rtNaN;
    } else {
      y /= (double)c;
    }
  }

  return y;
}

/*
 * File trailer for nanmean.c
 *
 * [EOF]
 */
