package com.microtech.aidexx.base

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.rxLifeScope
import com.microtechmd.cgms.util.LogUtils
//import com.microtechmd.cgms.util.code
//import com.microtechmd.cgms.util.msg
import kotlinx.coroutines.CoroutineScope
//import org.json.JSONObject
//import rxhttp.toStr

@Suppress("UNCHECKED_CAST")
open class BaseViewModel : ViewModel() {
    var errData = MutableLiveData<Throwable>()
    var showLoadData = MutableLiveData<String>()
    var dismissLoadData = MutableLiveData<String>()

    fun doRequest(request: suspend (CoroutineScope) -> Unit) {

        rxLifeScope.launch(request,
            {
                //失败回调
                LogUtils.eAiDex("fail :" + it)
                errData.value = it
            },
            {
                //开始回调
                showLoadData.value = ""
            },
            {
                //结束回调
                dismissLoadData.value = ""
            })
    }
}