/*
 * Academic License - for use in teaching, academic research, and meeting
 * course requirements at degree granting institutions only.  Not for
 * government, commercial, or other organizational use.
 * File: sum.h
 *
 * MATLAB Coder version            : 3.4
 * C/C++ source code generated on  : 30-Jun-2022 13:42:41
 */

#ifndef SUM_H
#define SUM_H

/* Include Files */
#include <math.h>
#include <stddef.h>
#include <stdlib.h>
#include <string.h>
#include "rt_nonfinite.h"
#include "rtwtypes.h"
#include "datools_types.h"

/* Function Declarations */
extern void b_sum(const emxArray_real_T *x, double y[2]);
extern double c_sum(const emxArray_boolean_T *x);
extern void d_sum(const emxArray_real_T *x, emxArray_real_T *y);
extern double sum(const emxArray_boolean_T *x);

#endif

/*
 * File trailer for sum.h
 *
 * [EOF]
 */
