package com.microtech.aidexx.common.net

class BizException(val code: Int, override val message: String?): Throwable() {
    override fun toString(): String {
        return "BizException: code=$code, message=$message"
    }
}