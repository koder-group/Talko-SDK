package com.koder.ellen

import android.util.Log
import com.koder.ellen.api.RetrofitClient
import com.koder.ellen.core.Utils
import com.koder.ellen.data.model.Conversation
import com.koder.ellen.data.model.Message
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class Client {

    // Enums

    enum class ConversationState(val value: Int)  {
        active(1),
        closed(2)
    }

    enum class ConversationType(val value: Int) {
        `private`(0),
        `public`(200)
    }

    companion object {
        const val TAG = "Client"
        val MEDIA_TYPE_JSON = "application/json; charset=utf-8".toMediaType()
    }

    fun createConversation() {}
    fun addParticipant() {}
    fun removeParticipant() {}
    fun banParticipant() {}
    fun silenceParticipant() {}
    fun unSilenceParticipant() {}
    fun addModerator() {}
    fun removeModerator() {}
    fun createMessage() {}
    fun getConversation() {}
    fun closeConversation() {}
    fun getLoggedInUserProfile() {}
    fun findUsers() {}

    fun getConversationsForLoggedInUser(): MutableList<Conversation> {
        try {
            val postBody = JSONObject()
            postBody.put("pageSize", 10000)
            val response = RetrofitClient.ellen.getConversations(
                body = postBody.toString().toRequestBody(MEDIA_TYPE_JSON)
            )
                .execute()
            Log.d(TAG, "${response}")
            if (response.isSuccessful) {
                val filtered = Utils.filterConversationsByState(
                    response.body()!!,
                    ConversationState.active.value
                )
                return filtered
            }
        } catch (e: Exception) {
            Log.d(TAG, "${e}")
        }
        return mutableListOf()
    }

    fun getMessagesForConversation(conversationId: String): MutableList<Message> {
        try {
            val postBody = JSONObject()
            postBody.put("sort", -1)    //  Sort by timeCreated
            postBody.put("pageSize", 100)
            val response = RetrofitClient.ellen.getMessagesForConversation(
                conversationId = conversationId,
                body = postBody.toString().toRequestBody())
                .execute()
            if(response.isSuccessful) {
                val body: MutableList<Message> = response.body()!!  //  Ordered by timeCreated DESC
                body.reverse()  //  Order by timeCreated ASC
                return body
            }
        } catch (e: Exception) {
            Log.d(TAG, "${e}")
        }
        return mutableListOf()
    }
}




