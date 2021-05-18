package com.koder.ellenlibrary

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.koder.ellen.*
import com.koder.ellen.data.Result
import kotlinx.android.synthetic.main.activity_auth.*

class AuthActivity : AppCompatActivity() {
    companion object {
        const val TAG = "MainActivity"
        const val TEST_OK = "TEST_OK"
        const val TEST_NOK = "TEST_NOK"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)


        buttonTalkoAuthenticate.setOnClickListener {

            val userToken = editTextTalkoAuthToken.text?.trim().toString()

            Messenger.screenBackgroundColor = "#5d4298"
            Messenger.screenCornerRadius = intArrayOf(20, 20, 0, 0)

            Messenger.set(userToken, applicationContext, object: CompletionCallback() {
                override fun onCompletion(result: Result<Any>) {
                    if(result is Result.Success) {
                        val intent = Intent(this@AuthActivity, MainActivity::class.java)
                        startActivity(intent)
                        setResult(Activity.RESULT_OK)
                        finish()
                    } else {
                        Toast.makeText(this@AuthActivity, "Talko Authentication failed. Try with different Auth Token", Toast.LENGTH_LONG).show()
                    }
                }
            })
        }
    }
}
