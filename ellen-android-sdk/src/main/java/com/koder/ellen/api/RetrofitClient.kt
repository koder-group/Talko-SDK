package com.koder.ellen.api

import com.koder.ellen.Messenger.Companion.prefs
import com.koder.ellen.core.AppConstants
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

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
    override fun intercept(chain: Interceptor.Chain) : Response {
        val request = chain.request().newBuilder()
            .header("Authorization", "Bearer ${prefs?.userToken}")
            .build()
        return chain.proceed(request)
    }
}