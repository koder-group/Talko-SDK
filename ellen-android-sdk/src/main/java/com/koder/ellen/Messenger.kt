package com.koder.ellen

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.koder.ellen.api.RetrofitClient
import com.koder.ellen.core.Prefs
import com.koder.ellen.core.Utils
import com.koder.ellen.model.*
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
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject


class Messenger {
    companion object {
        const val TAG = "Messenger"
        lateinit var pubNub: PubNub

        var prefs: Prefs? = null
        val channelList: MutableList<String> = mutableListOf()

        // Application context
        @JvmStatic fun init(appId: String, context: Context?) {
            context?.let {
                prefs = Prefs(context)
                prefs?.appId = appId
            }
        }

        @JvmStatic fun set(userToken: String, externalUserId: String, completion: CompletionCallback? = null) {
            // Store user token and user id
            prefs?.userToken = userToken
            prefs?.externalUserId = externalUserId

            GlobalScope.launch {
                // Get client configuration
                async(IO) { initClientConfiguration() }.await()

                // Get current user
                async(IO) { initCurrentUser() }.await()

                // Initialize PubNub client
                async(IO) { initPubNub() }.await()

                completion?.onCompletion(Result.Success(true))
            }
        }

        // Register Firebase Cloud Messaging notification token
        @JvmStatic fun setPushNotificationToken(fcmToken: String) {
            GlobalScope.launch {
                prefs?.notificationToken = fcmToken
                try {
                    val requestBody = JSONObject()
                    requestBody.put("token", prefs?.notificationToken)
                    requestBody.put("platform", "ANDROID")
                    val response = RetrofitClient.ellen.notificationRegistration(
                        body = requestBody.toString().toRequestBody(Utils.MEDIA_TYPE_JSON)
                    ).execute()
                    Log.d(TAG, "${response}")
                    if(response.isSuccessful) {
                        Log.d(TAG, "FCM notification token registered")
                    }
                } catch (e: Exception) {}
            }
        }

        // Initialize PubNub client
        fun initPubNub() {
            val pnConfiguration = PNConfiguration()
            pnConfiguration.subscribeKey = prefs?.clientConfiguration?.subscribeKey
            pnConfiguration.publishKey = prefs?.clientConfiguration?.publishKey
            pnConfiguration.authKey = prefs?.clientConfiguration?.secretKey
            pnConfiguration.uuid = prefs?.currentUser?.userId
            pubNub = PubNub(pnConfiguration)

            val userChannel = "${prefs?.tenantId}-${prefs?.currentUser?.userId}".toUpperCase()
            pubNub.run {
                subscribe()
                    .channels(mutableListOf(userChannel)) // subscribe to channels
                    .execute()
            }
        }

        @JvmStatic fun addEventHandler(eventCallback: EventCallback) {
            var subscribeCallback: SubscribeCallback = object : SubscribeCallback() {
                override fun signal(pubnub: PubNub, pnSignalResult: PNSignalResult) {
                }

                override fun status(pubnub: PubNub, pnStatus: PNStatus) {
                    Log.d(TAG, "${pnStatus}")
                }

                override fun user(pubnub: PubNub, pnUserResult: PNUserResult) {
                }

                override fun messageAction(pubnub: PubNub, pnMessageActionResult: PNMessageActionResult) {
                }

                override fun presence(pubnub: PubNub, pnPresenceEventResult: PNPresenceEventResult) {
                }

                override fun membership(pubnub: PubNub, pnMembershipResult: PNMembershipResult) {
                }

                override fun message(pubnub: PubNub, pnMessageResult: PNMessageResult) {
                    val gson = Gson()
                    Log.d(TAG, "${pnMessageResult}")
                    val eventName = pnMessageResult.message.asJsonObject.get("eventName").asString
                    Log.d(TAG, "eventName ${eventName}")
                    when(eventName) {
                        EventName.messagePublished.value -> {
                            val message = gson.fromJson(pnMessageResult.message.asJsonObject.get("model"), Message::class.java)
                            eventCallback.onMessageReceived(message)
                        }
                        EventName.conversationCreated.value -> {
                            // Conversation created
                            val conversation = gson.fromJson(pnMessageResult.message.asJsonObject.get("model"), Conversation::class.java)
                            eventCallback.onConversationCreated(conversation)
                            // Subscribe to PubNub channel
                            subscribeToChannel(conversation.conversationId)
                        }
                        EventName.conversationClosed.value -> {
                            // Conversation closed
                            val conversation = gson.fromJson(pnMessageResult.message.asJsonObject.get("context"), Conversation::class.java)
                            eventCallback.onConversationClosed(conversation)
                            // TODO Unsubscribe from channel -- needed?
                        }
                        EventName.conversationModified.value -> {
                            // Conversation modified
                            val conversationId = pnMessageResult.message.asJsonObject.get("context").asJsonObject.get("conversationId").asString
                            eventCallback.onConversationModified(conversationId)
                        }
                        EventName.participantAdded.value -> {
                            // Participant added
                            val addedUserId = pnMessageResult.message.asJsonObject.get("userId").asString
                            eventCallback.onAddedToConversation(addedUserId)
                        }
                        EventName.participantRemoved.value -> {
                            // Participant removed
                            val removedUserId = pnMessageResult.message.asJsonObject.get("userId").asString
                            eventCallback.onRemovedFromConversation(removedUserId)
                            // TODO Unsubscribe from channel, if this user is the removed participant -- needed?
//                            if(prefs?.currentUser?.userId?.toUpperCase().equals(removedUser.userId.toUpperCase())) pubNub?.unsubscribe()?.channels(listOf("${prefs?.tenantId}-${conversationId}".toUpperCase()))?.execute()
                        }
                        EventName.participantStateChange.value -> {
                            // Participant state changed
                            val participant = gson.fromJson(pnMessageResult.message.asJsonObject.get("model"), Participant::class.java)
                            eventCallback.onParticipantStateChanged(participant)
                        }
                        EventName.moderatorAdded.value -> {
                            val userId = pnMessageResult.message.asJsonObject.get("userId").asString
                            eventCallback.onModeratorAdded(userId)
                        }
                        EventName.moderatorRemoved.value -> {
                            val moderatorId = pnMessageResult.message.asJsonObject.get("moderatorId").asString
                            eventCallback.onModeratorRemoved(moderatorId)
                        }
                        EventName.messageUserReaction.value -> {
                            val message = gson.fromJson(pnMessageResult.message.asJsonObject.get("context"), Message::class.java)
                            eventCallback.onMessageUserReaction(message)
                        }
                        EventName.messageDeleted.value -> {
                            val message = gson.fromJson(pnMessageResult.message.asJsonObject.get("context"), Message::class.java)
                            eventCallback.onMessageDeleted(message)
                        }
                        EventName.messageRejected.value -> {
                            val message = gson.fromJson(pnMessageResult.message.asJsonObject.get("model"), Message::class.java)
                            eventCallback.onMessageRejected(message)
                        }
                        EventName.controlEvent.value -> {
                            // Control event
                            val eventName = pnMessageResult.message.asJsonObject.get("eventData").asJsonObject.get("eventName").asString
                            val initiatingUser = pnMessageResult.message.asJsonObject.get("eventData").asJsonObject.get("context").asJsonObject.get("initiatingUser").asString
                            when(eventName) {
                                EventName.typingStart.value -> {
                                    eventCallback.onUserTypingStart(initiatingUser)
                                }
                                EventName.typingStop.value -> {
                                    eventCallback.onUserTypingStop(initiatingUser)
                                }
                            }
                        }
                    }
                }

                override fun space(pubnub: PubNub, pnSpaceResult: PNSpaceResult) {
                }

                private fun subscribeToChannel(conversationId: String) {
                    pubNub?.subscribe()?.channels(mutableListOf("${prefs?.tenantId}-${conversationId}".toUpperCase()))?.execute()
                }
            }
            pubNub.addListener(subscribeCallback)
        }

        // Get and store client configuration
        private fun initClientConfiguration() {
            GlobalScope.launch {
                try {
                    val response = RetrofitClient.ellen.getClientConfiguration().execute()
                    Log.d(TAG, "${response}")
                    if (response.isSuccessful) {
                        val body: ClientConfiguration = response.body()!!
                        prefs?.clientConfiguration = body
                        Log.d(TAG, "Client configuration initalized")
                    }
                } catch (e: Exception) {}
            }
        }

        // Get and store current user
        private fun initCurrentUser() {
            GlobalScope.launch {
                try {
                    val response = RetrofitClient.ellen.getCurrentUser().execute()
                    Log.d(TAG, "${response}")
                    if (response.isSuccessful) {
                        val body: EllenUser = response.body()!!
                        prefs?.currentUser = body
                        Log.d(TAG, "Current user initialized")
                    }
                } catch (e: Exception) {}
            }
        }
    }

