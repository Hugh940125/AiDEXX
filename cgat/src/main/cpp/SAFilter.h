/*
 * Academic License - for use in teaching, academic research, and meeting
 * course requirements at degree granting institutions only.  Not for
 * government, commercial, or other organizational use.
 * File: SAFilter.h
 *
 * MATLAB Coder version            : 3.4
 * C/C++ source code generated on  : 30-Jun-2022 13:42:41
 */

#ifndef SAFILTER_H
#define SAFILTER_H

/* Include Files */
#include <math.h>
#include <stddef.h>
#include <stdlib.h>
#include <string.h>
#include "rt_nonfinite.h"
#include "rtwtypes.h"
#include "datools_types.h"

/* Function Declarations */
extern void SAFilter(const emxArray_real_T *sg, double minwin, double maxwin,
                     emxArray_real_T *fsg);
extern void b_SAFilter(const emxArray_real_T *sg, emxArray_real_T *fsg);

#endif

/*
 * File trailer for SAFilter.h
 *
 * [EOF]
 */
