package com.koder.ellen.ui.conversation

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import android.util.Log
import androidx.lifecycle.LiveData
import com.koder.ellen.Messenger
import com.koder.ellen.Messenger.Companion.db
import com.koder.ellen.Messenger.Companion.prefs
import com.koder.ellen.data.ConversationRepository
import com.koder.ellen.model.ClientConfiguration
import com.koder.ellen.model.Conversation
import com.koder.ellen.data.Result

internal class ConversationViewModel(val conversationRepository: ConversationRepository) : ViewModel(),
    CoroutineScope {

    // Fragment
    private val _title = MutableLiveData<String>()
    val title: LiveData<String>
        get() = _title
    fun updateActionBarTitle(title: String) = _title.postValue(title)
    //

    val TAG = "ConversationsViewModel"
    val conversations = MutableLiveData<MutableList<Conversation>>()
    val config = MutableLiveData<ClientConfiguration>()
    val delete = MutableLiveData<DeleteResult>()

    // Coroutine/async
    private val job = Job()
    override val coroutineContext = job + Dispatchers.Main

    fun loadClientConfig() {
        launch {
            try {
//                var userResult = async(Dispatchers.IO) { conversationRepository.getCurrentUser()}.await()
                val result = async(Dispatchers.IO) {conversationRepository.getClientConfig()}.await()
                config.value = prefs?.clientConfiguration
            } catch (e: Exception) {

            }
        }
    }

    fun loadConversations(forceLoad: Boolean = false) {
        launch {
            try {
                // Get Conversations
                var convos =  async(Dispatchers.IO) {conversationRepository.getConversations(forceLoad)}.await()

                // Get Conversation Messages
                convos = async(Dispatchers.IO) {conversationRepository.getConversationMessages(convos, forceLoad)}.await()
                Log.d(TAG, "${convos}")

                // User profile cache
                val result = async(IO) {
                    val userProfiles = db?.userProfileDao()?.getAll()
                    userProfiles?.let {
                        for(profile in userProfiles) {
                            Messenger.userProfileCache.put(profile.userId, profile)
                        }
                    }
                }.await()

                conversations.apply { value = convos }
            } catch (e: Exception) {
//                updateUi(e.toString())
            }
        }
    }

    fun deleteConversation(conversation: Conversation) {
        launch {
            try {
                val result = async(IO) { conversationRepository.deleteConversation(conversation.conversationId) }.await()
                var deleteResult = DeleteResult(conversation)
                deleteResult.conversation = conversation
                if(result is Result.Success) {
                    deleteResult.deleted = result.data
                }
                delete.value = deleteResult
            } catch (e: Exception) {

            }
        }
    }

    override fun onCleared() {
        job.cancel()
    }
}