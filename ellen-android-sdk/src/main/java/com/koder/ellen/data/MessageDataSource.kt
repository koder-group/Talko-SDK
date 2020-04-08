package com.koder.ellen.data

import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import com.google.gson.Gson
import com.koder.ellen.api.RetrofitClient
import com.koder.ellen.model.*
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

import java.lang.Exception
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import okio.IOException
import java.io.File


/**
 * Class that handles retrieving Conversation information.
 */
internal class MessageDataSource {
    private val TAG = "MessageDataSource"

    // https://afed418a.ngrok.io/users/{publicId}
    fun getUser(publicId: String): Result<EllenUser> {
        try {
            val response = RetrofitClient.ellen.getUser(
                    publicId = publicId
                    ).execute()
            Log.d(TAG, "${response}")
            if (response.isSuccessful) {
                val body: EllenUser = response.body()!!
                Log.d(TAG, "${body}")
                return Result.Success(body)
            }
        } catch (e: Throwable) {
            return Result.Error(IOException("Error getting user", e))
        }

        return Result.Error(IOException())
    }

//    val response = RetrofitClient.ellen.getUser(
//        publicId = publicId
//    ).execute()

    fun getEllenUser(publicId: String): Result<EllenUser> {
        try {
            val response = RetrofitClient.ellen.getUser(
                publicId = publicId
            ).execute()
            Log.d(TAG, "${response}")
            if (response.isSuccessful) {
                val body: EllenUser = response.body()!!
                Log.d(TAG, "${body}")
                return Result.Success(body)
            }
        } catch (e: Throwable) {
            return Result.Error(IOException("Error getting user", e))
        }

        return Result.Error(IOException())
    }

