package com.koder.ellen

import android.content.Context
import android.util.Log
import com.koder.ellen.api.RetrofitClient
import com.koder.ellen.core.Prefs
import com.koder.ellen.data.Repository
import com.koder.ellen.data.Result
import com.koder.ellen.data.model.CurrentUser
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
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch


class Messenger {
    companion object {
        const val TAG = "Messenger"
        lateinit var pubNub: PubNub

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
            initClientConfiguration()
            // Get current user
            initCurrentUser()
            // Initialize PubNub client
            initPubNub()
        }

        // Register Firebase Cloud Messaging notification token
        fun setPushNotificationToken(fcmToken: String) {
            GlobalScope.launch {
                prefs?.notificationToken = fcmToken
                val result = async(IO) {
                    Repository.registerNotificationToken()
                }.await()
                if (result is Result.Success) {
                    Log.d(TAG, "FCM notification token registered")
                }
            }
        }

        // Initialize PubNub client
        fun initPubNub() {
            val pnConfiguration = PNConfiguration()
            pnConfiguration.subscribeKey = prefs?.clientConfiguration?.subscribeKey
            pnConfiguration.publishKey = prefs?.clientConfiguration?.publishKey
            pnConfiguration.authKey = prefs?.clientConfiguration?.secretKey
            pnConfiguration.uuid = prefs?.externalUserId
            pubNub = PubNub(pnConfiguration)

            pubNub.run {
                subscribe()
                    .channels(mutableListOf("${prefs?.appId}-${prefs?.externalUserId}".toUpperCase())) // subscribe to channels
                    .execute()
            }
        }

        fun addEventHandler(eventCallback: EventCallback) {
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
                    Log.d(TAG, "${pnMessageResult}")
                    eventCallback.onMessageReceived(pnMessageResult.toString())
                }

                override fun space(pubnub: PubNub, pnSpaceResult: PNSpaceResult) {
                }
            }
            pubNub.addListener(subscribeCallback)
        }

        // Get and store client configuration
        private fun initClientConfiguration() {
            GlobalScope.launch {
                val result = async(IO) { Repository.getClientConfiguration() }.await()
                if (result is Result.Success) {
                    prefs?.clientConfiguration = result.data
//                    Log.d(TAG, "Client configuration initialized")
                }
            }
        }

        // Get and store current user
        private fun initCurrentUser() {
            GlobalScope.launch {
                val result = async(IO) { Repository.getCurrentUser() }.await()
                if (result is Result.Success) {
                    prefs?.currentUser = result.data
//                    Log.d(TAG, "Current user initialized")
                }
            }
        }
    }
}

// Interface for PubNub Subscribe callback
interface EventInterface {
    fun onMessageReceived(data: String)
    fun onParticipantStateChanged(data: String)
    fun onAddedToConversation(data: String)
    fun onRemovedFromConversation(data: String)
    fun onMessageRejected(data: String)
    fun onControlMessage(data: String)
}

// Client callback for PubNub
abstract class EventCallback: EventInterface {}