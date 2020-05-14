package com.koder.ellen.data

import android.util.Log
import com.google.gson.Gson
import com.koder.ellen.Messenger
import com.koder.ellen.core.Utils.Companion.sortConversationsByLatestMessage
import com.koder.ellen.model.ClientConfiguration
import com.koder.ellen.model.Conversation
import com.koder.ellen.model.EllenUser
import com.koder.ellen.model.Message
import com.koder.ellen.persistence.ConversationDao
import com.koder.ellen.persistence.MessageDao
import com.koder.ellen.persistence.TalkoDatabase
import org.json.JSONObject

internal class ConversationRepository(val dataSource: ConversationDataSource) {
    val TAG = "ConversationsRepository"

    private var conversationDao: ConversationDao? = Messenger.db?.conversationDao()
    private var messageDao: MessageDao? = Messenger.db?.messageDao()

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
        if(conversations!!.isEmpty() || forceLoad) {
            // Local cache empty, load from remote
            val remoteConversations = dataSource.getConversations()

            // Add conversations to database
            conversationDao?.deleteAll()
            for(conversation in remoteConversations) {
                val json = Gson().toJson(conversation)
                val convo = com.koder.ellen.persistence.Conversation(conversation.conversationId, json.toString().toByteArray(Charsets.UTF_8))
                conversationDao?.insert(convo)
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
        if(messages!!.isEmpty() || forceLoad) {
            val remoteConversationMessages = dataSource.getConversationMessages(conversations)
//            Log.d(TAG, "remoteMessages ${remoteConversationMessages.size}")

            // Add messages to database
            messageDao?.deleteAll()
            for(conversation in remoteConversationMessages) {
                for(message in conversation.messages) {
//                    Log.d(TAG, "msg ${message}")

                    val json = Gson().toJson(message)
                    val msg = com.koder.ellen.persistence.Message(message.messageId!!, message.conversationId, message.timeCreated.toLong(), json.toString().toByteArray(Charsets.UTF_8))
                    messageDao?.insert(msg)
                }
            }

            return remoteConversationMessages
        }

        // Return local conversation+messages
        val localConversationMessages = mutableListOf<Conversation>()
        for(conversation in conversations) {
            val messages: List<com.koder.ellen.persistence.Message> = messageDao?.getMessages(conversation.conversationId)!!
            val msgList = mutableListOf<Message>()
            for(message in messages) {
//                Log.d(TAG, "${message.messageTs} ${message.conversationId}")
                val str = String(message.payload)
                val msg = Gson().fromJson(JSONObject(str).toString(), Message::class.java)
                msgList.add(msg)
            }
            conversation.messages = msgList
            localConversationMessages.add(conversation)
        }

        return sortConversationsByLatestMessage(localConversationMessages)

//        return dataSource.getConversationMessages(conversations)
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
}