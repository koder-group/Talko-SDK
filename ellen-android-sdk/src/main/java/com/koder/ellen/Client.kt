package com.koder.ellen

import android.util.Log
import com.koder.ellen.Messenger.Companion.prefs
import com.koder.ellen.data.EllenRepository
import com.koder.ellen.data.Result
import com.pubnub.api.PNConfiguration
import com.pubnub.api.PubNub
import com.pubnub.api.callbacks.SubscribeCallback
import com.pubnub.api.models.consumer.PNStatus
import com.pubnub.api.models.consumer.pubsub.PNMessageResult
import com.pubnub.api.models.consumer.pubsub.PNPresenceEventResult
import com.pubnub.api.models.consumer.pubsub.PNSignalResult
import com.pubnub.api.models.consumer.pubsub.message_actions.PNMessageActionResult
import com.pubnub.api.models.consumer.pubsub.objects.PNMembershipResult
import com.pubnub.api.models.consumer.pubsub.objects.PNSpaceResult
import com.pubnub.api.models.consumer.pubsub.objects.PNUserResult
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO

class Client {
    companion object {
        const val TAG = "Client"
        lateinit var pubNub: PubNub

        fun logout() {
            prefs?.resetUser()
            pubNub.unsubscribeAll()
        }

        // Get Client Configuration and store it in SharedPreferences
        fun getClientConfiguration() {
            GlobalScope.launch {
                val result = async(IO) {
                    EllenRepository.getClientConfiguration()
                }.await()
                if (result is Result.Success) {
                    prefs?.clientConfiguration = result.data
                    Log.d(TAG, "Client configuration initialized")
                }
            }
        }

        fun getCurrentUser() {
            GlobalScope.launch {
                val result = async(IO) {
                    EllenRepository.getCurrentUser()
                }.await()
                if (result is Result.Success) {
                    prefs?.currentUser = result.data
                    Log.d(TAG, "Current user initialized")
                }
            }
        }

        fun initPubNub() {
            var subscribeCallback: SubscribeCallback = object : SubscribeCallback() {
                override fun signal(pubnub: PubNub, pnSignalResult: PNSignalResult) {
                }

                override fun status(pubnub: PubNub, pnStatus: PNStatus) {
                    Log.d(TAG, "${pnStatus}")
                }

                override fun user(pubnub: PubNub, pnUserResult: PNUserResult) {
                }

                override fun messageAction(
                    pubnub: PubNub,
                    pnMessageActionResult: PNMessageActionResult
                ) {
                }

                override fun presence(pubnub: PubNub, pnPresenceEventResult: PNPresenceEventResult) {
                }

                override fun membership(pubnub: PubNub, pnMembershipResult: PNMembershipResult) {
                }

                override fun message(pubnub: PubNub, pnMessageResult: PNMessageResult) {
                }

                override fun space(pubnub: PubNub, pnSpaceResult: PNSpaceResult) {
                }
            }

            val pnConfiguration = PNConfiguration()
            pnConfiguration.subscribeKey = prefs?.clientConfiguration?.subscribeKey
            pnConfiguration.publishKey = prefs?.clientConfiguration?.publishKey
            pnConfiguration.authKey = prefs?.clientConfiguration?.secretKey
            pnConfiguration.uuid = prefs?.externalUserId
            pubNub = PubNub(pnConfiguration)

            pubNub.run {
                addListener(subscribeCallback)
                subscribe()
                    .channels(mutableListOf("${prefs?.appId?.toUpperCase()}-${prefs?.externalUserId?.toUpperCase()}")) // subscribe to channels
                    .execute()
            }
        }

        fun setPushNotificationToken(fcmToken: String) {
            GlobalScope.launch {
                prefs?.notificationToken = fcmToken
                val result = async(IO) {
                    EllenRepository.registerNotificationToken()
                }.await()
                if (result is Result.Success) {
                    Log.d(TAG, "FCM notification token registered")
                }
            }
        }
    }

}