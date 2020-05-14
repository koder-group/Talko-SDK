package com.koder.ellen.ui.message

import android.app.Application
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.koder.ellen.model.Conversation
import com.koder.ellen.model.Message
import com.koder.ellen.model.User
import com.koder.ellen.data.MessageRepository
import com.koder.ellen.data.Result
import com.koder.ellen.model.MediaItem
import com.koder.ellen.ui.message.MessageFormState
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import okio.*
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.util.*

internal class MessageViewModel(private val messageRepository: MessageRepository, application: Application) :
    AndroidViewModel(application),
    CoroutineScope {

    private val TAG = "MessageViewModel"

    private val context = getApplication<Application>().applicationContext

    val conversation = MutableLiveData<Conversation>()
    val updateConversation = MutableLiveData<Conversation>()
    val messages = MutableLiveData<MutableList<Message>>()
    val participants = MutableLiveData<MutableList<User>>()
    val participantAdded = MutableLiveData<User>()
    val participantRemoved = MutableLiveData<User>()
    val addedMessage = MutableLiveData<Message>()
    val message = MutableLiveData<Message>()
    val reportedMessage = MutableLiveData<Message>()
    val deletedMessage = MutableLiveData<Message>()
    val mentionedParticipants = MutableLiveData<List<User>>()
    val conversationName = MutableLiveData<String>()
    val conversationDescription = MutableLiveData<String>()
    val deleteConversationResult = MutableLiveData<Boolean>()
    val userSearchResult = MutableLiveData<MutableList<User>>()
    val moderatorAdded = MutableLiveData<User>()
    val moderatorRemoved = MutableLiveData<User>()

    private val _messageForm = MutableLiveData<MessageFormState>()
    val messageFormState: LiveData<MessageFormState> = _messageForm

    private val _videoMessageForm = MutableLiveData<MessageFormState>()
    val videoMessageFormState: LiveData<MessageFormState> = _videoMessageForm

    fun messageDataChanged(text: String, mediaList: MutableList<Message>) {
        if (text.isNullOrBlank() && mediaList.size == 0) {
            _messageForm.value = MessageFormState(isDataValid = false)
        } else {
            _messageForm.value = MessageFormState(isDataValid = true)
        }

        findMentionedParticipants(text)
    }

    fun userSearchChanged(text: String) {
        if (text.isNotBlank()) {
            // Search user
            searchUser(text)
        }
    }

    fun videoMessageDataChanged(text: String) {
        if (text.isNullOrBlank()) {
            _videoMessageForm.value = MessageFormState(isDataValid = false)
        } else {
            _videoMessageForm.value = MessageFormState(isDataValid = true)
        }
    }

    // Return a list of mentioned participants from text input
    fun findMentionedParticipants(text: String) {
        // Detect if first character of word is @
        val words = text.split(" ")
        if(!words.last().isNullOrBlank() && words.last().length > 0) {
            // Match first letter
//            Log.d(TAG, "${words.last().first()}")
            if(words.last().first().equals('@', ignoreCase = true) && words.last().length == 1) {
                // If @ only
                mentionedParticipants.value = participants.value
                return
            }

            if(words.last().first().equals('@', ignoreCase = true) && words.last().length > 1) {
                // If @ + chars
                // Find matches in participants
                val word = words.last().substring(1)
//                Log.d(TAG, "${word}")
//                Log.d(TAG, "${participants.value}")
                mentionedParticipants.value  = participants.value?.filter { it.displayName.startsWith(word, true) }!!
//                Log.d(TAG, "${list?.size} ${list}")
                return
            }
        }
        mentionedParticipants.value = listOf()
    }

    // Coroutine/async
    private val job = Job()
    override val coroutineContext = job + Main

    fun send(message: Message) {
        launch {
            try {
                // Create message
                val result =  async(Dispatchers.IO) { messageRepository.createMessage(message) }.await()

            } catch (e: Exception) {
//                updateUi(e.toString())
            }
        }
    }

    fun createConversation(publicId: String) {
        launch {
            try {
                // Get participant user
//                val result = async(IO) { messageRepository.getUser(publicId) }.await()    // TODO
                val result = async(IO) { messageRepository.getEllenUser(publicId) }.await()
                if (result is Result.Success) {
                    // Create conversation
//                    val convoResult = async(IO) { messageRepository.createConversation(result.data) }.await() // TODO
                    val convoResult = async(IO) { messageRepository.createEllenConversation(result.data) }.await()

                    if (convoResult is Result.Success) conversation.value = convoResult.data
                }
            } catch (e: Exception) {
//                updateUi(e.toString())
            }
        }
    }

    fun updateConversation(conversationId: String) {
        launch {
            try {
                var result = async(IO) { messageRepository.getConversation(conversationId)}.await()
                if(result is Result.Success) updateConversation.value = result.data
            } catch (e: Exception) {

            }
        }
    }

    fun getConversation(conversationId: String) {
        launch {
            try {
                var result = async(IO) { messageRepository.getConversation(conversationId)}.await()
                if(result is Result.Success) conversation.value = result.data
            } catch (e: Exception) {

            }
        }
    }

    fun getMessages(conversationId: String, forceLoad: Boolean = false) {
        launch {
            try {
                var result = async(IO) { messageRepository.getMessages(conversationId, forceLoad)}.await()
                if(result is Result.Success) messages.value = result.data
            } catch (e: Exception) {

            }
        }
    }

    fun addParticipant(participantId: String, conversationId: String) {
        launch {
            try {
                // Get User
//                val userResult = async(IO) { messageRepository.getUser(participantId) }.await()
                val userResult = async(IO) { messageRepository.getEllenUser(participantId) }.await()
                if (userResult is Result.Success) {
                    Log.d(TAG, "${userResult.data}")
//                    val participantResult = async(IO) { messageRepository.addParticipant(userResult.data, conversationId) }.await()
                    // Convert EllenUser to User
                    val conversationUser = User(tenantId = userResult.data.tenantId, userId = userResult.data.userId, displayName = userResult.data.profile.displayName, profileImageUrl = userResult.data.profile.profileImageUrl)
                    val participantResult = async(IO) { messageRepository.addParticipant(conversationUser, conversationId) }.await()
                    if(participantResult is Result.Success) {
                        Log.d(TAG, "Participant added")
                        // Get user displayName
                        participantAdded.value = conversationUser
                    }
                }
            } catch (e: Exception) {

            }
        }
    }

    fun addUserToParticipants(userId: String) {
        launch {
            try {
                val userResult = async(IO) { messageRepository.getEllenUser(userId) }.await()
                if(userResult is Result.Success) {
                    // Convert EllenUser to User
                    val conversationUser = User(tenantId = userResult.data.tenantId, userId = userResult.data.userId, displayName = userResult.data.profile.displayName, profileImageUrl = userResult.data.profile.profileImageUrl)
                    participantAdded.value = conversationUser
                }
            } catch (e: Exception) {

            }
        }
    }

    fun removeParticipant(user: User, conversationId: String) {
        launch {
            try {
                val result = async(IO) { messageRepository.removeParticipant(user, conversationId) }.await()
                if (result is Result.Success) {
                    participantRemoved.value = user
                }
            } catch (e: Exception) {

            }
        }
    }

    fun addImage(message: Message, imageUri: Uri) {
        launch {
            val contentType = context.contentResolver.getType(imageUri)

            try {
                val result = async(IO) {
                    val filename = queryName(imageUri!!)
//                    Log.d(TAG, "Filename ${filename}")

                    // Create temp file
//                    val tempFile = kotlin.io.createTempFile(filename)
                    val tempFile = File(context.cacheDir, filename) // Excludes .tmp extension
//                    Log.d(TAG, "tempFile ${tempFile}")

                    // Save temp file
                    val cachedFile = saveContentToFile(imageUri, tempFile!!)
//                    Log.d(TAG, "cachededFile ${cachedFile}")

                    // Get content type
//                    val contentType = context.contentResolver.getType(imageUri)
//                    Log.d(TAG, "contentType ${contentType}")

                    contentType?.let {
                        messageRepository.createMediaItem(message.conversationId, cachedFile, contentType)
                    }
                }.await()

                if(result is Result.Success) {
                    val mediaItem: MediaItem = result.data
                    Log.d(TAG, "MediaItem ${mediaItem}")

                    // Replace with uploaded URLs
                    message.media?.content?.source = mediaItem.mediaUrl
                    message.media?.thumbnail?.source = mediaItem.thumbnailUrl

                    // Create new message with media
                    val createResult = async(IO) { messageRepository.createMessage(message) }.await()
                    if(createResult is Result.Success) {
                        Log.d(TAG, "createResult ${createResult.data}")
                        addedMessage.value = createResult.data
                    }
                }
            } catch (e: Exception) {

            }
        }
    }

    fun setReaction(message: Message, reaction: String) {
        launch {
            try {
                val result = async(IO) { messageRepository.setReaction(message, reaction) }.await()
            } catch (e: Exception) {

            }
        }
    }

    fun updateMessage(conversationMessage: Message) {
        launch {
            try {
                // Get message to update
                val result = async(IO) { messageRepository.getMessages(conversationMessage.conversationId) }.await()
                if(result is Result.Success) {
                    message.value = result.data.find { it.messageId.equals(conversationMessage.messageId, ignoreCase = true) }
                }
            } catch (e: Exception) {
            }
        }
    }

    override fun onCleared() {
        job.cancel()
    }

    // Get file name
    private fun queryName(uri: Uri): String {
        val returnCursor: Cursor = context.contentResolver.query(uri, null, null, null, null)!!
        val nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        returnCursor.moveToFirst()
        val name = returnCursor.getString(nameIndex)
        returnCursor.close()
        return name
    }

    // Create temp file
    private fun createTempFile(name: String): File? {
        var file: File? = null
        try {
            file = File.createTempFile(name, null, context.cacheDir)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return file
    }

    // Save temp file
    private fun saveContentToFile(uri: Uri, file: File): File {
        try {
            val stream = context.contentResolver.openInputStream(uri)
            val source: BufferedSource = stream!!.source().buffer()
            val sink: BufferedSink = file.sink().buffer()
            sink.writeAll(source)
            sink.close()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return file
    }

    fun reportMessage(message: Message) {
        launch {
            try {
                val result = async(IO) { messageRepository.reportMessage(message) }.await()
                if(result is Result.Success) {
                    reportedMessage.value = message
                }
            } catch (e: Exception) {
            }
        }
    }

    fun deleteMessage(message: Message) {
        launch {
            try {
                val result = async(IO) { messageRepository.deleteMessage(message) }.await()
                if(result is Result.Success) {
                    deletedMessage.value = message
                }
            } catch (e: Exception) {

            }
        }
    }

    fun updateConversationTitle(conversationId: String, title: String) {
        launch {
            try {
                val result = async(IO) { messageRepository.updateConversationTitle(conversationId, title) }.await()
                if(result is Result.Success) {
                    conversationName.value = title
                }
            } catch (e: Exception) {

            }
        }
    }

    fun updateConversationDescription(conversationId: String, description: String) {
        launch {
            try {
                val result = async(IO) { messageRepository.updateConversationDescription(conversationId, description) }.await()
                if(result is Result.Success) {
                    conversationDescription.value = description
                }
            } catch (e: Exception) {

            }
        }
    }

    fun deleteConversation(conversationId: String) {
        launch {
            try {
                val result = async(IO) { messageRepository.deleteConversation(conversationId) }.await()
//                var deleteResult = DeleteResult(conversation)
//                deleteResult.conversation = conversation
                if(result is Result.Success) {
//                    deleteResult.deleted = result.data
                    deleteConversationResult.value = true
                }
//                delete.value = deleteResult
            } catch (e: Exception) {

            }
        }
    }

    fun searchUser(text: String) {
        launch {
            try {
                val result = async(IO) { messageRepository.searchUser(text) }.await()
                if(result is Result.Success) {
                    userSearchResult.value = result.data
                }
            } catch (e: Exception) {

            }
        }
    }

    fun addModerator(user: User, conversationId: String) {
        launch {
            try {
                val result =
                    async(IO) { messageRepository.addModerator(user.userId, conversationId) }.await()
                Log.d(TAG, "${result}")
                if (result is Result.Success) {
                    // Update participant's role
                    moderatorAdded.value = user
                }
            } catch (e: Exception) {

            }
        }
    }

    fun removeModerator(user: User, conversationId: String) {
        launch {
            try {
                val result = async(IO) { messageRepository.removeModerator(user.userId, conversationId) }.await()
                Log.d(TAG, "${result}")
                if (result is Result.Success) {
                    // Update participant's role
                    moderatorRemoved.value = user
                }
            } catch (e: Exception) {

            }
        }
    }

    fun typingStarted(userId: String, conversationId: String) {
        launch {
            try {
                val result = async(IO) { messageRepository.typingStarted(userId.toUpperCase(), conversationId.toUpperCase()) }.await()
            } catch (e: Exception) {

            }
        }
    }

    fun typingStopped(userId: String, conversationId: String) {
        launch {
            try {
                val result = async(IO) { messageRepository.typingStopped(userId.toUpperCase(), conversationId.toUpperCase()) }.await()
            } catch (e: Exception) {

            }
        }
    }
}
