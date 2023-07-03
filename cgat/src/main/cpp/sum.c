/*
 * Academic License - for use in teaching, academic research, and meeting
 * course requirements at degree granting institutions only.  Not for
 * government, commercial, or other organizational use.
 * File: sum.c
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
#include "sum.h"
#include "combineVectorElements.h"
#include "datools_emxutil.h"

/* Function Definitions */

/*
 * Arguments    : const emxArray_real_T *x
 *                double y[2]
 * Return Type  : void
 */
void b_sum(const emxArray_real_T *x, double y[2])
{
  int i;
  int k;
  int xoffset;
  double b_y;
  if (x->size[1] == 0) {
    for (i = 0; i < 2; i++) {
      y[i] = 0.0;
    }
  } else {
    for (i = 0; i < 2; i++) {
      y[i] = x->data[i];
    }

    for (k = 2; k <= x->size[1]; k++) {
      xoffset = (k - 1) << 1;
      for (i = 0; i < 2; i++) {
        b_y = y[i] + x->data[xoffset + i];
        y[i] = b_y;
      }
    }
  }
}

/*
 * Arguments    : const emxArray_boolean_T *x
 * Return Type  : double
 */
double c_sum(const emxArray_boolean_T *x)
{
  return combineVectorElements(x);
}

/*
 * Arguments    : const emxArray_real_T *x
 *                emxArray_real_T *y
 * Return Type  : void
 */
void d_sum(const emxArray_real_T *x, emxArray_real_T *y)
{
  int i;
  int xpageoffset;
  i = y->size[0] * y->size[1];
  y->size[0] = 1;
  y->size[1] = x->size[1];
  emxEnsureCapacity_real_T(y, i);
  for (i = 0; i + 1 <= x->size[1]; i++) {
    xpageoffset = i << 1;
    y->data[i] = x->data[xpageoffset];
    y->data[i] += x->data[xpageoffset + 1];
  }
}

/*
 * Arguments    : const emxArray_boolean_T *x
 * Return Type  : double
 */
double sum(const emxArray_boolean_T *x)
{
  return combineVectorElements(x);
}

/*
 * File trailer for sum.c
 *
 * [EOF]
 */
