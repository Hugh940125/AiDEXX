/*
 * Academic License - for use in teaching, academic research, and meeting
 * course requirements at degree granting institutions only.  Not for
 * government, commercial, or other organizational use.
 * File: LBGD.c
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
#include "sum.h"

/* Function Definitions */

/*
 * Arguments    : const emxArray_real_T *sg
 *                double dt
 *                double hypo
 *                emxArray_real_T *lbgd
 * Return Type  : void
 */
void LBGD(const emxArray_real_T *sg, double dt, double hypo, emxArray_real_T
          *lbgd)
{
  int i;
  int loop_ub;
  double c1;
  double c2;
  boolean_T f;
  emxArray_real_T *b_lbgd;
  int i7;
  i = lbgd->size[0] * lbgd->size[1];
  lbgd->size[0] = 2;
  lbgd->size[1] = sg->size[1] + 1;
  emxEnsureCapacity_real_T(lbgd, i);
  loop_ub = (sg->size[1] + 1) << 1;
  for (i = 0; i < loop_ub; i++) {
    lbgd->data[i] = 0.0;
  }

  c1 = 0.0;
  c2 = 0.0;
  f = false;
  for (i = 0; i < sg->size[1]; i++) {
    for (loop_ub = 0; loop_ub < sg->size[0]; loop_ub++) {
      if (sg->data[loop_ub + sg->size[0] * i] < hypo) {
        c1++;
        c2 = 0.0;
      } else {
        c1 = 0.0;
        c2++;
      }

      if ((c1 >= 2.0) && (!f)) {
        f = true;
        lbgd->data[lbgd->size[0] * i]++;
      }

      if ((c2 >= 2.0) && f) {
        f = false;
      }

      if (f) {
        lbgd->data[1 + lbgd->size[0] * i] += dt;
      }
    }
  }

  if (1 > sg->size[1]) {
    loop_ub = 0;
  } else {
    loop_ub = sg->size[1];
  }

  emxInit_real_T1(&b_lbgd, 2);
  i = b_lbgd->size[0] * b_lbgd->size[1];
  b_lbgd->size[0] = 2;
  b_lbgd->size[1] = loop_ub;
  emxEnsureCapacity_real_T(b_lbgd, i);
  for (i = 0; i < loop_ub; i++) {
    for (i7 = 0; i7 < 2; i7++) {
      b_lbgd->data[i7 + b_lbgd->size[0] * i] = lbgd->data[i7 + lbgd->size[0] * i];
    }
  }

  b_sum(b_lbgd, *(double (*)[2])&lbgd->data[lbgd->size[0] * sg->size[1]]);
  i = 0;
  emxFree_real_T(&b_lbgd);
  while (i <= sg->size[1]) {
    if (lbgd->data[lbgd->size[0] * i] > 0.0) {
      lbgd->data[1 + lbgd->size[0] * i] /= lbgd->data[lbgd->size[0] * i];
    }

    i++;
  }
}

/*
 * File trailer for LBGD.c
 *
 * [EOF]
 */
