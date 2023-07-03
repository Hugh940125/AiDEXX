/*
 * Academic License - for use in teaching, academic research, and meeting
 * course requirements at degree granting institutions only.  Not for
 * government, commercial, or other organizational use.
 * File: SAFilter.c
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
#include "nanmean.h"
#include "datools_emxutil.h"
#include "nansum.h"
#include "abs.h"
#include "diff.h"

/* Function Definitions */

/*
 * Arguments    : const emxArray_real_T *sg
 *                double minwin
 *                double maxwin
 *                emxArray_real_T *fsg
 * Return Type  : void
 */
void SAFilter(const emxArray_real_T *sg, double minwin, double maxwin,
              emxArray_real_T *fsg)
{
  emxArray_real_T *bg;
  double hmaxw;
  double hminw;
  double ndbl;
  double apnd;
  int i19;
  int nm1d2;
  int i;
  emxArray_real_T *ni;
  emxArray_real_T *r43;
  emxArray_real_T *b_bg;
  double b_i;
  int i20;
  emxArray_boolean_T *r44;
  double cdiff;
  double absa;
  double absb;
  int ixstart;
  emxArray_int32_T *r45;
  int n;
  boolean_T exitg1;
  emxInit_real_T(&bg, 1);
  hmaxw = floor(maxwin / 2.0);
  hminw = floor(minwin / 2.0);
  ndbl = sg->data[0];
  apnd = sg->data[sg->size[0] * sg->size[1] - 1];
  i19 = bg->size[0];
  bg->size[0] = ((int)hmaxw + sg->size[0] * sg->size[1]) + (int)hmaxw;
  emxEnsureCapacity_real_T1(bg, i19);
  nm1d2 = (int)hmaxw;
  for (i19 = 0; i19 < nm1d2; i19++) {
    bg->data[i19] = ndbl;
  }

  nm1d2 = sg->size[0] * sg->size[1];
  for (i19 = 0; i19 < nm1d2; i19++) {
    bg->data[i19 + (int)hmaxw] = sg->data[i19];
  }

  nm1d2 = (int)hmaxw;
  for (i19 = 0; i19 < nm1d2; i19++) {
    bg->data[(i19 + (int)hmaxw) + sg->size[0] * sg->size[1]] = apnd;
  }

  i19 = fsg->size[0] * fsg->size[1];
  fsg->size[0] = sg->size[0];
  fsg->size[1] = sg->size[1];
  emxEnsureCapacity_real_T(fsg, i19);
  nm1d2 = sg->size[0] * sg->size[1];
  for (i19 = 0; i19 < nm1d2; i19++) {
    fsg->data[i19] = sg->data[i19];
  }

  i19 = (int)(((double)bg->size[0] - hmaxw) + (1.0 - (hmaxw + 1.0)));
  i = 0;
  emxInit_real_T1(&ni, 2);
  emxInit_real_T(&r43, 1);
  emxInit_real_T(&b_bg, 1);
  while (i <= i19 - 1) {
    b_i = (hmaxw + 1.0) + (double)i;
    if (b_i + 1.0 < b_i - 1.0) {
      i20 = ni->size[0] * ni->size[1];
      ni->size[0] = 1;
      ni->size[1] = 0;
      emxEnsureCapacity_real_T(ni, i20);
    } else if ((rtIsInf(b_i - 1.0) || rtIsInf(b_i + 1.0)) && (b_i - 1.0 == b_i +
                1.0)) {
      i20 = ni->size[0] * ni->size[1];
      ni->size[0] = 1;
      ni->size[1] = 1;
      emxEnsureCapacity_real_T(ni, i20);
      ni->data[0] = rtNaN;
    } else if (b_i - 1.0 == b_i - 1.0) {
      i20 = ni->size[0] * ni->size[1];
      ni->size[0] = 1;
      ni->size[1] = (int)((b_i + 1.0) - (b_i - 1.0)) + 1;
      emxEnsureCapacity_real_T(ni, i20);
      nm1d2 = (int)((b_i + 1.0) - (b_i - 1.0));
      for (i20 = 0; i20 <= nm1d2; i20++) {
        ni->data[ni->size[0] * i20] = (b_i - 1.0) + (double)i20;
      }
    } else {
      ndbl = floor(((b_i + 1.0) - (b_i - 1.0)) + 0.5);
      apnd = (b_i - 1.0) + ndbl;
      cdiff = apnd - (b_i + 1.0);
      absa = fabs(b_i - 1.0);
      absb = fabs(b_i + 1.0);
      if ((absa > absb) || rtIsNaN(absb)) {
        absb = absa;
      }

      if (fabs(cdiff) < 4.4408920985006262E-16 * absb) {
        ndbl++;
        apnd = b_i + 1.0;
      } else if (cdiff > 0.0) {
        apnd = (b_i - 1.0) + (ndbl - 1.0);
      } else {
        ndbl++;
      }

      if (ndbl >= 0.0) {
        n = (int)ndbl;
      } else {
        n = 0;
      }

      i20 = ni->size[0] * ni->size[1];
      ni->size[0] = 1;
      ni->size[1] = n;
      emxEnsureCapacity_real_T(ni, i20);
      if (n > 0) {
        ni->data[0] = b_i - 1.0;
        if (n > 1) {
          ni->data[n - 1] = apnd;
          nm1d2 = (n - 1) / 2;
          for (ixstart = 1; ixstart < nm1d2; ixstart++) {
            ni->data[ixstart] = (b_i - 1.0) + (double)ixstart;
            ni->data[(n - ixstart) - 1] = apnd - (double)ixstart;
          }

          if (nm1d2 << 1 == n - 1) {
            ni->data[nm1d2] = ((b_i - 1.0) + apnd) / 2.0;
          } else {
            ni->data[nm1d2] = (b_i - 1.0) + (double)nm1d2;
            ni->data[nm1d2 + 1] = apnd - (double)nm1d2;
          }
        }
      }
    }

    i20 = b_bg->size[0];
    b_bg->size[0] = ni->size[1];
    emxEnsureCapacity_real_T1(b_bg, i20);
    nm1d2 = ni->size[1];
    for (i20 = 0; i20 < nm1d2; i20++) {
      b_bg->data[i20] = bg->data[(int)ni->data[ni->size[0] * i20] - 1];
    }

    diff(b_bg, r43);
    b_abs(r43, b_bg);
    ixstart = 1;
    n = b_bg->size[0];
    apnd = b_bg->data[0];
    if (b_bg->size[0] > 1) {
      if (rtIsNaN(b_bg->data[0])) {
        nm1d2 = 2;
        exitg1 = false;
        while ((!exitg1) && (nm1d2 <= n)) {
          ixstart = nm1d2;
          if (!rtIsNaN(b_bg->data[nm1d2 - 1])) {
            apnd = b_bg->data[nm1d2 - 1];
            exitg1 = true;
          } else {
            nm1d2++;
          }
        }
      }

      if (ixstart < b_bg->size[0]) {
        while (ixstart + 1 <= n) {
          if (b_bg->data[ixstart] > apnd) {
            apnd = b_bg->data[ixstart];
          }

          ixstart++;
        }
      }
    }

    i20 = b_bg->size[0];
    b_bg->size[0] = ni->size[1];
    emxEnsureCapacity_real_T1(b_bg, i20);
    nm1d2 = ni->size[1];
    for (i20 = 0; i20 < nm1d2; i20++) {
      b_bg->data[i20] = bg->data[(int)ni->data[ni->size[0] * i20] - 1];
    }

    diff(b_bg, r43);
    b_abs(r43, b_bg);
    ndbl = nansum(b_bg);
    apnd *= 2.0;
    if ((ndbl > apnd) || rtIsNaN(apnd)) {
    } else {
      ndbl = apnd;
    }

    if (ndbl > 10.0) {
      ndbl = 10.0;
    }

    if ((ndbl < 1.0) || rtIsNaN(ndbl)) {
      ndbl = 1.0;
    }

    apnd = floor(hmaxw * ndbl / 10.0);
    if ((apnd > hminw) || rtIsNaN(hminw)) {
    } else {
      apnd = hminw;
    }

    if (apnd == 0.0) {
      fsg->data[(int)(b_i - hmaxw) - 1] = bg->data[(int)b_i - 1];
    } else {
      cdiff = b_i - apnd;
      ndbl = b_i + apnd;
      if (cdiff > ndbl) {
        i20 = 0;
        ixstart = 0;
      } else {
        i20 = (int)cdiff - 1;
        ixstart = (int)ndbl;
      }

      nm1d2 = b_bg->size[0];
      b_bg->size[0] = ixstart - i20;
      emxEnsureCapacity_real_T1(b_bg, nm1d2);
      nm1d2 = ixstart - i20;
      for (ixstart = 0; ixstart < nm1d2; ixstart++) {
        b_bg->data[ixstart] = bg->data[i20 + ixstart];
      }

      fsg->data[(int)(b_i - hmaxw) - 1] = c_nanmean(b_bg);
    }

    i++;
  }

  emxFree_real_T(&b_bg);
  emxFree_real_T(&r43);
  emxFree_real_T(&ni);
  emxFree_real_T(&bg);
  emxInit_boolean_T1(&r44, 2);
  i19 = r44->size[0] * r44->size[1];
  r44->size[0] = sg->size[0];
  r44->size[1] = sg->size[1];
  emxEnsureCapacity_boolean_T1(r44, i19);
  nm1d2 = sg->size[0] * sg->size[1];
  for (i19 = 0; i19 < nm1d2; i19++) {
    r44->data[i19] = rtIsNaN(sg->data[i19]);
  }

  ixstart = r44->size[0] * r44->size[1] - 1;
  nm1d2 = 0;
  for (i = 0; i <= ixstart; i++) {
    if (r44->data[i]) {
      nm1d2++;
    }
  }

  emxInit_int32_T1(&r45, 1);
  i19 = r45->size[0];
  r45->size[0] = nm1d2;
  emxEnsureCapacity_int32_T1(r45, i19);
  nm1d2 = 0;
  for (i = 0; i <= ixstart; i++) {
    if (r44->data[i]) {
      r45->data[nm1d2] = i + 1;
      nm1d2++;
    }
  }

  emxFree_boolean_T(&r44);
  nm1d2 = r45->size[0];
  for (i19 = 0; i19 < nm1d2; i19++) {
    fsg->data[r45->data[i19] - 1] = rtNaN;
  }

  emxFree_int32_T(&r45);
}

