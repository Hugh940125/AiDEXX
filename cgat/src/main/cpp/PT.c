/*
 * Academic License - for use in teaching, academic research, and meeting
 * course requirements at degree granting institutions only.  Not for
 * government, commercial, or other organizational use.
 * File: PT.c
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
#include "sum.h"
#include "datools_emxutil.h"
#include "rdivide.h"

/* Function Definitions */

/*
 * Arguments    : const emxArray_real_T *sg
 *                const emxArray_real_T *v
 *                emxArray_real_T *pt
 * Return Type  : void
 */
void PT(const emxArray_real_T *sg, const emxArray_real_T *v, emxArray_real_T *pt)
{
  int d;
  int p;
  int n;
  int iyStart;
  int outsize_idx_1;
  int i;
  emxArray_real_T *sgi;
  emxArray_boolean_T *count;
  emxArray_real_T *r38;
  emxArray_real_T *r39;
  emxArray_real_T *a;
  emxArray_real_T *b_y1;
  emxArray_real_T *y;
  emxArray_boolean_T *r40;
  emxArray_boolean_T *r41;
  emxArray_real_T *r42;
  int outsize_idx_0;
  int b_outsize_idx_0;
  double pi;
  boolean_T b_p;
  int tmp1;
  int dimSize;
  int r;
  unsigned int xSize[2];
  int ixLead;
  int iyLead;
  int work_data_idx_0;
  int m;
  int tmp2;
  d = sg->size[1];
  p = sg->size[0];
  n = v->size[0];
  iyStart = pt->size[0] * pt->size[1];
  pt->size[0] = v->size[0] - 1;
  pt->size[1] = sg->size[1] + 1;
  emxEnsureCapacity_real_T(pt, iyStart);
  outsize_idx_1 = (v->size[0] - 1) * (sg->size[1] + 1);
  for (iyStart = 0; iyStart < outsize_idx_1; iyStart++) {
    pt->data[iyStart] = 0.0;
  }

  i = 0;
  emxInit_real_T(&sgi, 1);
  emxInit_boolean_T1(&count, 2);
  emxInit_real_T1(&r38, 2);
  emxInit_real_T1(&r39, 2);
  emxInit_real_T1(&a, 2);
  emxInit_real_T1(&b_y1, 2);
  emxInit_real_T(&y, 1);
  emxInit_boolean_T(&r40, 1);
  emxInit_boolean_T(&r41, 1);
  emxInit_real_T(&r42, 1);
  if (0 <= sg->size[1]) {
    outsize_idx_0 = n;
    b_outsize_idx_0 = v->size[0];
  }

  while (i <= sg->size[1]) {
    if (1.0 + (double)i > d) {
      iyStart = sgi->size[0];
      sgi->size[0] = sg->size[0] * sg->size[1];
      emxEnsureCapacity_real_T1(sgi, iyStart);
      outsize_idx_1 = sg->size[0] * sg->size[1];
      for (iyStart = 0; iyStart < outsize_idx_1; iyStart++) {
        sgi->data[iyStart] = sg->data[iyStart];
      }

      pi = (double)p * (double)d;
    } else {
      outsize_idx_1 = sg->size[0];
      iyStart = sgi->size[0];
      sgi->size[0] = outsize_idx_1;
      emxEnsureCapacity_real_T1(sgi, iyStart);
      for (iyStart = 0; iyStart < outsize_idx_1; iyStart++) {
        sgi->data[iyStart] = sg->data[iyStart + sg->size[0] * i];
      }

      pi = p;
    }

    iyStart = a->size[0] * a->size[1];
    a->size[0] = 1;
    a->size[1] = sgi->size[0];
    emxEnsureCapacity_real_T(a, iyStart);
    outsize_idx_1 = sgi->size[0];
    for (iyStart = 0; iyStart < outsize_idx_1; iyStart++) {
      a->data[a->size[0] * iyStart] = sgi->data[iyStart];
    }

    outsize_idx_1 = a->size[1];
    iyStart = r38->size[0] * r38->size[1];
    r38->size[0] = outsize_idx_0;
    r38->size[1] = outsize_idx_1;
    emxEnsureCapacity_real_T(r38, iyStart);
    if (!(a->size[1] == 0)) {
      if (outsize_idx_0 == 0) {
        b_p = true;
      } else if (outsize_idx_1 == 0) {
        b_p = true;
      } else {
        b_p = false;
      }

      if (!b_p) {
        for (outsize_idx_1 = 0; outsize_idx_1 + 1 <= a->size[1]; outsize_idx_1++)
        {
          iyStart = outsize_idx_1 * n;
          for (tmp1 = 1; tmp1 <= n; tmp1++) {
            r38->data[(iyStart + tmp1) - 1] = a->data[outsize_idx_1];
          }
        }
      }
    }

    iyStart = r39->size[0] * r39->size[1];
    r39->size[0] = b_outsize_idx_0;
    r39->size[1] = (int)pi;
    emxEnsureCapacity_real_T(r39, iyStart);
    if (!(v->size[0] == 0)) {
      if (b_outsize_idx_0 == 0) {
        b_p = true;
      } else if ((int)pi == 0) {
        b_p = true;
      } else {
        b_p = false;
      }

      if (!b_p) {
        outsize_idx_1 = v->size[0];
        for (iyStart = 1; iyStart <= (int)pi; iyStart++) {
          tmp1 = (iyStart - 1) * outsize_idx_1;
          for (r = 1; r <= outsize_idx_1; r++) {
            r39->data[(tmp1 + r) - 1] = v->data[r - 1];
          }
        }
      }
    }

    iyStart = count->size[0] * count->size[1];
    count->size[0] = r38->size[0];
    count->size[1] = r38->size[1];
    emxEnsureCapacity_boolean_T1(count, iyStart);
    outsize_idx_1 = r38->size[0] * r38->size[1];
    for (iyStart = 0; iyStart < outsize_idx_1; iyStart++) {
      count->data[iyStart] = (r38->data[iyStart] >= r39->data[iyStart]);
    }

    dimSize = count->size[0];
    if (count->size[0] == 0) {
      for (iyStart = 0; iyStart < 2; iyStart++) {
        xSize[iyStart] = (unsigned int)count->size[iyStart];
      }

      iyStart = b_y1->size[0] * b_y1->size[1];
      b_y1->size[0] = 0;
      b_y1->size[1] = (int)xSize[1];
      emxEnsureCapacity_real_T(b_y1, iyStart);
    } else {
      outsize_idx_1 = count->size[0] - 1;
      if (!(outsize_idx_1 < 1)) {
        outsize_idx_1 = 1;
      }

      if (outsize_idx_1 < 1) {
        for (iyStart = 0; iyStart < 2; iyStart++) {
          xSize[iyStart] = (unsigned int)count->size[iyStart];
        }

        iyStart = b_y1->size[0] * b_y1->size[1];
        b_y1->size[0] = 0;
        b_y1->size[1] = (int)xSize[1];
        emxEnsureCapacity_real_T(b_y1, iyStart);
      } else {
        outsize_idx_1 = count->size[0] - 1;
        iyStart = b_y1->size[0] * b_y1->size[1];
        b_y1->size[0] = outsize_idx_1;
        b_y1->size[1] = count->size[1];
        emxEnsureCapacity_real_T(b_y1, iyStart);
        if (!((b_y1->size[0] == 0) || (b_y1->size[1] == 0))) {
          outsize_idx_1 = 0;
          iyStart = 0;
          for (r = 1; r <= count->size[1]; r++) {
            ixLead = outsize_idx_1 + 1;
            iyLead = iyStart;
            work_data_idx_0 = count->data[outsize_idx_1];
            for (m = 2; m <= dimSize; m++) {
              tmp1 = count->data[ixLead];
              tmp2 = work_data_idx_0;
              work_data_idx_0 = tmp1;
              tmp1 -= tmp2;
              ixLead++;
              b_y1->data[iyLead] = tmp1;
              iyLead++;
            }

            outsize_idx_1 += dimSize;
            iyStart = (iyStart + dimSize) - 1;
          }
        }
      }
    }

    iyStart = b_y1->size[0] * b_y1->size[1];
    emxEnsureCapacity_real_T(b_y1, iyStart);
    outsize_idx_1 = b_y1->size[0];
    iyStart = b_y1->size[1];
    outsize_idx_1 *= iyStart;
    for (iyStart = 0; iyStart < outsize_idx_1; iyStart++) {
      b_y1->data[iyStart] = -b_y1->data[iyStart];
    }

    if ((b_y1->size[0] == 0) || (b_y1->size[1] == 0)) {
      for (iyStart = 0; iyStart < 2; iyStart++) {
        xSize[iyStart] = (unsigned int)b_y1->size[iyStart];
      }

      iyStart = y->size[0];
      y->size[0] = (int)xSize[0];
      emxEnsureCapacity_real_T1(y, iyStart);
      outsize_idx_1 = (int)xSize[0];
      for (iyStart = 0; iyStart < outsize_idx_1; iyStart++) {
        y->data[iyStart] = 0.0;
      }
    } else {
      tmp1 = b_y1->size[0];
      iyStart = y->size[0];
      y->size[0] = b_y1->size[0];
      emxEnsureCapacity_real_T1(y, iyStart);
      for (outsize_idx_1 = 0; outsize_idx_1 + 1 <= tmp1; outsize_idx_1++) {
        y->data[outsize_idx_1] = (signed char)b_y1->data[outsize_idx_1];
      }

      for (r = 2; r <= b_y1->size[1]; r++) {
        iyStart = (r - 1) * tmp1;
        for (outsize_idx_1 = 0; outsize_idx_1 + 1 <= tmp1; outsize_idx_1++) {
          y->data[outsize_idx_1] += (double)(signed char)b_y1->data[iyStart +
            outsize_idx_1];
        }
      }
    }

    iyStart = r40->size[0];
    r40->size[0] = sgi->size[0];
    emxEnsureCapacity_boolean_T(r40, iyStart);
    outsize_idx_1 = sgi->size[0];
    for (iyStart = 0; iyStart < outsize_idx_1; iyStart++) {
      r40->data[iyStart] = rtIsNaN(sgi->data[iyStart]);
    }

    iyStart = r41->size[0];
    r41->size[0] = r40->size[0];
    emxEnsureCapacity_boolean_T(r41, iyStart);
    outsize_idx_1 = r40->size[0];
    for (iyStart = 0; iyStart < outsize_idx_1; iyStart++) {
      r41->data[iyStart] = !r40->data[iyStart];
    }

    iyStart = r42->size[0];
    r42->size[0] = y->size[0];
    emxEnsureCapacity_real_T1(r42, iyStart);
    outsize_idx_1 = y->size[0];
    for (iyStart = 0; iyStart < outsize_idx_1; iyStart++) {
      r42->data[iyStart] = 100.0 * y->data[iyStart];
    }

    rdivide(r42, c_sum(r41), y);
    outsize_idx_1 = y->size[0];
    for (iyStart = 0; iyStart < outsize_idx_1; iyStart++) {
      pt->data[iyStart + pt->size[0] * i] = y->data[iyStart];
    }

    i++;
  }

  emxFree_real_T(&r42);
  emxFree_boolean_T(&r41);
  emxFree_boolean_T(&r40);
  emxFree_real_T(&y);
  emxFree_real_T(&b_y1);
  emxFree_real_T(&a);
  emxFree_real_T(&r39);
  emxFree_real_T(&r38);
  emxFree_boolean_T(&count);
  emxFree_real_T(&sgi);
}

