package com.koder.ellen.data

import android.util.Log
import com.google.gson.Gson
import com.koder.ellen.Messenger
import com.koder.ellen.Messenger.Companion.userProfileCache
import com.koder.ellen.core.Utils
import com.koder.ellen.core.Utils.Companion.sortConversationsByLatestMessage
import com.koder.ellen.model.ClientConfiguration
import com.koder.ellen.model.Conversation
import com.koder.ellen.model.EllenUser
import com.koder.ellen.model.Message
import com.koder.ellen.persistence.ConversationDao
import com.koder.ellen.persistence.MessageDao
import com.koder.ellen.persistence.TalkoDatabase
import com.koder.ellen.persistence.UserProfileDao
import org.json.JSONObject

internal class ConversationRepository(val dataSource: ConversationDataSource) {
    val TAG = "ConversationsRepository"

    private var conversationDao: ConversationDao? = Messenger.db?.conversationDao()
    private var messageDao: MessageDao? = Messenger.db?.messageDao()
    private var userProfileDao: UserProfileDao? = Messenger.db?.userProfileDao()

    fun getClientConfig(): Result<ClientConfiguration> {
        val result = dataSource.getClientConfig()

        if(result is Result.Success) {
//            setClientConfig(result.data)
        }

        return result
    }

    fun registerNotificationToken() {
        dataSource.registerNotificationToken()
    }

    fun getCurrentUser(): Result<EllenUser> {
        val result = dataSource.getCurrentUser()

        if(result is Result.Success) setCurrentUser(result.data)

        return result
    }

    fun getConversations(forceLoad: Boolean = false): MutableList<Conversation> {
        // Check local if data is available
        val conversations = conversationDao?.getAll()

        // Return remote conversations
        if(conversations.isNullOrEmpty() || forceLoad) {
            // Local cache empty, load from remote
            val remoteConversations = dataSource.getConversations()

            // Add conversations to database
            conversationDao?.deleteAll()
            for(conversation in remoteConversations) {
                val json = Gson().toJson(conversation)
                val convo = com.koder.ellen.persistence.Conversation(conversation.conversationId, json.toString().toByteArray(Charsets.UTF_8))
                conversationDao?.insert(convo)

                // Add UserProfiles
                for(participant in conversation.participants) {
                    val user = participant.user
                    val json = Gson().toJson(user)
                    val obj = com.koder.ellen.persistence.UserProfile(user.userId, user.userId, user.displayName, user.profileImageUrl, System.currentTimeMillis(), json.toString().toByteArray(Charsets.UTF_8))
                    userProfileDao?.insert(obj)
                }
            }

            return remoteConversations
        }

        // Return local conversations
        val localConversations = mutableListOf<Conversation>()
        for(conversation in conversations) {
            val str = String(conversation.payload)
            val convo = Gson().fromJson(JSONObject(str).toString(), Conversation::class.java)
            localConversations.add(convo)
        }

        return localConversations

//        return dataSource.getConversations()
    }

    // Return conversations including messages
    fun getConversationMessages(conversations: MutableList<Conversation>, forceLoad: Boolean = false): MutableList<Conversation> {
        // Check local if data is available
        conversationDao = Messenger.db?.conversationDao()
        val messages = messageDao?.getAll()

        // Return remote conversation+messages
        if(messages.isNullOrEmpty() || forceLoad) {
            val remoteConversationMessages = dataSource.getConversationMessages(conversations)
//            Log.d(TAG, "remoteMessages ${remoteConversationMessages.size}")

            // Add messages to database
            messageDao?.deleteAll()
            userProfileDao?.deleteAll()
            for(conversation in remoteConversationMessages) {
                conversation.messages?.let { messages ->
                    for(message in messages) {
                        val json = Gson().toJson(message)
                        val msg = com.koder.ellen.persistence.Message(message.messageId!!, message.conversationId, message.timeCreated.toLong(), json.toString().toByteArray(Charsets.UTF_8))
                        messageDao?.insert(msg)
                        // Update profile image url
                        cacheUserIfNeeded(message)
                    }
                }
            }

            return remoteConversationMessages
        }

        // Return local conversation+messages
        val localConversationMessages = mutableListOf<Conversation>()
        userProfileDao?.deleteAll()
        for(conversation in conversations) {
            val messages: List<com.koder.ellen.persistence.Message> = messageDao?.getMessages(conversation.conversationId)!!
            val msgList = mutableListOf<Message>()
            for(message in messages) {
//                Log.d(TAG, "${message.messageTs} ${message.conversationId}")
                val str = String(message.payload)
                val msg = Gson().fromJson(JSONObject(str).toString(), Message::class.java)
                msgList.add(msg)

                // Update profile image url
                cacheUserIfNeeded(msg)
            }
            conversation.messages = msgList
            localConversationMessages.add(conversation)
        }

        return sortConversationsByLatestMessage(localConversationMessages)

//        return dataSource.getConversationMessages(conversations)
    }

    fun updateUserProfileCache() {

    }

    fun deleteConversation(conversationId: String): Result<Boolean> {
        // Delete from local db
        conversationDao?.deleteConversation(conversationId)
        messageDao?.deleteMessages(conversationId)
        return dataSource.deleteConversation(conversationId)
    }

    fun getConversation(conversationId: String): Result<Conversation> {
        return dataSource.getConversation(conversationId)
    }

    private fun setClientConfig(clientConfig: ClientConfiguration) {
//        prefs.saveClientConfig(clientConfig)
    }

    private fun setCurrentUser(currentUser: EllenUser) {
//        prefs.saveCurrentUser(currentUser)
    }

    private fun cacheUserIfNeeded(message: Message) {

//        Log.d(TAG, "cacheUserIfNeeded ${message}")
        val userId = message.sender.userId
        val userProfile = userProfileDao?.getUserProfile(userId)

        if(userProfile == null) {
            val user = message.sender
            var json = Gson().toJson(user)
            val obj = com.koder.ellen.persistence.UserProfile(user.userId, user.userId, user.displayName, user.profileImageUrl, message.timeCreated.toLong(), json.toString().toByteArray(Charsets.UTF_8))
            userProfileDao?.insert(obj)

            userProfileCache.put(user.userId, obj)
            return
        }


//        Log.d(TAG, "userProfile ${userProfile}")
        if(message.timeCreated.toString().contains("-")) {
            message.timeCreated = Utils.convertDateToLong(message.timeCreated.toString())
        }

        // If message is newer
//        Log.d(TAG, "${message.timeCreated.toLong() > userProfile.updatedTs} timeCreated ${message.timeCreated.toLong()} updatedTs ${userProfile.updatedTs}")
        if(message.timeCreated.toLong() > userProfile.updatedTs) {
//                Log.d(TAG, "update ${message.sender.displayName} ${message.sender.profileImageUrl}")
            // Update UserProfile
            val user = message.sender
            var json = Gson().toJson(user)
            val obj = com.koder.ellen.persistence.UserProfile(user.userId, user.userId, user.displayName, user.profileImageUrl, message.timeCreated.toLong(), json.toString().toByteArray(Charsets.UTF_8))
            userProfileDao?.update(obj)

            userProfileCache.put(user.userId, obj)
        }

    }
}