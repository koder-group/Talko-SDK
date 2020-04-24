package com.koder.ellen

import android.util.Log
import com.google.gson.Gson
import com.koder.ellen.api.RetrofitClient
import com.koder.ellen.core.Utils
import com.koder.ellen.data.Result
import com.koder.ellen.model.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.IOException

class Client {
    companion object {
        const val TAG = "Client"
        val MEDIA_TYPE_JSON = "application/json; charset=utf-8".toMediaType()
    }

    fun getUser(publicId: String, completion: CompletionCallback? = null): Result<EllenUser> {
        try {
            val response = RetrofitClient.ellen.getUser(
                publicId = publicId
            ).execute()
            Log.d(TAG, "${response}")
            if (response.isSuccessful) {
                Log.d(TAG, "getUser ${response.body()}")
                val body: EllenUser = response.body()!!
                Log.d(TAG, "getUser ${body}")
                completion?.onCompletion(Result.Success(completion))
                return Result.Success(body)
            } else {
                completion?.onCompletion(Result.Error(IOException("Error getting user")))
            }
        } catch (e: Throwable) {
            completion?.onCompletion(Result.Error(IOException("Error getting user")))
            return Result.Error(IOException("Error getting user", e))
        }

        return Result.Error(IOException())
    }

    // Find users by display name
    fun findUsers(displayNameFilter: String, completion: CompletionCallback? = null) {
        GlobalScope.launch {
            try {
                val requestBody = JSONObject()
                requestBody.put("pageNumber", 1)
                requestBody.put("pageSize", 10)
                requestBody.put("displayNameFilter", displayNameFilter)
                val response = RetrofitClient.ellen.searchUser(
                    body = requestBody.toString().toRequestBody(MEDIA_TYPE_JSON)).execute()
                Log.d(TAG, "${response}")
                if (response.isSuccessful) {
                    val body: MutableList<User> = response.body()!!
                    completion?.onCompletion(Result.Success(body))
                } else {
                    completion?.onCompletion(Result.Error(IOException("Error finding users")))
                }
            } catch (e: Throwable) {
                completion?.onCompletion(Result.Error(IOException("Error finding users")))
            }
        }
    }

    // Get current user
    fun getLoggedInUserProfile(completion: CompletionCallback? = null) {
        GlobalScope.launch {
            try {
                val response = RetrofitClient.ellen.getCurrentUser().execute()
                Log.d(TAG, "${response}")
                if (response.isSuccessful) {
                    val body: EllenUser = response.body()!!
                    completion?.onCompletion(Result.Success(body))
                } else {
                    completion?.onCompletion(Result.Error(IOException("Error getting logged in user profile")))
                }
            } catch (e: Throwable) {
                completion?.onCompletion(Result.Error(IOException("Error getting logged in user profile")))
            }
        }
    }

    // Get a list of active conversations for the current user
    fun getConversationsForLoggedInUser(completion: CompletionCallback? = null) {
        GlobalScope.launch {
            try {
                val postBody = JSONObject()
                postBody.put("pageSize", 10000)
                val response = RetrofitClient.ellen.getConversations(
                    body = postBody.toString().toRequestBody(MEDIA_TYPE_JSON)
                ).execute()
                Log.d(TAG, "${response}")
                if (response.isSuccessful) {
                    val filtered = Utils.filterConversationsByState(
                        response.body()!!,
                        ConversationState.active.value
                    )
                    completion?.onCompletion(Result.Success(filtered))
//                    return filtered
                } else {
                    completion?.onCompletion(Result.Error(IOException("Error getting conversations")))
                }
            } catch (e: Exception) {
                completion?.onCompletion(Result.Error(IOException("Error getting conversations")))
                Log.d(TAG, "${e}")
            }
//            return mutableListOf()
        }
    }

    fun getConversationMessages(completion: CompletionCallback? = null) {
        GlobalScope.launch {
            try {
                val postBody = JSONObject()
                postBody.put("pageSize", 10000)
                val response = RetrofitClient.ellen.getConversations(
                    body = postBody.toString().toRequestBody(MEDIA_TYPE_JSON)
                ).execute()
                Log.d(TAG, "${response}")
                if (response.isSuccessful) {
                    val filtered = Utils.filterConversationsByState(
                        response.body()!!,
                        ConversationState.active.value
                    )

                    for(conversation in filtered) {
                        val postBody = JSONObject()
                        postBody.put("sort", -1)    //  Sort by timeCreated
                        postBody.put("pageSize", 100)
                        val response = RetrofitClient.ellen.getMessagesForConversation(
                            conversationId = conversation.conversationId,
                            body = postBody.toString().toRequestBody()
                        ).execute()
                        if(response.isSuccessful) {
                            val body: MutableList<Message> =
                                response.body()!!  //  Ordered by timeCreated DESC
//                            body.reverse()  //  Order by timeCreated ASC
                            conversation.messages = body
                        }
                    }
                    val sortedConversations = Utils.sortConversationsByLatestMessage(filtered)
                    completion?.onCompletion(Result.Success(sortedConversations))
                }
            } catch (e: Exception) {
                completion?.onCompletion(Result.Error(IOException("Error getting conversations and messages")))
                Log.d(TAG, "${e}")
            }
        }
    }

