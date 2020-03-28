package com.koder.ellen

import android.util.Log
import com.google.gson.Gson
import com.koder.ellen.api.RetrofitClient
import com.koder.ellen.core.Utils
import com.koder.ellen.model.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class Client {
    companion object {
        const val TAG = "Client"
        val MEDIA_TYPE_JSON = "application/json; charset=utf-8".toMediaType()
    }

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

    private fun getUser(publicId: String): Result<EllenUser> {
        try {
            val response = RetrofitClient.ellen.getUser(
                publicId = publicId
            ).execute()
            Log.d(TAG, "${response}")
            if (response.isSuccessful) {
                Log.d(TAG, "getUser ${response.body()}")
                val body: EllenUser = response.body()!!
                Log.d(TAG, "getUser ${body}")
                return Result.Success(body)
            }
        } catch (e: Throwable) {
            return Result.Error(IOException("Error getting user", e))
        }

        return Result.Error(IOException())
    }

    // publicId: The user Id of the user to start a conversation with
    fun createConversation(publicId: String, completion: CompletionCallback? = null) {
        GlobalScope.launch {
            try {
                // Get recipient user details
                val result = async(IO) { getUser(publicId) }.await()
                if (result is Result.Success) {
                    // Recipient user details retrieved
                    // Create new conversation
                    val ellenUser = result.data
                    val recipient = User(tenantId = ellenUser.tenantId, userId = ellenUser.userId, displayName = ellenUser.profile.displayName, profileImageUrl = ellenUser.profile.profileImageUrl)
                    val participant = Participant(recipient, 0, 0)
                    val gson = Gson()
                    var participantJson = JSONObject(gson.toJson(participant))

                    // Array of Participants
                    val participants = JSONArray()
                    participants.put(participantJson)

                    val postBody = JSONObject()
                    postBody.put("participants", participants)

                    val response = RetrofitClient.ellen.createConversation(
                        body = postBody.toString().toRequestBody(MEDIA_TYPE_JSON)
                    ).execute()

                    if (response.isSuccessful) {
                        val body: Conversation = response.body()!!
                        completion?.onCompletion(Result.Success(body))
                    } else {
                        completion?.onCompletion(Result.Error(IOException("Error creating conversation")))
                    }

                }
            } catch (e: Exception) {}
        }
    }

    fun getConversation(conversationId: String, completion: CompletionCallback? = null) {
        GlobalScope.launch {
            try {
                Log.d(TAG, "Get ${conversationId}")
                val response = RetrofitClient.ellen.getConversation(conversationId = conversationId).execute()
                Log.d(TAG, "${response}")
                if (response.isSuccessful) {
                    val conversation = response.body()!!
                    completion?.onCompletion(Result.Success(conversation))
                }
            } catch (e: Throwable) {
                completion?.onCompletion(Result.Error(IOException("Error getting conversation")))
            }
        }
    }

    fun closeConversation() {}
    fun addParticipant() {}
    fun removeParticipant() {}
    fun banParticipant() {}
    fun silenceParticipant() {}
    fun unSilenceParticipant() {}
    fun addModerator() {}
    fun removeModerator() {}
    fun createMessage() {}
    fun getLoggedInUserProfile() {}
    fun findUsers() {}


    // Enums
    enum class ConversationState(val value: Int)  {
        active(1),
        closed(2)
    }

    enum class ConversationType(val value: Int) {
        `private`(0),
        `public`(200)
    }
}

interface CompletionInterface {
    fun onCompletion(result: Result<Any>)
}

// On completion callback for
abstract class CompletionCallback: CompletionInterface {}