    // https://kdrellenplatformapimd18.azure-api.net/conversation/{conversationId}/messages
    fun createMessage(message: Message): Result<Message> {
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
                body = postBody.toString().toRequestBody(ConversationDataSource.MEDIA_TYPE_JSON)
            ).execute()
            Log.d(TAG, "createMessage ${response}")
            if (response.isSuccessful) {
                val body: Message = response.body()!!
                return Result.Success(body)
            }
        } catch (e: Throwable) {
            return Result.Error(IOException("Error creating message", e))
        }

        return Result.Error(IOException())
    }

    //  https://kdrellenplatformapimd18.azure-api.net/conversation/
    fun createConversation(conversationUser: User): Result<Conversation> {
        try {
            // Participant
            val participant = Participant(conversationUser, 0, 0)
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
                return Result.Success(body)
            }
        } catch (e: Throwable) {
            return Result.Error(IOException("Error creating conversation", e))
        }

        return Result.Error(IOException())
    }

    fun createEllenConversation(ellenUser: EllenUser): Result<Conversation> {
        try {
            // Participant
//            val tenantId: String,
//    val userId: String,
//    val displayName: String,
//    val profileImageUrl: String,
            // Convert EllenUser to User
            val conversationUser = User(tenantId = ellenUser.tenantId, userId = ellenUser.userId, displayName = ellenUser.profile.displayName, profileImageUrl = ellenUser.profile.profileImageUrl)
            val participant = Participant(conversationUser, 0, 0)
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
                return Result.Success(body)
            }
        } catch (e: Throwable) {
            return Result.Error(IOException("Error creating conversation", e))
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

    fun getMessages(conversationId: String): Result<MutableList<Message>> {
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
                return Result.Success(body)
            }
        } catch (e: Exception) {
            return Result.Error(IOException("Error getting messages", e))
        }
        return Result.Error(IOException())
    }

    fun addParticipant(user: User, conversationId: String): Result<Any> {
        try {
            val response = RetrofitClient.ellen.addParticipant(
                conversationId = conversationId,
                participantId = user.userId)
                .execute()
            Log.d(TAG, "Response ${response}")
            if(response.code() == 200) {
                return Result.Success(true)
            }
        } catch (e: Exception) {
            Log.d(TAG, "Error ${e.toString()}")
            return Result.Error(IOException("Error adding participant", e))
        }
        return Result.Error(IOException())
    }

    fun removeParticipant(user: User, conversationId: String): Result<Any> {
        try {
            val response = RetrofitClient.ellen.removeParticipant(
                conversationId = conversationId,
                participantId = user.userId
            ).execute()
            Log.d(TAG, "${response}")
            if(response.code() == 200) {
                return Result.Success(true)
            }
        } catch (e: Exception) {
            return Result.Error(IOException("Error removing participant", e))
        }
        return Result.Error(IOException())
    }

    fun createMediaItem(conversationId: String, file: File, contentType: String): Result<MediaItem> {
        try {
//            Log.d(TAG, "conversationId ${conversationId}")
//            Log.d(TAG, "File ${file}")
//            Log.d(TAG, "MediaType ${contentType.toMediaType()}")

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
            if(response.isSuccessful) {
                val body: MediaItem = response.body()!!

                return Result.Success(body)
            }
        } catch (e: Exception) {
            return Result.Error(IOException("Error creating media item", e))
        }
        return Result.Error(IOException())
    }

    fun setReaction(message: Message, reaction: String): Result<Any> {
        try {
            val requestBody = JSONObject()
            requestBody.put("reactionCode", reaction)
            val response = RetrofitClient.ellen.setReaction(conversationId = message.conversationId,
                messageId = message.messageId!!,
                body = requestBody.toString().toRequestBody()).execute()
            Log.d(TAG, "setReaction ${response}")
            if(response.isSuccessful) {
                return Result.Success(true)
            }
        } catch (e: Exception) {
            return Result.Error(IOException("Error setting reaction", e))
        }
        return Result.Error(IOException())
    }

    fun reportMessage(message: Message): Result<Any> {
        try {
            val requestBody = JSONObject()
            val response = RetrofitClient.ellen.reportMessage(
                conversationId = message.conversationId,
                messageId = message.messageId!!,
                body = requestBody.toString().toRequestBody()
            ).execute()
            Log.d(TAG, "${response}")
            if(response.isSuccessful) {
                return Result.Success(true)
            }
        } catch (e: Exception) {
            return Result.Error(IOException("Error reporting message", e))
        }
        return Result.Error(IOException())
    }

    fun deleteMessage(message: Message): Result<Any> {
        try {
            Log.d(TAG, "Deleting ${message.messageId}")
            val response = RetrofitClient.ellen.deleteMessage(
                conversationId = message.conversationId,
                messageId = message.messageId!!
            ).execute()
            Log.d(TAG, "${response}")
            if(response.isSuccessful) {
                return Result.Success(true)
            }
        } catch (e: Exception) {
            return Result.Error(IOException("Error deleting message", e))
        }
        return Result.Error(IOException())
    }

    fun updateConversationTitle(conversationId: String, title: String): Result<Any> {
        try {
            val requestBody = JSONObject()
            requestBody.put("title", title)
            val response = RetrofitClient.ellen.updateConversation(
                conversationId = conversationId,
                body = requestBody.toString().toRequestBody(MEDIA_TYPE_JSON)
            ).execute()
            Log.d(TAG, "${response}")
            if(response.isSuccessful) {
                return Result.Success(true)
            }
        } catch (e:Exception) {
            return Result.Error(IOException("Error updating title", e))
        }
        return Result.Error(IOException())
    }

    fun updateConversationDescription(conversationId: String, description: String): Result<Any> {
        try {
            val requestBody = JSONObject()
            requestBody.put("description", description)
            val response = RetrofitClient.ellen.updateConversation(
                conversationId = conversationId,
                body = requestBody.toString().toRequestBody(MEDIA_TYPE_JSON)
            ).execute()
            Log.d(TAG, "${response}")
            if(response.isSuccessful) {
                return Result.Success(true)
            }
        } catch (e:Exception) {
            return Result.Error(IOException("Error updating description", e))
        }
        return Result.Error(IOException())
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

    fun searchUser(text: String): Result<MutableList<User>> {
        try {
            val requestBody = JSONObject()
            requestBody.put("pageNumber", 1)
            requestBody.put("pageSize", 10)
            requestBody.put("displayNameFilter", text)
            val response = RetrofitClient.ellen.searchUser(
                body = requestBody.toString().toRequestBody(MEDIA_TYPE_JSON)).execute()
            Log.d(TAG, "${response}")
            if (response.isSuccessful) {
                val body: MutableList<User> = response.body()!!
//                Log.d(TAG, "${body}")
                return Result.Success(body)
            }
        } catch (e: Throwable) {
            return Result.Error(IOException("Error searching user", e))
        }
        return Result.Error(IOException())
    }

    fun addModerator(userId: String, conversationId: String): Result<ResponseBody> {
        try {
            val response = RetrofitClient.ellen.addModerator(
                conversationId = conversationId,
                participantId = userId
            ).execute()
            if(response.isSuccessful) {
                return Result.Success(response.body()!!)
            }
        } catch (e: Throwable) {
            return Result.Error(IOException("Error promoting user to moderator", e))
        }
        return Result.Error(IOException())
    }

    fun removeModerator(userId: String, conversationId: String): Result<ResponseBody> {
        try {
            val response = RetrofitClient.ellen.deleteModerator(
                conversationId = conversationId,
                participantId = userId
            ).execute()
            if(response.isSuccessful) {
                return Result.Success(response.body()!!)
            }
        } catch (e: Throwable) {
            return Result.Error(IOException("Error removing user as moderator", e))
        }
        return Result.Error(IOException())
    }

    fun typingStarted(userId: String, conversationId: String): Result<Any> {
        try {
            val contextBody = JSONObject()
            contextBody.put("initiatingUser", userId)
            contextBody.put("conversationId", conversationId)

            val requestBody = JSONObject()
            requestBody.put("context", contextBody)
            requestBody.put("eventName", "user:typing:start")

            val response = RetrofitClient.ellen.postControlEvent(
                conversationId = conversationId,
                body = requestBody.toString().toRequestBody(MEDIA_TYPE_JSON)
                ).execute()
        } catch(e: Throwable) {
            return Result.Error(IOException("Error sending typing started", e))
        }
        return Result.Error(IOException())
    }

    fun typingStopped(userId: String, conversationId: String): Result<Any> {
        try {
            val contextBody = JSONObject()
            contextBody.put("initiatingUser", userId)
            contextBody.put("conversationId", conversationId)

            val requestBody = JSONObject()
            requestBody.put("context", contextBody)
            requestBody.put("eventName", "user:typing:stop")

            val response = RetrofitClient.ellen.postControlEvent(
                conversationId = conversationId,
                body = requestBody.toString().toRequestBody(MEDIA_TYPE_JSON)
            ).execute()
        } catch(e: Throwable) {
            return Result.Error(IOException("Error sending typing stopped", e))
        }
        return Result.Error(IOException())
    }

    companion object {
        val MEDIA_TYPE_JSON = "application/json; charset=utf-8".toMediaType()
    }
}

