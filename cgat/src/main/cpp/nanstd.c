/*
 * Academic License - for use in teaching, academic research, and meeting
 * course requirements at degree granting institutions only.  Not for
 * government, commercial, or other organizational use.
 * File: nanstd.c
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
#include "nanstd.h"

/* Function Definitions */

/*
 * Arguments    : const emxArray_real_T *varargin_1
 * Return Type  : double
 */
double nanstd(const emxArray_real_T *varargin_1)
{
  double y;
  int n;
  int ix;
  double xbar;
  int nn;
  int k;
  double r;
  n = varargin_1->size[0];
  if (varargin_1->size[0] == 0) {
    y = rtNaN;
  } else {
    ix = 0;
    xbar = 0.0;
    nn = 0;
    for (k = 1; k <= n; k++) {
      if (!rtIsNaN(varargin_1->data[ix])) {
        xbar += varargin_1->data[ix];
        nn++;
      }

      ix++;
    }

    if (nn == 0) {
      y = rtNaN;
    } else {
      xbar /= (double)nn;
      ix = 0;
      y = 0.0;
      for (k = 1; k <= n; k++) {
        if (!rtIsNaN(varargin_1->data[ix])) {
          r = varargin_1->data[ix] - xbar;
          y += r * r;
        }

        ix++;
      }

      if (nn > 1) {
        nn--;
      }

      y /= (double)nn;
    }
  }

  return sqrt(y);
}

/*
 * File trailer for nanstd.c
 *
 * [EOF]
 */
