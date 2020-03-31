package com.koder.ellen.data

import android.util.Log
import com.koder.ellen.Messenger.Companion.prefs
import com.koder.ellen.api.RetrofitClient
import com.koder.ellen.model.ClientConfiguration
import com.koder.ellen.model.Conversation
import com.koder.ellen.model.EllenUser

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException


/**
 * Class that handles retrieving Conversation information.
 */
class ConversationDataSource {
    private val TAG = "ConversationsDataSource"

    // https://kdrellenplatformapimd18.azure-api.net/users/current
    fun getCurrentUser(): Result<EllenUser> {
        try {
            val response = RetrofitClient.ellen.getCurrentUser().execute()
            Log.d(TAG, "${response}")
            if (response.isSuccessful) {
                val body: EllenUser = response.body()!!
                return Result.Success(body)
            }
        } catch (e: Throwable) {
            return Result.Error(IOException("Error getting current user", e))
        }

        return Result.Error(IOException())
    }

    // https://kdrellenplatformapimd18.azure-api.net/platform/clientConfiguration/android-sdk
    fun getClientConfig(): Result<ClientConfiguration> {
        try {
            val response = RetrofitClient.ellen.getClientConfiguration().execute()

            if (response.isSuccessful) {
                val body: ClientConfiguration = response.body()!!
                return Result.Success(body)
            }
        } catch (e: Throwable) {
            return Result.Error(IOException("Error getting client config", e))
        }

        return Result.Error(IOException())
    }

    fun registerNotificationToken(): Result<Any> {
        try {
            Log.d(TAG, "registerNotificationToken() ${prefs?.notificationToken}")
            val requestBody = JSONObject()
            requestBody.put("token", prefs?.notificationToken)
            requestBody.put("platform", "ANDROID")
//            Log.d(TAG, "${requestBody}")
            val response = RetrofitClient.ellen.notificationRegistration(
                    body = requestBody.toString().toRequestBody(MEDIA_TYPE_JSON)
            ).execute()
            Log.d(TAG, "${response}")
            if(response.isSuccessful) {
                return Result.Success(response)
            }
        } catch (e: Throwable) {
            return Result.Error(IOException("Error registering notification token", e))
        }
        return Result.Error(IOException())
    }

    // https://kdrellenplatformapimd18.azure-api.net/conversation/search
    var retry: Boolean = false
    fun getConversations(): MutableList<Conversation> {
        try {
            val postBody = JSONObject()
            postBody.put("pageSize", 10000)
            val response = RetrofitClient.ellen.getConversations(
                body = postBody.toString().toRequestBody(MEDIA_TYPE_JSON))
                .execute()
            Log.d(TAG, "${response}")
            if (response.isSuccessful) {
                // Filter Conversations where state = 1
//                Log.d(TAG, "${response.body()}")
                return filterConversationsByState(response.body()!!, 1)
            }

            if(response.code() == 401 && response.message().equals("Unauthorized")) {
                // Try refreshing
                // Update messaging token, ngrok signin
//                val result = refreshToken()
//                if(result is Result.Success) {
//                    // Retry 1 time
//                    if(!retry) {
//                        getConversations()
//                        retry = true
//                    }
//                }
                refreshToken()
            }
        } catch (e: Throwable) {
            Log.d(TAG, e.message)
            return mutableListOf()
        }

        return mutableListOf()
    }

//    fun refreshToken(): Result<LoggedInUser> {
//        Log.d(TAG, "Refresh token")
//        val result = LoginRepository(LoginDataSource()).auth(prefs.displayName(), Crypto.decodeAndDecrypt(prefs.p()))
//        return result
//    }
    // Refresh messaging token
    fun refreshToken() {
        Log.d(TAG, "Refresh messaging token")
//    val result = functions
//        .getHttpsCallable("getMessagingToken")
//        .call()
//        .continueWith { task ->
//            // This continuation runs on either success or failure, but if the task
//            // has failed then result will throw an Exception which will be
//            // propagated down.
//            val result = task.result?.data as String
//            val resultObj = JSONObject(result)
////                Log.d(TAG, "getMessagingToken result ${result}")
//            val messagingToken = resultObj.get("token") as String
//            Log.d(TAG, "getMessagingToken resultObj.token ${messagingToken}")
//            prefs.setMessagingToken(messagingToken)
//            getConversations()
//            result
//        }
    }

