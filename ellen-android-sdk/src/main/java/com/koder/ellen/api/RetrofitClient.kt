package com.koder.ellen.api

import android.os.Looper
import android.util.Base64
import android.util.Log
import com.koder.ellen.CompletionCallback
import com.koder.ellen.Messenger
import com.koder.ellen.Messenger.Companion.prefs
import com.koder.ellen.core.AppConstants
import com.koder.ellen.data.Result
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*
import java.util.concurrent.TimeUnit

internal object RetrofitClient {

    // Platform
    private val ellenClient = OkHttpClient().newBuilder()
            .readTimeout(60, TimeUnit.SECONDS)
            .connectTimeout(60, TimeUnit.SECONDS)
            .addInterceptor(AuthInterceptor())
            .build()

    val ellen: EllenApi by lazy {
        val retrofit = Retrofit.Builder()
                .client(ellenClient)
                .baseUrl(AppConstants.ELLEN_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

        retrofit.create(EllenApi::class.java)
    }
}

class AuthInterceptor : Interceptor {
    companion object {
        const val TAG = "AuthInterceptor"
    }

    override fun intercept(chain: Interceptor.Chain) : Response {
        val request = chain.request().newBuilder()
            .header("Authorization", "Bearer ${prefs?.userToken}")
            .build()

        // Refresh token if current is expired
        val compareTimestamps = isUserTokenExpired(prefs?.userToken!!)
//        Log.d(TAG, "compareTimestamps ${compareTimestamps}")
        if(compareTimestamps > 0) {
            // current userToken is expired

            // Send refresh token request to host app, synchronously
            val token = Messenger.refreshToken()

            // Create request with new token
            val request = chain.request().newBuilder()
                .header("Authorization", "Bearer ${token}")
                .build()

            return chain.proceed(request)
        }

        return chain.proceed(request)
    }

    fun isUserTokenExpired(userToken: String): Int {
        val currentTimestamp = Calendar.getInstance()
//        Log.d(TAG, "currentTimestamp ${currentTimestamp}")

        // Decode user token for user info
        val parts = userToken.split('.')
        val decoded = Base64.decode(parts[1], Base64.DEFAULT)
        val decodedStr = String(decoded)
        val decodedObj = JSONObject(decodedStr)

        val tokenExpiration = decodedObj.getLong("exp")  // seconds
        val tokenTimestamp = Calendar.getInstance()
        tokenTimestamp.timeInMillis = tokenExpiration * 1000    // to milliseconds
//        Log.d(TAG, "tokenExpiration ${tokenTimestamp}")

        return currentTimestamp.compareTo(tokenTimestamp)
    }
}