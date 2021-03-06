package com.koder.ellen.screen

import android.Manifest
import android.animation.*
import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.*
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RoundRectShape
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.view.animation.DecelerateInterpolator
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_DRAGGING
import androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_IDLE
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.koder.ellen.EventCallback
import com.koder.ellen.Messenger
import com.koder.ellen.Messenger.Companion.prefs
import com.koder.ellen.MessengerActivity
import com.koder.ellen.R
import com.koder.ellen.data.ConversationDataSource
import com.koder.ellen.data.ConversationRepository
import com.koder.ellen.data.MessageDataSource
import com.koder.ellen.data.MessageRepository
import com.koder.ellen.model.*
import com.koder.ellen.model.Message
import com.koder.ellen.ui.BaseViewModelFactory
import com.koder.ellen.ui.conversation.ConversationViewModel
import com.koder.ellen.ui.main.MainViewModel
import com.koder.ellen.ui.message.*
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import kotlinx.android.synthetic.main.fragment_message.*
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener
import net.yslibrary.android.keyboardvisibilityevent.util.UIUtil.hideKeyboard
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.lang.IndexOutOfBoundsException
import java.text.SimpleDateFormat
import java.util.*


open class MessageScreen : Fragment(),
    View.OnClickListener {

    companion object {
        fun newInstance() = MessageScreen()
        private const val TAG = "MessageScreen"
        private const val IMAGE_REQUEST = 2
        private const val MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 3
        private const val MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 4
        lateinit var viewAdapter: RecyclerView.Adapter<*>
//        lateinit var viewManager: RecyclerView.LayoutManager

        // Auto populate message
        const val AUTO_POPULATE_MSG = "AUTO_POPULATE_MSG"
        const val SEND_AUTO_POPULATE_MSG = "SEND_AUTO_POPULATE_MSG"

        const val ENABLE_MESSAGING = "ENABLE_MESSAGING"
    }

    // Interface for list adapter clicks
    interface ItemClickListener {
        fun onAvatarClick(view: View, user: User)
        fun onAvatarLongClick(view: View, user: User)
    }

    // Setting ItemClickListener will override the default functionality (currently sliding window)
    open fun setItemClickListener(listener: ItemClickListener) {
        itemClickListener = listener
    }

    private var conversationId: String? = null
    private var backgroundColor: String? = null
    private var cornerRadius: IntArray? = null

    // Bundle arguments
    private var enableMessaging: Boolean = true
    private var metadataFilterMap: HashMap<String, String>? = null
    private var filterKey: String? = null
    private var filterValue: String? = null

    private lateinit var rootView: View
    private lateinit var containerView: RelativeLayout
    private lateinit var viewModel: MainViewModel
    private lateinit var conversation: Conversation

    private var qrPublicId: String? = null
    private var created: Boolean = false

    //    private lateinit var mCompleteListener: OnLayoutCompleteListener
    private lateinit var listFrame: FrameLayout

    // RecyclerView
    private lateinit var recyclerView: RecyclerView

    //    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager
    val messages: MutableList<Message> = mutableListOf()
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    private var sendButtonDrawable: Drawable? = null
    private var imageButtonColor: String? = null

    private val messageViewModel: MessageViewModel by lazy {
        ViewModelProvider(this, BaseViewModelFactory {
            MessageViewModel(
                MessageRepository(MessageDataSource()),
                activity!!.application
            )
        }).get(MessageViewModel::class.java)
    }

    private val conversationViewModel: ConversationViewModel by lazy {
        ViewModelProvider(this, BaseViewModelFactory {
            ConversationViewModel(
                ConversationRepository(ConversationDataSource())
            )
        }).get(ConversationViewModel::class.java)
    }

    // Message chat input
    lateinit var messageSendButton: ImageView
    lateinit var addImageButton: ImageView
    lateinit var messageEditText: EditText

    // Mentions
    private lateinit var mentionViewAdapter: RecyclerView.Adapter<*>
    private lateinit var mentionViewManager: RecyclerView.LayoutManager
    private val mentionList: MutableList<User> = mutableListOf()
    private val participantsList: MutableList<User> = mutableListOf()

    // Zoom/Expanded view
    private lateinit var expandedImageView: ImageView
    private lateinit var expandedImageBg: ImageView
    private lateinit var appBar: AppBarLayout

    // Hold a reference to the current animator,
    // so that it can be canceled mid-way.
    private var currentAnimator: Animator? = null

    // The system "short" animation time duration, in milliseconds. This
    // duration is ideal for subtle animations or animations that occur
    // very frequently.
    private var shortAnimationDuration: Int = 0
    private var zoomOpen: Boolean = false
    private lateinit var startBounds: RectF
    private var startScale: Float = 0f
    private lateinit var thumbView: ImageView

    // User profile
    lateinit var slidingLayout: SlidingUpPanelLayout
    lateinit var slidingProfileImage: ImageView
    lateinit var slidingDisplayName: TextView
    private var itemClickListener: ItemClickListener? = null

    // User typing
    private val userTypingMap =
        hashMapOf<String, String>() // <User ID, Message.Metadata.localReferenceId GUID>

    // Status messages
    private val currentStatusMessages = mutableListOf<String>()
    private var messagesLoaded = false

    // Saving images
    private var requestPermissionUrl = ""

    // Media input
    private lateinit var mediaInputLayout: RelativeLayout
    private lateinit var mediaAdapter: RecyclerView.Adapter<*>
    private lateinit var mediaViewManager: RecyclerView.LayoutManager
    private val mediaList: MutableList<Message> = mutableListOf()

    // Auto populate message
    private var autoPopulateMessage: String? = null
    private var sendAutoPopulateMessage: Boolean = false

    // Reactions

    override fun onAttach(context: Context) {
        super.onAttach(context)
        setEventHandler()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Enable App Bar menu
        setHasOptionsMenu(true)

        // Always use the day (light) theme
//        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        conversationId = arguments?.getString("CONVERSATION_ID")
        qrPublicId = arguments?.getString("ADD_USER_ID")
        metadataFilterMap =
            arguments?.getSerializable("METADATA_FILTER") as HashMap<String, String>?

        // Filter
        if (metadataFilterMap != null) {
            filterKey = metadataFilterMap!!.keys.first()
            Log.d(TAG, "filterKey $filterKey")
            if (filterKey != null) filterValue = metadataFilterMap!![filterKey!!]
            Log.d(TAG, "filterValue $filterValue")
        }

        // Enable messaging
        if (arguments?.containsKey(ENABLE_MESSAGING)!!) enableMessaging =
            arguments?.getBoolean(ENABLE_MESSAGING)!!

        backgroundColor = arguments?.getString("BACKGROUND_COLOR")
        cornerRadius = arguments?.getIntArray("CORNER_RADIUS")

        // Set fragment conversation
        conversationId?.let {
            Messenger.currentConversationId = it
        }

        val found = Messenger.conversations.find { c ->
            c.conversationId.equals(
                conversationId,
                ignoreCase = true
            )
        }

        found?.let {
            Log.d(TAG, "found ${it}")
            conversation = it
            Messenger.currentConversationId = it.conversationId
            // Update unread count
            prefs?.setConversationLastRead(it.conversationId, System.currentTimeMillis())
            Messenger.unreadCallback?.onNewUnread(Messenger.getUnreadCount())
        }

        // Subscribe to conversation channel
        conversationId?.let {
            val channel = "${prefs?.tenantId}-${conversationId}".toUpperCase()
            Messenger.subscribeToChannelList(mutableListOf(channel))
        }

        // Auto populate message
        autoPopulateMessage = getArguments()?.getString(AUTO_POPULATE_MSG) ?: null
        sendAutoPopulateMessage = getArguments()?.getBoolean(SEND_AUTO_POPULATE_MSG) ?: false
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate Fragment's view
        rootView = inflater.inflate(R.layout.fragment_message, container, false)
        containerView = rootView.findViewById<RelativeLayout>(R.id.container)

        // Add SwipeRefreshLayout listener
        swipeRefreshLayout = rootView.findViewById(R.id.swipe_refresh_layout)
        swipeRefreshLayout.setOnRefreshListener {
            swipeRefreshLayout.setRefreshing(true)
            loadMessages(true)
        }

        slidingLayout = rootView.findViewById(R.id.sliding_layout)
        slidingProfileImage = rootView.findViewById(R.id.sliding_profile_image)
        slidingDisplayName = rootView.findViewById(R.id.sliding_display_name)

        // Chat input
        addImageButton = rootView.findViewById(R.id.add_image_btn)
        addImageButton.setOnClickListener(this)

        messageSendButton = rootView.findViewById(R.id.message_send_btn)
        messageSendButton.setOnClickListener(this)
        messageSendButton.isEnabled = false

        // Custom elements
        if (sendButtonDrawable != null) {
            messageSendButton.setImageDrawable(sendButtonDrawable)
        } else {
            DrawableCompat.setTint(messageSendButton.drawable, Color.parseColor("#1A73E8"))
        }

        imageButtonColor?.let {
            DrawableCompat.setTint(addImageButton.drawable, Color.parseColor(it))
        }

        messageEditText = rootView.findViewById(R.id.message_input)

        // Media input
        mediaInputLayout = rootView.findViewById(R.id.media_input_layout)

        // User start/stop typing
        val delay: Long = 4000
        var lastTextEdit: Long = 0
        val handler = Handler()
        var userTyping = false
        val inputFinishChecker = object : Runnable {
            override fun run() {
                if (System.currentTimeMillis() > (lastTextEdit + delay - 500) && userTyping) {
                    // User stopped typing
                    // Do stuff
//                    Log.d(TAG, "User stop typing")
                    userTyping = false
                    if(::conversation.isInitialized) {
                        prefs?.externalUserId?.let {
                            messageViewModel.typingStopped(it, conversation.conversationId)
                        }
                    }
                }
            }
        }

        messageEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!userTyping && s.toString().length > 0) {
                    Log.d(TAG, "User start typing")
                    userTyping = true
                    prefs?.externalUserId?.let {
                        if (::conversation.isInitialized) messageViewModel.typingStarted(
                            it,
                            conversation.conversationId
                        )
                    }
                }
                // Remove this to run only once
                handler.removeCallbacks(inputFinishChecker);
            }

            override fun afterTextChanged(s: Editable?) {
                // Avoid triggering event when text is empty
//                if (s.toString().length > 0) {
                lastTextEdit = System.currentTimeMillis()
                handler.postDelayed(inputFinishChecker, delay);
//                }

                messageViewModel.messageDataChanged(
                    messageEditText.text.toString(),    // Text input
                    mediaList                           // Media input
                )

                // Prevent infinite loop by unregistering and registering listener
                messageEditText.removeTextChangedListener(this)
                // Autocolor mentions
                val autocolored = autoColorMentions(s.toString())
//                messageEditText.setText(HtmlCompat.fromHtml(autocolored, HtmlCompat.FROM_HTML_MODE_COMPACT))
                s?.replace(
                    0,
                    s.length,
                    HtmlCompat.fromHtml(autocolored, HtmlCompat.FROM_HTML_MODE_COMPACT)
                )
                // Set cursor to the end of input
                messageEditText.setSelection(messageEditText.length())
                messageEditText.addTextChangedListener(this)
            }
        })

        // Zoom/Expanded View
        expandedImageView = rootView.findViewById(R.id.expanded_image)
        expandedImageBg = rootView.findViewById(R.id.expanded_image_bg)

        // Retrieve and cache the system's default "short" animation time.
        shortAnimationDuration = resources.getInteger(android.R.integer.config_shortAnimTime)

        // Prevent recreating when going back from Message Info
        if (!created) {
            qrPublicId?.let {
                // Message created by QR code
                Log.d(TAG, "Message created by QR code ${qrPublicId}")
                // Create a new conversation
                messageViewModel.createConversation(it)
                swipeRefreshLayout.setRefreshing(true)
                created = true  // TODO Run-once hack
            }
        }

        // Set AppBar for expanded images
