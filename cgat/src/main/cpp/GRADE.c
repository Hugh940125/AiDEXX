/*
 * Academic License - for use in teaching, academic research, and meeting
 * course requirements at degree granting institutions only.  Not for
 * government, commercial, or other organizational use.
 * File: GRADE.c
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
#include "nansum.h"
#include "nanmean.h"
#include "power.h"
#include "log10.h"

/* Function Definitions */

/*
 * Arguments    : const emxArray_real_T *sg
 *                double hypo
 *                double hyper
 *                double grade[4]
 * Return Type  : void
 */
void GRADE(const emxArray_real_T *sg, double hypo, double hyper, double grade[4])
{
  emxArray_real_T *g;
  int i;
  int loop_ub;
  emxArray_real_T *b_g;
  double gsum;
  double b_grade;
  int end;
  emxArray_int32_T *r9;
  emxArray_boolean_T *r10;
  double x;
  emxArray_boolean_T *r11;
  emxArray_int32_T *r12;
  double b_x;
  emxArray_int32_T *r13;
  double c_x;
  emxInit_real_T(&g, 1);
  i = g->size[0];
  g->size[0] = sg->size[0] * sg->size[1];
  emxEnsureCapacity_real_T1(g, i);
  loop_ub = sg->size[0] * sg->size[1];
  for (i = 0; i < loop_ub; i++) {
    g->data[i] = sg->data[i];
  }

  emxInit_real_T(&b_g, 1);
  b_log10(g);
  b_log10(g);
  i = b_g->size[0];
  b_g->size[0] = g->size[0];
  emxEnsureCapacity_real_T1(b_g, i);
  loop_ub = g->size[0];
  for (i = 0; i < loop_ub; i++) {
    b_g->data[i] = g->data[i] + 0.16;
  }

  b_power(b_g, g);
  i = g->size[0];
  emxEnsureCapacity_real_T1(g, i);
  loop_ub = g->size[0];
  for (i = 0; i < loop_ub; i++) {
    g->data[i] *= 425.0;
  }

  gsum = nansum(g);
  b_grade = c_nanmean(g);
  end = sg->size[0] * sg->size[1] - 1;
  loop_ub = 0;
  for (i = 0; i <= end; i++) {
    if (sg->data[i] < hypo) {
      loop_ub++;
    }
  }

  emxInit_int32_T1(&r9, 1);
  i = r9->size[0];
  r9->size[0] = loop_ub;
  emxEnsureCapacity_int32_T1(r9, i);
  loop_ub = 0;
  for (i = 0; i <= end; i++) {
    if (sg->data[i] < hypo) {
      r9->data[loop_ub] = i + 1;
      loop_ub++;
    }
  }

  i = b_g->size[0];
  b_g->size[0] = r9->size[0];
  emxEnsureCapacity_real_T1(b_g, i);
  loop_ub = r9->size[0];
  for (i = 0; i < loop_ub; i++) {
    b_g->data[i] = g->data[r9->data[i] - 1];
  }

  emxFree_int32_T(&r9);
  emxInit_boolean_T(&r10, 1);
  x = 100.0 * nansum(b_g);
  i = r10->size[0];
  r10->size[0] = sg->size[0] * sg->size[1];
  emxEnsureCapacity_boolean_T(r10, i);
  loop_ub = sg->size[0] * sg->size[1];
  for (i = 0; i < loop_ub; i++) {
    r10->data[i] = (sg->data[i] >= hypo);
  }

  emxInit_boolean_T(&r11, 1);
  i = r11->size[0];
  r11->size[0] = sg->size[0] * sg->size[1];
  emxEnsureCapacity_boolean_T(r11, i);
  loop_ub = sg->size[0] * sg->size[1];
  for (i = 0; i < loop_ub; i++) {
    r11->data[i] = (sg->data[i] <= hyper);
  }

  end = r10->size[0] - 1;
  loop_ub = 0;
  for (i = 0; i <= end; i++) {
    if (r10->data[i] && r11->data[i]) {
      loop_ub++;
    }
  }

  emxInit_int32_T1(&r12, 1);
  i = r12->size[0];
  r12->size[0] = loop_ub;
  emxEnsureCapacity_int32_T1(r12, i);
  loop_ub = 0;
  for (i = 0; i <= end; i++) {
    if (r10->data[i] && r11->data[i]) {
      r12->data[loop_ub] = i + 1;
      loop_ub++;
    }
  }

  emxFree_boolean_T(&r11);
  emxFree_boolean_T(&r10);
  i = b_g->size[0];
  b_g->size[0] = r12->size[0];
  emxEnsureCapacity_real_T1(b_g, i);
  loop_ub = r12->size[0];
  for (i = 0; i < loop_ub; i++) {
    b_g->data[i] = g->data[r12->data[i] - 1];
  }

  emxFree_int32_T(&r12);
  b_x = 100.0 * nansum(b_g);
  end = sg->size[0] * sg->size[1] - 1;
  loop_ub = 0;
  for (i = 0; i <= end; i++) {
    if (sg->data[i] > hyper) {
      loop_ub++;
    }
  }

  emxInit_int32_T1(&r13, 1);
  i = r13->size[0];
  r13->size[0] = loop_ub;
  emxEnsureCapacity_int32_T1(r13, i);
  loop_ub = 0;
  for (i = 0; i <= end; i++) {
    if (sg->data[i] > hyper) {
      r13->data[loop_ub] = i + 1;
      loop_ub++;
    }
  }

  i = b_g->size[0];
  b_g->size[0] = r13->size[0];
  emxEnsureCapacity_real_T1(b_g, i);
  loop_ub = r13->size[0];
  for (i = 0; i < loop_ub; i++) {
    b_g->data[i] = g->data[r13->data[i] - 1];
  }

  emxFree_int32_T(&r13);
  emxFree_real_T(&g);
  c_x = 100.0 * nansum(b_g);
  grade[0] = b_grade;
  grade[1] = x / gsum;
  grade[2] = b_x / gsum;
  grade[3] = c_x / gsum;
  emxFree_real_T(&b_g);
}

/*
 * File trailer for GRADE.c
 *
 * [EOF]
 */
