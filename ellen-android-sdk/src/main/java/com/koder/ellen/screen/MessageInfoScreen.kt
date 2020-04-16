package com.koder.ellen.screen

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.koder.ellen.Messenger
import com.koder.ellen.Messenger.Companion.prefs
import com.koder.ellen.MessengerActivity
import com.koder.ellen.R
import com.koder.ellen.core.AppConstants
import com.koder.ellen.data.MessageDataSource
import com.koder.ellen.data.MessageRepository
import com.koder.ellen.model.Conversation
import com.koder.ellen.model.User
import com.koder.ellen.ui.BaseViewModelFactory
import com.koder.ellen.ui.main.MainViewModel
import com.koder.ellen.ui.message.MessageInfoAdapter
import com.koder.ellen.ui.message.MessageViewModel


class MessageInfoScreen : Fragment(), View.OnClickListener {

    companion object {
        fun newInstance() = MessageInfoScreen()
        private const val TAG = "MessageInfoFragment"
    }

    private lateinit var viewModel: MainViewModel
    private val messageViewModel: MessageViewModel by lazy {
        ViewModelProvider(this, BaseViewModelFactory {
            MessageViewModel(
                MessageRepository(MessageDataSource()),
                activity!!.application
            )
        }).get(MessageViewModel::class.java)
    }

    private var conversationId: String? = null
    private lateinit var conversation: Conversation
    private val participants: MutableList<User> = mutableListOf()
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager

    // Views
    private lateinit var titleView: TextView
    private lateinit var descriptionView: TextView
    private lateinit var addParticipantLayout: ConstraintLayout
    private lateinit var closeConversationLayout: ConstraintLayout

    private var title: String = ""
    private var description: String = ""
    private val statusMessageList: MutableList<String> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Enable App Bar menu
        setHasOptionsMenu(true)

        conversationId = arguments?.getString("CONVERSATION_ID")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate Fragment's view
        val rootView = inflater.inflate(R.layout.fragment_message_info, container, false)

        addParticipantLayout = rootView.findViewById(R.id.add_participant_layout)
        closeConversationLayout = rootView.findViewById(R.id.close_conversation_layout)
        addParticipantLayout.setOnClickListener(this)
        closeConversationLayout.setOnClickListener(this)

        // Hide AppBarLayout for Screens
        val appBar = rootView.findViewById<AppBarLayout>(R.id.appbar_layout)
        appBar.visibility = View.GONE

        return rootView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        activity?.run {
//            viewModel = ViewModelProvider(this).get(MainViewModel::class.java)    // TODO UI Screens
//            viewModel.updateActionBarTitle(getResources().getString(R.string.info))   // TODO UI Screens
            // Toolbar
//            (this as MessengerActivity).setSupportActionBar(findViewById(R.id.toolbar))   // TODO UI Screens
//            supportActionBar?.setDisplayHomeAsUpEnabled(true)
//            supportActionBar?.setDisplayShowHomeEnabled(true)
//            supportActionBar?.title = resources.getString(R.string.info)  // TODO UI Screens
            // Set current conversation
//            if((this as MessengerActivity).getCurrentConversation() == null ) {   // TODO UI Screens
//                Log.d(TAG, "currentConversation null, go back to previous fragment")
//                activity?.supportFragmentManager?.popBackStack()
//                return
//            } // TODO UI Screens
//            conversation = (this as MessengerActivity).getCurrentConversation()!! // TODO UI Screens
            conversation = Messenger.conversations.find { c -> c.conversationId.equals(conversationId, ignoreCase = true) }!!
            if(participants.isEmpty()) participants.addAll(getParticipantsList(conversation))
            Log.d(TAG, "${conversation}")

            // RecyclerView
            viewManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

            // Get current user
            val currentUser = participants.find { it.userId.equals(prefs?.externalUserId, ignoreCase = true) }
            viewAdapter = MessageInfoAdapter(this, currentUser!!, participants, this@MessageInfoScreen)

            recyclerView = findViewById<RecyclerView>(R.id.recycler_view).apply {
                // use this setting to improve performance if you know that changes
                // in content do not change the layout size of the RecyclerView
                setHasFixedSize(false)

                // use a linear layout manager
                layoutManager = viewManager

                // specify an viewAdapter (see also next example)
                adapter = viewAdapter
            }
//            recyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

            // Title
            titleView = findViewById(R.id.title_text)
            title = if(conversation.title.isNullOrBlank()) getTitleByParticipants(participants) else conversation.title
            titleView.text = title
            val titleLayout = findViewById<ConstraintLayout>(R.id.title_layout)
            titleLayout.setOnClickListener {
                val input = EditText(activity)
//                val container = FrameLayout(activity as MessengerActivity)    // TODO UI Screens
                val container = FrameLayout(activity as Context)
                val params = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                input.setText(titleView.text)
                input.setHint("Add a name")
                input.setLayoutParams(params)
                container.setPadding(20.px, 0, 20.px, 0)
                container.addView(input)

                // Note: A second constructor exists to pass in a theme res ID
//                MaterialAlertDialogBuilder(activity as MessengerActivity)
                MaterialAlertDialogBuilder(activity as Context)
                    // Add customization options here
                    .setView(container)
                    .setTitle("Conversation name")
                    // Confirming action
                    .setPositiveButton("Update") { dialog, which ->
                        // Do something for button click
                        Log.d(TAG, "${input.text}")
                        val text = if(input.text.isBlank()) getTitleByParticipants(participants) else input.text
                        messageViewModel.updateConversationTitle(conversation.conversationId, text.toString())
                    }
                    // Dismissive action
                    .setNegativeButton("Cancel") { dialog, which ->
                        // Do something for button click
                    }
                    .show()
            }

            // Description
            descriptionView = findViewById(R.id.description_text)
            description = if(conversation.description.isNullOrBlank()) "Set conversation description" else conversation.description
            descriptionView.text = description
            val descriptionLayout = findViewById<ConstraintLayout>(R.id.description_layout)
            descriptionLayout.setOnClickListener {
                val input = EditText(activity)
//                val container = FrameLayout(activity as MessengerActivity)
                val container = FrameLayout(activity as Context)
                val params = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                if(!conversation.description.isNullOrBlank()) input.setText(descriptionView.text)
                input.setHint("Add a description")
                input.setLayoutParams(params)
                container.setPadding(20.px, 0, 20.px, 0)
                container.addView(input)

                // Note: A second constructor exists to pass in a theme res ID
                MaterialAlertDialogBuilder(activity)
                    // Add customization options here
                    .setView(container)
                    .setTitle("Conversation description")
                    // Confirming action
                    .setPositiveButton("Update") { dialog, which ->
                        // Do something for button click
                        messageViewModel.updateConversationDescription(conversation.conversationId, input.text.toString())
                    }
                    // Dismissive action
                    .setNegativeButton("Cancel") { dialog, which ->
                        // Do something for button click
                    }
                    .show()

            }
        } ?: throw Throwable("invalid activity")