//            appBar = (this).getAppBar()   // TODO UI Screens
        appBar = rootView.findViewById(R.id.appbar_layout)
        appBar.visibility = View.GONE

        // UI Screens
        listFrame = rootView.findViewById<FrameLayout>(R.id.list_frame)

        // Customizable UI options
        setBackgroundColor(Messenger.messageScreenBackgroundColor)
//        setListCornerRadius(Messenger.screenCornerRadius[0], Messenger.screenCornerRadius[1], Messenger.screenCornerRadius[2], Messenger.screenCornerRadius[3])

        // Enable messaging
        val messageInputFrame = rootView.findViewById<FrameLayout>(R.id.message_input_frame)
        if (!enableMessaging) messageInputFrame.visibility = View.GONE

        // Dark mode
        var color = "#FFFFFF"
        val mode = context?.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)
        when (mode) {
            Configuration.UI_MODE_NIGHT_YES -> {

                if (Messenger.messageScreenBackgroundColor == "#FFFFFF")
                    messageInputFrame.setBackgroundColor(activity?.resources!!.getColor(R.color.dmBackground))
                else
                    messageInputFrame.setBackgroundColor(Color.parseColor(Messenger.messageScreenBackgroundColor))

                val messageInputLayout =
                    rootView.findViewById<RelativeLayout>(R.id.message_input_layout)
                messageInputLayout.background =
                    activity?.resources!!.getDrawable(R.drawable.bg_round_border_message_dark)

                mediaInputLayout.background =
                    activity?.resources!!.getDrawable(R.drawable.bg_border_media_input_dark)

//                addImageButton.setColorFilter(ContextCompat.getColor(activity!!, R.color.dmTextHigh), android.graphics.PorterDuff.Mode.MULTIPLY);
                DrawableCompat.setTint(
                    addImageButton.drawable,
                    ContextCompat.getColor(activity!!, R.color.dmTextMed)
                )

                // SwipeRefreshLayout ProgressBar
                swipeRefreshLayout.setColorSchemeColors(activity?.resources!!.getColor(R.color.dmTextHigh))
                swipeRefreshLayout.setProgressBackgroundColorSchemeColor(
                    activity?.resources!!.getColor(
                        R.color.darkGray
                    )
                )

                // Sliding Profile view
                val dragView = rootView.findViewById<LinearLayout>(R.id.dragView)
                dragView.background = activity?.resources?.getDrawable(R.drawable.bg_round_top_dark)

                val dragHandle = rootView.findViewById<ImageView>(R.id.sliding_handle)
                DrawableCompat.setTint(
                    dragHandle.drawable,
                    ContextCompat.getColor(activity!!, R.color.dmTextDisabled)
                )

                val profileName = rootView.findViewById<TextView>(R.id.sliding_display_name)
                profileName.setTextColor(ContextCompat.getColor(activity!!, R.color.dmTextMed))
            }
        }

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val viewManager = MyLinearLayoutManager(view.context, LinearLayoutManager.VERTICAL, false)

        Log.d(TAG, "messages $messages");
        viewAdapter = MessageAdapter(view.context, messages, this@MessageScreen, itemClickListener)

        recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view).apply {

            // use a linear layout manager
            layoutManager = viewManager

            // specify an viewAdapter (see also next example)
            adapter = viewAdapter
        }

        // Initial X on down
        var downX = 0f
        // Moved-to X on drag
        var moveX = 0f
        // Store whether RecyclerView is scrolling/dragging vertically
        var draggingVertically = false
        var draggingHorizontally = false
        val timestampWidth = 200.px
//            Log.d(TAG, "timestampWidth ${timestampWidth}")

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                when (newState) {
                    SCROLL_STATE_DRAGGING -> {
//                            Log.d(TAG, "SCROLL_STATE_DRAGGING")
                        draggingVertically = true
                    }
                    SCROLL_STATE_IDLE -> {
//                            Log.d(TAG, "SCROLL_STATE_IDLE")
                        draggingVertically = false
                    }
                }
            }
        })

        recyclerView.addOnItemTouchListener(object : RecyclerView.OnItemTouchListener {
            override fun onTouchEvent(rv: RecyclerView, event: MotionEvent) {
                Log.d(TAG, "onItem onTouchEvent")   // Nada
            }

            override fun onInterceptTouchEvent(rv: RecyclerView, event: MotionEvent): Boolean {
//                    Log.d(TAG, "onInterceptTouchEvent")

                if (event.action == MotionEvent.ACTION_DOWN) {
                    downX = event.getX()
//                    Log.d(TAG, "downX ${downX}")
                }
                if (event.action == MotionEvent.ACTION_MOVE) {
                    moveX = event.getX()
//                        Log.d(TAG, "moveX ${moveX}")

                    val deltaX = Math.abs(downX - moveX)
//                    val deltaX = downX - moveX
//                    Log.d(TAG, "deltaX ${deltaX}")


                    if (!draggingVertically) {
                        // Not dragging
                        if (downX > moveX) {
                            // Swiping left

                            // Set horizontal drag status
                            draggingHorizontally = true

                            // Disable RecyclerView scrolling
//                            recyclerView.setLayoutFrozen(true)
                            (recyclerView.layoutManager as MyLinearLayoutManager).setScrollEnabled(
                                false
                            )

                            // Animate all messages
                            for (position in 0..messages.size) {
                                val view = recyclerView.getChildAt(position)
                                view?.let { v ->
                                    if (deltaX <= timestampWidth) {
                                        v.animate().translationX(-deltaX).setDuration(0)
                                    }
                                }
                            }
                        }
                    }
                }
                if (event.action == MotionEvent.ACTION_UP) {
                    // Enable RecyclerView scrolling
//                    recyclerView.setLayoutFrozen(false)
                    (recyclerView.layoutManager as MyLinearLayoutManager).setScrollEnabled(true)

                    // Animate all messages
                    for (position in 0..messages.size) {
                        val view = recyclerView.getChildAt(position)
                        view?.let { v ->
                            v.animate().translationX(0f).setDuration(100)
                        }
                    }

                    // Reset horizontal drag status
                    draggingHorizontally = false
                }
                return false
            }

            override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {
            }
        })

        // Load conversation
