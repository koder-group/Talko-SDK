package com.koder.ellen.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.koder.ellen.Messenger.Companion.prefs
import com.koder.ellen.data.ConversationRepository
import com.koder.ellen.model.ClientConfiguration
import com.koder.ellen.model.Conversation
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO

internal class MainViewModel(private val conversationRepository: ConversationRepository) : ViewModel(), CoroutineScope {
    // Coroutine/async
    private val job = Job()
    override val coroutineContext = job + Dispatchers.Main

//    val firebaseUser = MutableLiveData<FirebaseUser>()
    val config = MutableLiveData<ClientConfiguration>()
    val subscribeChannelList = MutableLiveData<MutableList<String>>()
    val conversations = MutableLiveData<MutableList<Conversation>>()
    val currentConversation = MutableLiveData<Conversation>()
    private val _title = MutableLiveData<String>()
    val title: LiveData<String>
        get() = _title
    fun updateActionBarTitle(title: String) = _title.postValue(title)
    fun loadClientConfig() {
        launch {
            try {
//                var userResult = async(IO) { conversationRepository.getCurrentUser() }.await()
                val result = async(IO) { conversationRepository.getClientConfig() }.await()
                config.value = prefs?.clientConfiguration
            } catch (e: Exception) {

            }
        }
    }
    fun registerNotificationToken() {
        launch {
            try {
//                var userResult = async(IO) { conversationRepository.getCurrentUser() }.await()
                val result = async(IO) { conversationRepository.registerNotificationToken() }.await()
            } catch (e: Exception) {

            }
        }
    }
    override fun onCleared() {
        job.cancel()
    }
}
