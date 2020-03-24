package com.koder.ellen

import android.content.Context
import android.util.Log
import com.koder.ellen.core.Prefs
import com.koder.ellen.data.Result
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext

class Messenger {
    companion object {
        const val TAG = "Messenger"
        var prefs: Prefs? = null

        // Application context
        fun init(appId: String, context: Context?) {
            context?.let {
                prefs = Prefs(context)
                prefs?.appId = appId
            }
        }

        fun set(userToken: String, externalUserId: String) {
            // Store user token and user id
            prefs?.userToken = userToken
            prefs?.externalUserId = externalUserId

            // Get client configuration
            Client.getClientConfiguration()
            // Get current user
            Client.getCurrentUser()
            // Initialize PubNub client
            Client.initPubNub()
        }
    }
}