//            if(!::conversation.isInitialized) {
//                messageViewModel.getConversation(conversationId)
//            }

        // Auto populate message
        autoPopulateMessage?.let { msg ->
            if (!sendAutoPopulateMessage) messageEditText.setText(msg)
        }

        // Load Messages
        if (!messagesLoaded) {
            loadMessages()
            messagesLoaded = true
        }

        // Keyboard listener
        KeyboardVisibilityEvent.setEventListener(
            requireActivity(),
            object : KeyboardVisibilityEventListener {
                override fun onVisibilityChanged(isOpen: Boolean) {
//                Log.d(TAG, "Keyboard open ${isOpen}")
                    // Scroll to latest message
                    if (isOpen && messages.size > 0) recyclerView.smoothScrollToPosition(messages.size - 1)
                }
            })

        // Mentions
        mentionViewManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        val mentionViewAdapter = MessageMentionAdapter(activity as Context, mentionList, this)

        mention_recycler_view.setHasFixedSize(false)
        mention_recycler_view.layoutManager = mentionViewManager
        mention_recycler_view.adapter = mentionViewAdapter

        mention_recycler_view.addItemDecoration(
            DividerItemDecoration(
                mention_recycler_view.context,
                DividerItemDecoration.VERTICAL
            )
        )
        // Populate participants list
        if (::conversation.isInitialized) messageViewModel.participants.value =
            getParticipantsList(conversation)    // TODO UI Screens

        // Media input
//        private lateinit var mediaRecyclerView: RecyclerView
//        private lateinit var mediaAdapter: RecyclerView.Adapter<*>
//        private lateinit var mediaViewManager: RecyclerView.LayoutManager
//        private val mediaList: MutableList<Message> = mutableListOf()
        val mediaManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
