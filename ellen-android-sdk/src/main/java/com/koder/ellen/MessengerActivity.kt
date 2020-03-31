package com.koder.ellen

import android.app.Activity
import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.Gravity
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.navigation.NavigationView
import com.google.gson.Gson
import com.koder.ellen.Messenger.Companion.prefs
import com.koder.ellen.core.*
import com.koder.ellen.data.ConversationDataSource
import com.koder.ellen.data.ConversationRepository
import com.koder.ellen.model.Conversation
import com.koder.ellen.model.Message
import com.koder.ellen.model.Participant
import com.koder.ellen.model.User
import com.koder.ellen.ui.BaseViewModelFactory
import com.koder.ellen.ui.conversation.ConversationFragment
import com.koder.ellen.ui.main.AvatarFragment
import com.koder.ellen.ui.main.MainViewModel
import com.koder.ellen.ui.message.MessageFragment
import com.koder.ellen.ui.message.MessageInfoFragment
import com.koder.ellen.ui.search.FindUserFragment
import com.koder.ellen.ui.search.SearchFragment
import com.pubnub.api.PNConfiguration
import com.pubnub.api.PubNub
import com.pubnub.api.callbacks.SubscribeCallback
import com.pubnub.api.models.consumer.PNStatus
import com.pubnub.api.models.consumer.pubsub.PNMessageResult
import com.pubnub.api.models.consumer.pubsub.PNPresenceEventResult
import com.pubnub.api.models.consumer.pubsub.PNSignalResult
import com.pubnub.api.models.consumer.pubsub.message_actions.PNMessageActionResult
import com.pubnub.api.models.consumer.pubsub.objects.PNMembershipResult
import com.pubnub.api.models.consumer.pubsub.objects.PNSpaceResult
import com.pubnub.api.models.consumer.pubsub.objects.PNUserResult
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class MessengerActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "MainActivity"
    }
    val viewModel: MainViewModel by lazy {
        ViewModelProvider(this, BaseViewModelFactory {
            MainViewModel(
                ConversationRepository(ConversationDataSource())
            )
        }).get(MainViewModel::class.java)
    }
//    private lateinit var mDrawer: DrawerLayout
//    private lateinit var mDrawerToggle: ActionBarDrawerToggle
//    private lateinit var mNavView: NavigationView
    private lateinit var displayName: TextView
    private lateinit var profileImageView: ImageView
    private lateinit var userLink: String
    private var pubNub: PubNub? = null
    private lateinit var conversationFragment: ConversationFragment
    private lateinit var messageFragment: MessageFragment
    private lateinit var infoFragment: MessageInfoFragment
    private val conversations = mutableListOf<Conversation>()
    private var currentConversation: Conversation? = null
    private val currentStatusMessages = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
//        setSupportActionBar(findViewById(R.id.toolbar))
//        supportActionBar?.setDisplayHomeAsUpEnabled(true)
//        supportActionBar?.setDisplayShowHomeEnabled(true)
        if (savedInstanceState == null) {
            conversationFragment = ConversationFragment.newInstance()
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, conversationFragment, getResources().getString(R.string.conversations))
                .commitNow()
        }
//        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        viewModel.title.observe(this, Observer {
            supportActionBar?.title = it
        })
        // Observer, getCurrentUser and getClientConfig
        viewModel.config.observe(this, Observer {
            val frag = supportFragmentManager.findFragmentByTag(resources.getString(R.string.conversations)) as ConversationFragment
            frag?.let {
                frag.loadConversations()
            }
        })
        // Subscribe to list of Conversation channels
        viewModel.conversations.observe(this, Observer {
            Log.d(TAG, "viewModel conversations observe")
            // Subscribe to conversations once on-load
//            if(conversations.isEmpty() && !it.isEmpty()) {

            // Update list of conversations
            conversations.clear()
            conversations.addAll(it)

            if(pubNub == null) {
                // Subscribe to conversations
                Log.d(TAG, "Subscribe to convos ${it}")
                initPubNub()
//                pubNub?.subscribe()?.channels(getChannelsList())?.execute()   // Subscribe in initPubNub()
            }
        })
        viewModel.subscribeChannelList.observe(this, Observer {
            pubNub?.subscribe()?.channels(it)?.execute()
        })
        viewModel.currentConversation.observe(this, Observer {
            currentConversation = it
        })

//        viewModel.firebaseUser.observe(this, Observer {
//            // Update display name and profile image
//            displayName.text = prefs.displayName()
//            Picasso.get().load(prefs.profileImageUrl()).into(profileImageView)
//        })

        // Navigation DrawerLayout
