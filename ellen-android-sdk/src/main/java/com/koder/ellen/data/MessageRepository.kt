package com.koder.ellen.data

import android.net.Uri
import android.util.Log
import com.google.gson.Gson
import com.koder.ellen.Messenger
import com.koder.ellen.core.Utils
import com.koder.ellen.data.MessageDataSource
import com.koder.ellen.model.*
import com.koder.ellen.persistence.ConversationDao
import com.koder.ellen.persistence.MessageDao
import okhttp3.MediaType
import okhttp3.ResponseBody
import org.json.JSONObject
import java.io.File

internal class MessageRepository(val dataSource: MessageDataSource) {
    val TAG = "MessageRepository"

    private var conversationDao: ConversationDao? = Messenger.db?.conversationDao()
    private var messageDao: MessageDao? = Messenger.db?.messageDao()

    fun getUser(publicId: String): Result<EllenUser> {
        return dataSource.getUser(publicId)
    }

    fun getEllenUser(publicId: String): Result<EllenUser> {
        return dataSource.getEllenUser(publicId)
    }

    fun createConversation(conversationUser: User): Result<Conversation> {
        return dataSource.createConversation(conversationUser)
    }

    fun createEllenConversation(ellenUser: EllenUser): Result<Conversation> {
        val result = dataSource.createEllenConversation(ellenUser)

        if(result is Result.Success) {
            // Add to db
            val conversation = result.data

            if(conversation.timeCreated.toString().contains("-")) {
                conversation.timeCreated = Utils.convertDateToLong(conversation.timeCreated.toString())
            }

            val json = Gson().toJson(conversation)
            val convo = com.koder.ellen.persistence.Conversation(conversation.conversationId, json.toString().toByteArray(Charsets.UTF_8))
            conversationDao?.insert(convo)
        }

        return result
    }

    fun getConversation(conversationId: String): Result<Conversation> {
        // Get from local db
        val conversation = conversationDao?.getConversation(conversationId)
        if(conversation != null) {
            val str = String(conversation.payload)
            val convo = Gson().fromJson(JSONObject(str).toString(), Conversation::class.java)
            return Result.Success(convo)
        }

        return dataSource.getConversation(conversationId)
    }

    fun createMessage(message: Message): Result<Message> {
        val result = dataSource.createMessage(message)

        if(result is Result.Success) {
            // Add to db
//            val message = result.data
//            Log.d(TAG, "createMessage ${message}")
//
//            if(message.timeCreated.toString().contains("-")) {
//                message.timeCreated = Utils.convertDateToLong(message.timeCreated.toString())
//            }
//
//            val json = Gson().toJson(message)
//            val msg = com.koder.ellen.persistence.Message(message.messageId!!, message.conversationId, message.timeCreated.toLong(), json.toString().toByteArray(Charsets.UTF_8))
//            messageDao?.insert(msg)
        }

        return result
    }

    fun getMessages(conversationId: String, forceLoad: Boolean = false): Result<MutableList<Message>> {
        // Check local
        val messages = messageDao?.getMessages(conversationId)

        // Return remote
        if(messages.isNullOrEmpty() || forceLoad) {
            val result = dataSource.getMessages(conversationId)

            if(result is Result.Success) {
                val remoteMessages = result.data

                messageDao?.deleteMessages(conversationId)
                for(message in remoteMessages) {
                    // Add to db
                    val json = Gson().toJson(message)
                    val msg = com.koder.ellen.persistence.Message(message.messageId!!, message.conversationId, message.timeCreated.toLong(), json.toString().toByteArray(Charsets.UTF_8))
                    messageDao?.insert(msg)
                }
            }

            return result
        }

        // Return local
        val localMessages = mutableListOf<Message>()
        for(message in messages) {
            val str = String(message.payload)
            val m = Gson().fromJson(JSONObject(str).toString(), Message::class.java)
            localMessages.add(m)
        }

        return Result.Success(localMessages.asReversed())

//        return dataSource.getMessages(conversationId)
    }

