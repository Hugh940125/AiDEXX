/*
 * Academic License - for use in teaching, academic research, and meeting
 * course requirements at degree granting institutions only.  Not for
 * government, commercial, or other organizational use.
 * File: MBG.c
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
 *                emxArray_real_T *mbg
 * Return Type  : void
 */
void MBG(const emxArray_real_T *sg, emxArray_real_T *mbg)
{
  emxArray_real_T *r26;
  int b_sg[1];
  emxArray_real_T c_sg;
  double d2;
  int i13;
  int loop_ub;
  emxInit_real_T1(&r26, 2);
  d_nanmean(sg, r26);
  b_sg[0] = sg->size[0] * sg->size[1];
  c_sg = *sg;
  c_sg.size = (int *)&b_sg;
  c_sg.numDimensions = 1;
  d2 = c_nanmean(&c_sg);
  i13 = mbg->size[0] * mbg->size[1];
  mbg->size[0] = 1;
  mbg->size[1] = r26->size[1] + 1;
  emxEnsureCapacity_real_T(mbg, i13);
  loop_ub = r26->size[1];
  for (i13 = 0; i13 < loop_ub; i13++) {
    mbg->data[mbg->size[0] * i13] = r26->data[r26->size[0] * i13];
  }

  mbg->data[mbg->size[0] * r26->size[1]] = d2;
  emxFree_real_T(&r26);
}

/*
 * File trailer for MBG.c
 *
 * [EOF]
 */
