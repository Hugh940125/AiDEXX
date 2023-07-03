/*
 * Academic License - for use in teaching, academic research, and meeting
 * course requirements at degree granting institutions only.  Not for
 * government, commercial, or other organizational use.
 * File: CV.c
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
 * Arguments    : const emxArray_real_T *mbg
 *                const emxArray_real_T *sdbg
 *                emxArray_real_T *cv
 * Return Type  : void
 */
void CV(const emxArray_real_T *mbg, const emxArray_real_T *sdbg, emxArray_real_T
        *cv)
{
  int i4;
  int loop_ub;
  i4 = cv->size[0] * cv->size[1];
  cv->size[0] = 1;
  cv->size[1] = sdbg->size[1];
  emxEnsureCapacity_real_T(cv, i4);
  loop_ub = sdbg->size[0] * sdbg->size[1];
  for (i4 = 0; i4 < loop_ub; i4++) {
    cv->data[i4] = 100.0 * sdbg->data[i4] / mbg->data[i4];
  }
}

/*
 * File trailer for CV.c
 *
 * [EOF]
 */
