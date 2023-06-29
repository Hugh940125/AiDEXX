package com.microtech.aidexx.views.dialog.customerservice

import com.microtech.aidexx.utils.mmkv.MmkvManager

class MessageManager private constructor() {
    val addListenerList = mutableListOf<(() -> Unit)?>()
    val clearListenerList = mutableListOf<(() -> Unit)?>()
    var onMessageAdded: (() -> Unit)? = null
    var onMessageClear: (() -> Unit)? = null

    fun setAddMessageListener(listener: (() -> Unit)?) {
        addListenerList.add(listener)
    }

    fun setClearMessageListener(listener: (() -> Unit)?) {
        clearListenerList.add(listener)
    }

    fun removeAllAddMessageListener() {
        addListenerList.clear()
    }

    fun removeAllClearMessageListener() {
        clearListenerList.clear()
    }

    fun removeAddMessageListener(listener: (() -> Unit)?) {
        addListenerList.remove(listener)
    }

    fun removeClearMessageListener(listener: (() -> Unit)?) {
        clearListenerList.remove(listener)
    }

    companion object {
        private val INSTANCE = MessageManager()

        fun instance(): MessageManager {
            return INSTANCE
        }
    }

    fun getMessageCount(): Int {
        return MmkvManager.getOnlineServiceMsgCount()
    }

    fun getMessageCountStr(): String {
        val savedMessageCount = getMessageCount()
        if (savedMessageCount > 99) {
            return "99+"
        }
        if (savedMessageCount == 0) {
            return ""
        }
        return savedMessageCount.toString()
    }

    fun addMessage() {
        var savedMessageCount = getMessageCount()
        savedMessageCount += 1
        MmkvManager.setOnlineServiceMsgCount(savedMessageCount)
        for (listener in addListenerList) {
            listener?.invoke()
        }
    }

    fun clearMessage() {
        MmkvManager.setOnlineServiceMsgCount(0)
        for (listener in clearListenerList) {
            listener?.invoke()
        }
    }
}