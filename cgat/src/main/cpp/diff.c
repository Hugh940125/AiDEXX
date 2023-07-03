/*
 * Academic License - for use in teaching, academic research, and meeting
 * course requirements at degree granting institutions only.  Not for
 * government, commercial, or other organizational use.
 * File: diff.c
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
#include "diff.h"
#include "datools_emxutil.h"

/* Function Definitions */

/*
 * Arguments    : const emxArray_boolean_T *x
 *                emxArray_real_T *y
 * Return Type  : void
 */
void b_diff(const emxArray_boolean_T *x, emxArray_real_T *y)
{
  int orderForDim;
  int ixLead;
  int iyLead;
  int work_data_idx_0;
  int m;
  int tmp2;
  orderForDim = x->size[0] - 1;
  if (!(orderForDim < 1)) {
    orderForDim = 1;
  }

  if (orderForDim < 1) {
    ixLead = y->size[0];
    y->size[0] = 0;
    emxEnsureCapacity_real_T1(y, ixLead);
  } else {
    orderForDim = x->size[0] - 1;
    ixLead = y->size[0];
    y->size[0] = orderForDim;
    emxEnsureCapacity_real_T1(y, ixLead);
    if (!(y->size[0] == 0)) {
      ixLead = 1;
      iyLead = 0;
      work_data_idx_0 = x->data[0];
      for (m = 2; m <= x->size[0]; m++) {
        orderForDim = x->data[ixLead];
        tmp2 = work_data_idx_0;
        work_data_idx_0 = orderForDim;
        orderForDim -= tmp2;
        ixLead++;
        y->data[iyLead] = orderForDim;
        iyLead++;
      }
    }
  }
}

/*
 * Arguments    : const emxArray_real_T *x
 *                emxArray_real_T *y
 * Return Type  : void
 */
void diff(const emxArray_real_T *x, emxArray_real_T *y)
{
  int orderForDim;
  int iyLead;
  double work_data_idx_0;
  int m;
  double tmp1;
  double tmp2;
  if (x->size[0] == 0) {
    iyLead = y->size[0];
    y->size[0] = 0;
    emxEnsureCapacity_real_T1(y, iyLead);
  } else {
    orderForDim = x->size[0] - 1;
    if (!(orderForDim < 1)) {
      orderForDim = 1;
    }

    if (orderForDim < 1) {
      iyLead = y->size[0];
      y->size[0] = 0;
      emxEnsureCapacity_real_T1(y, iyLead);
    } else {
      orderForDim = x->size[0] - 1;
      iyLead = y->size[0];
      y->size[0] = orderForDim;
      emxEnsureCapacity_real_T1(y, iyLead);
      if (!(y->size[0] == 0)) {
        orderForDim = 1;
        iyLead = 0;
        work_data_idx_0 = x->data[0];
        for (m = 2; m <= x->size[0]; m++) {
          tmp1 = x->data[orderForDim];
          tmp2 = work_data_idx_0;
          work_data_idx_0 = tmp1;
          tmp1 -= tmp2;
          orderForDim++;
          y->data[iyLead] = tmp1;
          iyLead++;
        }
      }
    }
  }
}

/*
 * File trailer for diff.c
 *
 * [EOF]
 */