        // Observer, Participant Added
        messageViewModel.participantAdded.observe(viewLifecycleOwner, Observer {
            Log.d(TAG, "participantAdded ${it}")
//            Log.d(TAG, "participants ${participants}")
            val found = participants.find { p -> p.userId.equals(it.userId, ignoreCase = true) }
//            if(!participants.contains(it)) {
//                participants.add(it)
//                viewAdapter.notifyItemInserted(participants.size-1)
//            }

            if(found == null) {
                participants.add(it)
                val index = participants.indexOf(it)
                viewAdapter.notifyItemInserted(index)

                // Update currentConversation in MainActivty
                (activity as MessengerActivity).addParticipantToCurrentConversation(conversation.conversationId, it)
                titleView.text = getConversationTitle()
            }
        })

        // Observer, Participant Removed
        messageViewModel.participantRemoved.observe(viewLifecycleOwner, Observer {
            Log.d(TAG, "${it}")
            val index = participants.indexOf(it)
            if(index > -1) {
                Log.d(TAG, "Index ${index}")
                participants.removeAt(index)
                Log.d(TAG, "Participants size ${participants.size}")
                viewAdapter.notifyItemRemoved(index)

                (activity as MessengerActivity).removeParticipantFromCurrentConversation(conversation.conversationId, it)
                titleView.text = getConversationTitle()
            }
        })

        // Observer, Moderator Added
        messageViewModel.moderatorAdded.observe(viewLifecycleOwner, Observer {
            Log.d(TAG, "moderatorAdded ${it}")
            val found = participants.find { p -> p.userId.equals(it.userId, ignoreCase = true) }
            found?.let {
                val index = participants.indexOf(found)
                found.role = 10
                participants.set(index, found)
                viewAdapter.notifyItemChanged(index)
            }
        })

        // Observer, Moderator Removed
        messageViewModel.moderatorRemoved.observe(viewLifecycleOwner, Observer {
            Log.d(TAG, "moderatorRemoved ${it}")
            val found = participants.find { p -> p.userId.equals(it.userId, ignoreCase = true) }
            found?.let {
                val index = participants.indexOf(found)
                found.role = 0
                participants.set(index, found)
                viewAdapter.notifyItemChanged(index)
            }
        })

