package com.koder.ellenlibrary

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.fragment.app.Fragment
import com.koder.ellen.Messenger
import com.koder.ellen.model.Conversation
import com.koder.ellen.model.User
import com.koder.ellen.screen.ConversationScreen
import com.koder.ellen.screen.MessageInfoScreen
import com.koder.ellen.screen.MessageScreen
import com.koder.ellen.screen.UserSearchScreen

class MainActivity : AppCompatActivity() {
    companion object {
        const val TAG = "MainActivity"
        const val TEST_OK = "TEST_OK"
        const val TEST_NOK = "TEST_NOK"
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
//        return super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.conversation_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_message -> {
                // Show user search fragment
                Log.d(TAG, "Show user search screen")
                val userSearchScreen = UserSearchScreen()
//                getSupportFragmentManager().beginTransaction().replace(R.id.screenFrame, userSearchScreen, resources.getString(R.string.search)).addToBackStack(resources.getString(R.string.search)).commit()
                getSupportFragmentManager().beginTransaction().replace(R.id.screenFrame, userSearchScreen, resources.getString(R.string.search)).commit()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
//        super.onBackPressed()
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack()
        } else {
            super.onBackPressed();
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Customizable UI options
        Messenger.screenBackgroundColor = "#00CCCC"
        Messenger.screenCornerRadius = intArrayOf(20, 20, 0, 0)

        setContentView(R.layout.activity_main)
        getSupportActionBar()?.setElevation(0f)

        if (savedInstanceState == null) {
            val conversationScreen = ConversationScreen()
            getSupportFragmentManager().beginTransaction().replace(
                R.id.screenFrame,
                conversationScreen,
                resources.getString(R.string.conversations)
            ).commit()

//            val parentFragment = ParentFragment()
//            getSupportFragmentManager().beginTransaction().replace(
//                R.id.screenFrame,
//                parentFragment
//            ).commit()
        }

        // Conversation Screen click listener
        ConversationScreen.setItemClickListener(object: ConversationScreen.OnItemClickListener() {
            override fun OnItemClickListener(conversation: Conversation, position: Int) {
                Log.d(TAG, "OnItemClickListener")
                Log.d(TAG, "Conversation ${conversation}")
                Log.d(TAG, "Position ${position}")

                // Show Message Screen
//                val bundle = Bundle()
//                val messageScreen = MessageScreen()
//                bundle.putString("CONVERSATION_ID", conversation.conversationId)
//                messageScreen.setArguments(bundle)
//                getSupportFragmentManager().beginTransaction().replace(R.id.screenFrame, messageScreen, resources.getString(R.string.message)).addToBackStack(resources.getString(R.string.message)).commit()

                // Show Message Info Screen
                val bundle = Bundle()
                val messageInfoScreen = MessageInfoScreen()
                bundle.putString("CONVERSATION_ID", conversation.conversationId)
                messageInfoScreen.setArguments(bundle)
                getSupportFragmentManager().beginTransaction().replace(R.id.screenFrame, messageInfoScreen, resources.getString(R.string.info)).addToBackStack(resources.getString(R.string.message)).commit()
            }
        })

        // Customize UI, background color and rounded corners
//        val conversationScreen = supportFragmentManager.findFragmentById(R.id.conversation_screen) as ConversationScreen
//        conversationScreen.setBackgroundColor("#00CCCC")
//        conversationScreen.setListCornerRadius(20, 20, 0, 0)

        // Message Screen
//        val bundle = Bundle()
//        val messageScreen = MessageScreen.newInstance()
//        bundle.putString("CONVERSATION_ID", "63faa400-6a83-44b2-9664-6f98d133203e")
////        bundle.putString("BACKGROUND_COLOR", "#00CCCC")
////        bundle.putIntArray("CORNER_RADIUS", intArrayOf(20, 20, 0, 0)) // top left, top right, bottom right, top left
//        messageScreen.setArguments(bundle)
//        getSupportFragmentManager().beginTransaction().replace(R.id.screenFrame, messageScreen, resources.getString(R.string.message)).commit()

        // Customize UI, background color and rounded corners
//        val messageFrag = supportFragmentManager.findFragmentByTag(resources.getString(R.string.message)) as MessageScreen?
//        messageScreen.setBackgroundColor("#00CCCC")
//        messageFrag?.setListCornerRadius(20, 20, 0, 0)

        // Fragment notifies Activity on layout complete
//        messageScreen.addLayoutCompleteListener(object: MessageScreen.OnLayoutCompleteListener() {
//            override fun onComplete() {
//                Log.d(TAG, "messageFrag ${messageScreen}")
//                messageScreen.setBackgroundColor("#00CCCC")
//                messageScreen.setListCornerRadius(20,20,0,0)
//            }
//        })

        // User search screen
        UserSearchScreen.setItemClickListener(object: UserSearchScreen.OnItemClickListener() {
            override fun OnItemClickListener(user: User, position: Int) {
                Log.d(TAG, "OnItemClickListener")
                Log.d(TAG, "User ${user}")
                Log.d(TAG, "Position ${position}")
                val bundle = Bundle()
                val messageScreen = MessageScreen()
                bundle.putString("ADD_USER_ID", user.userId)
                messageScreen.setArguments(bundle)
                getSupportFragmentManager().beginTransaction().replace(R.id.screenFrame, messageScreen, resources.getString(R.string.message)).addToBackStack(resources.getString(R.string.message)).commit()
            }
        })

//        val userToken = "eyJhbGciOiJIUzI1NiIsImtpZCI6IjE3YzRhYmQ4YTE3MjQ0OTdiZmViMjBiMWM0ZDhmYjU0IiwidHlwIjoiSldUIn0.eyJ0ZW5hbnRfaWQiOiJFRkI5NEM3RS03MUU5LTQwNkItOTA1OS02MUFDMDUyMjdGMUIiLCJ1c2VyX2lkIjoiNEVFRDg2Q0UtNDZCNi00NjNGLUJBMjgtQzgzN0IzNDVBRUIzIiwidXNlcl9uYW1lIjoiamVmZmF0a29kZXIiLCJwcm9maWxlX2ltYWdlIjoiaHR0cHM6Ly9maXJlYmFzZXN0b3JhZ2UuZ29vZ2xlYXBpcy5jb20vdjAvYi9lbGxlbi1maXJlYmFzZS1leGFtcGxlLmFwcHNwb3QuY29tL28vQXZhdGFycyUyRnVzZXItMjEucG5nP2FsdD1tZWRpYSZ0b2tlbj1lZjhhYmI1MC0wNjJkLTQ1ZDItOTcwYS1mNDIxNjRmYzA0OWYiLCJleHAiOjE1ODY0MDkyNTgsImlzcyI6Imh0dHBzOi8vZWxsZW4ua29kZXIuY29tL2FwaS9tYW5hZ2VtZW50IiwiYXVkIjoiaHR0cHM6Ly9lbGxlbi5rb2Rlci5jb20vYXBpL21lc3NhZ2luZyJ9.bYBdNZ6BDtpX4TUjDZZ_vOUP8Of87PHnd1Sb9EHtFwA"

//        val userId = "4eed86ce-46b6-463f-ba28-c837b345aeb3"
        // Message options
//        Messenger.senderMessageRadius = 8
//        Messenger.senderBackgroundColor = "#CC0000"
//        Messenger.selfMessageRadius = 0
//        Messenger.selfBackgroundColor = "#222222"
//        Messenger.set(userToken, applicationContext, object: CompletionCallback() {
//            override fun onCompletion(result: Result<Any>) {
//                if(result is Result.Success) {
//                    Log.d(TAG, "Messenger successfully set")
////                    val intent = Intent(this@MainActivity, MessengerActivity::class.java)
////                    startActivity(intent)
////                    setResult(Activity.RESULT_OK)
////                    finish()
//                }
//            }
//        })

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