//        mDrawer = findViewById(R.id.drawer_layout) as DrawerLayout
//        mDrawerToggle = ActionBarDrawerToggle(
//            this, mDrawer, R.string.drawer_open, R.string.drawer_close
//        )
//        // Setup toggle to display hamburger icon with nice animation
//        mDrawerToggle.setDrawerIndicatorEnabled(true)
//        mDrawerToggle.syncState()
//        // Tie DrawerLayout events to the ActionBarToggle
//        mDrawer.addDrawerListener(mDrawerToggle)
//
//        // Set DrawerIndicator true for Fragment tag "Conversations"
//        supportFragmentManager.addOnBackStackChangedListener(object: FragmentManager.OnBackStackChangedListener {
//           override fun onBackStackChanged() {
////               Log.d(TAG, "Backstack changed")
//                val currentBackStackFragment = supportFragmentManager.fragments.last()
////               Log.d(TAG, "${currentBackStackFragment.tag}")
//               when(currentBackStackFragment.tag) {
//                   resources.getString(R.string.conversations) -> {
//                       mDrawerToggle.setDrawerIndicatorEnabled(true)
//                       true
//                   }
//                   else -> {
//                       mDrawerToggle.setDrawerIndicatorEnabled(false)
//                   }
//               }
//            }
//        })
//
//        // Navigation view
//        mNavView = findViewById(R.id.nav_view)
//        mNavView.setNavigationItemSelectedListener { menuItem ->
//            when (menuItem.itemId) {
//                R.id.nav_share -> {
//                    shareUser()
////                    mDrawer.closeDrawers()
//                }
//                R.id.nav_logout -> {
//                    logoutUser()
//                }
//            }
//            true
//        }

        // Set Navigation Drawer layout
