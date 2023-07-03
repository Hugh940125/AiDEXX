/*
 * Academic License - for use in teaching, academic research, and meeting
 * course requirements at degree granting institutions only.  Not for
 * government, commercial, or other organizational use.
 * File: AUC.h
 *
 * MATLAB Coder version            : 3.4
 * C/C++ source code generated on  : 30-Jun-2022 13:42:41
 */

#ifndef AUC_H
#define AUC_H

/* Include Files */
#include <math.h>
#include <stddef.h>
#include <stdlib.h>
#include <string.h>
#include "rt_nonfinite.h"
#include "rtwtypes.h"
#include "datools_types.h"

/* Function Declarations */
extern void AUC(const emxArray_real_T *sg, double target, emxArray_real_T *auc);
extern void b_AUC(const emxArray_real_T *sg, emxArray_real_T *auc);

#endif

/*
 * File trailer for AUC.h
 *
 * [EOF]
 */
