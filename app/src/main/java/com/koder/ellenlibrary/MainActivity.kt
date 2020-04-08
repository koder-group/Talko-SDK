package com.koder.ellenlibrary

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.koder.ellen.*
import com.koder.ellen.data.Result

class MainActivity : AppCompatActivity() {
    companion object {
        const val TAG = "MainActivity"
        const val TEST_OK = "TEST_OK"
        const val TEST_NOK = "TEST_NOK"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val userToken = "eyJhbGciOiJIUzI1NiIsImtpZCI6IjE3YzRhYmQ4YTE3MjQ0OTdiZmViMjBiMWM0ZDhmYjU0IiwidHlwIjoiSldUIn0.eyJ0ZW5hbnRfaWQiOiJFRkI5NEM3RS03MUU5LTQwNkItOTA1OS02MUFDMDUyMjdGMUIiLCJ1c2VyX2lkIjoiNEVFRDg2Q0UtNDZCNi00NjNGLUJBMjgtQzgzN0IzNDVBRUIzIiwidXNlcl9uYW1lIjoiamVmZmF0a29kZXIiLCJwcm9maWxlX2ltYWdlIjoiaHR0cHM6Ly9maXJlYmFzZXN0b3JhZ2UuZ29vZ2xlYXBpcy5jb20vdjAvYi9lbGxlbi1maXJlYmFzZS1leGFtcGxlLmFwcHNwb3QuY29tL28vQXZhdGFycyUyRnVzZXItMjEucG5nP2FsdD1tZWRpYSZ0b2tlbj1lZjhhYmI1MC0wNjJkLTQ1ZDItOTcwYS1mNDIxNjRmYzA0OWYiLCJleHAiOjE1ODYxNzAwMjUsImlzcyI6Imh0dHBzOi8vZWxsZW4ua29kZXIuY29tL2FwaS9tYW5hZ2VtZW50IiwiYXVkIjoiaHR0cHM6Ly9lbGxlbi5rb2Rlci5jb20vYXBpL21lc3NhZ2luZyJ9.b49SVo7N142jBlLleIstWb8zz_JNFQTlCMWiM6NJQq4"

        val userId = "4eed86ce-46b6-463f-ba28-c837b345aeb3"
        // Message options
//        Messenger.senderMessageRadius = 8
//        Messenger.senderBackgroundColor = "#CC0000"
//        Messenger.selfMessageRadius = 0
//        Messenger.selfBackgroundColor = "#222222"
        Messenger.set(userToken, applicationContext, object: CompletionCallback() {
            override fun onCompletion(result: Result<Any>) {
                if(result is Result.Success) {
                    Log.d(TAG, "Messenger successfully set")
                    val intent = Intent(this@MainActivity, MessengerActivity::class.java)
                    startActivity(intent)
                    setResult(Activity.RESULT_OK)
                    finish()
                }
            }
        })

//        Messenger.addEventHandler(object: EventCallback() {
//            override fun onConversationCreated(conversation: Conversation) {
//                Log.d(TAG, "onConversationCreated ${conversation}")
//            }
//
//            override fun onConversationClosed(conversation: Conversation) {
//                Log.d(TAG, "onConversationClosed ${conversation}")
//            }
//
//            override fun onConversationModified(conversationId: String) {
//                Log.d(TAG, "onConversationModified ${conversationId}")
//            }
//
//            override fun onMessageReceived(message: Message) {
//                Log.d(TAG, "onMessageReceived ${message}")
//            }
//
//            override fun onParticipantStateChanged(participant: Participant) {
//                Log.d(TAG, "onParticipantStateChanged ${participant}")
//            }
//
//            override fun onAddedToConversation(addedUserId: String) {
//                Log.d(TAG, "onAddedToConversation ${addedUserId}")
//            }
//
//            override fun onRemovedFromConversation(removedUserId: String) {
//                Log.d(TAG, "onRemovedFromConversation ${removedUserId}")
//            }
//
//            override fun onMessageRejected(message: Message) {
//                Log.d(TAG, "onMessageRejected ${message}")
//            }
//
//            override fun onMessageDeleted(message: Message) {
//                Log.d(TAG, "onMessageDeleted ${message}")
//            }
//
//            override fun onMessageUserReaction(message: Message) {
//                Log.d(TAG, "onMessageUserReaction ${message}")
//            }
//
//            override fun onUserTypingStart(initiatingUserId: String) {
//                Log.d(TAG, "onUserTypingStart ${initiatingUserId}")
//            }
//
//            override fun onUserTypingStop(initiatingUserId: String) {
//                Log.d(TAG, "onUserTypingStop ${initiatingUserId}")
//            }
//
//            override fun onModeratorAdded(userId: String) {
//                Log.d(TAG, "onModeratorAdded ${userId}")
//            }
//
//            override fun onModeratorRemoved(userId: String) {
//                Log.d(TAG, "onModeratorRemoved ${userId}")
//            }
//        })
//
//        val client = Client()

//        val conversations = client.getConversationsForLoggedInUser()  // android.os.NetworkOnMainThreadException

//        client.findUsers("jef", object: CompletionCallback() {
//            override fun onCompletion(result: Result<Any>) {
//                if(result is Result.Success) {
//                    Log.d(TEST_OK, "findUsers ${result.data}")
//                } else {
//                    Log.d(TEST_NOK, "findUsers ${result}")
//                }
//            }
//        })

//        client.getLoggedInUserProfile(object: CompletionCallback() {
//            override fun onCompletion(result: Result<Any>) {
//                if(result is Result.Success) {
//                    Log.d(TEST_OK, "getLoggedInUserProfile ${result.data}")
//                } else {
//                    Log.d(TEST_NOK, "getLoggedInUserProfile ${result}")
//                }
//            }
//        })

//        client.getConversationsForLoggedInUser(object: CompletionCallback() {
//            override fun onCompletion(result: Result<Any>) {
//                if(result is Result.Success) {
//                    Log.d(TEST_OK, "getConversationsForLoggedInUser ${result.data}")
//                } else {
//                    Log.d(TEST_NOK, "getConversationsForLoggedInUser ${result}")
//                }
//            }
//        })

        val conversationId = "df46647a-7143-4456-ab90-38b8904bafaa"
//        client.getMessagesForConversation(conversationId, object: CompletionCallback() {
//            override fun onCompletion(result: Result<Any>) {
//                if(result is Result.Success) {
//                    Log.d(TEST_OK, "getMessagesForConversation ${result.data}")
//                } else {
//                    Log.d(TEST_NOK, "getMessagesForConversation ${result}")
//                }
//            }
//        })

//        client.getConversation(conversationId, object: CompletionCallback() {
//            override fun onCompletion(result: Result<Any>) {
//                if(result is Result.Success) {
//                    Log.d(TEST_OK, "getConversation ${result.data}")
//                } else {
//                    Log.d(TEST_NOK, "getConversation ${result}")
//                }
//            }
//        })

        val participantUserId = "ed4b93a3-3501-4a8b-bf4b-d755629ec493"  // happyatkoder

//        client.removeParticipant(participantUserId, conversationId, object: CompletionCallback() {
//            override fun onCompletion(result: Result<Any>) {
//                if(result is Result.Success) {
//                    Log.d(TEST_OK, "removeParticipant ${result.data}")
//                } else {
//                    Log.d(TEST_NOK, "removeParticipant ${result}")
//                }
//            }
//        })

//        client.addParticipant(participantUserId, conversationId, object: CompletionCallback() {
//            override fun onCompletion(result: Result<Any>) {
//                if(result is Result.Success) {
//                    Log.d(TEST_OK, "addParticipant ${result.data}")
//                } else {
//                    Log.d(TEST_NOK, "addParticipant ${result}")
//                }
//            }
//        })

//        client.addModerator(participantUserId, conversationId, object: CompletionCallback() {
//            override fun onCompletion(result: Result<Any>) {
//                if(result is Result.Success) {
//                    Log.d(TEST_OK, "addModerator ${result.data}")
//                } else {
//                    Log.d(TEST_NOK, "addModerator ${result}")
//                }
//            }
//        })

//        client.removeModerator(participantUserId, conversationId, object: CompletionCallback() {
//            override fun onCompletion(result: Result<Any>) {
//                if(result is Result.Success) {
//                    Log.d(TEST_OK, "removeModerator ${result.data}")
//                } else {
//                    Log.d(TEST_NOK, "removeModerator ${result}")
//                }
//            }
//        })

        // messageId=91390eb3-35cb-4357-988b-f4e41428cd7f, conversationId=df46647a-7143-4456-ab90-38b8904bafaa
        val messageId = "91390eb3-35cb-4357-988b-f4e41428cd7f"
        val messageConversationId = "df46647a-7143-4456-ab90-38b8904bafaa"
//        client.setReaction(messageId, messageConversationId, "REACTION_CODE_LIKE", object: CompletionCallback() {
//            override fun onCompletion(result: Result<Any>) {
//                if(result is Result.Success) {
//                    Log.d(TEST_OK, "setReaction ${result.data}")
//                } else {
//                    Log.d(TEST_NOK, "setReaction ${result}")
//                }
//            }
//        })

//        client.setReaction(messageId, messageConversationId, "REACTION_CODE_DISLIKE", object: CompletionCallback() {
//            override fun onCompletion(result: Result<Any>) {
//                if(result is Result.Success) {
//                    Log.d(TEST_OK, "setReaction ${result.data}")
//                } else {
//                    Log.d(TEST_NOK, "setReaction ${result}")
//                }
//            }
//        })

//        client.reportMessage(messageId, messageConversationId, object: CompletionCallback() {
//            override fun onCompletion(result: Result<Any>) {
//                if(result is Result.Success) {
//                    Log.d(TEST_OK, "reportMessage ${result.data}")
//                } else {
//                    Log.d(TEST_NOK, "reportMessage ${result}")
//                }
//            }
//        })

        val deleteMessageId = "153dd029-c19a-456d-a303-16868126b387"
//        client.deleteMessage(deleteMessageId, messageConversationId, object: CompletionCallback() {
//            override fun onCompletion(result: Result<Any>) {
//                if(result is Result.Success) {
//                    Log.d(TEST_OK, "deleteMessage ${result.data}")
//                } else {
//                    Log.d(TEST_NOK, "deleteMessage ${result}")
//                }
//            }
//        })

//        client.updateConversation(conversationId, "title_test", "description_test", object: CompletionCallback() {
//            override fun onCompletion(result: Result<Any>) {
//                if(result is Result.Success) {
//                    Log.d(TEST_OK, "updateConversation ${result.data}")
//                } else {
//                    Log.d(TEST_NOK, "updateConversation ${result}")
//                }
//            }
//        })

        // user:typing:start event
//        client.postControlEvent(userId, conversationId, Messenger.EventName.typingStart.value, object: CompletionCallback() {
//            override fun onCompletion(result: Result<Any>) {
//                if(result is Result.Success) {
//                    Log.d(TEST_OK, "postControlEvent ${Messenger.EventName.typingStart.value} ${result.data}")
//                } else {
//                    Log.d(TEST_NOK, "postControlEvent ${Messenger.EventName.typingStart.value} ${result}")
//                }
//            }
//        })

        // user:typing:stop event
//        client.postControlEvent(userId, conversationId, Messenger.EventName.typingStop.value, object: CompletionCallback() {
//            override fun onCompletion(result: Result<Any>) {
//                if(result is Result.Success) {
//                    Log.d(TEST_OK, "postControlEvent ${Messenger.EventName.typingStop.value} ${result.data}")
//                } else {
//                    Log.d(TEST_NOK, "postControlEvent ${Messenger.EventName.typingStop.value} ${result}")
//                }
//            }
//        })

//        val sender = User(tenantId = prefs?.tenantId!!, userId = prefs?.externalUserId!!, displayName = prefs?.currentUser?.profile?.displayName!!, profileImageUrl = prefs?.currentUser?.profile?.profileImageUrl!!)
//        val message = Message(conversationId = conversationId, body = "createMessage test", sender = sender, metadata = MessageMetadata(localReferenceId = UUID.randomUUID().toString()), mentions = mutableListOf<Mention>())
//        client.createMessage(message, object: CompletionCallback() {
//            override fun onCompletion(result: Result<Any>) {
//                if(result is Result.Success) {
//                    Log.d(TEST_OK, "createMessage ${result.data}")
//                } else {
//                    Log.d(TEST_NOK, "createMessage ${result}")
//                }
//            }
//        })

//        client.createConversation(participantUserId, object: CompletionCallback() {
//            override fun onCompletion(result: Result<Any>) {
//                if(result is Result.Success) {
//                    Log.d(TEST_OK, "createConversation ${result.data}")
//                } else {
//                    Log.d(TEST_NOK, "createConversation ${result}")
//                }
//            }
//        })

//        val closeConversationId = "7b4eab10-4a7a-4d39-bac4-377f33840047"
//        client.closeConversation(closeConversationId, object: CompletionCallback() {
//            override fun onCompletion(result: Result<Any>) {
//                if(result is Result.Success) {
//                    Log.d(TEST_OK, "closeConversation ${result.data}")
//                } else {
//                    Log.d(TEST_NOK, "closeConversation ${result}")
//                }
//            }
//        })
    }
}