/*
 * Arguments    : const emxArray_real_T *sg
 *                emxArray_real_T *pt
 * Return Type  : void
 */
void b_PT(const emxArray_real_T *sg, emxArray_real_T *pt)
{
  int d;
  int p;
  int work;
  int outsize_idx_1;
  int i;
  emxArray_real_T *sgi;
  emxArray_boolean_T *count;
  emxArray_real_T *r30;
  emxArray_real_T *r31;
  emxArray_real_T *a;
  emxArray_real_T *b_y1;
  emxArray_boolean_T *r32;
  emxArray_boolean_T *r33;
  double pi;
  int ixStart;
  static const double b_a[5] = { 0.0, 3.9, 8.9, 10.0, 30.0 };

  int iyStart;
  int r;
  int ixLead;
  int iyLead;
  int m;
  double y[4];
  d = sg->size[1];
  p = sg->size[0];
  work = pt->size[0] * pt->size[1];
  pt->size[0] = 4;
  pt->size[1] = sg->size[1] + 1;
  emxEnsureCapacity_real_T(pt, work);
  outsize_idx_1 = (sg->size[1] + 1) << 2;
  for (work = 0; work < outsize_idx_1; work++) {
    pt->data[work] = 0.0;
  }

  i = 0;
  emxInit_real_T(&sgi, 1);
  emxInit_boolean_T1(&count, 2);
  emxInit_real_T1(&r30, 2);
  emxInit_real_T1(&r31, 2);
  emxInit_real_T1(&a, 2);
  emxInit_real_T1(&b_y1, 2);
  emxInit_boolean_T(&r32, 1);
  emxInit_boolean_T(&r33, 1);
  while (i <= sg->size[1]) {
    if (1.0 + (double)i > d) {
      work = sgi->size[0];
      sgi->size[0] = sg->size[0] * sg->size[1];
      emxEnsureCapacity_real_T1(sgi, work);
      outsize_idx_1 = sg->size[0] * sg->size[1];
      for (work = 0; work < outsize_idx_1; work++) {
        sgi->data[work] = sg->data[work];
      }

      pi = (double)p * (double)d;
    } else {
      outsize_idx_1 = sg->size[0];
      work = sgi->size[0];
      sgi->size[0] = outsize_idx_1;
      emxEnsureCapacity_real_T1(sgi, work);
      for (work = 0; work < outsize_idx_1; work++) {
        sgi->data[work] = sg->data[work + sg->size[0] * i];
      }

      pi = p;
    }

    work = a->size[0] * a->size[1];
    a->size[0] = 1;
    a->size[1] = sgi->size[0];
    emxEnsureCapacity_real_T(a, work);
    outsize_idx_1 = sgi->size[0];
    for (work = 0; work < outsize_idx_1; work++) {
      a->data[a->size[0] * work] = sgi->data[work];
    }

    outsize_idx_1 = a->size[1];
    work = r30->size[0] * r30->size[1];
    r30->size[0] = 5;
    r30->size[1] = outsize_idx_1;
    emxEnsureCapacity_real_T(r30, work);
    if ((!(a->size[1] == 0)) && (!(outsize_idx_1 == 0))) {
      for (outsize_idx_1 = 0; outsize_idx_1 + 1 <= a->size[1]; outsize_idx_1++)
      {
        work = outsize_idx_1 * 5;
        for (ixStart = 0; ixStart < 5; ixStart++) {
          r30->data[work + ixStart] = a->data[outsize_idx_1];
        }
      }
    }

    work = r31->size[0] * r31->size[1];
    r31->size[0] = 5;
    r31->size[1] = (int)pi;
    emxEnsureCapacity_real_T(r31, work);
    if (!((int)pi == 0)) {
      for (outsize_idx_1 = 1; outsize_idx_1 <= (int)pi; outsize_idx_1++) {
        work = (outsize_idx_1 - 1) * 5;
        for (ixStart = 0; ixStart < 5; ixStart++) {
          r31->data[work + ixStart] = b_a[ixStart];
        }
      }
    }

    work = count->size[0] * count->size[1];
    count->size[0] = 5;
    count->size[1] = r30->size[1];
    emxEnsureCapacity_boolean_T1(count, work);
    outsize_idx_1 = r30->size[0] * r30->size[1];
    for (work = 0; work < outsize_idx_1; work++) {
      count->data[work] = (r30->data[work] >= r31->data[work]);
    }

    work = b_y1->size[0] * b_y1->size[1];
    b_y1->size[0] = 4;
    b_y1->size[1] = count->size[1];
    emxEnsureCapacity_real_T(b_y1, work);
    if (!(b_y1->size[1] == 0)) {
      ixStart = 1;
      iyStart = 0;
      for (r = 1; r <= count->size[1]; r++) {
        ixLead = ixStart;
        iyLead = iyStart;
        work = count->data[ixStart - 1];
        for (m = 0; m < 4; m++) {
          outsize_idx_1 = work;
          work = count->data[ixLead];
          outsize_idx_1 = count->data[ixLead] - outsize_idx_1;
          ixLead++;
          b_y1->data[iyLead] = outsize_idx_1;
          iyLead++;
        }

        ixStart += 5;
        iyStart += 4;
      }
    }

    work = b_y1->size[0] * b_y1->size[1];
    b_y1->size[0] = 4;
    emxEnsureCapacity_real_T(b_y1, work);
    outsize_idx_1 = b_y1->size[0];
    work = b_y1->size[1];
    outsize_idx_1 *= work;
    for (work = 0; work < outsize_idx_1; work++) {
      b_y1->data[work] = -b_y1->data[work];
    }

    if (b_y1->size[1] == 0) {
      for (outsize_idx_1 = 0; outsize_idx_1 < 4; outsize_idx_1++) {
        y[outsize_idx_1] = 0.0;
      }
    } else {
      for (outsize_idx_1 = 0; outsize_idx_1 < 4; outsize_idx_1++) {
        y[outsize_idx_1] = (signed char)b_y1->data[outsize_idx_1];
      }

      for (ixStart = 2; ixStart <= b_y1->size[1]; ixStart++) {
        work = (ixStart - 1) << 2;
        for (outsize_idx_1 = 0; outsize_idx_1 < 4; outsize_idx_1++) {
          pi = y[outsize_idx_1] + (double)(signed char)b_y1->data[work +
            outsize_idx_1];
          y[outsize_idx_1] = pi;
        }
      }
    }

    work = r32->size[0];
    r32->size[0] = sgi->size[0];
    emxEnsureCapacity_boolean_T(r32, work);
    outsize_idx_1 = sgi->size[0];
    for (work = 0; work < outsize_idx_1; work++) {
      r32->data[work] = rtIsNaN(sgi->data[work]);
    }

    work = r33->size[0];
    r33->size[0] = r32->size[0];
    emxEnsureCapacity_boolean_T(r33, work);
    outsize_idx_1 = r32->size[0];
    for (work = 0; work < outsize_idx_1; work++) {
      r33->data[work] = !r32->data[work];
    }

    pi = c_sum(r33);
    for (work = 0; work < 4; work++) {
      pt->data[work + pt->size[0] * i] = 100.0 * y[work] / pi;
    }

    i++;
  }

  emxFree_boolean_T(&r33);
  emxFree_boolean_T(&r32);
  emxFree_real_T(&b_y1);
  emxFree_real_T(&a);
  emxFree_real_T(&r31);
  emxFree_real_T(&r30);
  emxFree_boolean_T(&count);
  emxFree_real_T(&sgi);
}

/*
 * File trailer for PT.c
 *
 * [EOF]
 */