    // Get a list of messages for a conversation
    fun getMessagesForConversation(conversationId: String, completion: CompletionCallback? = null) {
        GlobalScope.launch {
            try {
                val postBody = JSONObject()
                postBody.put("sort", -1)    //  Sort by timeCreated
                postBody.put("pageSize", 100)
                val response = RetrofitClient.ellen.getMessagesForConversation(
                    conversationId = conversationId,
                    body = postBody.toString().toRequestBody()
                )
                    .execute()
                if (response.isSuccessful) {
                    val body: MutableList<Message> =
                        response.body()!!  //  Ordered by timeCreated DESC
                    body.reverse()  //  Order by timeCreated ASC
                    completion?.onCompletion(Result.Success(body))
//                    return body
                } else {
                    completion?.onCompletion(Result.Error(IOException("Error getting messages for conversation")))
                }
            } catch (e: Exception) {
                completion?.onCompletion(Result.Error(IOException("Error getting messages for conversation")))
                Log.d(TAG, "${e}")
            }
//            return mutableListOf()
        }
    }

    // Get the conversation
    fun getConversation(conversationId: String, completion: CompletionCallback? = null) {
        GlobalScope.launch {
            try {
                Log.d(TAG, "Get ${conversationId}")
                val response = RetrofitClient.ellen.getConversation(conversationId = conversationId).execute()
                Log.d(TAG, "${response}")
                if (response.isSuccessful) {
                    val conversation = response.body()!!
                    completion?.onCompletion(Result.Success(conversation))
                } else {
                    completion?.onCompletion(Result.Error(IOException("Error getting conversation")))
                }
            } catch (e: Throwable) {
                completion?.onCompletion(Result.Error(IOException("Error getting conversation")))
            }
        }
    }

    // Add a participant to a conversation
    fun addParticipant(userId: String, conversationId: String, completion: CompletionCallback? = null) {
        GlobalScope.launch {
            try {
                // Get user
                val userResult = async(IO) { getUser(userId) }.await()
                if (userResult is Result.Success) {
                    val user = User(tenantId = userResult.data.tenantId, userId = userResult.data.userId, displayName = userResult.data.profile.displayName, profileImageUrl = userResult.data.profile.profileImageUrl)

                    // Add participant
                    val response = RetrofitClient.ellen.addParticipant(
                        conversationId = conversationId,
                        participantId = user.userId)
                        .execute()
                    Log.d(TAG, "${response}")
                    if (response.isSuccessful) {
                        completion?.onCompletion(Result.Success(true))
                    } else {
                        completion?.onCompletion(Result.Error(IOException("Error adding participant")))
                    }
                }
            } catch (e: Throwable) {
                completion?.onCompletion(Result.Error(IOException("Error adding participant")))
            }
        }
    }

    // Remove a participant from a conversation
    fun removeParticipant(userId: String, conversationId: String, completion: CompletionCallback? = null) {
        GlobalScope.launch {
            try {
                val response = RetrofitClient.ellen.removeParticipant(
                    conversationId = conversationId,
                    participantId = userId
                ).execute()
                Log.d(TAG, "${response}")
                if (response.isSuccessful) {
                    completion?.onCompletion(Result.Success(true))
                } else {
                    completion?.onCompletion(Result.Error(IOException("Error removing participant")))
                }
            } catch (e: Throwable) {
                completion?.onCompletion(Result.Error(IOException("Error removing participant")))
            }
        }
    }

    // Add a moderator to a conversation
    fun addModerator(userId: String, conversationId: String, completion: CompletionCallback? = null) {
        GlobalScope.launch {
            try {
                val response = RetrofitClient.ellen.addModerator(
                    conversationId = conversationId,
                    participantId = userId
                ).execute()
                Log.d(TAG, "${response}")
                if (response.isSuccessful) {
                    completion?.onCompletion(Result.Success(response.body()!!))
                } else {
                    completion?.onCompletion(Result.Error(IOException("Error adding moderator")))
                }
            } catch (e: Throwable) {
                completion?.onCompletion(Result.Error(IOException("Error adding moderator")))
            }
        }
    }

