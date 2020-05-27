package com.koder.ellenlibrary

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Looper
import android.util.Log
import com.koder.ellen.CompletionCallback
import com.koder.ellen.Messenger
import com.koder.ellen.RequestHandler
import com.koder.ellen.data.Result

class EllenLibrary: Application() {

    companion object {
        const val TAG = "EllenLibrary"
    }

    // Application/Tenant Id
    val appId = "efb94c7e-71e9-406b-9059-61ac05227f1b"

    override fun onCreate() {
        super.onCreate()
//        Messenger.init(appId, this)

        // Refreshing user token
        Messenger.addRequestHandler(object: RequestHandler() {
            override fun onRefreshTokenRequest(): String {
                Log.d(TAG, "Refreshing token")

                // TODO Implement functionality to return a refreshed user token
//                Thread.sleep(4000)
                val refreshedToken = ""

                return refreshedToken
            }
        })
    }
}