/*
 * Arguments    : const emxArray_real_T *sg
 *                emxArray_real_T *fsg
 * Return Type  : void
 */
void b_SAFilter(const emxArray_real_T *sg, emxArray_real_T *fsg)
{
  emxArray_real_T *bg;
  double b_sg;
  double c_sg;
  int i12;
  int loop_ub;
  int i;
  emxArray_real_T *b_bg;
  emxArray_boolean_T *r21;
  int end;
  emxArray_int32_T *r22;
  emxInit_real_T(&bg, 1);
  b_sg = sg->data[0];
  c_sg = sg->data[sg->size[0] * sg->size[1] - 1];
  i12 = bg->size[0];
  bg->size[0] = 4 + sg->size[0] * sg->size[1];
  emxEnsureCapacity_real_T1(bg, i12);
  for (i12 = 0; i12 < 2; i12++) {
    bg->data[i12] = b_sg;
  }

  loop_ub = sg->size[0] * sg->size[1];
  for (i12 = 0; i12 < loop_ub; i12++) {
    bg->data[i12 + 2] = sg->data[i12];
  }

  for (i12 = 0; i12 < 2; i12++) {
    bg->data[(i12 + sg->size[0] * sg->size[1]) + 2] = c_sg;
  }

  i12 = fsg->size[0] * fsg->size[1];
  fsg->size[0] = sg->size[0];
  fsg->size[1] = sg->size[1];
  emxEnsureCapacity_real_T(fsg, i12);
  loop_ub = sg->size[0] * sg->size[1];
  for (i12 = 0; i12 < loop_ub; i12++) {
    fsg->data[i12] = sg->data[i12];
  }

  i = 1;
  emxInit_real_T(&b_bg, 1);
  while (i - 1 <= bg->size[0] - 5) {
    if (i > i + 4) {
      i12 = 0;
      end = 0;
    } else {
      i12 = i - 1;
      end = i + 4;
    }

    loop_ub = b_bg->size[0];
    b_bg->size[0] = end - i12;
    emxEnsureCapacity_real_T1(b_bg, loop_ub);
    loop_ub = end - i12;
    for (end = 0; end < loop_ub; end++) {
      b_bg->data[end] = bg->data[i12 + end];
    }

    fsg->data[i - 1] = c_nanmean(b_bg);
    i++;
  }

  emxFree_real_T(&b_bg);
  emxFree_real_T(&bg);
  emxInit_boolean_T1(&r21, 2);
  i12 = r21->size[0] * r21->size[1];
  r21->size[0] = sg->size[0];
  r21->size[1] = sg->size[1];
  emxEnsureCapacity_boolean_T1(r21, i12);
  loop_ub = sg->size[0] * sg->size[1];
  for (i12 = 0; i12 < loop_ub; i12++) {
    r21->data[i12] = rtIsNaN(sg->data[i12]);
  }

  end = r21->size[0] * r21->size[1] - 1;
  loop_ub = 0;
  for (i = 0; i <= end; i++) {
    if (r21->data[i]) {
      loop_ub++;
    }
  }

  emxInit_int32_T1(&r22, 1);
  i12 = r22->size[0];
  r22->size[0] = loop_ub;
  emxEnsureCapacity_int32_T1(r22, i12);
  loop_ub = 0;
  for (i = 0; i <= end; i++) {
    if (r21->data[i]) {
      r22->data[loop_ub] = i + 1;
      loop_ub++;
    }
  }

  emxFree_boolean_T(&r21);
  loop_ub = r22->size[0];
  for (i12 = 0; i12 < loop_ub; i12++) {
    fsg->data[r22->data[i12] - 1] = rtNaN;
  }

  emxFree_int32_T(&r22);
}

/*
 * File trailer for SAFilter.c
 *
 * [EOF]
 */