//        for(i in 1..10) {
//            mediaList.add("Test")
//        }
        mediaAdapter = MediaAdapter(activity as Context, mediaList, this@MessageScreen)
        media_recycler_view.setHasFixedSize(true)
        media_recycler_view.layoutManager = mediaManager
        media_recycler_view.adapter = mediaAdapter

        // Observe messageFormState
        messageViewModel.messageFormState.observe(viewLifecycleOwner, Observer {
            val messageState = it ?: return@Observer

            messageSendButton.isEnabled = messageState.isDataValid
//            Log.d(TAG, "${messageState.isDataValid}")
//            addImageButton.visibility = if(messageState.isDataValid) View.GONE else View.VISIBLE
            addImageButton.visibility =
                if (messageEditText.text.isBlank()) View.VISIBLE else View.GONE
        })

        // Observer, Messages
        messageViewModel.messages.observe(viewLifecycleOwner, Observer {
            Log.d(TAG, "Messages changed")
            Log.d(TAG, "${it}")

            messages.clear()
            messages.addAll(it)
            viewAdapter.notifyDataSetChanged()
            recyclerView.scrollToPosition(messages.size - 1)
            swipeRefreshLayout.setRefreshing(false)

            // Send auto populated message
            autoPopulateMessage?.let {
                if (sendAutoPopulateMessage) {
                    messageEditText.setText(autoPopulateMessage)
                    if (::conversation.isInitialized) messageSendButton.performClick()
                }
            }
        })

        // Mentioned Participants
        messageViewModel.mentionedParticipants.observe(viewLifecycleOwner, Observer {
            mentionViewAdapter.setData(it.toSet().toList())
        })

        // Observer, Update Message
        // message:published
        // message:userReaction
        messageViewModel.message.observe(viewLifecycleOwner, Observer {
            Log.d(TAG, "Update ${it}")
            val message = messages.find { m -> m.messageId.equals(it.messageId, ignoreCase = true) }
            val index = messages.indexOf(message)
            messages.set(index, it)
            viewAdapter.notifyItemChanged(index)
//            viewAdapter.notifyDataSetChanged()
        })

        // Observer, Reported Message
        messageViewModel.reportedMessage.observe(viewLifecycleOwner, Observer {
            Toast.makeText(
                activity,
                "Message reported",
                Toast.LENGTH_LONG
            ).show()
        })

        // Observer, Deleted Message
        messageViewModel.deletedMessage.observe(viewLifecycleOwner, Observer {
            deleteMessageFromList(it)
            Toast.makeText(
                activity,
                "Message deleted",
                Toast.LENGTH_LONG
            ).show()
        })

        // Observer, Conversation
        messageViewModel.conversation.observe(viewLifecycleOwner, Observer {
            Log.d(TAG, "Conversation created ${it}")
            conversation = it
            conversationId = conversation.conversationId

            // Populate participants list for Mentions
            messageViewModel.participants.value = getParticipantsList(it)

            // Subscribe to channel as needed
            val channel = "${prefs?.tenantId}-${it.conversationId}".toUpperCase()
            Messenger.subscribeToChannelList(mutableListOf(channel))

            Messenger.currentConversationId = it.conversationId

            swipeRefreshLayout.setRefreshing(false)
        })

        messageViewModel.updateConversation.observe(viewLifecycleOwner, Observer {
            conversation = it
        })
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
//        menu.clear()
//        inflater.inflate(R.menu.message_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean { // Handle presses on the action bar items
        return when (item.itemId) {
            android.R.id.home -> {
                Log.d(TAG, "popBackStack")
                activity?.supportFragmentManager?.popBackStack()
                true
            }
//            R.id.action_info -> {
//                // Hide keyboard
//                hideKeyboard(activity)
//                // Show MessageInfoFragment and add to backstack
//                (activity as MessengerActivity).showInfoFragment()
//                true
//            }
//            R.id.action_video -> {
//                hideKeyboard(activity)
//                // Start VideoFragment
//                (activity as MessengerActivity).showVideoFragment()
//                true
//            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.message_send_btn -> {
                if (!::conversation.isInitialized) return

                if (mediaList.size > 0) {
                    // Send media messages
                    for (message in mediaList) {
                        // Validate if allowed to send client-side
                        if (!allowedToSend()) {
                            message.metadata.error = true
                        }

                        if (allowedToSend()) messageViewModel.addImage(
                            message,
                            Uri.parse(message.media?.content?.source)
                        )

                        // Add media message as self-message
                        addMessageToMessages(message)
                    }

                    // Remove media from media input list
                    val removeList = mutableListOf<Message>()
                    removeList.addAll(mediaList)
                    for (message in removeList) {
                        removeMediaItem(message)
                    }
                }

                if (messageEditText.text.isNotBlank()) {
                    val text: String = messageEditText.text.toString()

                    messageEditText.setText("")

                    // Add Message to UI
                    val sender = User(
                        tenantId = prefs?.tenantId!!,
                        userId = prefs?.externalUserId!!,
                        displayName = prefs?.currentUser?.profile?.displayName!!,
                        profileImageUrl = prefs?.currentUser?.profile?.profileImageUrl!!
                    )

                    val setOfMentions = getSetOfMentions(text)
                    var mentions = mutableListOf<Mention>()

                    for (user in setOfMentions) {
                        val mention =
                            Mention(user = user, mentionTextPattern = "@${user.displayName}")
                        mentions.add(mention)
                    }

                    val metadata = MessageMetadata(localReferenceId = UUID.randomUUID().toString())
                    if (filterKey != null && filterValue != null) {
                        when (filterKey) {
                            "classId" -> metadata.classId = filterValue as String
                        }
                    }

                    val message = Message(
                        conversationId = conversation.conversationId,
                        body = text,
                        sender = sender,
                        metadata = metadata,
                        mentions = mentions
                    )
                    Log.d(TAG, "ifAllowedToSend() ${allowedToSend()}")

                    // Validate if allowed to send client-side
                    if (!allowedToSend()) {
                        message.metadata.error = true
                    }

                    // Add to Messages, self message
                    addMessageToMessages(message)
                    Log.d(TAG, "message_send_btn ${message}")  // messageId=null

                    if (allowedToSend()) messageViewModel.send(message)
                }

                true
//                }
            }
            R.id.add_image_btn -> {
//                Log.d(TAG, "Start photo picker")
//                startPhotoPicker()
                checkReadPermissions()  // Start photo picker thereafter
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)

        Log.d(TAG, "onActivityResult")
        Log.d(TAG, "${requestCode}")
        Log.d(TAG, "${resultCode}")
        Log.d(TAG, "${data}")

        // Check which request it is that we're responding to
        if (requestCode == IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            // Image selected
            Log.d(TAG, "Image selected")
            val imageUri = data.getData()
            Log.d(TAG, "${imageUri}")

            imageUri?.let {
                // Add image to media input layout
                // Make layout visible
                mediaInputLayout.visibility = View.VISIBLE

//                for(i in 0..10) {
                val mediaMessage = buildMediaMessage(it)
                if(mediaMessage != null) {
                    mediaList.add(mediaMessage)
                    mediaAdapter.notifyItemInserted(mediaList.size - 1)

                    // Add media to layout
                    // Scroll to latest media item
                    media_recycler_view.scrollToPosition(mediaList.size - 1)

                    messageViewModel.messageDataChanged(
                        messageEditText.text.toString(),    // Text input
                        mediaList                           // Media input
                    )
                }
            }
        }

        if (resultCode != RESULT_OK) {
            return
        }
    }

    fun removeMediaItem(message: Message) {
        val index = mediaList.indexOf(message)
        if (index > -1) {
            mediaList.removeAt(index)
            mediaAdapter.notifyItemRemoved(index)
        }

        if (mediaList.size == 0) {
            mediaInputLayout.visibility = View.GONE
        }

        messageViewModel.messageDataChanged(
            messageEditText.text.toString(),    // Text input
            mediaList                           // Media input
        )
    }

    private fun buildMediaMessage(imageUri: Uri): Message? {
        if(!::conversation.isInitialized) {
            return null
        }
        // Build Message
        val contentType = activity?.contentResolver?.getType(imageUri)
        val sender = User(
            tenantId = prefs?.tenantId!!,
            userId = prefs?.externalUserId!!,
            displayName = prefs?.currentUser?.profile?.displayName!!,
            profileImageUrl = prefs?.currentUser?.profile?.profileImageUrl!!
        )
        val conversationMedia = ConversationMedia(
            content = ConversationMediaItem(mimeType = contentType!!, source = imageUri.toString()),
            thumbnail = ConversationMediaItem(
                mimeType = contentType!!,
                source = imageUri.toString()
            )
        )
        val message = Message(
            conversationId = conversation.conversationId,
            body = "Sent an image",
            sender = sender,
            metadata = MessageMetadata(localReferenceId = UUID.randomUUID().toString()),
            media = conversationMedia
        )
        return message
    }

    override fun onDetach() {
        super.onDetach()
//        Log.d(TAG, "onDetach()")
        hideKeyboard(requireActivity())
    }

    private fun startPhotoPicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.setType("image/*")
        startActivityForResult(intent, IMAGE_REQUEST)
    }

    private fun checkReadPermissions() {
        // Check permissions
        // Here, thisActivity is the current activity
//        if (ContextCompat.checkSelfPermission(activity as MessengerActivity,
        if (ContextCompat.checkSelfPermission(
                activity as AppCompatActivity,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
            != PackageManager.PERMISSION_GRANTED
        ) {

            // Permission is not granted
            // Should we show an explanation?
//            if (ActivityCompat.shouldShowRequestPermissionRationale(activity as MainActivity,
//                    Manifest.permission.READ_CONTACTS)) {
//                // Show an explanation to the user *asynchronously* -- don't block
//                // this thread waiting for the user's response! After the user
//                // sees the explanation, try again to request the permission.
//            } else {
            // No explanation needed, we can request the permission.
//                ActivityCompat.requestPermissions(activity as MainActivity,
//                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
//                    MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE)
            requestPermissions(
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE
            )
            // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
            // app-defined int constant. The callback method gets the
            // result of the request.
//            }
        } else {
            // Permission has already been granted
            startPhotoPicker()
        }
    }

    private fun getConversationTitle(): String {
        val currentConversation = (activity as MessengerActivity).getCurrentConversation()
        if (currentConversation != null && !currentConversation.title.isNullOrBlank()) {
            return currentConversation.title
        }

        if (!::conversation.isInitialized) {
            return ""
        }

        if (conversation.title.isNullOrBlank()) {
            return getTitleByParticipants(conversation!!.participants)
        }

        return conversation.title
    }

    // Return participants in format Participant1, Participant2, ...
    private fun getTitleByParticipants(participants: MutableList<Participant>): String {
        var title = ""

        // If the only participant is the sender (myself)
        if (participants.size == 1 && participants.first().user.userId.equals(
                prefs?.externalUserId,
                ignoreCase = true
            )
        )
            return "Me"

        for (participant in participants) {
            if (participant.user.displayName.equals(
                    prefs?.currentUser?.profile?.displayName,
                    ignoreCase = true
                )
            ) continue
            if (title.isEmpty()) {
                title += participant.user.displayName
                continue
            }
            title += ", ${participant.user.displayName}"
        }
        return title
    }

    // Load Messages
    private fun loadMessages(forceLoad: Boolean = false) {
//        Log.d(TAG, "loadMessages ${::conversation.isInitialized}")
        Log.d(TAG, "loadMessages")
        if (::conversation.isInitialized) {
            if (!conversation.conversationId.isNullOrBlank()) {
                swipeRefreshLayout.isRefreshing = true
                messageViewModel.getMessages(conversation.conversationId, forceLoad)
            }
        } else if (!conversationId.isNullOrBlank()) {
            swipeRefreshLayout.isRefreshing = true
            messageViewModel.getMessages(conversationId!!, forceLoad)
        }
    }

    fun addMessage(message: Message) {
        if (!message.sender.userId.equals(prefs?.externalUserId, ignoreCase = true)) {
            // Add message from others
            addMessageToMessages(message)
        } else {
            // Update my message
            updateSelfMessage(message)
        }
    }

    private fun addMessageToMessages(message: Message) {
        Log.d(TAG, "addMessageToMessages ${message}")
        // Format timeCreated
        if (message.timeCreated.toString().contains("-")) {
            message.timeCreated = convertDateToLong(message.timeCreated.toString())
        }

        // Someone else's message
        messages.add(message)

        // Sort Messages in case message:published is not in order
//        var sorted = sortByTimeCreated(messages)
//        Log.d(TAG, "${sorted}")

//        messages.clear()
//        messages.addAll(sorted)

        val position = messages.indexOf(message)

        viewAdapter.notifyItemInserted(position)
        // Notify item before current message position
        if (position > 0)
            viewAdapter.notifyItemChanged(position - 1)
        // Notify item after current message position
        if (position < messages.size - 2)
            viewAdapter.notifyItemChanged(position + 1)

//        viewAdapter.notifyDataSetChanged()
        recyclerView.scrollToPosition(messages.size - 1)
    }

    fun convertDateToLong(date: String): Long {
        val df = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        df.setTimeZone(TimeZone.getTimeZone("UTC"))
        return df.parse(date).time
    }

    fun sortByTimeCreated(messages: MutableList<Message>): MutableList<Message> {
        val sorted = messages.sortedWith(object : Comparator<Message> {
            override fun compare(m1: Message, m2: Message): Int {
                return (m1.timeCreated!!.toLong() - m2.timeCreated!!.toLong()).toInt()
            }
        })
        return sorted.toMutableList()
    }

    private fun updateSelfMessage(message: Message) {
        Log.d(TAG, "updateSelfMessage ${message}")
//        message.metadata.localReferenceId  // Delivered
        val found = messages.find {
            it.metadata.localReferenceId.equals(
                message.metadata.localReferenceId,
                ignoreCase = true
            )
        }
        found?.let {
            Log.d(TAG, "found ${found}")
            val index = messages.indexOf(found)

            // Format timeCreated date
            if (message.timeCreated.toString().contains("-")) {
                message.timeCreated = convertDateToLong(message.timeCreated.toString())
            }

            // Update previous Delivered status
            val lastFound = messages.findLast {
                it.messageId != null && it.sender.userId.equals(
                    message.sender.userId,
                    ignoreCase = true
                )
            }
            val lastIndex = messages.indexOf(lastFound)

            messages.set(index, message)

            // Update previous Delivered status
            viewAdapter.notifyItemChanged(lastIndex, Unit)
            // Update current Delivered status
            viewAdapter.notifyItemChanged(index, Unit)

//            if(messages.size > 0) recyclerView.smoothScrollToPosition(messages.size - 1)
//            Log.d(TAG, "index ${index}")
//            Log.d(TAG, "messages.size-1 ${messages.size-1}")
            // Scroll down a little to show Delivered text
            if (index == messages.size - 1) recyclerView.smoothScrollToPosition(messages.size - 1)
        }

    }

    // Mentions
    // Participant mentioned
    fun mentionUser(view: View, user: User) {
        Log.d(TAG, "Mention ${user}")

        // Reset mention view
        messageViewModel.mentionedParticipants.value = listOf()

        // Auto-complete selected name in message input
        val messageText = message_input.text
        val words = messageText.split(" ").toMutableList()
        // Set latest mention with a space at the end
        words.set(words.size - 1, "@${user.displayName}")
        Log.d(TAG, "${words}")
        // Set color for mentioned names
        words.forEachIndexed { index, word ->
            if (word.first().equals('@', ignoreCase = true)) {
                // @
                val name = word.substring(1).trim()

                val found = participantsList.find { it.displayName.equals(name, ignoreCase = true) }
                found?.let {
//                    words.set(index, "<font color='#1A73E9'>${word}</font>")
                    words.set(index, "<font color='${Messenger.mentionInputColor}'>${word}</font>")
                }
            }
        }

        val joined = words.joinToString(separator = " ")
        message_input.setText(HtmlCompat.fromHtml("${joined} ", HtmlCompat.FROM_HTML_MODE_COMPACT))

        // Set cursor to the end of input
        message_input.setSelection(message_input.length())
    }

    // Get list of mentioned users
    private fun getSetOfMentions(text: String): MutableSet<User> {
        val setOfMentions = mutableSetOf<User>()
        val words = text.split(" ").toMutableList()
        words.forEach { word ->
            if (word.isNotEmpty() && word.first()
                    .equals('@', ignoreCase = true) && word.length > 1
            ) {
                val name = word.substring(1)
                if(::conversation.isInitialized) {
                    val found = conversation.participants.find {
                        it.user.displayName.equals(
                            name,
                            ignoreCase = true
                        )
                    }
                    found?.let {
                        setOfMentions.add(found.user)
                    }
                }
            }
        }

        return setOfMentions
    }

    private fun autoColorMentions(text: String): String {
        val words = text.split(" ").toMutableList()
//        Log.d(TAG, "words ${words}")
        // Set color for mentioned names
        words.forEachIndexed { index, word ->
            if (word.length > 1 && word.first().equals('@', ignoreCase = true)) {
                // @
                val name = word.substring(1)
                if(::conversation.isInitialized) {
                    val found = conversation.participants.find {
                        it.user.displayName.equals(
                            name,
                            ignoreCase = true
                        )
                    }
                    found?.let {
                        words.set(
                            index,
                            "<font color='${Messenger.mentionInputColor}'>${word}</font>"
                        )
                    }
                }
            }
        }
        return words.joinToString(separator = " ")
    }

    private fun getParticipantsList(conversation: Conversation): MutableList<User> {
        val list = mutableListOf<User>()
        for (participant in conversation.participants) {
            list.add(participant.user)
        }
        return list
    }

    fun showExpandedImage(view: ImageView, url: String) {
        Log.d(TAG, "Show expanded image")
        Log.d(TAG, "${url}")

        thumbView = view

        val bitmap = (view.drawable as BitmapDrawable).bitmap
        zoomImageFromThumb(view, bitmap, url)
    }

    //    private fun zoomImageFromThumb(thumbView: View, imageResId: Int) {
//    private fun zoomImageFromThumb(thumbView: View, url: String) {
    private fun zoomImageFromThumb(thumbView: View, bitmap: Bitmap, url: String) {
        hideKeyboard(requireActivity())

        // Handle back press
        zoomOpen = true

        // If there's an animation in progress, cancel it
        // immediately and proceed with this one.
        currentAnimator?.cancel()

        // Load the high-resolution "zoomed-in" image.
//        val expandedImageView: ImageView = findViewById(R.id.expanded_image)
        expandedImageView.setImageBitmap(bitmap)

        // Calculate the starting and ending bounds for the zoomed-in image.
        // This step involves lots of math. Yay, math.
        val startBoundsInt = Rect()
        val finalBoundsInt = Rect()
        val globalOffset = Point()

        // The start bounds are the global visible rectangle of the thumbnail,
        // and the final bounds are the global visible rectangle of the container
        // view. Also set the container view's offset as the origin for the
        // bounds, since that's the origin for the positioning animation
        // properties (X, Y).
        thumbView.getGlobalVisibleRect(startBoundsInt)
        rootView.findViewById<View>(R.id.container)
            .getGlobalVisibleRect(finalBoundsInt, globalOffset)
//        activity!!.findViewById<View>(R.id.container_content)
//                .getGlobalVisibleRect(finalBoundsInt, globalOffset)
        startBoundsInt.offset(-globalOffset.x, -globalOffset.y)
        finalBoundsInt.offset(-globalOffset.x, -globalOffset.y)

//        val startBounds = RectF(startBoundsInt)
        val finalBounds = RectF(finalBoundsInt)
        startBounds = RectF(startBoundsInt)
//        finalBounds = RectF(finalBoundsInt)

        // Adjust the start bounds to be the same aspect ratio as the final
        // bounds using the "center crop" technique. This prevents undesirable
        // stretching during the animation. Also calculate the start scaling
        // factor (the end scaling factor is always 1.0).
//        val startScale: Float
        if ((finalBounds.width() / finalBounds.height() > startBounds.width() / startBounds.height())) {
            // Extend start bounds horizontally
            startScale = startBounds.height() / finalBounds.height()
            val startWidth: Float = startScale * finalBounds.width()
            val deltaWidth: Float = (startWidth - startBounds.width()) / 2
            startBounds.left -= deltaWidth.toInt()
            startBounds.right += deltaWidth.toInt()
        } else {
            // Extend start bounds vertically
            startScale = startBounds.width() / finalBounds.width()
            val startHeight: Float = startScale * finalBounds.height()
            val deltaHeight: Float = (startHeight - startBounds.height()) / 2f
            startBounds.top -= deltaHeight.toInt()
            startBounds.bottom += deltaHeight.toInt()
        }

        // Hide the thumbnail and show the zoomed-in view. When the animation
        // begins, it will position the zoomed-in view in the place of the
        // thumbnail.
        thumbView.alpha = 0f
        expandedImageView.visibility = View.VISIBLE
        expandedImageBg.visibility = View.VISIBLE

        // Set the pivot point for SCALE_X and SCALE_Y transformations
        // to the top-left corner of the zoomed-in view (the default
        // is the center of the view).
        expandedImageView.pivotX = 0f
        expandedImageView.pivotY = 0f

        // Construct and run the parallel animation of the four translation and
        // scale properties (X, Y, SCALE_X, and SCALE_Y).
        currentAnimator = AnimatorSet().apply {
            play(
                ObjectAnimator.ofFloat(
                    expandedImageView,
                    View.X,
                    startBounds.left,
                    finalBounds.left
                )
            ).apply {
                with(
                    ObjectAnimator.ofFloat(
                        expandedImageView,
                        View.Y,
                        startBounds.top,
                        finalBounds.top
                    )
                )
                with(ObjectAnimator.ofFloat(expandedImageView, View.SCALE_X, startScale, 1f))
                with(ObjectAnimator.ofFloat(expandedImageView, View.SCALE_Y, startScale, 1f))
            }
            duration = shortAnimationDuration.toLong()
            interpolator = DecelerateInterpolator()
            addListener(object : AnimatorListenerAdapter() {

                override fun onAnimationEnd(animation: Animator) {
                    currentAnimator = null

                    // Load full quality image
                    Log.d(TAG, "Load full quality image ${url}")
                    // Create the downloader for Picasso to use
//                    Picasso.get().setLoggingEnabled(true)
                    val displayMetrics = DisplayMetrics()
                    activity!!.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics)
                    val width = displayMetrics.widthPixels
                    Log.d(TAG, "screen width ${width}")

                    Picasso.get().load(url).resize(width, 0).into(object : Target {
                        override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
                        }

                        override fun onBitmapFailed(
                            e: java.lang.Exception?,
                            errorDrawable: Drawable?
                        ) {
                        }

                        override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                            try {
                                bitmap?.let {
                                    expandedImageView.setImageBitmap(bitmap)
                                    Log.d(TAG, "Full quality image set")
                                }
                            } catch (e: Exception) {
                            }
                        }
                    })
                }

                override fun onAnimationCancel(animation: Animator) {
                    currentAnimator = null
                    closeExpandedView()
                }
            })
            start()
        }

        // Fade background
        val anim = ValueAnimator()
        anim.setIntValues(Color.TRANSPARENT, Color.WHITE)
        anim.setEvaluator(ArgbEvaluator())
        anim.addUpdateListener(ValueAnimator.AnimatorUpdateListener() {
            expandedImageBg.setBackgroundColor(it.getAnimatedValue() as Int)
        })
        anim.duration = shortAnimationDuration.toLong()
        anim.start()

        // AppBar
        // Animate the loading view to 0% opacity. After the animation ends,
        // set its visibility to GONE as an optimization step (it won't
        // participate in layout passes, etc.)
        appBar.animate()
            .alpha(0f)
            .setDuration(shortAnimationDuration.toLong())
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
//                        appBar.visibility = View.GONE
//                    appBar.visibility = View.INVISIBLE
                }
            })

        // Upon clicking the zoomed-in image, it should zoom back down
        // to the original bounds and show the thumbnail instead of
        // the expanded image.
        expandedImageView.setOnClickListener {
            currentAnimator?.cancel()

            // Animate the four positioning/sizing properties in parallel,
            // back to their original values.
            currentAnimator = AnimatorSet().apply {
                closeExpandedView()
            }
        }
    }