//        initFirebaseUser()
//        setDrawerContent()
    }

    override fun onDestroy() {
        super.onDestroy()
        if(pubNub != null) pubNub!!.unsubscribeAll()
    }

    // Share user link
    private fun shareUser() {
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, "Chat with me on Ellen: ${userLink}\n\n(Copy to clipboard and create a new message in the app)")
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, null)
        startActivity(shareIntent)
    }

    // Logout user and return to Login Activity
    private fun logoutUser() {
//        prefs.resetUser() // TODO SDK
//        auth.signOut()
//
//        val loginIntent = Intent(this, LoginActivity::class.java)
//        startActivity(loginIntent)
//        setResult(Activity.RESULT_OK)
//        finish()  // TODO SDK
    }

    private fun setDrawerContent() {
//        // Inflate the header view at runtime
//        val headerLayout = mNavView.inflateHeaderView(R.layout.nav_header)
//
//        userLink = "https://ellen.koder.com/u/${prefs?.currentUser?.externalIdentifier}"
//        displayName = headerLayout.findViewById(R.id.display_name)
////        val shareBtn: MaterialButton = findViewById(R.id.btn_share)
////        val logoutBtn: MaterialButton = findViewById(R.id.btn_logout)
//        profileImageView = headerLayout.findViewById(R.id.profile_image)
////
//        if(!prefs?.currentUser?.profile?.displayName.isNullOrBlank()) {
//            displayName.setText(prefs?.currentUser?.profile?.displayName)
//
//            // Edit display name
//            val displayNameLayout = headerLayout.findViewById<ConstraintLayout>(R.id.display_name_layout)
//            displayNameLayout.setOnClickListener {
//                val input = EditText(this)
//                val container = FrameLayout(this)
//                val params = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
//                input.setText(prefs?.currentUser?.profile?.displayName)
//                input.setHint("Enter a display name")
//                input.setLayoutParams(params)
//                container.setPadding(20.px, 0, 20.px, 0)
//                container.addView(input)
//
//                // Note: A second constructor exists to pass in a theme res ID
//                MaterialAlertDialogBuilder(this)
//                    // Add customization options here
//                    .setView(container)
//                    .setTitle("Display name")
//                    // Confirming action
//                    .setPositiveButton("Update") { dialog, which ->
//                        // Do something for button click
//                        if(input.text.length > 0) {
//                            Log.d(TAG, "Set display name ${input.text}")
//                            editUser(displayName = input.text.toString())
//                        }
//                    }
//                    // Dismissive action
//                    .setNegativeButton("Cancel") { dialog, which ->
//                        // Do something for button click
//                    }
//                    .show()
//            }
//
//            // Edit profile image
//            val profileImageLayout = headerLayout.findViewById<ConstraintLayout>(R.id.profile_image_layout)
//            profileImageLayout.setOnClickListener {
//                Log.d(TAG, "Show avatar fragment")
//                showAvatarFragment()
//                mDrawer.closeDrawers()
//            }
//        }
////        shareBtn.setOnClickListener(this)
////        logoutBtn.setOnClickListener(this)
////
////        // Load user profile image
//        if(!prefs?.currentUser?.profile?.profileImageUrl.isNullOrBlank()) {
//            Picasso.get().load(prefs.profileImageUrl()).into(profileImageView)
//        }
////
//        // Generate QR code
//        try {
//            val barcodeEncoder = BarcodeEncoder()
//            val hintMap = HashMap<EncodeHintType, Any>()
//            hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.Q)
//            hintMap.put(EncodeHintType.MARGIN, 0)
//            val bitmap = barcodeEncoder.encodeBitmap(userLink, BarcodeFormat.QR_CODE, 400, 400, hintMap)
//            val imageViewQrCode: ImageView = headerLayout.findViewById(R.id.qr_code)
//            imageViewQrCode.setImageBitmap(bitmap)
//        } catch (e: java.lang.Exception) {
//        }
    }

    private fun initPubNub() {
        var subscribeCallback: SubscribeCallback = object : SubscribeCallback() {
            var timer: CountDownTimer? = null

            override fun status(pubnub: PubNub, status: PNStatus) {
                runOnUiThread {
                    //                subscribeText.text = message.message.toString()
                    Log.d(TAG, "${status}")
                    Log.d(TAG, "${status.isError}")
                    Log.d(TAG, "${status.affectedChannels}")
                    Log.d(TAG, "${status.operation}")
//                    res


                    val affectedChannels = mutableListOf<String>()
                    if(status.isError && status.operation.toString().equals("PNSubscribeOperation")) {
                        // Retry subscribe
                        status.affectedChannels?.let {
                            for(channel in status.affectedChannels!!) {
                                timer?.cancel()
                                timer = object : CountDownTimer(1000, 1500) {
                                    override fun onTick(millisUntilFinished: Long) {}
                                    override fun onFinish() {
                                        Log.d(TAG, "resubscribe ${channel}")
                                        pubNub?.subscribe()?.channels(mutableListOf(channel))?.execute()
                                    }
                                }.start()
                            }
                        }
                    }
                }
            }
            override fun message(pubnub: PubNub, message: PNMessageResult) {
                runOnUiThread {
                    //  subscribeText.text = message.message.toString()
//                    Log.d(TAG, "${message}")
                    val eventName = message.message.asJsonObject.get("eventName").toString().replace("\"","")
                    Log.d(TAG, "${eventName}")
                    Log.d(TAG, "${message.message}")

                    val gson = Gson()
                    if (message.message.asJsonObject.get("eventName").asString.contains("message:published")) {
                        // New Conversation Message
                        Log.d(TAG, "${message.message.asJsonObject.get("model")}")
//                        Log.d(TAG, "currentConversation ${currentConversation?.conversationId}")

                        val conversationMessage = gson.fromJson(message.message.asJsonObject.get("model"), Message::class.java)
//                        Log.d(TAG, "${conversationMessage}")
//                        addMessageToConversations(conversationMessage)  // TODO

                        // Time created
                        Log.d(TAG, "timeCreated ${conversationMessage.timeCreated}")
                        val timeCreated = convertDateToLong(conversationMessage.timeCreated.toString())
                        Log.d(TAG, "timeCreated ${timeCreated}")

//                        Log.d(TAG, "conversationId ${conversationMessage.conversationId}")
//                        Log.d(TAG, "${currentConversation?.conversationId.equals(conversationMessage.conversationId)}")

                        // If current fragment is MessageFragment
                        val frag: MessageFragment? = supportFragmentManager.findFragmentByTag(resources.getString(R.string.message)) as MessageFragment?
                        Log.d(TAG, "message frag ${frag}")
                        frag?.let {
                            if(currentConversation?.conversationId.equals(conversationMessage.conversationId)) {
//                                Log.d(TAG, "publish message to ${currentConversation}")
                                // Update if the published message is for the current conversation
                                it.addMessage(conversationMessage)

//                                val timeCreated = convertDateToLong(conversationMessage.timeCreated.toString())
//                                Log.d(TAG, "timeCreated ${convertDateToLong(conversationMessage.timeCreated.toString())}")
                                prefs?.setConversationLastRead(conversationMessage.conversationId, timeCreated)

                            }
                        }

                        // Conversation Fragment
                        val convoFrag: ConversationFragment? = supportFragmentManager.findFragmentByTag(resources.getString(R.string.conversations)) as ConversationFragment?
                        convoFrag?.let {

                            it.addMessageToConversations(conversationMessage)
                        }
                    }
                    else if (message.message.asJsonObject.get("eventName").asString.contains("conversation:created")) {
                        // New Conversation
                        val conversation = gson.fromJson(message.message.asJsonObject.get("model"), Conversation::class.java)
                        Log.d(TAG, "${conversation}")
//                        addToConversations(conversation)

                        // Add conversation if DNE
                        val found = conversations.find { it.conversationId.equals(conversation.conversationId) }
                        Log.d(TAG, "found ${found}")
                        if(found == null) {
                            val convoFrag = supportFragmentManager.findFragmentByTag(resources.getString(R.string.conversations)) as ConversationFragment
                            convoFrag?.let {
                                convoFrag.addConversation(conversation)
                            }
                        }
                    }
                    else if (message.message.asJsonObject.get("eventName").asString.contains("conversation:closed")) {
                        // Conversation closed
                        val conversation = gson.fromJson(message.message.asJsonObject.get("context"), Conversation::class.java)
//                        Log.d(TAG, "Remove ${conversation}")
//                        removeFromConversations(conversation.conversationId)  // TODO
//                        conversationFragment.removeConversation(conversation)
                        val convoFrag = supportFragmentManager.findFragmentByTag(resources.getString(R.string.conversations)) as ConversationFragment
                        convoFrag?.let {
                            convoFrag.removeConversation(conversation)
                        }

                        // Close Message fragment
                        if(currentConversation?.conversationId.equals(conversation.conversationId)) {
                            val infoFrag: MessageInfoFragment? = supportFragmentManager.findFragmentByTag(resources.getString(R.string.info)) as MessageInfoFragment?
                            val messageFrag: MessageFragment? = supportFragmentManager.findFragmentByTag(resources.getString(R.string.message)) as MessageFragment?

                            // Close MessageInfo fragment
                            infoFrag?.let {
                                supportFragmentManager.popBackStack()
                            }
                            messageFrag?.let {
                                supportFragmentManager.popBackStack()
                            }
                        }

                    } else if (message.message.asJsonObject.get("eventName").asString.contains("conversation:participant:added")) {
                        Log.d(TAG, "Participant added")
                        Log.d(TAG, "${message.message.asJsonObject.get("context").asJsonObject.get("conversationId").asString}")

                        val tenantId = message.message.asJsonObject.get("tenantId").asString  // TODO
                        val conversationId = message.message.asJsonObject.get("context").asJsonObject.get("conversationId").asString
                        val userId = message.message.asJsonObject.get("userId").asString

                        val initiatingUser = gson.fromJson(message.message.asJsonObject.get("context").asJsonObject.get("initiatingUser"), User::class.java)

                        // Get Conversation
//                        conversationViewModel.loadConversations() // TODO
                        val convoFrag = supportFragmentManager.findFragmentByTag(resources.getString(R.string.conversations)) as ConversationFragment?
                        convoFrag?.let {
                            it.loadConversations()
                        }

                        // Subscribe to channel, if this user is the added participant
                        if(prefs?.externalUserId.equals(userId)) pubNub?.subscribe()?.channels(listOf("${tenantId}-${conversationId}".toUpperCase()))?.execute()   // TODO

                        // Update MessageFragment title when participant is added
                        val messageFrag: MessageFragment? = supportFragmentManager.findFragmentByTag(resources.getString(R.string.message)) as MessageFragment?
                        val infoFrag = supportFragmentManager.findFragmentByTag(resources.getString(R.string.info)) as MessageInfoFragment?
                        if(currentConversation?.conversationId.equals(conversationId)) {
                            // Update MessageFragment
                            Log.d(TAG, "Update message fragment")
                            messageFrag?.let {
                                if(it.isVisible) it.updateTitle()
                                it.showStatusMessage("${initiatingUser.displayName} added a new user to the conversation.")
                                it.updateConversation(conversationId)
                            }
                            // Update MessageInfoFragment?
                            infoFrag?.let {
                                it.addParticipant(userId)
                                currentStatusMessages.add("${initiatingUser.displayName} added a new user to the conversation.")
                            }
                        }

                    } else if (message.message.asJsonObject.get("eventName").asString.contains("conversation:participant:removed")) {
                        Log.d(TAG, "Participant removed")
                        Log.d(TAG, "${message.message.asJsonObject.get("context").asJsonObject.get("conversationId").asString}")
                        val conversationId = message.message.asJsonObject.get("context").asJsonObject.get("conversationId").asString
                        val removedUser = gson.fromJson(message.message.asJsonObject.get("model"), User::class.java)
                        val initiatingUser = gson.fromJson(message.message.asJsonObject.get("context").asJsonObject.get("initiatingUser"), User::class.java)

                        // Update MessageFragment title when participant is removed
                        if(currentConversation?.conversationId.equals(conversationId)) {
                            // Current conversation
                            Log.d(TAG, "currentConversation ${currentConversation}")
                            val infoFrag: MessageInfoFragment? = supportFragmentManager.findFragmentByTag(resources.getString(R.string.info)) as MessageInfoFragment?
                            val messageFrag: MessageFragment? = supportFragmentManager.findFragmentByTag(resources.getString(R.string.message)) as MessageFragment?

                            val foundUser = currentConversation?.participants?.find { p -> p.user.userId.equals(removedUser.userId) }
                            var statusMessage = ""
                            Log.d(TAG, "foundUser ${foundUser}")
                            foundUser?.let { u ->
//                                it.showStatusMessage("${initiatingUser.displayName} removed ${u.user.displayName} from the conversation.")
////                                it.updateConversation(conversationId)
////                                it.removeParticipant(conversationId, initiatingUser.displayName, removedUser.userId)
//                                it.updateConversation(conversationId)
                                statusMessage = "${initiatingUser.displayName} removed ${u.user.displayName} from the conversation."
                            }

                            messageFrag?.let {
                                Log.d(TAG, "initiatingUser ${initiatingUser.displayName}")
//                                Log.d(TAG, "removedUser ${removedUserName}")
                                val foundUser = currentConversation?.participants?.find { p -> p.user.userId.equals(removedUser.userId) }
                                foundUser?.let { u ->
                                    it.showStatusMessage("${initiatingUser.displayName} removed ${u.user.displayName} from the conversation.")
//                                it.updateConversation(conversationId)
//                                it.removeParticipant(conversationId, initiatingUser.displayName, removedUser.userId)
                                    it.updateConversation(conversationId)
                                }
                            }
                            val found = currentConversation?.participants?.find { p -> p.user.userId.equals(removedUser.userId) }
//                            Log.d(TAG, "currentConversation ${currentConversation}")
                            found?.let {
                                // Remove participant from current conversation
                                currentConversation?.participants?.remove(found)
                            }

                            infoFrag?.let {
                                it.removeParticipant(removedUser.userId)
                                if(statusMessage.isNotBlank()) currentStatusMessages.add(statusMessage)
                            }
                        }

                        // If current user
                        if(removedUser.userId.equals(prefs?.externalUserId)) {
                            // Remove Conversation
//                        removeFromConversations(conversationId)   // TODO

                            val convoFrag: ConversationFragment? = supportFragmentManager.findFragmentByTag(resources.getString(R.string.conversations)) as ConversationFragment?
                            convoFrag?.let {
                                it.removeFromConversations(conversationId)
                            }

                            // Close Message fragment
                            if(currentConversation?.conversationId.equals(conversationId)) {
                                val infoFrag: MessageInfoFragment? = supportFragmentManager.findFragmentByTag(resources.getString(R.string.info)) as MessageInfoFragment?
                                val messageFrag: MessageFragment? = supportFragmentManager.findFragmentByTag(resources.getString(R.string.message)) as MessageFragment?

                                // Close MessageInfo fragment
                                infoFrag?.let {
                                    supportFragmentManager.popBackStack()
                                }
                                messageFrag?.let {
                                    supportFragmentManager.popBackStack()
                                }
                            }
                        }

                        // Unsubscribe from channel, if this user is the removed participant
                        if(prefs?.externalUserId.equals(removedUser.userId)) pubNub?.unsubscribe()?.channels(listOf("${prefs?.tenantId}-${conversationId}".toUpperCase()))?.execute() // TODO

                    } else if (message.message.asJsonObject.get("eventName").toString().replace("\"","")
                        == "message:userReaction") {
                        // Add reaction to message
                        Log.d(TAG, "Message user reaction")
                        val conversationMessage = gson.fromJson(message.message.asJsonObject.get("context"), Message::class.java)
//                        Log.d(TAG, "${conversationMessage}")
                        val reactionCode = message.message.asJsonObject.get("reactionCode")
//                        Log.d(TAG, "${reactionCode}")

                        // Update reaction directly, results in inaccurate counts
//                        updateReaction(conversationMessage, reactionCode.toString().replace("\"",""))

                        // Update reaction by getting updated message from platform API
//                        messageViewModel.updateMessage(conversationMessage)   // TODO
                        // If current fragment is MessageFragment
                        val frag = supportFragmentManager.findFragmentByTag(resources.getString(R.string.message)) as MessageFragment
                        frag?.let {
                            if(currentConversation?.conversationId.equals(conversationMessage.conversationId)) {
                                // Update if the published message is for the current conversation
                                it.updateMessage(conversationMessage)
                            }
                        }
                    } else if (message.message.asJsonObject.get("eventName").toString().replace("\"","")
                        == "message:deleted") {
                        val conversationMessage = gson.fromJson(message.message.asJsonObject.get("context"), Message::class.java)
//                        deleteMessageFromList(conversationMessage)    // TODO
                        val frag = supportFragmentManager.findFragmentByTag(resources.getString(R.string.message)) as MessageFragment?
                        frag?.let {
                            if(currentConversation?.conversationId.equals(conversationMessage.conversationId)) {
                                // Update if the published message is for the current conversation
                                it.deleteMessageFromList(conversationMessage)
                            }
                        }
                    } else if (message.message.asJsonObject.get("eventName").toString().replace("\"","")
                        == "conversation:modified") {
                        val conversationId = message.message.asJsonObject.get("context").asJsonObject.get("conversationId").asString.replace("\"","")
                        val title = message.message.asJsonObject.get("title")
                        val description = message.message.asJsonObject.get("description")
                        Log.d(TAG, "Conversation modified ${conversationId} ${title}")
                        val initiatingUserName = message.message.asJsonObject.get("context").asJsonObject.get("initiatingUser").asJsonObject.get("displayName")

                        // Update list
                        val convoFrag = supportFragmentManager.findFragmentByTag(resources.getString(R.string.conversations)) as ConversationFragment?
                        convoFrag?.let {
                            it.loadConversations()
                        }

                        if(currentConversation?.conversationId.equals(conversationId)) {
                            val infoFrag = supportFragmentManager.findFragmentByTag(resources.getString(R.string.info)) as MessageInfoFragment?
                            val messageFrag = supportFragmentManager.findFragmentByTag(resources.getString(R.string.message)) as MessageFragment?

                            // Update MessageInfoFragment
                            infoFrag?.let {
                                // Update if the published message is for the current conversation
                                if(!title.isJsonNull) {
                                    currentConversation?.title = title.asString
                                    infoFrag?.updateTitle(title.asString)

                                    currentStatusMessages.add("${initiatingUserName.asString} changed the conversation name to ${title.asString}")
                                }
                                if(!description.isJsonNull) {
                                    currentConversation?.description = description.asString
                                    infoFrag?.updateDescription(description.asString)
                                }
                            }

                            // Update MessageFragment
                            messageFrag?.let {
                                // Update if the published message is for the current conversation
                                if(!title.isJsonNull) {
                                    currentConversation?.title = title.asString
                                    messageFrag?.updateTitle(title.asString)
                                    it.showStatusMessage("${initiatingUserName.asString} changed the conversation name to ${title.asString}")
                                }
                            }
                        }
                    } else if (message.message.asJsonObject.get("eventName").toString().replace("\"","")
                        == "message:rejected") {
                        // Update message frag
                        val conversationMessage = gson.fromJson(message.message.asJsonObject.get("model"), Message::class.java)
                        Log.d(TAG, "message:rejected, cId ${conversationMessage.conversationId} lRI ${conversationMessage.metadata.localReferenceId}")
                        val errorMessage = message.message.asJsonObject.get("rejectionReason").asJsonObject.get("message").asString

                        if(currentConversation?.conversationId.equals(conversationMessage.conversationId) &&
                                conversationMessage.sender.userId.equals(prefs?.externalUserId)) {
                            // Current conversation and current user
                            // Update message body
                            val messageFrag = supportFragmentManager.findFragmentByTag(resources.getString(R.string.message)) as MessageFragment?
                            messageFrag?.let {
                                it.showMessageError(conversationMessage.metadata.localReferenceId, errorMessage)
                            }
                        }
                    } else if (message.message.asJsonObject.get("eventName").toString().replace("\"","")
                        == "conversation:participant:statusChange") {
                        // Update ConversationParticipant.state
                        Log.d(TAG, "participant:statusChange ${currentConversation}")
                        // model to ConversationParticipant
                        val participant = gson.fromJson(message.message.asJsonObject.get("model"), Participant::class.java)
                        val conversationId = message.message.asJsonObject.get("context").asJsonObject.get("conversationId").asString.replace("\"","")
                        // Update current conversation for Message/InfoFragments
                        currentConversation?.let { convo ->
                            if(convo.conversationId.equals(conversationId)) {
                                // Current conversation
                                // Update participant state
                                val found = convo.participants.find { part -> part.user.userId.equals(participant.user.userId) }
                                found?.let {part ->
                                    val index = convo.participants.indexOf(part)
                                    part.state = participant.state
                                    convo.participants.set(index, found)
                                    currentConversation = convo
                                }
                            }
                        }
                        // Update conversation in list
                        val convoFrag = supportFragmentManager.findFragmentByTag(resources.getString(R.string.conversations)) as ConversationFragment?
                        convoFrag?.let {
                            it.updateConversationParticipant(conversationId, participant)   // Used when user is un/silenced

                            // Remove from current users's conversations list if banned
                            if(participant.user.userId.equals(prefs?.externalUserId)) {
                                when(participant.state) {
                                    20 -> {
                                        // Participant banned, remove conversation from list
                                        it.removeFromConversations(conversationId)

                                        // Close MessageInfoFragment and MessageFragment
                                        val infoFrag: MessageInfoFragment? = supportFragmentManager.findFragmentByTag(resources.getString(R.string.info)) as MessageInfoFragment?
                                        val messageFrag: MessageFragment? = supportFragmentManager.findFragmentByTag(resources.getString(R.string.message)) as MessageFragment?

                                        // Close MessageInfo fragment
                                        infoFrag?.let {
                                            supportFragmentManager.popBackStack()
                                        }
                                        messageFrag?.let {
                                            supportFragmentManager.popBackStack()
                                        }
                                    }
                                    else -> it.loadConversations()  // Hack to quickly show conversation after unbanned
                                }
                            }

                        }
                    } else if(eventName.equals("controlEvent")) {
                        // {"eventName":"controlEvent","eventData":{"context":{"initiatingUser":"52312c01-c3d5-4ff3-8734-957f7f7377dd","conversationId":"d8be43ab-1caf-4468-aab9-b59598a5e7c6"},"eventName":"user:typing:start"}}

                        // message.message.asJsonObject.get("context").asJsonObject.get("conversationId").asString.replace("\"","")
                        val initiatingUser = message.message.asJsonObject.get("eventData").asJsonObject.get("context").asJsonObject.get("initiatingUser").asString.replace("\"","")
                        val conversationId = message.message.asJsonObject.get("eventData").asJsonObject.get("context").asJsonObject.get("conversationId").asString.replace("\"","")
                        val eventName = message.message.asJsonObject.get("eventData").asJsonObject.get("eventName").asString.replace("\"","")
//                        Log.d(TAG, "initiatingUser ${initiatingUser}")
//                        Log.d(TAG, "conversationId ${conversationId}")
//                        Log.d(TAG, "eventName ${eventName}")

                        // Send to current MessageFragment if current conversation and current user isn't the initiating user (self)
                        if(currentConversation?.conversationId?.toUpperCase().equals(conversationId.toUpperCase()) &&
                                !prefs?.externalUserId?.toUpperCase().equals(initiatingUser.toUpperCase())) {
                            val messageFrag: MessageFragment? = supportFragmentManager.findFragmentByTag(resources.getString(R.string.message)) as MessageFragment?

                            messageFrag?.let {
                                when (eventName) {
                                    "user:typing:start" -> {
                                        messageFrag.userStartTyping(initiatingUser)
                                    }
                                    "user:typing:stop" -> {
                                        messageFrag.userStopTyping(initiatingUser)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            override fun presence(pubnub: PubNub, presence: PNPresenceEventResult) {
                runOnUiThread {
                    //                subscribeText.text = message.message.toString()
                    Log.d(TAG, "${presence.event}")
                }
            }
            override fun signal(pubnub: PubNub, pnSignalResult: PNSignalResult) {
                runOnUiThread {
                    //                subscribeText.text = message.message.toString()
                    Log.d(TAG, "signal")
                }
            }

            override fun user(pubnub: PubNub, pnUserResult: PNUserResult) {
                runOnUiThread {
                    //                subscribeText.text = message.message.toString()
                    Log.d(TAG, "user")
                }
            }

            override fun messageAction(pubnub: PubNub, pnMessageActionResult: PNMessageActionResult) {
                runOnUiThread {
                    //                subscribeText.text = message.message.toString()
                    Log.d(TAG, "messageAction")
                }
            }

            override fun membership(pubnub: PubNub, pnMembershipResult: PNMembershipResult) {
                runOnUiThread {
                    //                subscribeText.text = message.message.toString()
                    Log.d(TAG, "membership")
                }
            }

            override fun space(pubnub: PubNub, pnSpaceResult: PNSpaceResult) {
                runOnUiThread {
                    //                subscribeText.text = message.message.toString()
                    Log.d(TAG, "space")
                }
            }
        }

        val pnConfiguration = PNConfiguration()
        pnConfiguration.subscribeKey = prefs?.clientConfiguration?.subscribeKey
        pnConfiguration.publishKey = prefs?.clientConfiguration?.publishKey
        pnConfiguration.authKey = prefs?.clientConfiguration?.secretKey
        pnConfiguration.uuid = prefs?.externalUserId
        pubNub = PubNub(pnConfiguration)

        pubNub!!.run {
            addListener(subscribeCallback)
            subscribe()
                .channels(getChannelsList()) // subscribe to channels
                .execute()
        }
        Log.d(TAG, "subscribedChannels ${pubNub?.subscribedChannels}")
    }

    private fun getDisplayName(conversationId: String?, userId: String): String? {
//        val found = conversations.find { c -> c.conversationId.equals(conversationId) }
//        found?.let { c ->
            // Find participant
            val participant = currentConversation?.participants?.find { it.user.userId.toUpperCase().equals(userId.toUpperCase()) }
            participant?.let {
                return participant.user.displayName
            }
//        }
        return null
    }

    private fun getChannelsList(): MutableList<String> {
        val list = mutableListOf<String>()
        //  tenant_id-user_id
        list.add("${prefs?.tenantId}-${prefs?.externalUserId}".toUpperCase())
        for (conversation in conversations) {
            //  tenant_id-conversation_id
            list.add("${conversation.tenantId}-${conversation.conversationId}".toUpperCase())
        }
        Log.d(TAG, "${list}")
        return list
    }

    fun subscribeToChannel(channel: String) {
        Log.d(TAG, "subscribeToChannel ${channel}")
        pubNub?.subscribe()?.channels(mutableListOf(channel))?.execute()
    }

    fun showMessageFragment(conversation: Conversation) {
        currentConversation = conversation
        // Show MessageFragment and add to backstack
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager?.beginTransaction()
        messageFragment = MessageFragment()
        fragmentTransaction?.replace(R.id.container, messageFragment, resources.getString(R.string.message))
        fragmentTransaction?.addToBackStack(null)
        fragmentTransaction?.commit()
    }

    fun showVideoFragment() {
        // Show VideoFragment and add to backstack
//        val fragmentManager = supportFragmentManager  // TODO Add VideoFragment
//        val fragmentTransaction = fragmentManager?.beginTransaction()
//        val videoFragment = VideoFragment()
//        fragmentTransaction?.replace(R.id.container, videoFragment, resources.getString(R.string.video))
//        fragmentTransaction?.addToBackStack(null)
//        fragmentTransaction?.commit() // TODO
    }

    fun showInfoFragment() {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager?.beginTransaction()
        val messageFrag = supportFragmentManager.findFragmentByTag(resources.getString(R.string.message)) as MessageFragment?
        infoFragment = MessageInfoFragment()
        infoFragment.setTargetFragment(messageFrag, AppConstants.FRAGMENT_CODE)
        fragmentTransaction?.replace(R.id.container, infoFragment, resources.getString(R.string.info))
        fragmentTransaction?.addToBackStack(null)
        fragmentTransaction?.commit()
    }

    // Start a new message fragment. Search for users
    fun showSearchFragment() {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager?.beginTransaction()
        val searchFrag = SearchFragment()
        fragmentTransaction?.replace(R.id.container, searchFrag, resources.getString(R.string.search))
        fragmentTransaction?.addToBackStack(null)
        fragmentTransaction?.commit()
    }

    // Adding a participant. Search for a user
    fun showFindUserFragment(participants: MutableList<User>) {
        val userIds = getUserIds(participants)
        val bundle = Bundle()
        bundle.putStringArrayList("userIds", userIds);

        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager?.beginTransaction()
        val addUserFrag = FindUserFragment()
        val messageInfoFrag = supportFragmentManager.findFragmentByTag(resources.getString(R.string.info)) as MessageInfoFragment?
        addUserFrag.setTargetFragment(messageInfoFrag, AppConstants.FRAGMENT_CODE)
        // Send user IDs to fragment
        addUserFrag.arguments = bundle
        fragmentTransaction?.replace(R.id.container, addUserFrag, resources.getString(R.string.add_participant))
        fragmentTransaction?.addToBackStack(null)
        fragmentTransaction?.commit()
    }

    fun showAvatarFragment() {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager?.beginTransaction()
        val avatarFrag = AvatarFragment()
        fragmentTransaction?.replace(R.id.container, avatarFrag, resources.getString(R.string.select_avatar))
        fragmentTransaction?.addToBackStack(null)
        fragmentTransaction?.commit()
    }

    fun createConversationFromSearch(publicId: String) {
        // Reset current conversation
        resetCurrentConversation()

        // Show MessageFragment and add to backstack
        // Send user's publicId over
//        val publicId = result.contents.split("/").last()
        val bundle = Bundle()
        bundle.putString("public_id", publicId)

        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager?.beginTransaction()
        val fragment = MessageFragment()
        fragment.arguments = bundle
        fragmentTransaction?.replace(R.id.container, fragment, resources.getString(R.string.message))
        // Pop this SearchFragment off
        supportFragmentManager?.popBackStack()
        fragmentTransaction?.addToBackStack(null)
        fragmentTransaction?.commit()
    }

    fun getCurrentConversation(): Conversation? {
        return currentConversation
    }

    fun setCurrentConversation(conversation: Conversation) {
        currentConversation = conversation
    }

    fun resetCurrentConversation() {
        currentConversation = null
    }

    fun getAppBar(): AppBarLayout {
        return findViewById(R.id.appbar_layout)
    }

    fun getUserIds(participants: MutableList<User>): ArrayList<String> {
        val userIds = ArrayList<String>()
        for(participant in participants) {
            userIds.add(participant.userId)
        }
        return userIds
    }

    // Improve setting conversation name for MessageFragment
    fun updateCurrentConversationName(title: String, conversationId: String) {
        if(currentConversation != null){
            if(currentConversation?.conversationId?.toUpperCase().equals(conversationId.toUpperCase())) {
                currentConversation?.title = title
                Log.d(TAG, "updateCurrentConversationName ${currentConversation}")
            }
        }
    }

    fun openDrawer() {
//        mDrawer.openDrawer(Gravity.LEFT)
    }

    fun setAvatar(profileImageUrl: String) {
//        editUser(profileImageUrl = profileImageUrl)   // TODO
//        Picasso.get().load(url).into(profileImageView)
        supportFragmentManager.popBackStack()
//        mDrawer.openDrawer(Gravity.LEFT)
    }

    fun convertDateToLong(date: String): Long {
        val df = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        df.setTimeZone(TimeZone.getTimeZone("UTC"))
        return df.parse(date).time
    }

    // Add added Participant to currentConversation
    fun addParticipantToCurrentConversation(conversationId: String, conversationUser: User) {
        if(currentConversation?.conversationId.equals(conversationId)) {
            val newParticipant = Participant(user = conversationUser)
            val found = currentConversation?.participants?.find { p -> p.user.userId.equals(conversationUser.userId) }
            if(found == null) {
                currentConversation?.participants?.add(newParticipant)
            }
        }
    }

    fun removeParticipantFromCurrentConversation(conversationId: String, conversationUser: User) {
        if(currentConversation?.conversationId.equals(conversationId)) {
            val found = currentConversation?.participants?.find { p -> p.user.userId.equals(conversationUser.userId) }
            found?.let {
                currentConversation?.participants?.remove(found)
            }
        }
    }

    fun isCurrentStatusMessagesEmpty(): Boolean {
        return currentStatusMessages.isEmpty()
    }
    fun clearCurrentStatusMessages() {
        currentStatusMessages.clear()
    }
    fun getAndClearCurrentStatusMessages(): MutableList<String> {
        val list = mutableListOf<String>()
        list.addAll(currentStatusMessages)
        currentStatusMessages.clear()
        return list
    }

    // Finish Activity
    fun finishActivity() {
        setResult(Activity.RESULT_OK)
        finish()
    }

    // Extensions for dp-px conversion
    val Int.dp: Int
        get() = (this / Resources.getSystem().displayMetrics.density).toInt()
    val Int.px: Int
        get() = (this * Resources.getSystem().displayMetrics.density).toInt()


    //        [
    //            "displayName": displayName,
    //        "photoURL": photoURL ?? user?.photoURL?.absoluteString,
    //        "token": token
    //        ]
//    private fun editUser(displayName: String? = null, profileImageUrl: String? = null) {
//        val data = hashMapOf<Any, Any>()
//        data.put("displayName", if(displayName.isNullOrBlank()) prefs.displayName() else displayName)
//        data.put("photoURL", if(profileImageUrl.isNullOrBlank()) prefs.profileImageUrl() else profileImageUrl)
//        data.put("token", prefs.messagingToken() as String)
//
//        Log.d(TAG, "editUserName data ${data}")
//
//        functions
//            .getHttpsCallable("editUserName")
//            .call(data)
//            .continueWith { task ->
//                // This continuation runs on either success or failure, but if the task
//                // has failed then result will throw an Exception which will be
//                // propagated down.
//                val result = task.result?.data as String
//                val resultObj = JSONObject(result)
//
//                Log.d(TAG, "editUserName task.result ${task.result}")
//                Log.d(TAG, "editUserName ${result}")
//                Log.d(TAG, "editUserName ${resultObj.get("displayName")}")
//                Log.d(TAG, "editUserName ${resultObj.get("profileImageUrl")}")
////                val messagingToken = resultObj.get("token") as String
////                Log.d(TAG, "setMessageToken ${messagingToken}")
////                prefs.setMessagingToken(messagingToken)
////                updateFirebaseUser(resultObj.get("displayName"), resultObj.get(""))
//
//                result
//            }
//            .addOnSuccessListener {
//                Log.d(TAG, "onSuccessListener ${it}")
//            }
//    }
//
//    private fun initFirebaseUser() {
//        // Get user info
//        // db "messaging" > user.uid
//        val ref = database.reference.child("messaging").child(auth.currentUser?.uid!!)
//
//        ref.addValueEventListener(object : ValueEventListener {
//            override fun onDataChange(dataSnapshot: DataSnapshot) {
//                // This method is called once with the initial value and again
//                // whenever data at this location is updated.
//                val value = dataSnapshot.getValue(com.koder.ellen.data.model.FirebaseUser::class.java)
//                Log.d(TAG, "onDataChange value ${value}")
//                value?.let {
//                    prefs.saveFirebaseUser(it)
//                    viewModel.firebaseUser.value = it
//                }
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//                // Failed to read value
//                Log.w(TAG, "Failed to read value.", error.toException())
//            }
//        })
//    }
}
