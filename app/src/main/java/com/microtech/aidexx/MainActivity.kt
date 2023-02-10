package com.microtech.aidexx

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.microtech.aidexx.base.BaseActivity
import com.microtech.aidexx.base.BaseViewModel
import com.microtech.aidexx.databinding.ActivityMainBinding

class MainActivity : BaseActivity<BaseViewModel,ActivityMainBinding>() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun getViewBinding(): ActivityMainBinding {
    }
}