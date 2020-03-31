package com.koder.ellen.ui.conversation

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.koder.ellen.Messenger.Companion.prefs
import com.koder.ellen.MessengerActivity
import com.koder.ellen.R
import com.koder.ellen.data.ConversationDataSource
import com.koder.ellen.data.ConversationRepository
import com.koder.ellen.model.Conversation
import com.koder.ellen.model.Message
import com.koder.ellen.model.Participant
import com.koder.ellen.ui.BaseViewModelFactory
import com.koder.ellen.ui.main.MainViewModel
import java.text.SimpleDateFormat
import java.util.*


class ConversationFragment : Fragment() {

    companion object {
        fun newInstance() = ConversationFragment()
        private const val TAG = "ConversationFragment"
    }

    private lateinit var viewModel: MainViewModel
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var mDrawer: DrawerLayout
    private lateinit var mDrawerToggle: ActionBarDrawerToggle
    private lateinit var mToolbar: Toolbar

    // RecyclerView
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyView: TextView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager
    private var conversations: MutableList<Conversation> = mutableListOf()

    val conversationViewModel: ConversationViewModel by lazy {
        ViewModelProvider(this, BaseViewModelFactory {
            ConversationViewModel(
                ConversationRepository(ConversationDataSource())
            )
        }).get(ConversationViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Enable App Bar menu
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate Fragment's view
        val rootView = inflater.inflate(R.layout.fragment_conversation, container, false)

        mToolbar = rootView.findViewById<Toolbar>(R.id.toolbar)

        // Set SwipeRefreshLayout listener and refresh Conversations
        swipeRefreshLayout = rootView.findViewById(R.id.swipe_refresh_layout)
        swipeRefreshLayout.setOnRefreshListener {
            conversationViewModel.loadConversations()
//            swipeRefreshLayout.setRefreshing(false)
        }

        return rootView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        // App Bar title
        activity?.run {
            // Communicate with host Activity
            viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
//            viewModel.updateActionBarTitle(getResources().getString(R.string.conversations))

            // Toolbar, DrawerLayout
//            TODO This Activity already has an action bar supplied by the window decor. Do not request Window.FEATURE_SUPPORT_ACTION_BAR and set windowActionBar to false in your theme to use a Toolbar instead.
            (this as MessengerActivity).setSupportActionBar(findViewById(R.id.toolbar))
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setDisplayShowHomeEnabled(true)
            supportActionBar?.title = resources.getString(R.string.conversations)

            // Reference DrawerLayout to open Drawer
            mDrawer = activity?.findViewById(R.id.drawer_layout) as DrawerLayout

            // Navigation DrawerLayout
            mDrawer = findViewById(R.id.drawer_layout) as DrawerLayout
            mDrawerToggle = ActionBarDrawerToggle(
                this, mDrawer, R.string.drawer_open, R.string.drawer_close
            )
            // Setup toggle to display hamburger icon with nice animation
            mDrawerToggle.setDrawerIndicatorEnabled(true)
            mDrawerToggle.syncState()
            // Tie DrawerLayout events to the ActionBarToggle
            mDrawer.addDrawerListener(mDrawerToggle)

            // Setup RecyclerView
            viewManager = LinearLayoutManager(this)
            viewAdapter = ConversationAdapter(this, conversations)
            recyclerView = findViewById<RecyclerView>(R.id.recycler_view).apply {
                // use this setting to improve performance if you know that changes
                // in content do not change the layout size of the RecyclerView
                setHasFixedSize(true)

                // use a linear layout manager
                layoutManager = viewManager

                // specify an viewAdapter (see also next example)
                adapter = viewAdapter
            }
            recyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
            emptyView = findViewById(R.id.empty_conversations_view)

            if(prefs?.clientConfiguration?.subscribeKey.isNullOrBlank()) {
                Log.d(TAG, "Load Client Config")
//                conversationViewModel.loadClientConfig()
                viewModel.loadClientConfig()
                viewModel.registerNotificationToken()
            } else {
                Log.d(TAG, "Load Conversations")
                swipeRefreshLayout.setRefreshing(true)
                conversationViewModel.loadConversations()
            }

            // Swipe-to-delete Conversation
            val deleteDrawable = AppCompatResources.getDrawable(this, R.drawable.ic_delete_24)

            lateinit var itemTouchHelper: ItemTouchHelper
            val swipeCallback = object : SwipeToDeleteCallback(this, deleteDrawable!!) {
                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
//                adapter.removeAt(viewHolder.adapterPosition)
                    Log.d(TAG, "Remove ${viewHolder.adapterPosition}")
                    Log.d(TAG, "Remove ${conversations.get(viewHolder.adapterPosition)}")
//                val position = viewHolder.adapterPosition
                    // TODO
//                    swipeRefreshLayout.setRefreshing(true)
//                    conversationViewModel.deleteConversation(conversations.get(viewHolder.adapterPosition))
//                    conversations.removeAt(viewHolder.adapterPosition)
//                    recyclerView.adapter!!.notifyItemRemoved(viewHolder.adapterPosition)
                    // TODO
                    // Show confirmation to delete
                    Log.d(TAG, "swipeCallback show confirmation to delete")
                    MaterialAlertDialogBuilder(context)
                        .setTitle("Close Conversation")
                        .setMessage("Are you sure you would like to close this conversation? This will close the conversation for all participants.")
                        .setNegativeButton("Cancel") { dialog, which ->
                            // Delete cancelled, reset swiped item
//                            viewAdapter.notifyItemChanged(viewHolder.adapterPosition)

                            itemTouchHelper?.attachToRecyclerView(null)
                            itemTouchHelper?.attachToRecyclerView(recyclerView)
                        }
                        .setPositiveButton("Confirm") { dialog, which ->
                            // Delete confirmed
                            swipeRefreshLayout.setRefreshing(true)
                            conversationViewModel.deleteConversation(conversations.get(viewHolder.adapterPosition))
                            conversations.removeAt(viewHolder.adapterPosition)
                            recyclerView.adapter!!.notifyItemRemoved(viewHolder.adapterPosition)
                        }
                        .show()
                }
            }
//            val itemTouchHelper = ItemTouchHelper(swipeCallback)
            itemTouchHelper = ItemTouchHelper(swipeCallback)
            itemTouchHelper.attachToRecyclerView(recyclerView)

            // Observer, getConversations
            conversationViewModel.conversations.observe(viewLifecycleOwner, Observer {
                Log.d(TAG, "Conversations changed")
                conversations.clear()
                conversations.addAll(it)
                viewAdapter.notifyDataSetChanged()
                updateRV(it)
//                if(pubNub == null) initPubNub()
                swipeRefreshLayout.setRefreshing(false)
                // Communicate to host activity to subscribe to Conversation channels, one time only
                viewModel.conversations.value = it
            })

            // Observer, deleteConversation
            conversationViewModel.delete.observe(viewLifecycleOwner, Observer {
                Log.d(TAG, "Delete conversation ${it.conversation.conversationId}")
                if(!it.deleted) {
                    Toast.makeText(this, R.string.conversation_retry, Toast.LENGTH_LONG).show()
                    // Add conversation back to list
                    conversations.add(it.conversation)
                    conversationViewModel.conversations.value = ConversationDataSource().sortConversationsByLatestMessage(conversations)
                } else {
//                Toast.makeText(this, R.string.conversation_closed, Toast.LENGTH_LONG).show()
                    viewAdapter.notifyDataSetChanged()
                    updateRV(conversations)
                    swipeRefreshLayout.setRefreshing(false)
                }
            })
        } ?: throw Throwable("invalid activity")
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
        inflater.inflate(R.menu.conversation_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean { // Handle presses on the action bar items
        return when (item.itemId) {
            android.R.id.home -> {
                // Open host Activity's Navigation Drawer
//                Log.d(TAG, "Open drawer")
                mDrawer.openDrawer(GravityCompat.START)
                true
            }
            R.id.action_message -> {
//                val integrator = IntentIntegrator.forSupportFragment(this)
//                integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
//                integrator.setOrientationLocked(false)
//                integrator.setPrompt("")
//                integrator.setBeepEnabled(false)
//                integrator.setCaptureActivity(CustomScannerActivity::class.java)
//                integrator.initiateScan()
//                Log.d(TAG, "action_message")

                // Show user search fragment
                (context as MessengerActivity).showSearchFragment()
                true
            }
            else -> super.onOptionsItemSelected(item)
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
//                Toast.makeText(activity, "Scanned: " + result.contents, Toast.LENGTH_LONG).show()
//                Log.d(TAG, result.contents)
//                val intent = Intent(this, MessageActivity::class.java).apply {
//                    putExtra("CONVERSATION_ID", "")
//                    putExtra("CONVERSATION_TITLE", "")
//                    putExtra("CONVERSATION_DESCRIPTION", "")
//                    putExtra("PARTICIPANT_URL", result.contents)
//                    putExtra("CONVERSATION_PARTICIPANTS", ArrayList<User>())
//                }
//                startActivity(intent)
//
//                // Start Fragment
//                // TODO Move existing functionality over
//                // Reset current conversation
//                (activity as MessengerActivity).resetCurrentConversation()
//
//                // Show MessageFragment and add to backstack
//                // Send user's publicId over
//                val publicId = result.contents.split("/").last()
//                val bundle = Bundle()
//                bundle.putString("public_id", publicId)
//
//                val fragmentManager = activity?.supportFragmentManager
//                val fragmentTransaction = fragmentManager?.beginTransaction()
//                val fragment = MessageFragment()
//                fragment.arguments = bundle
//                fragmentTransaction?.replace(R.id.container, fragment, resources.getString(R.string.message))
//                fragmentTransaction?.addToBackStack(null)
//                fragmentTransaction?.commit()
//            }
//        } else {
//            super.onActivityResult(requestCode, resultCode, data)
//        }
    }

    // Update RececyclerView based on the dataset
    private fun updateRV(list: MutableList<Conversation>) {
        if (list.isEmpty()) {
            Log.d(TAG, "Dataset empty")
            emptyView.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
            return
        }

        Log.d(TAG, "Dataset not empty")
        emptyView.visibility = View.GONE
        recyclerView.visibility = View.VISIBLE
    }

    fun loadConversations() {
        Log.d(TAG, "Load conversations")
        swipeRefreshLayout.isRefreshing = true
        conversationViewModel.loadConversations()
    }

    // conversation:created
    fun addConversation(conversation: Conversation) {
//        val filteredList: List<Conversation> = conversations.filter { it.conversationId.equals(conversation.conversationId)}
        val found = conversations.find { it.conversationId.equals(conversation.conversationId) }
//        if (filteredList.isEmpty()) {
        if(found == null) {
            if(conversation.timeCreated.toString().contains("-")) {
                conversation.timeCreated = convertDateToLong(conversation.timeCreated.toString())
            }
            conversation.messages = mutableListOf()
            conversations.add(conversation)
            conversationViewModel.conversations.value = ConversationDataSource().sortConversationsByLatestMessage(conversations)
//            activity.pubNub?.subscribe()?.channels(mutableListOf("${prefs.tenantId()}-${conversation.conversationId}".toUpperCase()))?.execute() // TODO
            // Communicate with host Activity to subscribe to this channel
            viewModel.subscribeChannelList.value = mutableListOf("${prefs?.externalUserId}-${conversation.conversationId}".toUpperCase())
        }
    }

    // conversation:closed
    fun removeConversation(conversation: Conversation) {
        val filteredList: List<Conversation> = conversations.filter { it.conversationId.equals(conversation.conversationId) }
        if(filteredList.isNotEmpty()) {
            val conversation = filteredList.first()
            val position = conversations.indexOf(conversation)
//            Log.d(TAG, "${position}")
            conversations.removeAt(position)
//            conversationViewModel.conversations.value = dataSource.sortConversationsByLatestMessage(conversations)
            viewAdapter.notifyItemRemoved(position)
            updateRV(conversations)
        }
    }

    // message:published
    fun addMessageToConversations(message: Message) {
        val filteredList: List<Conversation> = conversations.filter { it.conversationId.equals(message.conversationId)}
        if (filteredList.isNotEmpty()) {
            val conversation = filteredList.first()

            // Unify date format, as its form is like "timeCreated":"2020-01-20T03:40:03.056Z"
            if(message.timeCreated.toString().contains("-")) {
                message.timeCreated = convertDateToLong(message.timeCreated.toString())
            }

            val newMessages = mutableListOf(message)
            conversation.messages = newMessages
            conversations.set(conversations.indexOf(conversation), conversation)
            // Sort and set conversations
            conversationViewModel.conversations.value = ConversationDataSource().sortConversationsByLatestMessage(conversations)
        }
    }

    // conversation:participant:removed
    fun removeFromConversations(conversationId: String) {
        val filteredList: List<Conversation> = conversations.filter { it.conversationId.equals(conversationId) }
        if(filteredList.isNotEmpty()) {
            val conversation = filteredList.first()
            val position = conversations.indexOf(conversation)
//            Log.d(TAG, "${position}")
            conversations.removeAt(position)
//            conversationViewModel.conversations.value = dataSource.sortConversationsByLatestMessage(conversations)
            viewAdapter.notifyItemRemoved(position)
            updateRV(conversations)
        }
    }

    fun convertDateToLong(date: String): Long {
        val df = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        df.setTimeZone(TimeZone.getTimeZone("UTC"))
        return df.parse(date).time
    }

    // Update participant state
    fun updateConversationParticipant(conversationId: String, participant: Participant) {
        val found = conversations.find { it.conversationId.equals(conversationId) }
        val index = conversations.indexOf(found)
        found?.let { convo ->
            // Found conversation
            // Update participant state
            val partFound = convo.participants.find { part -> part.user.userId.equals(participant.user.userId) }
            partFound?.let {part ->
                val partIndex = convo.participants.indexOf(part)
                part.state = participant.state
                convo.participants.set(partIndex, part)
                conversations.set(index, convo)
            }
        }
    }
}