//    override fun onBackPressed() {
////        super.onBackPressed()
//        Log.d(TAG, "Back pressed")
//        if(zoomOpen) {
//            closeExpandedView()
//            return
//        }
//        super.onBackPressed()
//    }

    private fun closeExpandedView() {
        zoomOpen = false
        currentAnimator?.cancel()

        // Animate the four positioning/sizing properties in parallel,
        // back to their original values.
        currentAnimator = AnimatorSet().apply {
            play(ObjectAnimator.ofFloat(expandedImageView, View.X, startBounds.left)).apply {
                with(ObjectAnimator.ofFloat(expandedImageView, View.Y, startBounds.top))
                with(ObjectAnimator.ofFloat(expandedImageView, View.SCALE_X, startScale))
                with(ObjectAnimator.ofFloat(expandedImageView, View.SCALE_Y, startScale))
            }
            duration = shortAnimationDuration.toLong()
            interpolator = DecelerateInterpolator()
            addListener(object : AnimatorListenerAdapter() {

                override fun onAnimationEnd(animation: Animator) {
                    thumbView.alpha = 1f
                    expandedImageView.visibility = View.GONE
                    expandedImageBg.visibility = View.GONE
                    currentAnimator = null
                }

                override fun onAnimationCancel(animation: Animator) {
                    thumbView.alpha = 1f
                    expandedImageView.visibility = View.GONE
                    expandedImageBg.visibility = View.GONE
                    currentAnimator = null
                }
            })
            start()
        }

        // Expanded image background, fade in
        val anim = ValueAnimator()
        anim.setIntValues(Color.WHITE, Color.TRANSPARENT)
        anim.setEvaluator(ArgbEvaluator())
        anim.addUpdateListener(ValueAnimator.AnimatorUpdateListener() {
            expandedImageBg.setBackgroundColor(it.getAnimatedValue() as Int)
        })
        anim.duration = shortAnimationDuration.toLong()
        anim.start()

        // AppBar, fade in
        appBar.apply {
            // Set the content view to 0% opacity but visible, so that it is visible
            // (but fully transparent) during the animation.
            alpha = 0f
//            visibility = View.VISIBLE
            visibility = View.GONE

            // Animate the content view to 100% opacity, and clear any animation
            // listener set on the view.
            animate()
                .alpha(1f)
                .setDuration(shortAnimationDuration.toLong())
                .setListener(null)
        }
    }

    // BottomSheetDialog actions
    fun setReaction(message: Message, reactionCode: String) {
//        Log.d(TAG, "sendReaction ${message}")
//        Log.d(TAG, "reaction ${reaction}")
        messageViewModel.setReaction(message, reactionCode)
    }

    open fun setMessageReactions() {

    }

    // Update message with Reaction
    fun updateMessage(message: Message) {
        messageViewModel.updateMessage(message)
    }

    fun copyText(message: Message, view: View) {
        val myClipboard = activity?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val myClip: ClipData = ClipData.newPlainText("note_copy", message.body)
        myClipboard.setPrimaryClip(myClip)
        Toast.makeText(
            activity,
            "Text copied",
            Toast.LENGTH_LONG
        ).show()
    }

    fun reportMessage(message: Message, view: View) {
        messageViewModel.reportMessage(message)
    }

    // Delete request to platform API
    fun deleteMessage(message: Message, view: View) {
        messageViewModel.deleteMessage(message)
    }

    fun deleteMessageFromList(message: Message) {
        val found = messages.find { it.messageId.equals(message.messageId, ignoreCase = true) }
        found?.let {
            val index = messages.indexOf(found)
            messages.removeAt(index)
            Messenger.deleteMessage(found)
            viewAdapter.notifyItemRemoved(index)
            Log.d(TAG, "Deleted ${message}")
        }
    }

    fun saveMedia(message: Message, view: View) {
        val url = message.media?.content?.source

        Log.d(TAG, "Save ${url}")

        requestPermissionUrl = url!!

        // Check permissions
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(
                activity as AppCompatActivity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            != PackageManager.PERMISSION_GRANTED
        ) {

            // Permission is not granted
            // Should we show an explanation?
//            if (ActivityCompat.shouldShowRequestPermissionRationale(activity as MainActivity,
//                    Manifest.permission.READ_CONTACTS)) {
//                // Show an explanation to the user *asynchronously* -- don't block
//                // this thread waiting for the user's response! After the user
//                // sees the explanation, try again to request the permission.
//            } else {
            // No explanation needed, we can request the permission.
//                ActivityCompat.requestPermissions(activity as MainActivity,
//                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
//                    MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE)
            requestPermissions(
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE
            )
            // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
            // app-defined int constant. The callback method gets the
            // result of the request.
//            }
        } else {
            // Permission has already been granted
            getImageWithGlide(url!!)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    Log.d(TAG, "Write permission granted")
                    if (requestPermissionUrl.isNotBlank()) getImageWithGlide(requestPermissionUrl)
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return
            }
            MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    Log.d(TAG, "Read permission granted")
                    startPhotoPicker()
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return
            }
            // Add other 'when' lines to check for other
            // permissions this app might request.
            else -> {
                // Ignore all other requests.
            }
        }
    }

    fun getImageWithGlide(url: String) {
        Glide.with(this)
            .asBitmap()
            .load(url)
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(bitmap: Bitmap, transition: Transition<in Bitmap>?) {
//                    imageView.setImageBitmap(resource)
                    saveImageToFile(bitmap, url.toString())
//                    createDirectoryAndSaveFile(bitmap, url.toString())
                    Toast.makeText(
                        activity,
                        "Photo saved",
                        Toast.LENGTH_LONG
                    ).show()
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                    // this is called when imageView is cleared on lifecycle call or for
                    // some other reason.
                    // if you are referencing the bitmap somewhere else too other than this imageView
                    // clear it here as you can no longer have the bitmap
                }
            })
    }

    // Saves image media to Environment.DIRECTORY_PICTURES + File.separator + "Ellen"
    fun saveImageToFile(bitmap: Bitmap, url: String) {
        val uri = Uri.parse(url)
        val file = File(uri.toString())
        val mimeType = (activity as AppCompatActivity).getContentResolver().getType(uri)

        var fos: OutputStream? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Log.d(TAG, ">= Build Q")
//            val resolver = (activity as MessengerActivity).getContentResolver()
            val resolver = (activity as AppCompatActivity).getContentResolver()
            val contentValues = ContentValues()
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, file.name)
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
            contentValues.put(
                MediaStore.MediaColumns.RELATIVE_PATH,
                Environment.DIRECTORY_PICTURES + File.separator + "Ellen"
            )
            val imageUri =
                resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            fos = resolver.openOutputStream(Objects.requireNonNull(imageUri!!))
        } else {
            Log.d(TAG, "< Build Q")
            createDirectoryAndSaveFile(bitmap, url)
            return
        }
        bitmap.compress(Bitmap.CompressFormat.JPEG, 88, fos)
        Objects.requireNonNull(fos)?.close()
    }

    private fun createDirectoryAndSaveFile(
        imageToSave: Bitmap,
        url: String
    ) {
        val uri = Uri.parse(url)
        val f = File(uri.toString())
        val direct = File(
            (activity as AppCompatActivity).getExternalFilesDir(null)?.absolutePath + "/Pictures/Ellen"
        )
        if (!direct.exists()) {
            val wallpaperDirectory = File("/sdcard/Pictures/Ellen/")
            wallpaperDirectory.mkdirs()
        }
        val file = File("/sdcard/Pictures/Ellen/", f.name)
        if (file.exists()) {
            file.delete()
        }
        try {
            val out = FileOutputStream(file)
            imageToSave.compress(Bitmap.CompressFormat.JPEG, 88, out)
            out.flush()
            out.close()

            MediaScannerConnection.scanFile(
                activity as AppCompatActivity,
                arrayOf(file.getPath()),
                arrayOf("image/jpeg"),
                null
            );

        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    fun showMessageError(localReferenceId: String, errorMessage: String) {
        Log.d(TAG, "localReferenceId ${localReferenceId}")
        Log.d(TAG, "errorMessage ${errorMessage}")
        val found = messages.find {
            it.metadata.localReferenceId.equals(
                localReferenceId,
                ignoreCase = true
            )
        }
        found?.let {
            val index = messages.indexOf(found)
            found.metadata.error = true
            found.metadata.errorMessage = errorMessage
            Log.d(TAG, "showMessageError ${found}")

            // Add error message to db
            found.messageId = localReferenceId
            Messenger.addMessage(found)

            viewAdapter.notifyItemChanged(index)

            // Scroll down a little to show status indicator icon
            if (index == messages.size - 1) recyclerView.smoothScrollToPosition(messages.size - 1)
        }
    }

    // state
    // 0 = default
    // 10 = silenced
    // 20 = banned
    private fun allowedToSend(): Boolean {
//        val conversation = (activity as MessengerActivity).getCurrentConversation()   // TODO UI Screens
//        conversation?.let {
//            // Current conversation
//            val participant = conversation.participants.find { it.user.userId.equals(prefs?.externalUserId, ignoreCase = true) }
//            participant?.let {
//                // Current participant
//                return participant.state == 0
//            }
//        }
//        return false // TODO UI Screens
        return true // for now, TODO UI Screens
    }

    // User profile (slide-up panel)
    fun showProfile(user: User) {
        Log.d(TAG, "showProfile ${user}")
        slidingDisplayName.text = user.displayName
        slidingProfileImage.setImageResource(R.drawable.ic_account_circle_24dp)
        Picasso.get().load(user.profileImageUrl).into(slidingProfileImage)
        slidingLayout.panelState = SlidingUpPanelLayout.PanelState.EXPANDED
    }

    fun userStartTyping(initiatingUserId: String) {
        Log.d(TAG, "userStartTyping ${initiatingUserId}")

        if(!::conversation.isInitialized) {
            return
        }

        val displayName = getDisplayName(initiatingUserId) + " is typing..."
        val profileImageUrl = getProfileImageUrl(initiatingUserId)
        val localReferenceId = UUID.randomUUID().toString()
        val sender = User(
            tenantId = prefs?.tenantId!!,
            userId = initiatingUserId,
            displayName = displayName,
            profileImageUrl = ""
        )
        val message = Message(
            conversationId = conversation.conversationId,
            body = "",
            sender = sender,
            metadata = MessageMetadata(localReferenceId = localReferenceId)
        )

        if (!userTypingMap.containsKey(initiatingUserId)) {
            // Store reference of <initiatingUserId, localReferenceId>
            userTypingMap.set(initiatingUserId, localReferenceId)

            // Show message
            messages.add(message)
            val index = messages.indexOf(message)
            viewAdapter.notifyItemInserted(index)
            if (index == messages.size - 1) recyclerView.smoothScrollToPosition(index)
        }
    }

    fun userStopTyping(initiatingUserId: String) {
//        Log.d(TAG, "userStopTyping ${initiatingUserId}")

        if (userTypingMap.containsKey(initiatingUserId)) {
            // Find message
            val localReferenceId = userTypingMap.get(initiatingUserId)
            localReferenceId?.let {
                val message = messages.find { m ->
                    m.metadata.localReferenceId.equals(
                        localReferenceId,
                        ignoreCase = true
                    )
                }
                message?.let {
                    val index = messages.indexOf(message)
                    messages.remove(message)
                    viewAdapter.notifyItemRemoved(index)

                    // Remove stored reference of <initiatingUserId, localReferenceId>
                    userTypingMap.remove(initiatingUserId)
                }
            }
        }
    }

    fun getDisplayName(userId: String): String {
//        val conversation: Conversation? = null  // TODO UI Screens
//        val conversation = (activity as MessengerActivity).getCurrentConversation()   // TODO UI Screens

        if(::conversation.isInitialized && conversation != null) {
            val found = conversation.participants.find { p ->
                    p.user.userId.equals(
                        userId,
                        ignoreCase = true
                    )
                }
            found?.let {
                return found.user.displayName
            }
        }
        return ""
    }

    fun getProfileImageUrl(userId: String): String {
//        val conversation = (activity as MessengerActivity).getCurrentConversation()
        if(::conversation.isInitialized && conversation != null) {
            val found = conversation.participants.find { p ->
                    p.user.userId.equals(
                        userId,
                        ignoreCase = true
                    )
                }
            found?.let {
                return found.user.profileImageUrl
            }
        }
        return ""
    }

    fun updateTitle() {
        (activity as MessengerActivity).supportActionBar?.title = getConversationTitle()
    }

    fun showStatusMessage(message: String) {
        if (Messenger.messageStatusText) {
            Log.d(TAG, "${message}")
            val message = Message(
                sender = User(
                    tenantId = prefs?.tenantId!!,
                    userId = prefs?.externalUserId!!,
                    displayName = prefs?.currentUser?.profile?.displayName!!,
                    profileImageUrl = prefs?.currentUser?.profile?.profileImageUrl!!
                ), metadata = MessageMetadata(statusMessage = message)
            )
            // Show message
            messages.add(message)
            Log.d(TAG, "messages ${messages}")
            val index = messages.indexOf(message)
            viewAdapter.notifyItemInserted(index)
            if (index == messages.size - 1) recyclerView.smoothScrollToPosition(index)
        }
    }

    fun updateConversation(conversationId: String) {
        messageViewModel.updateConversation(conversationId)
    }

    fun showAllCurrentStatusMessages() {
        for (status in currentStatusMessages) {
            showStatusMessage(status)
        }
        currentStatusMessages.clear()
    }

    // Extensions for dp-px conversion
    val Int.dp: Int
        get() = (this / Resources.getSystem().displayMetrics.density).toInt()
    val Int.px: Int
        get() = (this * Resources.getSystem().displayMetrics.density).toInt()

    private fun setEventHandler() {
        Messenger.addEventHandler(object : EventCallback() {
            override fun onConversationCreated(conversation: Conversation) {
            }

            override fun onConversationClosed(conversation: Conversation) {
//                if(conversationId.equals(conversation.conversationId, ignoreCase = true)) {
//                    activity?.getSupportFragmentManager()?.popBackStack()
//                }
            }

            override fun onConversationModified(
                initiatingUser: User,
                title: String?,
                description: String?,
                conversationId: String
            ) {
                activity?.runOnUiThread {
                    title?.let { t ->
                        showStatusMessage("${initiatingUser.displayName} changed the conversation name to ${t}")
                    }
                }
            }

            override fun onParticipantStateChanged(
                participant: Participant,
                conversationId: String
            ) {
            }

            override fun onAddedToConversation(
                initiatingUser: User,
                addedUserId: String,
                conversationId: String
            ) {
                activity?.runOnUiThread {
                    if (this@MessageScreen.conversationId.equals(
                            conversationId,
                            ignoreCase = true
                        )
                    ) {
                        showStatusMessage("${initiatingUser.displayName} ${Messenger.messageStatusTextAdded}")
                    }
                }
            }

            override fun onRemovedFromConversation(
                initiatingUser: User,
                removedUserId: String,
                conversationId: String
            ) {
                activity?.runOnUiThread {
                    if (this@MessageScreen.conversationId.equals(
                            conversationId,
                            ignoreCase = true
                        )
                    ) {
                        showStatusMessage("${initiatingUser.displayName} ${Messenger.messageStatusTextRemoved}")
                    }

                    if (removedUserId.equals(prefs?.externalUserId, ignoreCase = true)) {
                        // Close conversation for removed user
                    }
                }
            }

            override fun onMessageReceived(message: Message) {
                activity?.runOnUiThread {
                    if (conversationId.equals(message.conversationId, ignoreCase = true)) {

                        // Filter
                        if (filterKey != null && filterValue != null) {
                            when (filterKey) {
                                "classId" -> if (message.metadata.classId != filterValue) return@runOnUiThread
                            }
                        }

                        val found = messages.find { m ->
                            m.messageId.equals(
                                message.messageId,
                                ignoreCase = true
                            )
                        }
                        if (found == null) addMessage(message)
//                        Log.d(TAG, "${message.timeCreated.toString()}")
                        var timeCreated = message.timeCreated
//                        prefs?.setConversationLastRead(message.conversationId, message.timeCreated.toLong())
                        if (timeCreated.toString().contains("-")) {
                            timeCreated = convertDateToLong(timeCreated.toString())
                        }
                        prefs?.setConversationLastRead(message.conversationId, timeCreated.toLong())
                        Messenger.unreadCallback?.onNewUnread(Messenger.getUnreadCount())
                    }
                }
            }

            override fun onMessageRejected(message: Message, errorMessage: String) {
                activity?.runOnUiThread {
                    if (conversationId.equals(message.conversationId, ignoreCase = true) &&
                        message.sender.userId.equals(prefs?.externalUserId, ignoreCase = true)
                    ) {
                        showMessageError(message.metadata.localReferenceId, errorMessage)
                    }
                }
            }

            override fun onMessageDeleted(message: Message) {
                activity?.runOnUiThread {
                    if (conversationId.equals(message.conversationId, ignoreCase = true)) {
                        deleteMessageFromList(message)
                    }
                }
            }

            override fun onMessageUserReaction(message: Message) {
                activity?.runOnUiThread {
                    if (conversationId.equals(message.conversationId, ignoreCase = true)) {
                        Log.d(TAG, "onMessageUserReaction ${message}")
                        updateMessage(message)
                    }
                }
            }

            override fun onUserTypingStart(initiatingUserId: String) {
                activity?.runOnUiThread {
                    if (!prefs?.userId.equals(initiatingUserId, ignoreCase = true)) userStartTyping(
                        initiatingUserId
                    )
                }
            }

            override fun onUserTypingStop(initiatingUserId: String) {
                activity?.runOnUiThread {
                    userStopTyping(initiatingUserId)
                }
            }

            override fun onModeratorAdded(userId: String) {
                Log.d(TAG, "Moderator added ${userId}")
            }

            override fun onModeratorRemoved(userId: String) {
                Log.d(TAG, "Moderator removed ${userId}")
            }
        })
    }

    // UI Screens
    fun setBackgroundColor(color: String) {
        containerView.setBackgroundColor(Color.parseColor(color))
    }

    @SuppressLint("ResourceType")
    fun setListCornerRadius(topLeft: Int, topRight: Int, bottomRight: Int, bottomLeft: Int) {
        var color = "#FFFFFF"
        val mode = context?.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)
        when (mode) {
            Configuration.UI_MODE_NIGHT_YES -> {
                color = activity?.resources!!.getString(R.color.dmBackground)
            }
        }
        val shape = getShape(
            topLeft.px.toFloat(),
            topRight.px.toFloat(),
            bottomRight.px.toFloat(),
            bottomLeft.px.toFloat(),
            color
        )
        listFrame.background = shape
    }

    // Return shape drawable with corner radius and background color
    // radius in pixels
    // color in hex string #FFFFFF
    private fun getShape(
        topLeft: Float,
        topRight: Float,
        bottomRight: Float,
        bottomLeft: Float,
        color: String
    ): ShapeDrawable {
        val shape = ShapeDrawable(
            RoundRectShape(
                floatArrayOf(
                    topLeft,
                    topLeft,
                    topRight,
                    topRight,
                    bottomRight,
                    bottomRight,
                    bottomLeft,
                    bottomLeft
                ), null, null
            )
        )
        shape.getPaint().setColor(Color.parseColor(color))
        return shape
    }

    fun setSendButtonDrawable(drawable: Drawable) {
        sendButtonDrawable = drawable
    }

    fun setImageButtonColor(hex: String) {
        imageButtonColor = hex
    }

    open fun showBottomSheetDialog(view: View, message: Message) {
        val dialog = BottomSheetDialog(view.context)
//                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
//        dialog.setTitle("Title")
        dialog.setContentView(R.layout.dialog_message_actions)
        val likeBtn = dialog.findViewById<TextView>(R.id.like)
        val dislikeBtn = dialog.findViewById<TextView>(R.id.dislike)
        val copyText = dialog.findViewById<TextView>(R.id.copy_text)
        val saveMedia = dialog.findViewById<TextView>(R.id.save_media)
        val report = dialog.findViewById<TextView>(R.id.report)
        val delete = dialog.findViewById<TextView>(R.id.delete)
        val errorMessage = dialog.findViewById<TextView>(R.id.error_message)

        // Dark mode
        val mode = context?.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)
        when (mode) {
            Configuration.UI_MODE_NIGHT_YES -> {
                val background = dialog.findViewById<ConstraintLayout>(R.id.dialog_bottom_sheet)
                background?.background = resources.getDrawable(R.drawable.dialog_round_top_dark)

                copyText?.setTextColor(resources.getColor(R.color.dmTextHigh))
                var drawables = copyText?.compoundDrawables
                drawables?.let {
                    for (drawable in it) {
                        drawable?.let {
                            DrawableCompat.setTint(it, resources.getColor(R.color.dmTextMed))
                        }
                    }
                }

                saveMedia?.setTextColor(resources.getColor(R.color.dmTextHigh))
                drawables = saveMedia?.compoundDrawables
                drawables?.let {
                    for (drawable in it) {
                        drawable?.let {
                            DrawableCompat.setTint(it, resources.getColor(R.color.dmTextMed))
                        }
                    }
                }

                errorMessage?.setTextColor(resources.getColor(R.color.dmTextHigh))
            }
        }

        // Reaction, Like
        likeBtn!!.setOnClickListener {
            setReaction(
                message,
                resources.getString(R.string.reaction_code_like)
            )
            Handler().postDelayed({
                dialog.dismiss()
            }, 200)
        }

        // Reaction, Dislike
        dislikeBtn?.setOnClickListener {
            setReaction(
                message,
                resources.getString(R.string.reaction_code_dislike)
            )
            Handler().postDelayed({
                dialog.dismiss()
            }, 200)
        }

        // Show Copy text or Save (Image)
        if (message.media == null || message.media?.content == null || message.media?.thumbnail == null) {
            // Show Copy text
            copyText!!.visibility = View.VISIBLE
            copyText!!.setOnClickListener {
                copyText(message, it)
                Handler().postDelayed({
                    dialog.dismiss()
                }, 200)
            }
        } else {
            // Show Save
            saveMedia!!.visibility = View.VISIBLE
            saveMedia!!.setOnClickListener {

                saveMedia(message, it)

                Handler().postDelayed({
                    dialog.dismiss()
                }, 200)
            }
        }

        if (!message.sender.userId.equals(prefs?.externalUserId, ignoreCase = true)) {
            // Report
            report!!.visibility = View.VISIBLE
            report!!.setOnClickListener {

                reportMessage(message, it)

                Handler().postDelayed({
                    dialog.dismiss()
                }, 200)
            }
        } else {
            // Delete
            delete!!.visibility = View.VISIBLE
            delete!!.setOnClickListener {

                deleteMessage(message, it)

                Handler().postDelayed({
                    dialog.dismiss()
                }, 200)
            }
        }

        // Error message
        if (!message.metadata.errorMessage.isNullOrBlank()) {
            likeBtn.visibility = View.GONE
            dislikeBtn?.visibility = View.GONE
//            copyText!!.visibility = View.GONE
            copyText!!.visibility = View.VISIBLE

            errorMessage!!.text = message.metadata.errorMessage
            errorMessage!!.visibility = View.VISIBLE

            delete!!.visibility = View.VISIBLE
            delete!!.text = "Remove"
            delete!!.setOnClickListener {

                deleteMessageFromList(message)

                Handler().postDelayed({
                    dialog.dismiss()
                }, 200)
            }
        }

        dialog.show()
    }
}

// Override LayoutManager to disable scroll
class MyLinearLayoutManager(context: Context?, orientation: Int, reverseLayout: Boolean) :
    LinearLayoutManager(context, orientation, reverseLayout) {
    private var isScrollEnabled = true
    fun setScrollEnabled(flag: Boolean) {
        isScrollEnabled = flag
    }

    override fun canScrollVertically(): Boolean {
        //Similarly you can customize "canScrollHorizontally()" for managing horizontal scroll
        return isScrollEnabled && super.canScrollVertically()
    }

    override fun onLayoutChildren(recycler: RecyclerView.Recycler?, state: RecyclerView.State?) {
        try {
            super.onLayoutChildren(recycler, state)
        } catch (ex: IndexOutOfBoundsException) {
            ex.printStackTrace()
        }
    }
}
