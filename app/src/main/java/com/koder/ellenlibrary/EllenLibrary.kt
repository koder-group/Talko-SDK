package com.koder.ellenlibrary

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.koder.ellen.Messenger


class EllenLibrary: Application() {

    // Application/Tenant Id
    val appId = "efb94c7e-71e9-406b-9059-61ac05227f1b"

    override fun onCreate() {
        super.onCreate()
//        Messenger.init(appId, this)
    }
}