/*
 * Academic License - for use in teaching, academic research, and meeting
 * course requirements at degree granting institutions only.  Not for
 * government, commercial, or other organizational use.
 * File: HBGD.c
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
 *                double hyper
 *                emxArray_real_T *hbgd
 * Return Type  : void
 */
void HBGD(const emxArray_real_T *sg, double dt, double hyper, emxArray_real_T
          *hbgd)
{
  int i;
  int loop_ub;
  double c1;
  double c2;
  boolean_T f;
  emxArray_real_T *b_hbgd;
  int i6;
  i = hbgd->size[0] * hbgd->size[1];
  hbgd->size[0] = 2;
  hbgd->size[1] = sg->size[1] + 1;
  emxEnsureCapacity_real_T(hbgd, i);
  loop_ub = (sg->size[1] + 1) << 1;
  for (i = 0; i < loop_ub; i++) {
    hbgd->data[i] = 0.0;
  }

  c1 = 0.0;
  c2 = 0.0;
  f = false;
  for (i = 0; i < sg->size[1]; i++) {
    for (loop_ub = 0; loop_ub < sg->size[0]; loop_ub++) {
      if (sg->data[loop_ub + sg->size[0] * i] > hyper) {
        c1++;
        c2 = 0.0;
      } else {
        c1 = 0.0;
        c2++;
      }

      if ((c1 >= 2.0) && (!f)) {
        f = true;
        hbgd->data[hbgd->size[0] * i]++;
      }

      if ((c2 >= 2.0) && f) {
        f = false;
      }

      if (f) {
        hbgd->data[1 + hbgd->size[0] * i] += dt;
      }
    }
  }

  if (1 > sg->size[1]) {
    loop_ub = 0;
  } else {
    loop_ub = sg->size[1];
  }

  emxInit_real_T1(&b_hbgd, 2);
  i = b_hbgd->size[0] * b_hbgd->size[1];
  b_hbgd->size[0] = 2;
  b_hbgd->size[1] = loop_ub;
  emxEnsureCapacity_real_T(b_hbgd, i);
  for (i = 0; i < loop_ub; i++) {
    for (i6 = 0; i6 < 2; i6++) {
      b_hbgd->data[i6 + b_hbgd->size[0] * i] = hbgd->data[i6 + hbgd->size[0] * i];
    }
  }

  b_sum(b_hbgd, *(double (*)[2])&hbgd->data[hbgd->size[0] * sg->size[1]]);
  i = 0;
  emxFree_real_T(&b_hbgd);
  while (i <= sg->size[1]) {
    if (hbgd->data[hbgd->size[0] * i] > 0.0) {
      hbgd->data[1 + hbgd->size[0] * i] /= hbgd->data[hbgd->size[0] * i];
    }

    i++;
  }
}

/*
 * File trailer for HBGD.c
 *
 * [EOF]
 */
