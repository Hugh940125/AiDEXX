/*
 * Academic License - for use in teaching, academic research, and meeting
 * course requirements at degree granting institutions only.  Not for
 * government, commercial, or other organizational use.
 * File: HBGI.c
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
#include "power.h"
#include "log.h"

/* Function Definitions */

/*
 * Arguments    : const emxArray_real_T *sg
 * Return Type  : double
 */
double HBGI(const emxArray_real_T *sg)
{
  double hbgi;
  emxArray_real_T *bg;
  int end;
  int loop_ub;
  emxArray_real_T *tbg;
  emxInit_real_T(&bg, 1);
  end = bg->size[0];
  bg->size[0] = sg->size[0] * sg->size[1];
  emxEnsureCapacity_real_T1(bg, end);
  loop_ub = sg->size[0] * sg->size[1];
  for (end = 0; end < loop_ub; end++) {
    bg->data[end] = sg->data[end];
  }

  end = sg->size[0] * sg->size[1];
  for (loop_ub = 0; loop_ub < end; loop_ub++) {
    if (bg->data[loop_ub] < 6.25) {
      bg->data[loop_ub] = 6.25;
    }
  }

  emxInit_real_T(&tbg, 1);
  b_log(bg);
  c_power(bg, tbg);
  end = tbg->size[0];
  emxEnsureCapacity_real_T1(tbg, end);
  loop_ub = tbg->size[0];
  for (end = 0; end < loop_ub; end++) {
    tbg->data[end] = 1.794 * (tbg->data[end] - 1.861);
  }

  b_power(tbg, bg);
  end = bg->size[0];
  emxEnsureCapacity_real_T1(bg, end);
  loop_ub = bg->size[0];
  emxFree_real_T(&tbg);
  for (end = 0; end < loop_ub; end++) {
    bg->data[end] *= 10.0;
  }

  hbgi = c_nanmean(bg);
  emxFree_real_T(&bg);
  return hbgi;
}

/*
 * File trailer for HBGI.c
 *
 * [EOF]
 */
