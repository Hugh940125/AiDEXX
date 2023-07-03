/*
 * Academic License - for use in teaching, academic research, and meeting
 * course requirements at degree granting institutions only.  Not for
 * government, commercial, or other organizational use.
 * File: LAGE.c
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

/* Function Definitions */

/*
 * Arguments    : const emxArray_real_T *sg
 *                emxArray_real_T *lage
 * Return Type  : void
 */
void LAGE(const emxArray_real_T *sg, emxArray_real_T *lage)
{
  emxArray_real_T *b_lage;
  int i;
  int n;
  emxArray_real_T *r15;
  int ix;
  int ixstart;
  int ixstop;
  double mtmp;
  boolean_T exitg1;
  double b_mtmp;
  emxInit_real_T1(&b_lage, 2);
  i = b_lage->size[0] * b_lage->size[1];
  b_lage->size[0] = 1;
  b_lage->size[1] = sg->size[1];
  emxEnsureCapacity_real_T(b_lage, i);
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
          if (sg->data[ixstart] > mtmp) {
            mtmp = sg->data[ixstart];
          }

          ixstart++;
        }
      }
    }

    b_lage->data[i] = mtmp;
  }

  emxInit_real_T1(&r15, 2);
  i = r15->size[0] * r15->size[1];
  r15->size[0] = 1;
  r15->size[1] = sg->size[1];
  emxEnsureCapacity_real_T(r15, i);
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

    r15->data[i] = mtmp;
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
        if (sg->data[ixstart] > mtmp) {
          mtmp = sg->data[ixstart];
        }

        ixstart++;
      }
    }
  }

  ixstart = 1;
  n = sg->size[0] * sg->size[1];
  b_mtmp = sg->data[0];
  if (sg->size[0] * sg->size[1] > 1) {
    if (rtIsNaN(sg->data[0])) {
      ix = 2;
      exitg1 = false;
      while ((!exitg1) && (ix <= n)) {
        ixstart = ix;
        if (!rtIsNaN(sg->data[ix - 1])) {
          b_mtmp = sg->data[ix - 1];
          exitg1 = true;
        } else {
          ix++;
        }
      }
    }

    if (ixstart < sg->size[0] * sg->size[1]) {
      while (ixstart + 1 <= n) {
        if (sg->data[ixstart] < b_mtmp) {
          b_mtmp = sg->data[ixstart];
        }

        ixstart++;
      }
    }
  }

  i = lage->size[0] * lage->size[1];
  lage->size[0] = 1;
  lage->size[1] = b_lage->size[1] + 1;
  emxEnsureCapacity_real_T(lage, i);
  ixstop = b_lage->size[1];
  for (i = 0; i < ixstop; i++) {
    lage->data[lage->size[0] * i] = b_lage->data[b_lage->size[0] * i] -
      r15->data[r15->size[0] * i];
  }

  emxFree_real_T(&r15);
  lage->data[lage->size[0] * b_lage->size[1]] = mtmp - b_mtmp;
  if (1 > lage->size[1] - 1) {
    ixstop = 0;
  } else {
    ixstop = lage->size[1] - 1;
  }

  if (1 > lage->size[1] - 1) {
    n = 0;
  } else {
    n = lage->size[1] - 1;
  }

  ixstart = 1;
  mtmp = lage->data[0];
  if (n > 1) {
    if (rtIsNaN(mtmp)) {
      ix = 2;
      exitg1 = false;
      while ((!exitg1) && (ix <= n)) {
        ixstart = ix;
        if (!rtIsNaN(lage->data[ix - 1])) {
          mtmp = lage->data[ix - 1];
          exitg1 = true;
        } else {
          ix++;
        }
      }
    }

    if (ixstart < n) {
      while (ixstart + 1 <= n) {
        if (lage->data[ixstart] > mtmp) {
          mtmp = lage->data[ixstart];
        }

        ixstart++;
      }
    }
  }

  i = b_lage->size[0] * b_lage->size[1];
  b_lage->size[0] = 1;
  b_lage->size[1] = ixstop;
  emxEnsureCapacity_real_T(b_lage, i);
  for (i = 0; i < ixstop; i++) {
    b_lage->data[b_lage->size[0] * i] = lage->data[i];
  }

  b_mtmp = nanmean(b_lage);
  i = b_lage->size[0] * b_lage->size[1];
  b_lage->size[0] = 1;
  b_lage->size[1] = lage->size[1] + 2;
  emxEnsureCapacity_real_T(b_lage, i);
  ixstop = lage->size[1];
  for (i = 0; i < ixstop; i++) {
    b_lage->data[b_lage->size[0] * i] = lage->data[lage->size[0] * i];
  }

  b_lage->data[b_lage->size[0] * lage->size[1]] = b_mtmp;
  b_lage->data[b_lage->size[0] * (lage->size[1] + 1)] = mtmp;
  i = lage->size[0] * lage->size[1];
  lage->size[0] = 1;
  lage->size[1] = b_lage->size[1];
  emxEnsureCapacity_real_T(lage, i);
  ixstop = b_lage->size[1];
  for (i = 0; i < ixstop; i++) {
    lage->data[lage->size[0] * i] = b_lage->data[b_lage->size[0] * i];
  }

  emxFree_real_T(&b_lage);
}

/*
 * File trailer for LAGE.c
 *
 * [EOF]
 */
