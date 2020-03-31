package com.koder.ellen.data

import android.net.Uri
import android.util.Log
import com.koder.ellen.data.MessageDataSource
import com.koder.ellen.model.*
import okhttp3.MediaType
import okhttp3.ResponseBody
import java.io.File

class MessageRepository(val dataSource: MessageDataSource) {
    val TAG = "MessageRepository"

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
        return dataSource.createEllenConversation(ellenUser)
    }

    fun getConversation(conversationId: String): Result<Conversation> {
        return dataSource.getConversation(conversationId)
    }

    fun createMessage(message: Message): Result<Message> {
        return dataSource.createMessage(message)
    }

    fun getMessages(conversationId: String): Result<MutableList<Message>> {
        return dataSource.getMessages(conversationId)
    }

    fun addParticipant(user: User, conversationId: String): Result<Any> {
        return dataSource.addParticipant(user, conversationId)
    }

    fun removeParticipant(user: User, conversationId: String): Result<Any> {
        return dataSource.removeParticipant(user, conversationId)
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
        return dataSource.deleteMessage(message)
    }

    fun updateConversationTitle(conversationId: String, title: String): Result<Any> {
        return dataSource.updateConversationTitle(conversationId, title)
    }

    fun updateConversationDescription(conversationId: String, description: String): Result<Any> {
        return dataSource.updateConversationDescription(conversationId, description)
    }

    fun deleteConversation(conversationId: String): Result<Boolean> {
        return dataSource.deleteConversation(conversationId)
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