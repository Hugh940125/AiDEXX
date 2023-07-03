/*
 * Academic License - for use in teaching, academic research, and meeting
 * course requirements at degree granting institutions only.  Not for
 * government, commercial, or other organizational use.
 * File: HbA1c.c
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
 * Return Type  : double
 */
double HbA1c(const emxArray_real_T *sg)
{
  emxArray_real_T *sgm;
  double y;
  int ix;
  int ixstart;
  int iy;
  int i;
  double b_y[48];
  int k;
  double s;
  emxInit_real_T(&sgm, 1);
  b_nanmean(sg, sgm);
  y = (double)sgm->size[0] / 48.0;
  if ((int)y == 0) {
    for (ixstart = 0; ixstart < 48; ixstart++) {
      b_y[ixstart] = rtNaN;
    }
  } else {
    ix = -1;
    iy = -1;
    for (i = 0; i < 48; i++) {
      ixstart = ix + 1;
      ix++;
      if (!rtIsNaN(sgm->data[ixstart])) {
        s = sgm->data[ixstart];
        ixstart = 1;
      } else {
        s = 0.0;
        ixstart = 0;
      }

      for (k = 2; k <= (int)y; k++) {
        ix++;
        if (!rtIsNaN(sgm->data[ix])) {
          s += sgm->data[ix];
          ixstart++;
        }
      }

      if (ixstart == 0) {
        s = rtNaN;
      } else {
        s /= (double)ixstart;
      }

      iy++;
      b_y[iy] = s;
    }
  }

  emxFree_real_T(&sgm);
  y = b_y[0];
  for (k = 0; k < 47; k++) {
    y += b_y[k + 1];
  }

  return (y / 48.0 + 0.582) / 1.198;
}

/*
 * File trailer for HbA1c.c
 *
 * [EOF]
 */
