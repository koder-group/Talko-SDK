package com.koder.ellenlibrary

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.koder.ellen.Client
import com.koder.ellen.EventCallback
import com.koder.ellen.Messenger
import com.koder.ellen.model.Conversation
import com.koder.ellen.model.Message
import com.koder.ellen.model.Participant
import com.koder.ellen.model.User
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    companion object {
        const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val userToken = "eyJhbGciOiJIUzI1NiIsImtpZCI6IjE3YzRhYmQ4YTE3MjQ0OTdiZmViMjBiMWM0ZDhmYjU0IiwidHlwIjoiSldUIn0.eyJ0ZW5hbnRfaWQiOiJFRkI5NEM3RS03MUU5LTQwNkItOTA1OS02MUFDMDUyMjdGMUIiLCJ1c2VyX2lkIjoiNEVFRDg2Q0UtNDZCNi00NjNGLUJBMjgtQzgzN0IzNDVBRUIzIiwidXNlcl9uYW1lIjoiamVmZmF0a29kZXIiLCJwcm9maWxlX2ltYWdlIjoiaHR0cHM6Ly9maXJlYmFzZXN0b3JhZ2UuZ29vZ2xlYXBpcy5jb20vdjAvYi9lbGxlbi1maXJlYmFzZS1leGFtcGxlLmFwcHNwb3QuY29tL28vQXZhdGFycyUyRnVzZXItMjEucG5nP2FsdD1tZWRpYSZ0b2tlbj1lZjhhYmI1MC0wNjJkLTQ1ZDItOTcwYS1mNDIxNjRmYzA0OWYiLCJleHAiOjE1ODUyODM4NzYsImlzcyI6Imh0dHBzOi8vZWxsZW4ua29kZXIuY29tL2FwaS9tYW5hZ2VtZW50IiwiYXVkIjoiaHR0cHM6Ly9lbGxlbi5rb2Rlci5jb20vYXBpL21lc3NhZ2luZyJ9.NvM5oeZadn2CpDOdCImH7FKjIdGkXk58gpYCAxCU0A0"
        val userId = "4eed86ce-46b6-463f-ba28-c837b345aeb3"
        Messenger.set(userToken, userId)
        Messenger.addEventHandler(object: EventCallback() {
            override fun onConversationCreated(conversation: Conversation) {
                Log.d(TAG, "onConversationCreated ${conversation}")
            }

            override fun onConversationClosed(conversation: Conversation) {
                Log.d(TAG, "onConversationClosed ${conversation}")
            }

            override fun onConversationModified(conversationId: String) {
                Log.d(TAG, "onConversationModified ${conversationId}")
            }

            override fun onMessageReceived(message: Message) {
                Log.d(TAG, "onMessageReceived ${message}")
            }

            override fun onParticipantStateChanged(participant: Participant) {
                Log.d(TAG, "onParticipantStateChanged ${participant}")
            }

            override fun onAddedToConversation(addedUserId: String) {
                Log.d(TAG, "onAddedToConversation ${addedUserId}")
            }

            override fun onRemovedFromConversation(removedUserId: String) {
                Log.d(TAG, "onRemovedFromConversation ${removedUserId}")
            }

            override fun onMessageRejected(message: Message) {
                Log.d(TAG, "onMessageRejected ${message}")
            }

            override fun onMessageDeleted(message: Message) {
                Log.d(TAG, "onMessageDeleted ${message}")
            }

            override fun onMessageUserReaction(message: Message) {
                Log.d(TAG, "onMessageUserReaction ${message}")
            }

            override fun onUserTypingStart(initiatingUserId: String) {
                Log.d(TAG, "onUserTypingStart ${initiatingUserId}")
            }

            override fun onUserTypingStop(initiatingUserId: String) {
                Log.d(TAG, "onUserTypingStop ${initiatingUserId}")
            }

            override fun onModeratorAdded(userId: String) {
                Log.d(TAG, "onModeratorAdded ${userId}")
            }

            override fun onModeratorRemoved(userId: String) {
                Log.d(TAG, "onModeratorRemoved ${userId}")
            }
        })

        val client = Client()

//        val conversations = client.getConversationsForLoggedInUser()  // android.os.NetworkOnMainThreadException

        GlobalScope.launch {
            // Background
            val conversations = async(IO) { client.getConversationsForLoggedInUser() }.await()
            for (conversation in conversations) {
                Log.d(TAG, "${conversation}")
            }

            val messages = async(IO) {
                client.getMessagesForConversation("650d5171-2451-4b24-9fad-5d63eec47201")
            }.await()
            for (message in messages) {
                Log.d(TAG, "${messages}")
            }
        }
    }
}
