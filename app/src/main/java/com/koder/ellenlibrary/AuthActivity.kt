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

        val userToken = "eyJhbGciOiJIUzI1NiIsImtpZCI6IjA2NTIyZjNkMmNmNTRjNGNhYWYyZDdhYjAxOTY4NjEzIiwidHlwIjoiSldUIn0.eyJ0ZW5hbnRfaWQiOiI3MURCMjBBMi1CNDYzLTQ5NzctOEZGNC1DOEMyMTdBQjE5RDYiLCJ1c2VyX2lkIjoiQURCOEZGMzMtMzA5QS00RDE1LTg5N0QtMzlERUEyMDJGNEE1IiwidXNlcl9uYW1lIjoidGVzdDI1IiwicHJvZmlsZV9pbWFnZSI6Imh0dHBzOi8va29kZXJ1c3RvcmFnZWRldi5ibG9iLmNvcmUud2luZG93cy5uZXQvcHJvZmlsZS1waWN0dXJlcy9kYWRmNmQxYy05MmUxLTRhYzAtODFmZC0yODYyYTBlNTAyNWYlMjVDNSUyNTkyYmRlODQzY2NfMzUxNV80YjUyX2E0MTFfOWJjNmNiMTU2ZDIyX3RodW1ibmFpbC5qcGciLCJleHAiOjE2MTI0NjE5ODcsImlzcyI6Imh0dHBzOi8vYXBpLnRhbGtvLmFpLyIsImF1ZCI6Imh0dHBzOi8vYXBpLnRhbGtvLmFpLyJ9.GXM4_4OygD7rfB2Ay9LX9ZutXytVYXtWO3IqW-_lP94"

        Messenger.screenBackgroundColor = "#5d4298"
        Messenger.screenCornerRadius = intArrayOf(20, 20, 0, 0)

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
    }
}
