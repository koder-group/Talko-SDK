package com.koder.ellen.screen

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RoundRectShape
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.koder.ellen.EventCallback
import com.koder.ellen.Messenger
import com.koder.ellen.Messenger.Companion.prefs
import com.koder.ellen.R
import com.koder.ellen.data.ConversationDataSource
import com.koder.ellen.data.ConversationRepository
import com.koder.ellen.model.Conversation
import com.koder.ellen.model.Message
import com.koder.ellen.model.Participant
import com.koder.ellen.model.User
import com.koder.ellen.ui.BaseViewModelFactory
import com.koder.ellen.ui.conversation.ConversationAdapter
import com.koder.ellen.ui.conversation.ConversationViewModel
import com.koder.ellen.ui.conversation.SwipeToDeleteCallback
import com.koder.ellen.ui.main.MainViewModel
import java.text.SimpleDateFormat
import java.util.*


class ConversationScreen : Fragment() {

    companion object {
        fun newInstance() = ConversationScreen()
        private const val TAG = "ConversationScreen"

        var mClickListener: OnItemClickListener? = null
        @JvmStatic fun setItemClickListener(onItemClickListener: OnItemClickListener) {
            mClickListener = onItemClickListener
        }
    }

    abstract class OnItemClickListener: ClickInterface {}
    interface ClickInterface {
        fun OnItemClickListener(conversation: Conversation, position: Int)
    }

    private lateinit var viewModel: MainViewModel
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
//    private lateinit var mDrawer: DrawerLayout
//    private lateinit var mDrawerToggle: ActionBarDrawerToggle
    private lateinit var mToolbar: Toolbar
    private lateinit var rootView: View
    private lateinit var listFrame: FrameLayout

    // RecyclerView
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyView: TextView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager
    private var conversations: MutableList<Conversation> = mutableListOf()

    private val conversationViewModel: ConversationViewModel by lazy {
        ViewModelProvider(this, BaseViewModelFactory {
            ConversationViewModel(
                ConversationRepository(ConversationDataSource())
            )
        }).get(ConversationViewModel::class.java)
    }

    private var filterUserIds: ArrayList<String>? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        setEventHandler()
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
        if(savedInstanceState != null) {
            filterUserIds = savedInstanceState.getStringArrayList("userIds") ?: null
        } else {
            filterUserIds = getArguments()?.getStringArrayList("userIds") ?: null
        }
        Log.d(TAG, "filterUserIds ${filterUserIds}")

        // Inflate Fragment's view
        rootView = inflater.inflate(R.layout.fragment_conversation, container, false)

        mToolbar = rootView.findViewById<Toolbar>(R.id.toolbar)

        // Set SwipeRefreshLayout listener and refresh Conversations
        swipeRefreshLayout = rootView.findViewById(R.id.swipe_refresh_layout)
        swipeRefreshLayout.setOnRefreshListener {
            conversationViewModel.loadConversations()
//            swipeRefreshLayout.setRefreshing(false)
        }

        listFrame = rootView.findViewById<FrameLayout>(R.id.list_frame)
        val appBar = rootView.findViewById(R.id.appbar_layout) as AppBarLayout
        appBar.visibility = View.GONE

        // Customizable UI options
        setBackgroundColor(Messenger.screenBackgroundColor)
        setListCornerRadius(Messenger.screenCornerRadius[0], Messenger.screenCornerRadius[1], Messenger.screenCornerRadius[2], Messenger.screenCornerRadius[3])

