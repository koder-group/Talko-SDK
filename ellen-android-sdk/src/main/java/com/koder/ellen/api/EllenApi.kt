package com.koder.ellen.api

import com.koder.ellen.core.Keys
import com.koder.ellen.model.*
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface EllenApi {
    // Ellen
    // https://kdrellenplatformapimd18.azure-api.net/
    @GET("users/{publicId}")
    fun getUser(@Path("publicId") publicId: String): Call<EllenUser>

    @GET("users/current")
    fun getCurrentUser(): Call<EllenUser>

    @GET("platform/clientConfiguration/android-sdk")
    fun getClientConfiguration(): Call<ClientConfiguration>

    @PUT("notifications/registration")
    fun notificationRegistration(@Body body: RequestBody): Call<ResponseBody>

    @POST("conversation/search")
    fun getConversations(@Body body: RequestBody): Call<MutableList<Conversation>>

    @POST("conversation/{conversationId}/messages/search")
    fun getMessagesForConversation(@Path("conversationId") conversationId: String,
                                @Body body: RequestBody): Call<MutableList<Message>>

    @POST("conversation/")
    fun createConversation(@Body body: RequestBody): Call<Conversation>

    @GET("conversation/{conversationId}")
    fun getConversation(@Path("conversationId") conversationId: String): Call<Conversation>

    @POST("conversation/{conversationId}/messages")
    fun createMessage(@Path("conversationId") conversationId: String,
                      @Body body: RequestBody): Call<Message>

    @DELETE("conversation/{conversationId}")
    fun deleteConversation(@Path("conversationId") conversationId: String): Call<Boolean>

    @PUT("conversation/{conversationId}/participants/{participantId}")
    fun addParticipant(@Path("conversationId") conversationId: String,
                       @Path("participantId") participantId: String): Call<Any>

    @DELETE("conversation/{conversationId}/participants/{participantId}")
    fun removeParticipant(@Path("conversationId") conversationId: String,
                          @Path("participantId") participantId: String): Call<Any>

    @Headers("x-functions-key: ${Keys.X_FUNCTIONS_KEY}")
    @POST("conversation/{conversationId}/media")
    fun createMediaItem(@Path("conversationId") conversationId: String,
                        @Body body: RequestBody
                        ): Call<MediaItem>

    @PUT("conversation/{conversationId}/messages/{messageId}/reaction")
    fun setReaction(@Path("conversationId") conversationId: String,
                    @Path("messageId") messageId: String,
                    @Body body: RequestBody): Call<Any>

    @PUT("conversation/{conversationId}/messages/{messageId}/report")
    fun reportMessage(@Path("conversationId") conversationId: String,
                      @Path("messageId") messageId: String,
                      @Body body: RequestBody): Call<Any>

    @DELETE("conversation/{conversationId}/messages/{messageId}")
    fun deleteMessage(@Path("conversationId") conversationId: String,
                      @Path("messageId") messageId: String): Call<Any>

    @PUT("conversation/{conversationId}")
    fun updateConversation(@Path("conversationId") conversationId: String,
                           @Body body: RequestBody): Call<ResponseBody>

    @POST("users/search")
        fun searchUser(@Body body: RequestBody): Call<MutableList<User>>

    @PUT("conversation/{conversationId}/moderators/{participantId}")
    fun addModerator(@Path("conversationId") conversationId: String,
                     @Path("participantId") participantId: String): Call<ResponseBody>

    @DELETE("conversation/{conversationId}/moderators/{participantId}")
    fun deleteModerator(@Path("conversationId") conversationId: String,
                    @Path("participantId") participantId: String): Call<ResponseBody>

    @POST("conversation/{conversationId}/events")
    fun postControlEvent(@Path("conversationId") conversationId: String,
                         @Body body: RequestBody): Call<Any>
}