    // Remove a moderfator from a conversation
    fun removeModerator(userId: String, conversationId: String, completion: CompletionCallback? = null) {
        GlobalScope.launch {
            try {
                val response = RetrofitClient.ellen.deleteModerator(
                    conversationId = conversationId,
                    participantId = userId
                ).execute()
                if (response.isSuccessful) {
                    completion?.onCompletion(Result.Success(response.body()!!))
                } else {
                    completion?.onCompletion(Result.Error(IOException("Error removing moderator")))
                }
            } catch (e: Throwable) {
                completion?.onCompletion(Result.Error(IOException("Error removing moderator")))
            }
        }
    }

    // Set a reaction for a message in a conversation
    fun setReaction(messageId: String, conversationId: String, reaction: String, completion: CompletionCallback? = null) {
        GlobalScope.launch {
            try {
                val requestBody = JSONObject()
                requestBody.put("reactionCode", reaction)
                val response = RetrofitClient.ellen.setReaction(conversationId = conversationId,
                    messageId = messageId,
                    body = requestBody.toString().toRequestBody()).execute()
                Log.d(TAG, "${response}")
                if (response.isSuccessful) {
                    completion?.onCompletion(Result.Success(true))
                } else {
                    completion?.onCompletion(Result.Error(IOException("Error setting reaction")))
                }
            } catch (e: Throwable) {
                completion?.onCompletion(Result.Error(IOException("Error setting reaction")))
            }
        }
    }

    // Report a message in a conversation
    fun reportMessage(messageId: String, conversationId: String, completion: CompletionCallback? = null) {
        GlobalScope.launch {
            try {
                val requestBody = JSONObject()
                val response = RetrofitClient.ellen.reportMessage(
                    conversationId = conversationId,
                    messageId = messageId,
                    body = requestBody.toString().toRequestBody()
                ).execute()
                Log.d(TAG, "${response}")
                if (response.isSuccessful) {
                    completion?.onCompletion(Result.Success(true))
                } else {
                    completion?.onCompletion(Result.Error(IOException("Error reporting message")))
                }
            } catch (e: Throwable) {
                completion?.onCompletion(Result.Error(IOException("Error reporting message")))
            }
        }
    }

