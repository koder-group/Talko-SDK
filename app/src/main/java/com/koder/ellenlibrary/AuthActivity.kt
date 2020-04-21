package com.koder.ellenlibrary

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.koder.ellen.*
import com.koder.ellen.data.Result

class AuthActivity : AppCompatActivity() {
    companion object {
        const val TAG = "MainActivity"
        const val TEST_OK = "TEST_OK"
        const val TEST_NOK = "TEST_NOK"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        val userToken = "eyJhbGciOiJIUzI1NiIsImtpZCI6IjE3YzRhYmQ4YTE3MjQ0OTdiZmViMjBiMWM0ZDhmYjU0IiwidHlwIjoiSldUIn0.eyJ0ZW5hbnRfaWQiOiJFRkI5NEM3RS03MUU5LTQwNkItOTA1OS02MUFDMDUyMjdGMUIiLCJ1c2VyX2lkIjoiNEVFRDg2Q0UtNDZCNi00NjNGLUJBMjgtQzgzN0IzNDVBRUIzIiwidXNlcl9uYW1lIjoiamVmZmF0a29kZXIiLCJwcm9maWxlX2ltYWdlIjoiaHR0cHM6Ly9maXJlYmFzZXN0b3JhZ2UuZ29vZ2xlYXBpcy5jb20vdjAvYi9lbGxlbi1maXJlYmFzZS1leGFtcGxlLmFwcHNwb3QuY29tL28vQXZhdGFycyUyRnVzZXItMjEucG5nP2FsdD1tZWRpYSZ0b2tlbj1lZjhhYmI1MC0wNjJkLTQ1ZDItOTcwYS1mNDIxNjRmYzA0OWYiLCJleHAiOjE1ODc0NTc3MTUsImlzcyI6Imh0dHBzOi8vZWxsZW4ua29kZXIuY29tL2FwaS9tYW5hZ2VtZW50IiwiYXVkIjoiaHR0cHM6Ly9lbGxlbi5rb2Rlci5jb20vYXBpL21lc3NhZ2luZyJ9.qvA8pAdsDbNpS1VvVzOQgk8dizDEYDmWGCBVHUXSfzM"

        Messenger.set(userToken, applicationContext, object: CompletionCallback() {
            override fun onCompletion(result: Result<Any>) {
                if(result is Result.Success) {
                    Log.d(TAG, "Messenger successfully set")
                    // UI Unified
//                    val intent = Intent(this@AuthActivity, MessengerActivity::class.java)
                    // UI Screens
                    val intent = Intent(this@AuthActivity, MainActivity::class.java)
                    startActivity(intent)
                    setResult(Activity.RESULT_OK)
                    finish()

//                    val conversationId: String? = Messenger.fetchConversationId("ed4b93a3-3501-4a8b-bf4b-d755629ec493","4eed86ce-46b6-463f-ba28-c837b345aeb3")
//                    Log.d(TAG, "${conversationId}")
                }
            }
        })

        Messenger.screenBackgroundColor = "#00CCCC"
        Messenger.screenCornerRadius = intArrayOf(20, 20, 0, 0)
    }
}