        return rootView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        // App Bar title
        activity?.run {

            // Setup RecyclerView
            viewManager = LinearLayoutManager(this)
            viewAdapter = ConversationAdapter(this, conversations, this@ConversationScreen)
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

            // Load conversations
            swipeRefreshLayout.setRefreshing(true)
            conversationViewModel.loadConversations()

            // Swipe-to-delete Conversation
            val deleteDrawable = AppCompatResources.getDrawable(this, R.drawable.ic_delete_24)

            lateinit var itemTouchHelper: ItemTouchHelper
            val swipeCallback = object : SwipeToDeleteCallback(this, deleteDrawable!!) {
                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
//                adapter.removeAt(viewHolder.adapterPosition)
                    Log.d(TAG, "Remove ${viewHolder.adapterPosition}")
                    Log.d(TAG, "Remove ${conversations.get(viewHolder.adapterPosition)}")

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
                            Messenger.removeConversation(conversations.get(viewHolder.adapterPosition).conversationId)
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

                val filtered = filterConversations(it)

                conversations.clear()
                conversations.addAll(filtered)
                viewAdapter.notifyDataSetChanged()
                updateRV(filtered)
                swipeRefreshLayout.setRefreshing(false)

                // Subscribe to channels
                subscribeToConversations(it)

                Messenger.conversations.clear()
                Messenger.conversations.addAll(it)
//                Log.d(TAG, "${Messenger.conversations}")
            })