    // Delete a message from a conversation
    fun deleteMessage(messageId: String, conversationId: String, completion: CompletionCallback? = null) {
        GlobalScope.launch {
            try {
                val response = RetrofitClient.ellen.deleteMessage(
                    conversationId = conversationId,
                    messageId = messageId
                ).execute()
                Log.d(TAG, "${response}")
                if (response.isSuccessful) {
                    completion?.onCompletion(Result.Success(true))
                } else {
                    completion?.onCompletion(Result.Error(IOException("Error deleting message")))
                }
            } catch (e: Throwable) {
                completion?.onCompletion(Result.Error(IOException("Error deleting message")))
            }
        }
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
                    Log.d(TAG, "${response}")
                    if (response.isSuccessful) {
                        val body: Conversation = response.body()!!
                        completion?.onCompletion(Result.Success(body))
                    } else {
                        completion?.onCompletion(Result.Error(IOException("Error creating conversation")))
                    }

                }
            } catch (e: Exception) {
                completion?.onCompletion(Result.Error(IOException("Error creating conversation")))
            }
        }
    }

    // Close a conversation
    fun closeConversation(conversationId: String, completion: CompletionCallback? = null) {
        GlobalScope.launch {
            try {
                Log.d(TAG, "Delete ${conversationId}")
                val response = RetrofitClient.ellen.deleteConversation(conversationId = conversationId).execute()
                Log.d(TAG, "${response}")
                if (response.isSuccessful) {
//                    return Result.Success(true)
                    completion?.onCompletion(Result.Success(true))
                } else {
                    completion?.onCompletion(Result.Error(IOException("Error closing conversation")))
                }
            } catch (e: Throwable) {
                completion?.onCompletion(Result.Error(IOException("Error closing conversation")))
            }
        }
    }

    // Create a message in a conversation
    fun createMessage(message: Message, completion: CompletionCallback? = null) {
        GlobalScope.launch {
            try {
                val postBody = JSONObject()
                postBody.put("body", message.body)

                val gson = Gson()

                // Metadata
                var metadataJson = JSONObject(gson.toJson(message.metadata))
                postBody.put("metadata", metadataJson)

                // Media
                message.media?.let {
                    val mediaJson = JSONObject(gson.toJson(message.media))
                    postBody.put("media", mediaJson)
                    Log.d(TAG,"${mediaJson}")
                }

                // Mention
                if(!message.mentions.isNullOrEmpty()) {
                    // Array of Mentions
                    val mentions = JSONArray()

                    message.mentions.forEach {
                        val mentionJson = JSONObject(gson.toJson(it))
                        mentions.put(mentionJson)
                    }

                    postBody.put("mentions", mentions)
                }

                val response = RetrofitClient.ellen.createMessage(
                    conversationId = message.conversationId,
                    body = postBody.toString().toRequestBody(Utils.MEDIA_TYPE_JSON)
                ).execute()

                Log.d(TAG, "${response}")

                if (response.isSuccessful) {
                    val body: Message = response.body()!!
                    completion?.onCompletion(Result.Success(body))
                }
            } catch (e: Throwable) {
                completion?.onCompletion(Result.Error(IOException("Error creating message")))
            }
        }
    }

    // Create a message media item for a message, effectively uploading an image
    fun createMediaItem(conversationId: String, file: File, contentType: String, completion: CompletionCallback? = null) {
        GlobalScope.launch {
            try {
                // create RequestBody instance from file
                val requestFile = RequestBody.create(contentType.toMediaType(),file)
                val requestBody = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("media", file.name, requestFile)
                    .build()

                val response = RetrofitClient.ellen.createMediaItem(
                    conversationId = conversationId,
                    body = requestBody
                ).execute()
                Log.d(TAG, "${response}")
                if (response.isSuccessful) {
                    val body: MediaItem = response.body()!!
                    completion?.onCompletion(Result.Success(body))
                } else {
                    completion?.onCompletion(Result.Error(IOException("Error creating media item")))
                }
            } catch (e: Throwable) {
                completion?.onCompletion(Result.Error(IOException("Error creating media item")))
            }
        }
    }

    // Update the title and/or description of a conversation
    fun updateConversation(conversationId: String, title: String? = null, description: String? = null, completion: CompletionCallback? = null) {
        GlobalScope.launch {
            try {
                val requestBody = JSONObject()
                title?.let {
                    requestBody.put("title", title)
                }
                description?.let {
                    requestBody.put("description", description)
                }
                val response = RetrofitClient.ellen.updateConversation(
                    conversationId = conversationId,
                    body = requestBody.toString().toRequestBody(MEDIA_TYPE_JSON)
                ).execute()
                Log.d(TAG, "${response}")
                if (response.isSuccessful) {
                    completion?.onCompletion(Result.Success(true))
                } else {
                    completion?.onCompletion(Result.Error(IOException("Error updating conversation")))
                }
            } catch (e: Throwable) {
                completion?.onCompletion(Result.Error(IOException("Error updating conversation")))
            }
        }
    }

    // Post a control event when a user starts or stops typing
    fun postControlEvent(userId: String, conversationId: String, eventName: String, completion: CompletionCallback? = null) {
        GlobalScope.launch {
            try {
                val contextBody = JSONObject()
                contextBody.put("initiatingUser", userId)
                contextBody.put("conversationId", conversationId)

                val requestBody = JSONObject()
                requestBody.put("context", contextBody)
                // user:typing:start
                // user:typing:stop
                requestBody.put("eventName", eventName)

                val response = RetrofitClient.ellen.postControlEvent(
                    conversationId = conversationId,
                    body = requestBody.toString().toRequestBody(MEDIA_TYPE_JSON)
                ).execute()
                Log.d(TAG, "postControlEvent ${response}")
                if (response.isSuccessful) {
                    completion?.onCompletion(Result.Success(true))
                } else {
                    completion?.onCompletion(Result.Error(IOException("Error posting control event")))
                }
            } catch (e: Throwable) {
                completion?.onCompletion(Result.Error(IOException("Error posting control event")))
            }
        }
    }

    // Currently unused in mobile apps
//    fun banParticipant(completion: CompletionCallback? = null) {
//        GlobalScope.launch {
//            try {
//                if (response.isSuccessful) {
//                }
//            } catch (e: Throwable) {
//                completion?.onCompletion(Result.Error(IOException("Error banning participant")))
//            }
//        }
//    }
//    fun silenceParticipant(completion: CompletionCallback? = null) {
//        GlobalScope.launch {
//            try {
//                if (response.isSuccessful) {
//                }
//            } catch (e: Throwable) {
//                completion?.onCompletion(Result.Error(IOException("Error silencing participant")))
//            }
//        }
//    }
//    fun unSilenceParticipant(completion: CompletionCallback? = null) {
//        GlobalScope.launch {
//            try {
//                if (response.isSuccessful) {
//                }
//            } catch (e: Throwable) {
//                completion?.onCompletion(Result.Error(IOException("Error unsilencing participant")))
//            }
//        }
//    }

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



