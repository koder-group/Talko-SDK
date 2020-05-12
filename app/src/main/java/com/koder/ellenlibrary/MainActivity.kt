package com.koder.ellenlibrary

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.koder.ellen.Client
import com.koder.ellen.CompletionCallback
import com.koder.ellen.Messenger
import com.koder.ellen.Messenger.Companion.getUserId
import com.koder.ellen.UnreadCallback
import com.koder.ellen.data.Result
import com.koder.ellen.model.Conversation
import com.koder.ellen.model.User
import com.koder.ellen.screen.*

class MainActivity : AppCompatActivity() {
    companion object {
        const val TAG = "MainActivity"
    }

    private lateinit var mMenu: Menu

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Options
        Messenger.screenBackgroundColor = "#5d4298"
        Messenger.screenCornerRadius = intArrayOf(20, 20, 0, 0)
        Messenger.conversationSwipeToDelete = true
        Messenger.conversationLongClickToDelete = false

        setContentView(R.layout.activity_main)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.white))
        setSupportActionBar(toolbar)
        supportActionBar?.elevation = 0f

        if (savedInstanceState == null) {
            // Conversation Screen
            // Show the list of conversations

            // To filter by user IDs
//            val bundle = Bundle()
//            val userIds = arrayListOf("ed4b93a3-3501-4a8b-bf4b-d755629ec493")
//            bundle.putStringArrayList("userIds", userIds)

            val conversationScreen = ConversationScreen()
//            conversationScreen.arguments = bundle
            getSupportFragmentManager().beginTransaction().replace(
                R.id.frame_layout,
                conversationScreen,
                resources.getString(R.string.conversations)
            ).commit()
        }


        // Conversation Screen item click listener
        ConversationScreen.setItemClickListener(object: ConversationScreen.OnItemClickListener() {
            override fun OnItemClickListener(conversation: Conversation, position: Int) {
                // Show the conversation
                val bundle = Bundle()
                val messageScreen = MessageScreen()
                bundle.putString("CONVERSATION_ID", conversation.conversationId)

                // Auto populate message
//                val message = "Did you know you can easily start accepting payments on BRAVO?\n\nhttps://www.trybravo.com/help"
//                bundle.putString(MessageScreen.AUTO_POPULATE_MSG, message)
//                bundle.putBoolean(MessageScreen.SEND_AUTO_POPULATE_MSG, true)

                messageScreen.setArguments(bundle)
                getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout, messageScreen, resources.getString(R.string.message)).addToBackStack(resources.getString(R.string.message)).commit()
            }
        })


        // User search screen item click listener
        UserSearchScreen.setItemClickListener(object: UserSearchScreen.OnItemClickListener() {
            override fun OnItemClickListener(user: User, position: Int) {
                findOrCreateConversation(user)
                hideKeyboard(this@MainActivity)
            }
        })


        // Message Info, Add Participant item click listener
        MessageInfoScreen.setItemClickListener(object: MessageInfoScreen.OnItemClickListener() {
            override fun onClickAddParticipant(conversationId: String) {
                // Show the Add Participant Screen for the current conversation
                Log.d(TAG, "onClickAddParticipant ${conversationId}")
                val bundle = Bundle()
                val addParticipantScreen = AddParticipantScreen()
                bundle.putString("CONVERSATION_ID", conversationId)
                addParticipantScreen.setArguments(bundle)
                getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout, addParticipantScreen, resources.getString(R.string.add_participant)).addToBackStack(resources.getString(R.string.add_participant)).commit()
            }
        })


        // Add Participant Screen item click listener
        AddParticipantScreen.setItemClickListener(object: AddParticipantScreen.OnItemClickListener() {
            override fun OnItemClickListener(
                userId: String,
                conversationId: String,
                position: Int
            ) {
                Log.d(TAG, "Add Participant")
                Log.d(TAG, "User Id ${userId}")
                Log.d(TAG, "Conversation Id ${conversationId}")

                getSupportFragmentManager().popBackStack()
                hideKeyboard(this@MainActivity)

                // Add the selected participant to the conversation
                val client = Client()
                client.addParticipant(userId, conversationId, object: CompletionCallback() {
                    override fun onCompletion(result: Result<Any>) {
                        if(result is Result.Success) {
                            Log.d(TAG, "addParticipant ${result.data}") // Boolean
                        }
                    }
                })
            }
        })


        // Unread count
        // The number of unread conversations
        Log.d(TAG, "Unread count ${Messenger.getUnreadCount()}")

        Messenger.setUnreadListener(object: UnreadCallback() {
            override fun onNewUnread(unreadCount: Int) {
                Log.d(TAG, "unreadCount ${unreadCount}")
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
//        return super.onCreateOptionsMenu(menu)
        mMenu = menu!!
        menuInflater.inflate(R.menu.main_menu, menu)
        mMenu.setGroupVisible(R.id.conversation_menu, false)
        mMenu.setGroupVisible(R.id.search_menu, false)
        mMenu.setGroupVisible(R.id.message_menu, false)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        val conversationScreen = supportFragmentManager.findFragmentByTag(resources.getString(R.string.conversations))
        val userSearchScreen = supportFragmentManager.findFragmentByTag(resources.getString(R.string.search))
        val messageScreen = supportFragmentManager.findFragmentByTag(resources.getString(R.string.message))

        if (conversationScreen != null && conversationScreen.isVisible) {
            mMenu.setGroupVisible(R.id.conversation_menu, true)
            mMenu.setGroupVisible(R.id.search_menu, false)
            mMenu.setGroupVisible(R.id.message_menu, false)
        }
        if (userSearchScreen != null && userSearchScreen.isVisible) {
            mMenu.setGroupVisible(R.id.conversation_menu, false)
            mMenu.setGroupVisible(R.id.search_menu, true)
            mMenu.setGroupVisible(R.id.message_menu, false)
        }
        if (messageScreen != null && messageScreen.isVisible) {
            mMenu.setGroupVisible(R.id.conversation_menu, false)
            mMenu.setGroupVisible(R.id.search_menu, false)
            mMenu.setGroupVisible(R.id.message_menu, true)
        }

        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_message -> {
                // Show User Search Screen
                val userSearchScreen = UserSearchScreen()
                getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout, userSearchScreen, resources.getString(R.string.search)).addToBackStack(resources.getString(R.string.search)).commit()
                true
            }
            R.id.action_search -> {
                // Get selected user
                val userSearchScreen = supportFragmentManager.findFragmentByTag(resources.getString(R.string.search)) as UserSearchScreen
                val user = userSearchScreen.getSelectedUser()
                user?.let {
                    findOrCreateConversation(user)
                    hideKeyboard(this@MainActivity)
                }
                true
            }
            R.id.action_info -> {
                // Show Message Info Screen
                val bundle = Bundle()
                val messageInfoScreen = MessageInfoScreen()
                bundle.putString("CONVERSATION_ID", Messenger.currentConversationId)
                messageInfoScreen.setArguments(bundle)
                getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout, messageInfoScreen, resources.getString(R.string.info)).addToBackStack(resources.getString(R.string.info)).commit()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
//        super.onBackPressed()
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack()
            hideKeyboard(this@MainActivity)
        } else {
            super.onBackPressed();
        }
    }

    private fun findOrCreateConversation(user: User) {
        // Find an existing conversation
        val conversation = Messenger.fetchConversation(getUserId(), user.userId)
        if(conversation == null) {
            // Create a new conversation
            val bundle = Bundle()
            val messageScreen = MessageScreen()
            bundle.putString("ADD_USER_ID", user.userId)
            messageScreen.setArguments(bundle)
            supportFragmentManager.popBackStack()
            getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout, messageScreen, resources.getString(R.string.message)).addToBackStack(resources.getString(R.string.message)).commit()
        } else {
            // Show the conversation with the selected user
            val bundle = Bundle()
            val messageScreen = MessageScreen.newInstance()
            Messenger.currentConversationId = conversation.conversationId
            bundle.putString("CONVERSATION_ID", conversation.conversationId)
            messageScreen.setArguments(bundle)
            supportFragmentManager.popBackStack()
            getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout, messageScreen, resources.getString(R.string.message)).addToBackStack(resources.getString(R.string.message)).commit()
        }
    }

    private fun hideKeyboard(activity: Activity) {
        val imm: InputMethodManager =
            activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        //Find the currently focused view, so we can grab the correct window token from it.
        var view: View? = activity.currentFocus
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = View(activity)
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0)
    }
}
