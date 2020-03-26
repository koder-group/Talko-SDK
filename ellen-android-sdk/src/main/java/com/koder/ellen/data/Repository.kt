package com.koder.ellen.data

import android.util.Log
import com.koder.ellen.Messenger.Companion.prefs
import com.koder.ellen.api.RetrofitClient
import com.koder.ellen.model.ClientConfiguration
import com.koder.ellen.model.CurrentUser
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class Repository {
    companion object {
        const val TAG = "Repository"
        val MEDIA_TYPE_JSON = "application/json; charset=utf-8".toMediaType()

        fun getClientConfiguration(): Result<ClientConfiguration> {
            val response = RetrofitClient.ellen.getClientConfiguration().execute()
            Log.d(TAG, "${response}")
            if (response.isSuccessful) {
                val body: ClientConfiguration = response.body()!!
                return Result.Success(body)
            }
            return Result.Error(IOException("Error getting client configuration"))
        }

        fun getCurrentUser(): Result<CurrentUser> {
                val response = RetrofitClient.ellen.getCurrentUser().execute()
                Log.d(TAG, "${response}")
                if (response.isSuccessful) {
                    val body: CurrentUser = response.body()!!
                    return Result.Success(body)
                }
            return Result.Error(IOException("Error getting current user"))
        }

        fun registerNotificationToken(): Result<Any> {
            val requestBody = JSONObject()
            requestBody.put("token", prefs?.notificationToken)
            requestBody.put("platform", "ANDROID")
            val response = RetrofitClient.ellen.notificationRegistration(
                body = requestBody.toString().toRequestBody(MEDIA_TYPE_JSON)
            ).execute()
            Log.d(TAG, "${response}")
            if(response.isSuccessful) {
                return Result.Success(response)
            }
            return Result.Error(IOException("Error registering notification token"))
        }
    }
}