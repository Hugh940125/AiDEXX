/*
 * Academic License - for use in teaching, academic research, and meeting
 * course requirements at degree granting institutions only.  Not for
 * government, commercial, or other organizational use.
 * File: JINDEX.c
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
#include "power.h"

/* Function Definitions */

/*
 * Arguments    : const emxArray_real_T *mbg
 *                const emxArray_real_T *sdbg
 *                emxArray_real_T *jindex
 * Return Type  : void
 */
void JINDEX(const emxArray_real_T *mbg, const emxArray_real_T *sdbg,
            emxArray_real_T *jindex)
{
  emxArray_real_T *b_mbg;
  int b_jindex;
  int loop_ub;
  emxInit_real_T1(&b_mbg, 2);
  b_jindex = b_mbg->size[0] * b_mbg->size[1];
  b_mbg->size[0] = 1;
  b_mbg->size[1] = mbg->size[1];
  emxEnsureCapacity_real_T(b_mbg, b_jindex);
  loop_ub = mbg->size[0] * mbg->size[1];
  for (b_jindex = 0; b_jindex < loop_ub; b_jindex++) {
    b_mbg->data[b_jindex] = mbg->data[b_jindex] + sdbg->data[b_jindex];
  }

  power(b_mbg, jindex);
  b_jindex = jindex->size[0] * jindex->size[1];
  jindex->size[0] = 1;
  emxEnsureCapacity_real_T(jindex, b_jindex);
  b_jindex = jindex->size[0];
  loop_ub = jindex->size[1];
  loop_ub *= b_jindex;
  emxFree_real_T(&b_mbg);
  for (b_jindex = 0; b_jindex < loop_ub; b_jindex++) {
    jindex->data[b_jindex] *= 0.324;
  }
}

/*
 * File trailer for JINDEX.c
 *
 * [EOF]
 */
