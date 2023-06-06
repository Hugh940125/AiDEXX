package com.microtech.aidexx.base

typealias AfterLeaveCallback = ()->Unit

interface PageActions {
    fun canLeave(): AfterLeaveCallback?
}