    fun getMessage(messageId: String): Message? {
        val msg = messageDao?.getMessage(messageId)
        if(msg != null) {
            val str = String(msg.payload)
            val m = Gson().fromJson(JSONObject(str).toString(), Message::class.java)
            return m
        }
        return null
    }

    fun addParticipant(user: User, conversationId: String): Result<Any> {
        val result = dataSource.addParticipant(user, conversationId)

        if(result is Result.Success) {
            // Update local db
            val conversation = conversationDao?.getConversation(conversationId)
//            Log.d(TAG, "conversation ${conversation}")
            if(conversation != null) {
                val str = String(conversation.payload)
                val convo = Gson().fromJson(JSONObject(str).toString(), Conversation::class.java)

                val newParticipant = Participant(user = user)

                convo.participants.add(newParticipant)

                val json = Gson().toJson(convo)
                val conversation = com.koder.ellen.persistence.Conversation(conversation.conversationId, json.toString().toByteArray(Charsets.UTF_8))
                conversationDao?.update(conversation)
            }
        }

        return result
    }

    fun removeParticipant(user: User, conversationId: String): Result<Any> {
        val result = dataSource.removeParticipant(user, conversationId)

        if(result is Result.Success) {
            // Update local db
            val conversation = conversationDao?.getConversation(conversationId)
//            Log.d(TAG, "conversation ${conversation}")
            if(conversation != null) {
                val str = String(conversation.payload)
                val convo = Gson().fromJson(JSONObject(str).toString(), Conversation::class.java)

                val found = convo?.participants?.find { p -> p.user.userId.equals(user.userId, ignoreCase = true) }
                found?.let {
                    convo.participants?.remove(found)
//                    Log.d(TAG, "participants ${convo.participants}")
                }

                val json = Gson().toJson(convo)
                val conversation = com.koder.ellen.persistence.Conversation(conversation.conversationId, json.toString().toByteArray(Charsets.UTF_8))
                conversationDao?.update(conversation)
            }
        }

        return result
    }

    fun createMediaItem(conversationId: String, file: File, contentType: String): Result<MediaItem> {
        return dataSource.createMediaItem(conversationId, file, contentType)
    }

    fun setReaction(message: Message, reaction: String): Result<Any> {
        return dataSource.setReaction(message, reaction)
    }

    fun reportMessage(message: Message): Result<Any> {
        return dataSource.reportMessage(message)
    }

    fun deleteMessage(message: Message): Result<Any> {
        Messenger.deleteMessage(message)
        return dataSource.deleteMessage(message)
    }

    fun updateConversationTitle(conversationId: String, title: String): Result<Any> {
        val result = dataSource.updateConversationTitle(conversationId, title)
        if(result is Result.Success) {
            // Update local db
            Messenger.updateTitle(conversationId, title)
        }
        return result
    }

    fun updateConversationDescription(conversationId: String, description: String): Result<Any> {
        val result = dataSource.updateConversationDescription(conversationId, description)
        if(result is Result.Success) {
            Messenger.updateDescription(conversationId, description)
        }
        return result
    }

    fun deleteConversation(conversationId: String): Result<Boolean> {
        val result = dataSource.deleteConversation(conversationId)
        if(result is Result.Success) {
            Messenger.removeConversation(conversationId)
        }
        return result
    }

    fun searchUser(text: String): Result<MutableList<User>> {
        return dataSource.searchUser(text)
    }

    fun addModerator(userId: String, conversationId: String): Result<ResponseBody> {
        return dataSource.addModerator(userId, conversationId)
    }

    fun removeModerator(userId: String, conversationId: String): Result<Any> {
        return dataSource.removeModerator(userId, conversationId)
    }

    fun typingStarted(userId: String, conversationId: String): Result<Any> {
        return dataSource.typingStarted(userId, conversationId)
    }

    fun typingStopped(userId: String, conversationId: String): Result<Any> {
        return dataSource.typingStopped(userId, conversationId)
    }
}