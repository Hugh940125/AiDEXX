/*
 * Academic License - for use in teaching, academic research, and meeting
 * course requirements at degree granting institutions only.  Not for
 * government, commercial, or other organizational use.
 * File: neg2nan.c
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
 * Arguments    : emxArray_real_T *sg
 * Return Type  : void
 */
void neg2nan(emxArray_real_T *sg)
{
  int end;
  int trueCount;
  int i;
  emxArray_int32_T *r46;
  end = sg->size[0] * sg->size[1] - 1;
  trueCount = 0;
  for (i = 0; i <= end; i++) {
    if (sg->data[i] <= 0.0) {
      trueCount++;
    }
  }

  emxInit_int32_T1(&r46, 1);
  i = r46->size[0];
  r46->size[0] = trueCount;
  emxEnsureCapacity_int32_T1(r46, i);
  trueCount = 0;
  for (i = 0; i <= end; i++) {
    if (sg->data[i] <= 0.0) {
      r46->data[trueCount] = i + 1;
      trueCount++;
    }
  }

  trueCount = r46->size[0];
  for (i = 0; i < trueCount; i++) {
    sg->data[r46->data[i] - 1] = rtNaN;
  }

  emxFree_int32_T(&r46);
}

/*
 * File trailer for neg2nan.c
 *
 * [EOF]
 */
