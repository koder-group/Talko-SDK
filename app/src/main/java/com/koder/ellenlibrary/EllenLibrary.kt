package com.koder.ellenlibrary

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Looper
import android.util.Log
import com.koder.ellen.Messenger
import com.koder.ellen.RequestHandler


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
                val refreshedToken = "eyJhbGciOiJIUzI1NiIsImtpZCI6IjE3YzRhYmQ4YTE3MjQ0OTdiZmViMjBiMWM0ZDhmYjU0IiwidHlwIjoiSldUIn0.eyJ0ZW5hbnRfaWQiOiJFRkI5NEM3RS03MUU5LTQwNkItOTA1OS02MUFDMDUyMjdGMUIiLCJ1c2VyX2lkIjoiNEVFRDg2Q0UtNDZCNi00NjNGLUJBMjgtQzgzN0IzNDVBRUIzIiwidXNlcl9uYW1lIjoiamVmZmF0a29kZXIiLCJwcm9maWxlX2ltYWdlIjoiaHR0cHM6Ly9maXJlYmFzZXN0b3JhZ2UuZ29vZ2xlYXBpcy5jb20vdjAvYi9lbGxlbi1maXJlYmFzZS1leGFtcGxlLmFwcHNwb3QuY29tL28vQXZhdGFycyUyRnVzZXItMjEucG5nP2FsdD1tZWRpYSZ0b2tlbj1lZjhhYmI1MC0wNjJkLTQ1ZDItOTcwYS1mNDIxNjRmYzA0OWYiLCJleHAiOjE1ODY0MDkyNTgsImlzcyI6Imh0dHBzOi8vZWxsZW4ua29kZXIuY29tL2FwaS9tYW5hZ2VtZW50IiwiYXVkIjoiaHR0cHM6Ly9lbGxlbi5rb2Rlci5jb20vYXBpL21lc3NhZ2luZyJ9.bYBdNZ6BDtpX4TUjDZZ_vOUP8Of87PHnd1Sb9EHtFwA"

                return refreshedToken
            }
        })
    }
}