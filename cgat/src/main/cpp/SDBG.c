/*
 * Academic License - for use in teaching, academic research, and meeting
 * course requirements at degree granting institutions only.  Not for
 * government, commercial, or other organizational use.
 * File: SDBG.c
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
#include "nanstd.h"
#include "sqrt.h"

/* Function Definitions */

/*
 * Arguments    : const emxArray_real_T *sg
 *                emxArray_real_T *sdbg
 * Return Type  : void
 */
void SDBG(const emxArray_real_T *sg, emxArray_real_T *sdbg)
{
  emxArray_real_T *y;
  int n;
  int iy;
  int ix;
  int b_sg[1];
  int i;
  emxArray_real_T c_sg;
  double xbar;
  int b_ix;
  double b_y;
  int nn;
  int k;
  double r;
  emxInit_real_T1(&y, 2);
  n = sg->size[0];
  iy = y->size[0] * y->size[1];
  y->size[0] = 1;
  y->size[1] = sg->size[1];
  emxEnsureCapacity_real_T(y, iy);
  if (!(y->size[1] == 0)) {
    ix = 0;
    iy = -1;
    for (i = 1; i <= sg->size[1]; i++) {
      iy++;
      if ((sg->size[0] == 0) || (sg->size[1] == 0)) {
        b_y = rtNaN;
      } else {
        b_ix = ix;
        xbar = 0.0;
        nn = 0;
        for (k = 1; k <= n; k++) {
          if (!rtIsNaN(sg->data[b_ix])) {
            xbar += sg->data[b_ix];
            nn++;
          }

          b_ix++;
        }

        if (nn == 0) {
          b_y = rtNaN;
        } else {
          xbar /= (double)nn;
          b_ix = ix;
          b_y = 0.0;
          for (k = 1; k <= n; k++) {
            if (!rtIsNaN(sg->data[b_ix])) {
              r = sg->data[b_ix] - xbar;
              b_y += r * r;
            }

            b_ix++;
          }

          if (nn > 1) {
            nn--;
          }

          b_y /= (double)nn;
        }
      }

      y->data[iy] = b_y;
      ix += n;
    }
  }

  b_sqrt(y);
  b_sg[0] = sg->size[0] * sg->size[1];
  c_sg = *sg;
  c_sg.size = (int *)&b_sg;
  c_sg.numDimensions = 1;
  xbar = nanstd(&c_sg);
  iy = sdbg->size[0] * sdbg->size[1];
  sdbg->size[0] = 1;
  sdbg->size[1] = y->size[1] + 1;
  emxEnsureCapacity_real_T(sdbg, iy);
  ix = y->size[1];
  for (iy = 0; iy < ix; iy++) {
    sdbg->data[sdbg->size[0] * iy] = y->data[y->size[0] * iy];
  }

  sdbg->data[sdbg->size[0] * y->size[1]] = xbar;
  emxFree_real_T(&y);
}

/*
 * File trailer for SDBG.c
 *
 * [EOF]
 */
