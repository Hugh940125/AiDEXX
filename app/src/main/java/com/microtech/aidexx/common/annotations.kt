package com.microtech.aidexx.common

import androidx.annotation.IntDef

const val LOGIN_TYPE_VER_CODE = 1
const val LOGIN_TYPE_PWD = 2
const val LOGIN_TYPE_DIRECT = 3
const val LOGIN_TYPE_EMAIL_VER_CODE = 4

@IntDef(LOGIN_TYPE_VER_CODE, LOGIN_TYPE_PWD, LOGIN_TYPE_DIRECT, LOGIN_TYPE_EMAIL_VER_CODE)
annotation class LoginType