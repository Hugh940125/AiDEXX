/*
 * Academic License - for use in teaching, academic research, and meeting
 * course requirements at degree granting institutions only.  Not for
 * government, commercial, or other organizational use.
 * File: MINBG.c
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
 *                emxArray_real_T *minbg
 * Return Type  : void
 */
void MINBG(const emxArray_real_T *sg, emxArray_real_T *minbg)
{
  emxArray_real_T *varargout_1;
  int n;
  int i;
  int ixstart;
  int ix;
  double mtmp;
  int ixstop;
  boolean_T exitg1;
  emxInit_real_T1(&varargout_1, 2);
  n = varargout_1->size[0] * varargout_1->size[1];
  varargout_1->size[0] = 1;
  varargout_1->size[1] = sg->size[1];
  emxEnsureCapacity_real_T(varargout_1, n);
  n = sg->size[0];
  for (i = 0; i + 1 <= sg->size[1]; i++) {
    ix = i * n;
    ixstart = i * n + 1;
    ixstop = ix + n;
    mtmp = sg->data[ix];
    if (n > 1) {
      if (rtIsNaN(sg->data[ix])) {
        ix = ixstart + 1;
        exitg1 = false;
        while ((!exitg1) && (ix <= ixstop)) {
          ixstart = ix;
          if (!rtIsNaN(sg->data[ix - 1])) {
            mtmp = sg->data[ix - 1];
            exitg1 = true;
          } else {
            ix++;
          }
        }
      }

      if (ixstart < ixstop) {
        while (ixstart + 1 <= ixstop) {
          if (sg->data[ixstart] < mtmp) {
            mtmp = sg->data[ixstart];
          }

          ixstart++;
        }
      }
    }

    varargout_1->data[i] = mtmp;
  }

  ixstart = 1;
  n = sg->size[0] * sg->size[1];
  mtmp = sg->data[0];
  if (sg->size[0] * sg->size[1] > 1) {
    if (rtIsNaN(sg->data[0])) {
      ix = 2;
      exitg1 = false;
      while ((!exitg1) && (ix <= n)) {
        ixstart = ix;
        if (!rtIsNaN(sg->data[ix - 1])) {
          mtmp = sg->data[ix - 1];
          exitg1 = true;
        } else {
          ix++;
        }
      }
    }

    if (ixstart < sg->size[0] * sg->size[1]) {
      while (ixstart + 1 <= n) {
        if (sg->data[ixstart] < mtmp) {
          mtmp = sg->data[ixstart];
        }

        ixstart++;
      }
    }
  }

  n = minbg->size[0] * minbg->size[1];
  minbg->size[0] = 1;
  minbg->size[1] = varargout_1->size[1] + 1;
  emxEnsureCapacity_real_T(minbg, n);
  i = varargout_1->size[1];
  for (n = 0; n < i; n++) {
    minbg->data[minbg->size[0] * n] = varargout_1->data[varargout_1->size[0] * n];
  }

  minbg->data[minbg->size[0] * varargout_1->size[1]] = mtmp;
  emxFree_real_T(&varargout_1);
}

/*
 * File trailer for MINBG.c
 *
 * [EOF]
 */
