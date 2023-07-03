/*
 * Academic License - for use in teaching, academic research, and meeting
 * course requirements at degree granting institutions only.  Not for
 * government, commercial, or other organizational use.
 * File: SelectByHour.c
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
 *                double sh
 *                double eh
 *                emxArray_real_T *sbg
 * Return Type  : void
 */
void SelectByHour(const emxArray_real_T *sg, double sh, double eh,
                  emxArray_real_T *sbg)
{
  cell_wrap_1 reshapes[2];
  emxArray_real_T *b_sg;
  emxArray_real_T *c_sg;
  double n;
  int i21;
  double si;
  int i22;
  int loop_ub;
  int i23;
  int b_loop_ub;
  int i24;
  int result;
  boolean_T empty_non_axis_sizes;
  int b_result;
  int i25;
  int c_loop_ub;
  int i26;
  emxInitMatrix_cell_wrap_1(reshapes);
  emxInit_real_T1(&b_sg, 2);
  emxInit_real_T1(&c_sg, 2);
  if ((sh < 0.0) || (sh > 24.0) || (eh < 0.0) || (eh > 24.0)) {
    i21 = sbg->size[0] * sbg->size[1];
    sbg->size[0] = 0;
    sbg->size[1] = 0;
    emxEnsureCapacity_real_T(sbg, i21);
  } else {
    n = (double)sg->size[0] / 24.0;
    si = sh * n + 1.0;
    n *= eh;
    if (eh >= sh) {
      if (si > n) {
        i21 = 0;
        i22 = 0;
      } else {
        i21 = (int)si - 1;
        i22 = (int)n;
      }

      loop_ub = sg->size[1];
      i23 = sbg->size[0] * sbg->size[1];
      sbg->size[0] = i22 - i21;
      sbg->size[1] = loop_ub;
      emxEnsureCapacity_real_T(sbg, i23);
      for (i23 = 0; i23 < loop_ub; i23++) {
        b_loop_ub = i22 - i21;
        for (i24 = 0; i24 < b_loop_ub; i24++) {
          sbg->data[i24 + sbg->size[0] * i23] = sg->data[(i21 + i24) + sg->size
            [0] * i23];
        }
      }
    } else {
      if (si > sg->size[0]) {
        i21 = 1;
        i22 = 1;
      } else {
        i21 = (int)si;
        i22 = sg->size[0] + 1;
      }

      if (1 > sg->size[1] - 1) {
        loop_ub = 0;
      } else {
        loop_ub = sg->size[1] - 1;
      }

      if (1.0 > n) {
        b_loop_ub = 0;
      } else {
        b_loop_ub = (int)n;
      }

      if (2 > sg->size[1]) {
        i23 = 1;
        i24 = 1;
      } else {
        i23 = 2;
        i24 = sg->size[1] + 1;
      }

      if (!((i22 - i21 == 0) || (loop_ub == 0))) {
        result = loop_ub;
      } else if (!((b_loop_ub == 0) || (i24 - i23 == 0))) {
        result = i24 - i23;
      } else {
        if (loop_ub > 0) {
          result = loop_ub;
        } else {
          result = 0;
        }

        if (i24 - i23 > result) {
          result = i24 - i23;
        }
      }

      empty_non_axis_sizes = (result == 0);
      if (empty_non_axis_sizes || (!((i22 - i21 == 0) || (loop_ub == 0)))) {
        b_result = i22 - i21;
      } else {
        b_result = 0;
      }

      i25 = b_sg->size[0] * b_sg->size[1];
      b_sg->size[0] = i22 - i21;
      b_sg->size[1] = loop_ub;
      emxEnsureCapacity_real_T(b_sg, i25);
      for (i25 = 0; i25 < loop_ub; i25++) {
        c_loop_ub = i22 - i21;
        for (i26 = 0; i26 < c_loop_ub; i26++) {
          b_sg->data[i26 + b_sg->size[0] * i25] = sg->data[((i21 + i26) +
            sg->size[0] * i25) - 1];
        }
      }

      i21 = reshapes[0].f1->size[0] * reshapes[0].f1->size[1];
      reshapes[0].f1->size[0] = b_result;
      reshapes[0].f1->size[1] = result;
      emxEnsureCapacity_real_T(reshapes[0].f1, i21);
      for (i21 = 0; i21 < result; i21++) {
        for (i22 = 0; i22 < b_result; i22++) {
          reshapes[0].f1->data[i22 + reshapes[0].f1->size[0] * i21] = b_sg->
            data[i22 + b_result * i21];
        }
      }

      if (empty_non_axis_sizes || (!((b_loop_ub == 0) || (i24 - i23 == 0)))) {
        b_result = b_loop_ub;
      } else {
        b_result = 0;
      }

      i21 = c_sg->size[0] * c_sg->size[1];
      c_sg->size[0] = b_loop_ub;
      c_sg->size[1] = i24 - i23;
      emxEnsureCapacity_real_T(c_sg, i21);
      loop_ub = i24 - i23;
      for (i21 = 0; i21 < loop_ub; i21++) {
        for (i22 = 0; i22 < b_loop_ub; i22++) {
          c_sg->data[i22 + c_sg->size[0] * i21] = sg->data[i22 + sg->size[0] *
            ((i23 + i21) - 1)];
        }
      }

      i21 = sbg->size[0] * sbg->size[1];
      sbg->size[0] = reshapes[0].f1->size[0] + b_result;
      sbg->size[1] = reshapes[0].f1->size[1];
      emxEnsureCapacity_real_T(sbg, i21);
      loop_ub = reshapes[0].f1->size[1];
      for (i21 = 0; i21 < loop_ub; i21++) {
        b_loop_ub = reshapes[0].f1->size[0];
        for (i22 = 0; i22 < b_loop_ub; i22++) {
          sbg->data[i22 + sbg->size[0] * i21] = reshapes[0].f1->data[i22 +
            reshapes[0].f1->size[0] * i21];
        }
      }

      for (i21 = 0; i21 < result; i21++) {
        for (i22 = 0; i22 < b_result; i22++) {
          sbg->data[(i22 + reshapes[0].f1->size[0]) + sbg->size[0] * i21] =
            c_sg->data[i22 + b_result * i21];
        }
      }
    }
  }

  emxFree_real_T(&c_sg);
  emxFree_real_T(&b_sg);
  emxFreeMatrix_cell_wrap_1(reshapes);
}

/*
 * File trailer for SelectByHour.c
 *
 * [EOF]
 */
