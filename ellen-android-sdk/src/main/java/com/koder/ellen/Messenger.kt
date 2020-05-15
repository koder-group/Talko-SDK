package com.koder.ellen

import android.content.Context
import android.os.CountDownTimer
import android.util.Base64
import android.util.Log
import androidx.lifecycle.Lifecycle
import com.facebook.stetho.Stetho
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.koder.ellen.api.RetrofitClient
import com.koder.ellen.core.Prefs
import com.koder.ellen.core.Utils
import com.koder.ellen.model.*
import com.koder.ellen.data.Result
import com.koder.ellen.persistence.TalkoDatabase
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
import java.io.IOException


class Messenger {
    companion object {
        const val TAG = "Messenger"
        lateinit var pubNub: PubNub
        lateinit var requestHandler: RequestHandler
        lateinit var unreadCallback: UnreadCallback

        internal var prefs: Prefs? = null
        internal val subscribedChannels: MutableSet<String> = mutableSetOf()
        private val reactionMap: MutableMap<String, Long> = mutableMapOf()

        // Options
        // All Screens
        @JvmStatic var screenBackgroundColor = "#FFFFFF"
        @JvmStatic var screenCornerRadius = intArrayOf(0, 0, 0, 0) // top left, top right, bottom right, bottom left
        // Conversation Screen
        @JvmStatic var conversationItemTopPadding = 10 // dp
        @JvmStatic var conversationItemBottomPadding = 10 // dp
        @JvmStatic var conversationIconRadius = 21 // dp
        @JvmStatic var conversationTitleSize = 14f // sp, float
        @JvmStatic var conversationSubtitleSize = 14f // sp, float
        @JvmStatic var conversationSwipeToDelete = true // true = enabled, false = disabled
        @JvmStatic var conversationLongClickToDelete = false // true = enabled, false = disabled
        // Message Screen
        @JvmStatic var senderMessageRadius = 18 // dp
        @JvmStatic var selfMessageRadius = 18
        @JvmStatic var senderBackgroundColor = "#88000000"  // gray
        @JvmStatic var selfBackgroundColor = "#1A73E9"  // blue

        var conversations = mutableListOf<Conversation>()
        var currentConversationId = ""

        var db: TalkoDatabase? = null

        // Application context
        @JvmStatic fun init(appId: String, context: Context?) {
            context?.let {
                prefs = Prefs(context)
                prefs?.appId = appId
            }
        }

//        @JvmStatic fun set(userToken: String, externalUserId: String, completion: CompletionCallback? = null) {
        @JvmStatic fun set(userToken: String, applicationContext: Context, completion: CompletionCallback? = null) {
            Stetho.initializeWithDefaults(applicationContext)
            db = TalkoDatabase.getInstance(applicationContext)

            // Decode user token for user info
            val parts = userToken.split('.')
            val decoded = Base64.decode(parts[1], Base64.DEFAULT)
            val decodedStr = String(decoded)
            val decodedObj = JSONObject(decodedStr)

            // Create current user object
            val currentUser = EllenUser(userId = decodedObj.get("user_id").toString().toLowerCase(), tenantId = decodedObj.get("tenant_id").toString(), profile = UserProfile(displayName = decodedObj.get("user_name").toString(), profileImageUrl = decodedObj.get("profile_image").toString()))

            // Init Prefs
            prefs = Prefs(applicationContext)
            // Set user token
            prefs?.userToken = userToken
            // Set tenant Id
            prefs?.tenantId = decodedObj.get("tenant_id").toString()
            // Set user Id
            prefs?.userId = decodedObj.get("user_id").toString().toLowerCase()
            // Set current user
            prefs?.currentUser = currentUser

            GlobalScope.launch {
                // Get client configuration
                val clientConfig = async(IO) { initClientConfiguration() }

                // Initialize PubNub client
                async(IO) { initPubNub() }.await()


                if(prefs?.clientConfiguration == null || prefs?.clientConfiguration.toString().isBlank()) {
                    val clientConfigResult = clientConfig.await()

                    if(clientConfigResult is Result.Success) {
    //                    completion?.onCompletion(Result.Success(true))

                        // Populate initial conversations with messages
                        val client = Client()
                        client.getConversationMessages(object: CompletionCallback() {
                            override fun onCompletion(result: Result<Any>) {
                                if(result is Result.Success) {
                                    conversations = result.data as MutableList<Conversation>

                                    completion?.onCompletion(Result.Success(true))
                                }
                            }
                        })
                    } else {
                        completion?.onCompletion(Result.Error(IOException("Error setting Messenger")))
                    }
                } else {
                    completion?.onCompletion(Result.Success(true))
                }
            }
        }

        @JvmStatic fun signOut() {
            prefs?.resetUser()
            pubNub.unsubscribeAll()
        }

        @JvmStatic fun isClientConfigSet(): Boolean {
            return (prefs?.clientConfiguration != null && !prefs?.clientConfiguration.toString().isBlank())
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
                var timer: CountDownTimer? = null

                override fun signal(pubnub: PubNub, pnSignalResult: PNSignalResult) {
                }

                override fun status(pubnub: PubNub, pnStatus: PNStatus) {
                    Log.d(TAG, "${pnStatus}")
                    if(pnStatus.isError && pnStatus.operation.toString().equals("PNSubscribeOperation", ignoreCase = true)) {
                        // Retry subscribe
                        pnStatus.affectedChannels?.let {
                            for(channel in pnStatus.affectedChannels!!) {
                                timer?.cancel()
                                timer = object : CountDownTimer(1000, 1500) {
                                    override fun onTick(millisUntilFinished: Long) {}
                                    override fun onFinish() {
                                        Log.d(TAG, "Retry subscribe ${channel}")
                                        pubNub?.subscribe()?.channels(mutableListOf(channel))?.execute()
                                    }
                                }.start()
                            }
                        }
                    }
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
//                    Log.d(TAG, "${pnMessageResult}")
                    val eventName = pnMessageResult.message.asJsonObject.get("eventName").asString
//                    Log.d(TAG, "eventName ${eventName}")
                    when(eventName) {
                        EventName.messagePublished.value -> {
                            val message = gson.fromJson(pnMessageResult.message.asJsonObject.get("model"), Message::class.java)
                            eventCallback.onMessageReceived(message)
                            unreadCallback.onNewUnread(getUnreadCount())
                            addMessage(message)
                        }
                        EventName.conversationCreated.value -> {
                            // Conversation created
                            val conversation = gson.fromJson(pnMessageResult.message.asJsonObject.get("model"), Conversation::class.java)
                            eventCallback.onConversationCreated(conversation)
                            // Subscribe to PubNub channel
                            subscribeToChannel(conversation.conversationId)
                            addConversation(conversation)
                        }
                        EventName.conversationClosed.value -> {
                            // Conversation closed
                            val conversation = gson.fromJson(pnMessageResult.message.asJsonObject.get("context"), Conversation::class.java)
                            eventCallback.onConversationClosed(conversation)
                            removeConversation(conversation.conversationId)
                        }
                        EventName.conversationModified.value -> {
                            // Conversation modified
                            val conversationId = pnMessageResult.message.asJsonObject.get("context").asJsonObject.get("conversationId").asString
                            val initiatingUser = gson.fromJson(pnMessageResult.message.asJsonObject.get("context").asJsonObject.get("initiatingUser"), User::class.java)
                            var titleEl: JsonElement? = pnMessageResult.message.asJsonObject.get("title")
                            val descriptionEl = pnMessageResult.message.asJsonObject.get("description")
                            var title: String? = null
                            var description: String? = null
                            if(!titleEl!!.isJsonNull) {
                                title = titleEl.asString
                                updateTitle(conversationId, title)
                            }
                            if(!descriptionEl!!.isJsonNull) {
                                description = descriptionEl.asString
                                updateDescription(conversationId, description)
                            }
                            eventCallback.onConversationModified(initiatingUser, title, description, conversationId)
                        }
                        EventName.participantAdded.value -> {
                            // Participant added
                            val addedUserId = pnMessageResult.message.asJsonObject.get("userId").asString
                            val conversationId = pnMessageResult.message.asJsonObject.get("context").asJsonObject.get("conversationId").asString
                            val initiatingUser = gson.fromJson(pnMessageResult.message.asJsonObject.get("context").asJsonObject.get("initiatingUser"), User::class.java)
                            eventCallback.onAddedToConversation(initiatingUser, addedUserId, conversationId)
                            if(prefs?.userId.equals(addedUserId, ignoreCase = true)) {
                                // Current user, subscribe to channel
                                subscribeToChannelList(mutableListOf("${prefs?.tenantId}-${conversationId}".toUpperCase()))
                            }
                            addParticipant(conversationId, addedUserId)
                        }
                        EventName.participantRemoved.value -> {
                            // Participant removed
                            val removedUser = gson.fromJson(pnMessageResult.message.asJsonObject.get("model"), User::class.java)
                            val conversationId = pnMessageResult.message.asJsonObject.get("context").asJsonObject.get("conversationId").asString
                            val initiatingUser = gson.fromJson(pnMessageResult.message.asJsonObject.get("context").asJsonObject.get("initiatingUser"), User::class.java)
                            eventCallback.onRemovedFromConversation(initiatingUser, removedUser.userId, conversationId)
                            if(removedUser.userId.equals(prefs?.userId, ignoreCase = true)) {
                                // Current user, unsubscribe to channel
                                unsubscribeFromChannelList(listOf("${prefs?.tenantId}-${conversationId}".toUpperCase()))
                            }
                            removeParticipant(conversationId, removedUser.userId)
                        }
                        EventName.participantStateChange.value -> {
                            // Participant state changed
                            val participant = gson.fromJson(pnMessageResult.message.asJsonObject.get("model"), Participant::class.java)
                            val conversationId = pnMessageResult.message.asJsonObject.get("context").asJsonObject.get("conversationId").asString
                            eventCallback.onParticipantStateChanged(participant, conversationId)
                        }
                        EventName.moderatorAdded.value -> {
                            val userId = pnMessageResult.message.asJsonObject.get("userId").asString
                            eventCallback.onModeratorAdded(userId)
                        }
                        EventName.moderatorRemoved.value -> {
                            val userId = pnMessageResult.message.asJsonObject.get("moderatorId").asString
                            eventCallback.onModeratorRemoved(userId)
                        }
                        EventName.messageUserReaction.value -> {
                            var message = gson.fromJson(pnMessageResult.message.asJsonObject.get("context"), Message::class.java)
                            val reactionCode = pnMessageResult.message.asJsonObject.get("reactionCode").asString

                            // Prevent duplicate based on time token
                            if(reactionMap.get(message.messageId!!) != pnMessageResult.timetoken || !reactionMap.containsKey(message.messageId!!)) {
                                reactionMap.put(message.messageId!!, pnMessageResult.timetoken)

                                updateMessageReaction(message, reactionCode)
                            }

                            eventCallback.onMessageUserReaction(message)
                        }
                        EventName.messageDeleted.value -> {
                            val message = gson.fromJson(pnMessageResult.message.asJsonObject.get("context"), Message::class.java)
                            eventCallback.onMessageDeleted(message)
                            deleteMessage(message)
                        }
                        EventName.messageRejected.value -> {
                            val message = gson.fromJson(pnMessageResult.message.asJsonObject.get("model"), Message::class.java)
                            val errorMessage = pnMessageResult.message.asJsonObject.get("rejectionReason").asJsonObject.get("message").asString
                            eventCallback.onMessageRejected(message, errorMessage)
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

        fun addConversation(conversation: Conversation) {
            val found = conversations.find { it.conversationId.equals(conversation.conversationId, ignoreCase = true) }
            if(found == null) {
                if(conversation.timeCreated.toString().contains("-")) {
                    conversation.timeCreated = Utils.convertDateToLong(conversation.timeCreated.toString())
                }
                conversation.messages = mutableListOf()
                conversations.add(conversation)
                val sorted = Utils.sortConversationsByLatestMessage(conversations)
                conversations.clear()
                conversations.addAll(sorted)
            }

            // Add to db
            if(conversation.timeCreated.toString().contains("-")) {
                conversation.timeCreated = Utils.convertDateToLong(conversation.timeCreated.toString())
            }
            val json = Gson().toJson(conversation)
            val convo = com.koder.ellen.persistence.Conversation(conversation.conversationId, json.toString().toByteArray(Charsets.UTF_8))
            db?.conversationDao()?.insert(convo)
        }

        fun removeConversation(conversationId: String) {
            val found = conversations.find { c -> c.conversationId.equals(conversationId, ignoreCase = true) }
            found?.let {
                Log.d(TAG, "Remove ${it}")
                conversations.remove(found)
            }

            // Remove from db
            GlobalScope.launch {
                async(IO) {
                    db?.conversationDao()?.deleteConversation(conversationId)
                    db?.messageDao()?.deleteMessages(conversationId)
                }.await()
            }
        }

        // Add to db
        fun addMessage(message: Message) {
            if(message.timeCreated.toString().contains("-")) {
                message.timeCreated = Utils.convertDateToLong(message.timeCreated.toString())
            }

            val json = Gson().toJson(message)
            val msg = com.koder.ellen.persistence.Message(message.messageId!!, message.conversationId, message.timeCreated.toLong(), json.toString().toByteArray(Charsets.UTF_8))
            db?.messageDao()?.insert(msg)
        }

        fun deleteMessage(message: Message) {
            // Remove from db
            GlobalScope.launch {
                async(IO) {
                    db?.messageDao()?.deleteMessage(message.messageId!!)
                }.await()
            }
        }

        // Update local db
        fun updateMessageReaction(message: Message, reactionCode: String): Message? {
            val msg = db?.messageDao()?.getMessage(message.messageId!!)
            if(msg != null) {
                val str = String(msg.payload)
                val m = Gson().fromJson(JSONObject(str).toString(), Message::class.java)

                if(m.reactionSummary == null) {
                    m.reactionSummary = ReactionSummary(Reaction(summaryText = "RESOURCE_REACTION_REACTION_CODE_LIKE"),
                        Reaction(summaryText = "RESOURCE_REACTION_REACTION_CODE_DISLIKE"))
                }

                when(reactionCode) {
                    "REACTION_CODE_LIKE" -> {
                        if(m.reactionSummary!!.reactioN_CODE_LIKE == null) {
                            m.reactionSummary!!.reactioN_CODE_LIKE = Reaction(summaryText = "RESOURCE_REACTION_REACTION_CODE_LIKE")
                        }

                        m.reactionSummary!!.reactioN_CODE_LIKE!!.count += 1
                    }
                    "REACTION_CODE_DISLIKE" -> {
                        if(m.reactionSummary!!.reactioN_CODE_DISLIKE == null) {
                            m.reactionSummary!!.reactioN_CODE_DISLIKE = Reaction(summaryText = "RESOURCE_REACTION_REACTION_CODE_DISLIKE")
                        }

                        m.reactionSummary!!.reactioN_CODE_DISLIKE!!.count += 1
                    }
                }

                val json = Gson().toJson(m)
                val dbMsg = com.koder.ellen.persistence.Message(m.messageId!!, m.conversationId, m.timeCreated.toLong(), json.toString().toByteArray(Charsets.UTF_8))
                db?.messageDao()?.update(dbMsg)

                return m
            }

            return null
        }

        fun updateTitle(conversationId: String, title: String) {
            val found = conversations.find { c -> c.conversationId.equals(conversationId, ignoreCase = true) }
            found?.let {
//                Log.d(TAG, "Update title ${it}")
                it.title = title
            }

            // Update local db
            var conversation = db?.conversationDao()?.getConversation(conversationId) as com.koder.ellen.persistence.Conversation
            val str = String(conversation.payload)
            val convo = Gson().fromJson(JSONObject(str).toString(), Conversation::class.java)
            convo.title = title
            val json = Gson().toJson(convo)
            conversation = com.koder.ellen.persistence.Conversation(conversation.conversationId, json.toString().toByteArray(Charsets.UTF_8))
            db?.conversationDao()?.update(conversation)
        }

        fun updateDescription(conversationId: String, description: String) {
            val found = conversations.find { c -> c.conversationId.equals(conversationId, ignoreCase = true) }
            found?.let {
//                Log.d(TAG, "Update description ${it}")
                it.description = description
            }

            // Update local db
            var conversation = db?.conversationDao()?.getConversation(conversationId) as com.koder.ellen.persistence.Conversation
            val str = String(conversation.payload)
            val convo = Gson().fromJson(JSONObject(str).toString(), Conversation::class.java)
            convo.description = description
            val json = Gson().toJson(convo)
            conversation = com.koder.ellen.persistence.Conversation(conversation.conversationId, json.toString().toByteArray(Charsets.UTF_8))
            db?.conversationDao()?.update(conversation)
        }

        private fun addParticipant(conversationId: String, addedUserId: String) {
            val conversation = conversations.find { c -> c.conversationId.equals(conversationId, ignoreCase = true) }
            conversation?.let { c ->
                val found = c.participants?.find { p -> p.user.userId.equals(addedUserId, ignoreCase = true) }
//                Log.d(TAG, "${addedUserId} ${found}")
                if(found == null) {
                    // Get user
                    val client = Client()
                    client.getUser(addedUserId, object: CompletionCallback() {
                        override fun onCompletion(result: Result<Any>) {
                            if(result is Result.Success) {
                                val ellenUser = result.data as EllenUser
                                val user = User(tenantId = ellenUser.tenantId, userId = ellenUser.userId, displayName = ellenUser.profile.displayName, profileImageUrl = ellenUser.profile.profileImageUrl)
                                val newParticipant = Participant(user = user)
                                c.participants.add(newParticipant)

                                // Update local db
                                val localConvo = db?.conversationDao()?.getConversation(conversationId)
                                if(localConvo != null) {
                                    val str = String(localConvo.payload)
                                    val convo = Gson().fromJson(JSONObject(str).toString(), Conversation::class.java)

                                    val newParticipant = Participant(user = user)
                                    convo.participants.add(newParticipant)

                                    var json = Gson().toJson(convo)
                                    val conversation = com.koder.ellen.persistence.Conversation(conversationId, json.toString().toByteArray(Charsets.UTF_8))
                                    db?.conversationDao()?.update(conversation)

                                    // Add UserProfile
                                    json = Gson().toJson(user)
                                    val obj = com.koder.ellen.persistence.UserProfile(user.userId, user.userId, user.displayName, user.profileImageUrl, System.currentTimeMillis(), json.toString().toByteArray(Charsets.UTF_8))
                                    db?.userProfileDao()?.insert(obj)
                                }
                            }
                        }
                    })
                }
            }
        }

        fun addParticipant(conversationId:String, user: User) {
            val conversation = conversations.find { c -> c.conversationId.equals(conversationId, ignoreCase = true) }
            conversation?.let { c ->
                val found = c.participants?.find { p ->
                    p.user.userId.equals(
                        user.userId,
                        ignoreCase = true
                    )
                }
                if(found == null) {
                    val newParticipant = Participant(user = user)
                    c.participants.add(newParticipant)
                }
            }

            // Update local db
            val localConvo = db?.conversationDao()?.getConversation(conversationId)
//            Log.d(TAG, "conversation ${localConvo}")
            if(localConvo != null) {
                val str = String(localConvo.payload)
                val convo = Gson().fromJson(JSONObject(str).toString(), Conversation::class.java)

                val newParticipant = Participant(user = user)
                convo.participants.add(newParticipant)
//                Log.d(TAG, "participants ${convo.participants}")

                var json = Gson().toJson(convo)
                val conversation = com.koder.ellen.persistence.Conversation(conversationId, json.toString().toByteArray(Charsets.UTF_8))
                db?.conversationDao()?.update(conversation)

                // Add UserProfile
                json = Gson().toJson(user)
                val obj = com.koder.ellen.persistence.UserProfile(user.userId, user.userId, user.displayName, user.profileImageUrl, System.currentTimeMillis(), json.toString().toByteArray(Charsets.UTF_8))
                db?.userProfileDao()?.insert(obj)
            }
        }

        fun removeParticipant(conversationId: String, userId: String) {
            val conversation = conversations.find { c -> c.conversationId.equals(conversationId, ignoreCase = true) }
            conversation?.let { c ->
                val found = c?.participants?.find { p -> p.user.userId.equals(userId, ignoreCase = true) }
                found?.let {
                    conversation.participants?.remove(found)
                }
            }

            // Update local db
            val localConvo = db?.conversationDao()?.getConversation(conversationId)
//            Log.d(TAG, "conversation ${conversation}")
            if(localConvo != null) {
                val str = String(localConvo.payload)
                val convo = Gson().fromJson(JSONObject(str).toString(), Conversation::class.java)

                val found = convo?.participants?.find { p -> p.user.userId.equals(userId, ignoreCase = true) }
                found?.let {
                    convo.participants?.remove(found)
//                    Log.d(TAG, "participants ${convo.participants}")
                }

                val json = Gson().toJson(convo)
                val conversation = com.koder.ellen.persistence.Conversation(conversationId, json.toString().toByteArray(Charsets.UTF_8))
                db?.conversationDao()?.update(conversation)
            }
        }

        // Get and store client configuration
        private fun initClientConfiguration(): Result<ClientConfiguration> {
//            GlobalScope.launch {
                try {
                    Log.d(TAG, "Init client config")
                    val response = RetrofitClient.ellen.getClientConfiguration().execute()
                    Log.d(TAG, "${response}")
                    if (response.isSuccessful) {
                        val body: ClientConfiguration = response.body()!!
                        prefs?.clientConfiguration = body
                        Log.d(TAG, "Client configuration initalized")
                        return Result.Success(body)
                    }
                } catch (e: Exception) {}
//            }
            return Result.Error(IOException("Error getting client configuration"))
        }

        // Get and store current user
        private fun initCurrentUser(): Result<EllenUser> {
//            GlobalScope.launch {
                try {
                    Log.d(TAG, "Init current user")
                    val response = RetrofitClient.ellen.getCurrentUser().execute()
                    Log.d(TAG, "${response}")
                    if (response.isSuccessful) {
                        val body: EllenUser = response.body()!!
                        prefs?.currentUser = body
                        Log.d(TAG, "Current user initialized")
                        return Result.Success(body)
                    }
                } catch (e: Exception) {}
//            }
            return Result.Error(IOException("Error getting current user"))
        }

        // Set token refresh handler
        @JvmStatic fun addRequestHandler(handler: RequestHandler) {
            requestHandler = handler
        }

        @JvmStatic fun subscribeToChannelList(channelList: MutableList<String>) {
//            Log.d(TAG, "subscribeToChannelList ${channelList}")
            pubNub.subscribe().channels(channelList).execute()
            subscribedChannels.addAll(channelList)
        }

        @JvmStatic fun unsubscribeFromChannelList(channelList: List<String>) {
            pubNub.unsubscribe()?.channels(channelList).execute()
            for(channel in channelList) {
                subscribedChannels.remove(channel)
            }
        }

        // Request refresh token
        fun refreshToken(): String {
            val token = requestHandler.onRefreshTokenRequest()
//            Log.d(TAG, "onUserTokenRequest ${token}")
            // Set new token
            prefs?.userToken = token
            return token
        }

        // Get Conversation Id between 2 participants. Returns the first Conversation Id found.
        @JvmStatic fun fetchConversation(participantId1: String, participantId2: String): Conversation? {
            for(conversation in conversations) {
                val p1found = conversation.participants.find { p -> p.user.userId.equals(participantId1, ignoreCase = true) }
                val p2found = conversation.participants.find { p -> p.user.userId.equals(participantId2, ignoreCase = true) }

                if(p1found != null && p2found != null && conversation.participants.size == 2) {
                    // Conversation between 2 participants
                    return conversation
                }
            }
            return null
        }

        @JvmStatic fun getUserId(): String {
            return prefs?.userId!!
        }

        @JvmStatic fun getUnreadCount(): Int {
            var unreadCount = 0
            val list = conversations.toList()
            for(conversation in list) {
                conversation.messages.firstOrNull()?.let {
                    val latestMessageCreated = conversation.messages.first().timeCreated.toLong()
                    val lastRead = prefs?.getConversationLastRead(conversation.conversationId) ?: 0

                    if (latestMessageCreated > lastRead) unreadCount = unreadCount + 1
                }
            }
            return unreadCount
        }

        // Return participants in format Participant1, Participant2, ...
        @JvmStatic fun fetchConversationTitle(conversationId: String): String {
            val conversation = conversations.find { c -> c.conversationId.equals(conversationId, ignoreCase = true) }
            val participants = conversation?.participants

            // Return conversation title if exists // TODO needed?
            if(!conversation?.title.isNullOrBlank()) return conversation!!.title

            var title = ""

            // If the only participant is the sender (myself)
            if(participants?.size == 1 && participants.first().user.userId.equals(prefs?.externalUserId, ignoreCase = true))
                return "Me"

            participants?.let {
                for (participant in participants) {
                    if(participant.user.displayName != null) {
                        if (participant.user.displayName.equals(prefs?.currentUser?.profile?.displayName, ignoreCase = true)) continue
                        if (title.isEmpty()) {
                            title += participant.user.displayName
                            continue
                        }
                        title += ", ${participant.user.displayName}"
                    }
                }
            }

            return title
        }

        @JvmStatic fun setUnreadListener(callback: UnreadCallback) {
            unreadCallback = callback
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

// Interface for unread messages
interface UnreadInterface {
    fun onNewUnread(unreadCount: Int)
}
abstract class UnreadCallback: UnreadInterface {}

// Interface for PubNub Subscribe callback
interface EventInterface {
    fun onConversationCreated(conversation: Conversation)
    fun onConversationClosed(conversation: Conversation)
    fun onConversationModified(initiatingUser: User, title: String?, description: String?, conversationId: String)
    fun onParticipantStateChanged(participant: Participant, conversationId: String)
    fun onAddedToConversation(initiatingUser: User, addedUserId: String, conversationId: String)
    fun onRemovedFromConversation(initiatingUser: User, removedUserId: String, conversationId: String)
    fun onMessageReceived(message: Message)
    fun onMessageRejected(message: Message, errorMessage: String)
    fun onMessageDeleted(message: Message)
    fun onMessageUserReaction(message: Message)
//    fun onControlMessage(data: String)
    fun onUserTypingStart(initiatingUserId: String)
    fun onUserTypingStop(initiatingUserId: String)
    fun onModeratorAdded(userId: String)
    fun onModeratorRemoved(userId: String)
}

// Interface for refreshing user tokens
interface RequestHandlerInterface {
    fun onRefreshTokenRequest(): String
}

// Client callback for PubNub
abstract class EventCallback: EventInterface {}
// Request handler for refreshing user tokens
abstract class RequestHandler: RequestHandlerInterface {}