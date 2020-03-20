package com.koder.ellenlibrary

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.koder.ellen.ToasterMessage

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ToasterMessage.createToast(this, "Hello world")
    }
}