            // Observer, deleteConversation
            conversationViewModel.delete.observe(viewLifecycleOwner, Observer {
//                Log.d(TAG, "Delete conversation ${it.conversation.conversationId}")
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

    // Update RececyclerView based on the dataset
    private fun updateRV(list: MutableList<Conversation>) {
        if (list.isEmpty()) {
//            Log.d(TAG, "Dataset empty")
            emptyView.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
            return
        }

//        Log.d(TAG, "Dataset not empty")
        emptyView.visibility = View.GONE
        recyclerView.visibility = View.VISIBLE
    }

    fun loadConversations() {
        Log.d(TAG, "Load conversations")
        activity?.runOnUiThread {
            swipeRefreshLayout?.isRefreshing = true
        }
        conversationViewModel.loadConversations()
    }

    // conversation:created
    fun addConversation(conversation: Conversation) {
//        val filteredList: List<Conversation> = conversations.filter { it.conversationId.equals(conversation.conversationId)}
        val found = conversations.find { it.conversationId.equals(conversation.conversationId, ignoreCase = true) }
//        if (filteredList.isEmpty()) {
        if(found == null) {
            if(conversation.timeCreated.toString().contains("-")) {
                conversation.timeCreated = convertDateToLong(conversation.timeCreated.toString())
            }
            conversation.messages = mutableListOf()
            conversations.add(conversation)
//            conversationViewModel.conversations.value = ConversationDataSource().sortConversationsByLatestMessage(conversations)
            conversationViewModel.conversations.postValue(ConversationDataSource().sortConversationsByLatestMessage(conversations))

            Messenger.subscribeToChannelList(mutableListOf("${prefs?.tenantId}-${conversation.conversationId}".toUpperCase()) )
        }
    }

    // conversation:closed
    fun removeConversation(conversation: Conversation) {
        val filteredList: List<Conversation> = conversations.filter { it.conversationId.equals(conversation.conversationId, ignoreCase = true) }
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
        val filteredList: List<Conversation> = conversations.filter { it.conversationId.equals(message.conversationId, ignoreCase = true)}
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
//            conversationViewModel.conversations.value = ConversationDataSource().sortConversationsByLatestMessage(conversations)
            conversationViewModel.conversations.postValue(ConversationDataSource().sortConversationsByLatestMessage(conversations))
        }
    }

    // conversation:participant:removed
    fun removeFromConversations(conversationId: String) {
        val filteredList: List<Conversation> = conversations.filter { it.conversationId.equals(conversationId, ignoreCase = true) }
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
        val found = conversations.find { it.conversationId.equals(conversationId, ignoreCase = true) }
        val index = conversations.indexOf(found)
        found?.let { convo ->
            // Found conversation
            // Update participant state
            val partFound = convo.participants.find { part -> part.user.userId.equals(participant.user.userId, ignoreCase = true) }
            partFound?.let {part ->
                val partIndex = convo.participants.indexOf(part)
                part.state = participant.state
                convo.participants.set(partIndex, part)
                conversations.set(index, convo)
            }
        }
    }

    private fun setEventHandler() {
        Messenger.addEventHandler(object:EventCallback() {
            override fun onConversationCreated(conversation: Conversation) {
//                Log.d(TAG, "onConversationCreated ${conversation}")
                addConversation(conversation)
            }

            override fun onConversationClosed(conversation: Conversation) {
//                Log.d(TAG, "onConversationClosed ${conversation}")
                removeConversation(conversation)
            }

            override fun onConversationModified(
                initiatingUser: User,
                title: String?,
                description: String?,
                conversationId: String
            ) {
                loadConversations()
            }

            override fun onParticipantStateChanged(participant: Participant, conversationId: String) {
                updateConversationParticipant(conversationId, participant)
                if(participant.user.userId.equals(prefs?.userId, ignoreCase = true)) {
                    if(participant.state != 20) loadConversations()
                }
            }

            override fun onAddedToConversation(
                initiatingUser: User,
                userId: String,
                conversationId: String
            ) {
                loadConversations()
            }

            override fun onRemovedFromConversation(
                initiatingUser: User,
                userId: String,
                conversationId: String
            ) {
                if(userId.equals(prefs?.userId, ignoreCase = true)) {
                    // Current user
                    removeFromConversations(conversationId)
                }
            }

            override fun onMessageReceived(message: Message) {
                addMessageToConversations(message)
            }

            override fun onMessageRejected(message: Message, errorMessage: String) {
            }

            override fun onMessageDeleted(message: Message) {
            }

            override fun onMessageUserReaction(message: Message) {
            }

            override fun onUserTypingStart(initiatingUserId: String) {
            }

            override fun onUserTypingStop(initiatingUserId: String) {
            }

            override fun onModeratorAdded(userId: String) {
            }

            override fun onModeratorRemoved(userId: String) {
            }
        })
    }

    private fun subscribeToConversations(conversations: MutableList<Conversation>) {
//        Log.d(TAG, "subscribeToConversations")
        val list = mutableListOf<String>()
        for (conversation in conversations) {
            val channel = "${conversation.tenantId}-${conversation.conversationId}".toUpperCase()
            if(!Messenger.subscribedChannels.contains(channel)) {
                //  tenant_id-conversation_id
                list.add(channel)
                Messenger.subscribedChannels.add(channel)
            }
        }
        if(list.size > 0) Messenger.subscribeToChannelList(list)
    }

    fun sendClick(conversation: Conversation, position: Int) {
        mClickListener?.OnItemClickListener(conversation, position)
        Messenger.unreadCallback.onNewUnread(Messenger.getUnreadCount())
    }

    // UI Screens
    fun setBackgroundColor(color: String) {
//        Log.d(TAG, "setBackgroundColor ${color}")
        rootView.setBackgroundColor(Color.parseColor(color))
    }

    fun setListCornerRadius(topLeft: Int, topRight: Int, bottomRight: Int, bottomLeft: Int) {
        val shape = getShape(topLeft.px.toFloat(), topRight.px.toFloat(), bottomRight.px.toFloat(), bottomLeft.px.toFloat(), "#FFFFFF")
        listFrame.background = shape
    }

    // Return shape drawable with corner radius and background color
    // radius in pixels
    // color in hex string #FFFFFF
    private fun getShape(topLeft: Float, topRight: Float, bottomRight: Float, bottomLeft: Float, color: String): ShapeDrawable {
        val shape = ShapeDrawable(RoundRectShape(floatArrayOf(topLeft, topLeft, topRight, topRight, bottomRight, bottomRight, bottomLeft, bottomLeft), null, null))
        shape.getPaint().setColor(Color.parseColor(color))
        return shape
    }

    // Extensions for dp-px conversion
    val Int.dp: Int
        get() = (this / Resources.getSystem().displayMetrics.density).toInt()
    val Int.px: Int
        get() = (this * Resources.getSystem().displayMetrics.density).toInt()

    private fun filterConversations(conversations: MutableList<Conversation>): MutableList<Conversation> {
        val filtered = mutableListOf<Conversation>()
        if(filterUserIds != null) {
            for(userId in filterUserIds!!) {
                for(conversation in conversations) {
                    val found = conversation.participants.find { p -> p.user.userId.equals(userId, ignoreCase = true) }
                    found?.let {
                        filtered.add(conversation)
                    }
                }
            }
            return filtered
        }
        return conversations
    }
}