    // https://kdrellenplatformapimd18.azure-api.net/conversation/CONVERSATION_ID/messages/search
    fun getConversationMessages(conversations: MutableList<Conversation>): MutableList<Conversation> {

        // Get messages for each Conversation
        for (conversation in conversations) {
            try {
                val postBody = JSONObject()
                postBody.put("sort", -1) // Sort by Message time created ASC

                val response = RetrofitClient.ellen.getMessagesForConversation(
                                conversationId = conversation.conversationId,
                                body = postBody.toString().toRequestBody(MEDIA_TYPE_JSON)
                        ).execute()
                if (response.isSuccessful) {
                    conversation.messages = response.body()!!
                }

            } catch (e: Throwable) {
                Log.d(TAG, e.message)
                return conversations
            }
        }

        return sortConversationsByLatestMessage(conversations)
    }

    fun deleteConversation(conversationId: String): Result<Boolean> {
        try {
            Log.d(TAG, "Delete ${conversationId}")
            val response = RetrofitClient.ellen.deleteConversation(conversationId = conversationId).execute()
            Log.d(TAG, "${response}")
            if (response.isSuccessful) {
                return Result.Success(true)
            }
        } catch (e: Throwable) {
            return Result.Error(IOException("Error deleting conversation", e))
        }
        return Result.Error(IOException())
    }

    fun getConversation(conversationId: String): Result<Conversation> {
        try {
            Log.d(TAG, "Get ${conversationId}")
            val response = RetrofitClient.ellen.getConversation(conversationId = conversationId).execute()
            Log.d(TAG, "${response}")
            if (response.isSuccessful) {
                val conversation = response.body()!!
                return Result.Success(conversation)
            }
        } catch (e: Throwable) {
            return Result.Error(IOException("Error getting conversation", e))
        }
        return Result.Error(IOException())
    }

    companion object {
        val MEDIA_TYPE_JSON = "application/json; charset=utf-8".toMediaType()
    }

    private fun filterConversationsByState(list: MutableList<Conversation>, state: Int): MutableList<Conversation> {
        val filteredList = list.filter { it.state == state }
//        Log.d(TAG, "${filteredList}")
        return filteredList.toMutableList()
    }

    fun sortConversationsByLatestMessage(conversations: MutableList<Conversation>): MutableList<Conversation> {

        val latestMessageMap: MutableMap<Conversation, Long> = mutableMapOf()
        val noMessageMap: MutableMap<Conversation, Long> = mutableMapOf()
        val sortedConversationList = mutableListOf<Conversation>()

        for (conversation in conversations) {
            if(conversation.messages.isEmpty()) {
                noMessageMap.put(conversation, conversation.timeCreated.toLong())
                continue
            }
            latestMessageMap.put(conversation, conversation.messages.first().timeCreated!!.toLong()) // First message should be latest, from sort: -1
        }

        val sortedMessageMap = sortByDescending(latestMessageMap)
        val sortedNoMessageMap = sortByDescending(noMessageMap)

        addConversationsToList(sortedNoMessageMap, sortedConversationList)
        addConversationsToList(sortedMessageMap, sortedConversationList)

        return sortedConversationList
    }

    private fun sortByDescending(messageMap: MutableMap<Conversation, Long>): Map<Conversation, Long> {
        return messageMap.toList().sortedByDescending { (_, value) -> value }.toMap()
    }

    private fun addConversationsToList(map: Map<Conversation, Long>, list: MutableList<Conversation>) {
        for (entry in map) {
            list.add(entry.key)
        }
    }
}