    enum class EventName(val value: String) {
        messagePublished("message:published"),
        messageUserReaction("message:userReaction"),
        messageDeleted("message:deleted"),
        messageRejected("message:rejected"),
        conversationCreated("conversation:created"),
        conversationClosed("conversation:closed"),
        conversationModified("conversation:modified"),
        participantAdded("conversation:participant:added"),
        participantRemoved("conversation:participant:removed"),
        participantStateChange("conversation:participant:statusChange"),
        controlEvent("controlEvent"),
        typingStart("user:typing:start"),
        typingStop("user:typing:stop"),
        moderatorAdded("conversation:moderator:added"),
        moderatorRemoved("conversation:moderator:removed")
    }
}

// Interface for PubNub Subscribe callback
interface EventInterface {
    fun onConversationCreated(conversation: Conversation)
    fun onConversationClosed(conversation: Conversation)
    fun onConversationModified(conversationId: String)
    fun onParticipantStateChanged(participant: Participant)
    fun onAddedToConversation(userId: String)
    fun onRemovedFromConversation(userId: String)
    fun onMessageReceived(message: Message)
    fun onMessageRejected(message: Message)
    fun onMessageDeleted(message: Message)
    fun onMessageUserReaction(message: Message)
//    fun onControlMessage(data: String)
    fun onUserTypingStart(initiatingUserId: String)
    fun onUserTypingStop(initiatingUserId: String)
    fun onModeratorAdded(userId: String)
    fun onModeratorRemoved(moderatorId: String)
}

// Client callback for PubNub
abstract class EventCallback: EventInterface {}