        // Observer, Conversation Name
        messageViewModel.conversationName.observe(viewLifecycleOwner, Observer {
            Log.d(TAG, "conversationName ${it}")
            (activity as MessengerActivity).updateCurrentConversationName(it, conversation.conversationId)
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
//        inflater.inflate(R.menu.message_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean { // Handle presses on the action bar items
        return when (item.itemId) {
            android.R.id.home -> {
                activity?.supportFragmentManager?.popBackStack()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.add_participant_layout -> {
                Log.d(TAG, "Add Participant")
                //  TODO
//                val integrator = IntentIntegrator.forSupportFragment(this)
//                integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
//                integrator.setOrientationLocked(false)
//                integrator.setPrompt("")
//                integrator.setBeepEnabled(false)
//                integrator.setCaptureActivity(CustomScannerActivity::class.java)
//                integrator.initiateScan()
                //  TODO

                // Show AddUserFragment
//                (activity as MessengerActivity).showFindUserFragment(participants)    // TODO UI Screens
                true
            }
            R.id.close_conversation_layout -> {
                Log.d(TAG, "Close Conversation")
                messageViewModel.deleteConversation(conversation.conversationId)
                true
            }
        }
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
//        val result =
//            IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
//        if (result != null) {
//            if (result.contents == null) {
////                Toast.makeText(activity, "Cancelled", Toast.LENGTH_LONG).show()
//            } else {
////                Toast.makeText(activity, "Scanned: " + result.contents, Toast.LENGTH_LONG).show()
//                Log.d(TAG, result.contents)
//
//                val participantId = result.contents.split("/").last()
//
//                // Add Participant
//                messageViewModel.addParticipant(participantId, conversation.conversationId)
//            }
//        } else {
//            super.onActivityResult(requestCode, resultCode, data)
//        }

        if (resultCode == RESULT_OK) {
            if (requestCode== AppConstants.FRAGMENT_CODE){
                val userId = data?.getStringExtra("userId")
                val displayName = data?.getStringExtra("displayName")
                val tenantId = data?.getStringExtra("tenantId")

                Log.d(TAG, "userId ${userId}")
                Log.d(TAG, "displayName ${displayName}")

                // Add user to participants view
                val user = User(tenantId = tenantId!!, userId = userId!!, displayName = displayName!!, profileImageUrl = "")
                messageViewModel.participantAdded.value = user
            }
        }
    }

    private fun getParticipantsList(conversation: Conversation): MutableList<User> {
        val list = mutableListOf<User>()
        for (participant in conversation.participants) {
            // Add role to user hack
            val user = participant.user
            user.role = participant.role
            list.add(user)
        }
        return list
    }

    // Return participants in format Participant1, Participant2, ...
    private fun getTitleByParticipants(participants: MutableList<User>): String {
        var title = ""

        // If the only participant is the sender (myself)
        if(participants.size == 1 && participants.first().userId.equals(prefs?.externalUserId, ignoreCase = true))
            return "Me"

        for (participant in participants) {
            if (participant.displayName.equals(prefs?.currentUser?.profile?.displayName, ignoreCase = true)) continue
            if (title.isEmpty()) {
                title += participant.displayName
                continue
            }
            title += ", ${participant.displayName}"
        }
        return title
    }

    // Extensions for dp-px conversion
    val Int.dp: Int
        get() = (this / Resources.getSystem().displayMetrics.density).toInt()
    val Int.px: Int
        get() = (this * Resources.getSystem().displayMetrics.density).toInt()

    fun removeFromConversation(user: User) {
        Log.d(TAG, "Remove userId ${user.userId}")
        Log.d(TAG, "from Conversation ${conversation.conversationId}")
        messageViewModel.removeParticipant(user, conversation.conversationId)
    }

    fun addModerator(user: User) {
        Log.d(TAG, "Promote ${user.userId}")
        messageViewModel.addModerator(user, conversation.conversationId)
    }

    fun removeModerator(user: User) {
        Log.d(TAG, "Remove moderator ${user}")
        messageViewModel.removeModerator(user, conversation.conversationId)
    }

    fun updateTitle(newTitle: String) {
        Log.d(TAG, "${newTitle}")
        titleView.text = newTitle
        title = newTitle
    }

    fun updateDescription(newDescription: String) {
        Log.d(TAG, "updateDescription ${newDescription}")
        var desc = newDescription
        if(desc.isBlank()) {
            desc = "Set conversation description"
        }
        descriptionView.text = desc
        description = desc
    }

    fun addParticipant(userId: String) {
        Log.d(TAG, "addParticipant ${userId}")
        messageViewModel.addUserToParticipants(userId)
    }

    fun removeParticipant(userId:String) {
        Log.d(TAG, "removeParticipant ${userId}")
        val found = participants.find { p -> p.userId.equals(userId, ignoreCase = true) }
        found?.let {
            val index = participants.indexOf(found)
            participants.removeAt(index)
            viewAdapter.notifyItemRemoved(index)
        }
    }

    private fun getConversationTitle(): String {
        val currentConversation = (activity as MessengerActivity).getCurrentConversation()
        if(currentConversation != null && !currentConversation.title.isNullOrBlank()) {
            return currentConversation.title
        }

        if(!::conversation.isInitialized) {
            return ""
        }

        if(currentConversation?.title.isNullOrBlank()) {
            return getTitleByParticipants(getParticipantsList(currentConversation!!))
        }

        return conversation.title
    }

    fun addStatusMessage(statusMessage: String) {
        statusMessageList.add(statusMessage)
    }
}
