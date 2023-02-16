package com.microtech.aidexx.db

import android.content.Context
import com.microtech.aidexx.db.entity.MyObjectBox
import io.objectbox.BoxStore

/**
 *@date 2023/2/15
 *@author Hugh
 *@desc
 */
object ObjectBox {
    lateinit var store: BoxStore
        private set

    fun init(context: Context) {
        store = MyObjectBox.builder()
            .androidContext(context.applicationContext)
            .build()
    }
}