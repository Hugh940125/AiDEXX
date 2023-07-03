/*
 * Academic License - for use in teaching, academic research, and meeting
 * course requirements at degree granting institutions only.  Not for
 * government, commercial, or other organizational use.
 * File: MODD.c
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
 *                emxArray_real_T *modd
 * Return Type  : void
 */
void MODD(const emxArray_real_T *sg, emxArray_real_T *modd)
{
  emxArray_real_T *b_y1;
  unsigned int ySize[2];
  int ix;
  int stride;
  int iy;
  int s;
  emxArray_real_T *y;
  int ixLead;
  int iyLead;
  double work;
  int m;
  double tmp1;
  emxArray_real_T *r27;
  emxInit_real_T1(&b_y1, 2);
  if (sg->size[1] <= 1) {
    for (ix = 0; ix < 2; ix++) {
      ySize[ix] = (unsigned int)sg->size[ix];
    }

    ix = b_y1->size[0] * b_y1->size[1];
    b_y1->size[0] = (int)ySize[0];
    b_y1->size[1] = 0;
    emxEnsureCapacity_real_T(b_y1, ix);
  } else {
    ySize[1] = (unsigned int)(sg->size[1] - 1);
    ix = b_y1->size[0] * b_y1->size[1];
    b_y1->size[0] = sg->size[0];
    b_y1->size[1] = (int)ySize[1];
    emxEnsureCapacity_real_T(b_y1, ix);
    if (!(b_y1->size[0] == 0)) {
      stride = sg->size[0];
      ix = 0;
      iy = 0;
      for (s = 1; s <= stride; s++) {
        ixLead = ix + stride;
        iyLead = iy;
        work = sg->data[ix];
        for (m = 2; m <= sg->size[1]; m++) {
          tmp1 = work;
          work = sg->data[ixLead];
          tmp1 = sg->data[ixLead] - tmp1;
          ixLead += stride;
          b_y1->data[iyLead] = tmp1;
          iyLead += stride;
        }

        ix++;
        iy++;
      }
    }
  }

  iy = b_y1->size[0] * b_y1->size[1];
  for (ix = 0; ix < 2; ix++) {
    ySize[ix] = (unsigned int)b_y1->size[ix];
  }

  emxInit_real_T1(&y, 2);
  ix = y->size[0] * y->size[1];
  y->size[0] = (int)ySize[0];
  y->size[1] = (int)ySize[1];
  emxEnsureCapacity_real_T(y, ix);
  for (stride = 0; stride + 1 <= iy; stride++) {
    y->data[stride] = fabs(b_y1->data[stride]);
  }

  emxFree_real_T(&b_y1);
  emxInit_real_T1(&r27, 2);
  d_nanmean(y, modd);
  tmp1 = nanmean(modd);
  ix = r27->size[0] * r27->size[1];
  r27->size[0] = 1;
  r27->size[1] = 2 + modd->size[1];
  emxEnsureCapacity_real_T(r27, ix);
  r27->data[0] = rtNaN;
  stride = modd->size[1];
  emxFree_real_T(&y);
  for (ix = 0; ix < stride; ix++) {
    r27->data[r27->size[0] * (ix + 1)] = modd->data[modd->size[0] * ix];
  }

  r27->data[r27->size[0] * (1 + modd->size[1])] = tmp1;
  ix = modd->size[0] * modd->size[1];
  modd->size[0] = 1;
  modd->size[1] = r27->size[1];
  emxEnsureCapacity_real_T(modd, ix);
  stride = r27->size[1];
  for (ix = 0; ix < stride; ix++) {
    modd->data[modd->size[0] * ix] = r27->data[r27->size[0] * ix];
  }

  emxFree_real_T(&r27);
}

/*
 * File trailer for MODD.c
 *
 * [EOF]
 */
