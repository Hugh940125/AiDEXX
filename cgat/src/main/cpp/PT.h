/*
 * Academic License - for use in teaching, academic research, and meeting
 * course requirements at degree granting institutions only.  Not for
 * government, commercial, or other organizational use.
 * File: PT.h
 *
 * MATLAB Coder version            : 3.4
 * C/C++ source code generated on  : 30-Jun-2022 13:42:41
 */

#ifndef PT_H
#define PT_H

/* Include Files */
#include <math.h>
#include <stddef.h>
#include <stdlib.h>
#include <string.h>
#include "rt_nonfinite.h"
#include "rtwtypes.h"
#include "datools_types.h"

/* Function Declarations */
extern void PT(const emxArray_real_T *sg, const emxArray_real_T *v,
               emxArray_real_T *pt);
extern void b_PT(const emxArray_real_T *sg, emxArray_real_T *pt);

#endif

/*
 * File trailer for PT.h
 *
 * [EOF]
 */