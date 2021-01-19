package com.koder.ellen.ui.conversation

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Bitmap.createScaledBitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.text.Html
import android.text.format.DateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.material.card.MaterialCardView
import com.koder.ellen.Messenger
import com.koder.ellen.Messenger.Companion.prefs
import com.koder.ellen.MessengerActivity
import com.koder.ellen.R
import com.koder.ellen.core.Utils.Companion.getShape
import com.koder.ellen.model.Conversation
import com.koder.ellen.model.Participant
import com.koder.ellen.model.User
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.floor


internal class ConversationAdapter(
    private val context: Context,
    private val dataset: MutableList<Conversation>,
    private val fragment: Fragment? = null
) :
    RecyclerView.Adapter<ConversationAdapter.MyViewHolder>() {

    val TAG = "ConversationsAdapter"

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder.
    // Each data item is just a string in this case that is shown in a TextView.
//    class MyViewHolder(val textView: TextView) : RecyclerView.ViewHolder(textView)
    class MyViewHolder(val layout: ConstraintLayout) : RecyclerView.ViewHolder(layout)

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyViewHolder {
        // create a new view
//        val textView = LayoutInflater.from(parent.context)
//            .inflate(R.layout.item_conversations, parent, false) as TextView
        val layout = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_conversations, parent, false) as ConstraintLayout
        // set the view's size, margins, paddings and layout parameters
        // ...
        // Top and bottom padding
        layout.setPadding(
            layout.paddingLeft,
            Messenger.conversationItemTopPadding.px,
            layout.paddingRight,
            Messenger.conversationItemBottomPadding.px
        )

        // Title and subtitle text size
        val title = layout.findViewById<TextView>(R.id.conversation_title)
        val subtitle = layout.findViewById<TextView>(R.id.conversation_subtitle)
        title.textSize = Messenger.conversationTitleSize
        subtitle.textSize = Messenger.conversationSubtitleSize

        // Icon
        val icon = layout.findViewById<MaterialCardView>(R.id.conversation_icon_layout)
        icon.radius = Messenger.conversationIconRadius.px.toFloat()
        icon.layoutParams.height = Messenger.conversationIconRadius.px * 2
        icon.layoutParams.width = Messenger.conversationIconRadius.px * 2

        // New message checkmark
        val check = layout.findViewById<ImageView>(R.id.new_message_check)
        DrawableCompat.setTint(
            check.getDrawable(),
            Color.parseColor(Messenger.conversationNewMessageColor)
        );

        // Date position
        if(Messenger.conversationNewMessageCheckmark) {
            // Display date view on subtitle-row
            val constraintSet = ConstraintSet()
            constraintSet.clone(layout)
//            app:layout_constraintBottom_toBottomOf="@id/conversation_subtitle"
            constraintSet.connect(
                R.id.conversation_date,
                ConstraintSet.BOTTOM,
                R.id.conversation_subtitle,
                ConstraintSet.BOTTOM,
                0
            )
            constraintSet.applyTo(layout)
        } else {
            // Remove checkmark from layout
            val parent = check.parent as ViewGroup
            parent.removeView(check)

            // Display date view on title-row
            val constraintSet = ConstraintSet()
            constraintSet.clone(layout)
//            app:layout_constraintTop_toTopOf="@id/conversation_title"
            constraintSet.connect(
                R.id.conversation_date,
                ConstraintSet.TOP,
                R.id.conversation_title,
                ConstraintSet.TOP,
                0
            )
            constraintSet.applyTo(layout)
        }

        return MyViewHolder(layout)
    }

    // Replace the contents of a view (invoked by the layout manager)
    @SuppressLint("ResourceType")
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
//        holder.textView.text = myDataset[position]
//        holder.textView.setOnClickListener { view ->
//            Toast.makeText(holder.textView.context, holder.textView.text, Toast.LENGTH_SHORT).show()
//        }
//
        val layout = holder.layout.findViewById<ConstraintLayout>(R.id.conversation_item_layout)
        layout.background = getShape(0.px.toFloat(), 0.px.toFloat(), 0.px.toFloat(), 0.px.toFloat(), "#FFFFFF")

        if(position == 0) {
            val shape = getShape(Messenger.screenCornerRadius[0].px.toFloat(), Messenger.screenCornerRadius[1].px.toFloat(), 0.px.toFloat(), 0.px.toFloat(), "#FFFFFF")
            layout.background = shape
        }

        if(position == itemCount - 1) {
            val shape = getShape(0.px.toFloat(), 0.px.toFloat(), Messenger.screenCornerRadius[2].px.toFloat(), Messenger.screenCornerRadius[3].px.toFloat(), "#FFFFFF")
            layout.background = shape
        }

        val cardView = holder.layout.findViewById<MaterialCardView>(R.id.conversation_icon_layout)

        val newMessageDot = holder.layout.findViewById<ImageView>(R.id.new_message_dot)
        newMessageDot.setColorFilter(Color.parseColor(Messenger.conversationNewMessageColor))

        val newMessageCheck = holder.layout.findViewById<ImageView>(R.id.new_message_check)
        cardView.strokeWidth = 0
        newMessageDot.visibility = View.GONE
        newMessageCheck?.visibility = View.GONE

        val titleView = holder.layout.findViewById<TextView>(R.id.conversation_title)

        val dateView = holder.layout.findViewById<TextView>(R.id.conversation_date)
        dateView.setTextColor(Color.parseColor("#000000"))
        dateView.setAlpha(0.54f)
        dateView.setTypeface(null, Typeface.NORMAL)

        val mode = context?.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)
        when (mode) {
            Configuration.UI_MODE_NIGHT_YES -> {
                titleView.setTextColor(context?.resources!!.getColor(R.color.dmTextHigh))
                dateView.setTextColor(context?.resources!!.getColor(R.color.dmTextMed))
            }
        }

        // Set title
        var title = ""
        if (dataset.get(position).title.isNullOrBlank()) {
            // Set title to participant(s) display names
            title = getTitleByParticipants(dataset.get(position).participants)
        } else {
            title = dataset.get(position).title
        }
        titleView.text = title

        // Set date
        var date = ""
        dateView.text = date

        // Set subtitle
        var subtitle = ""
        if (!dataset.get(position).messages.isEmpty()) {
            subtitle = prefixSubtitle(dataset.get(position))

            // New message indicator
            // If latest message is newer than last read
            val latestMessage = dataset.get(position).messages.first()
            val latestMessageCreated = dataset.get(position).messages.first().timeCreated.toLong()
//            Log.d(TAG, "latestMessageCreated ${latestMessageCreated}")
//            Log.d(TAG, "System.currentTimeMillis() ${System.currentTimeMillis()}")

            // Set time ago date
            var date = if(!Messenger.conversationTimeAgoDateNames) {
                    getTimeAgo(latestMessageCreated)
                } else {
                    getTodayYestDateFromMilli(latestMessageCreated)
                }
            dateView.text = date

            // Get last-read timestamp for this conversation
            val lastRead = prefs?.getConversationLastRead(dataset.get(position).conversationId) ?: 0

            if(latestMessageCreated > lastRead) {
                // There are unread messages

                if(!latestMessage.sender.userId.equals(prefs?.userId, ignoreCase = true)) {
                    // Latest message is not current user's

                    // Show new message indicator
                    newMessageDot.visibility = View.VISIBLE

                    if(Messenger.conversationIconStroke) {
                        cardView.strokeWidth = 2.px
                    }

                    if(Messenger.conversationTimeAgoDateHighlight) {
                        dateView.setTextColor(
                            ContextCompat.getColor(
                                context,
                                R.color.material_blue_500
                            )
                        )
                        dateView.setTypeface(null, Typeface.BOLD)
                        dateView.alpha = 1.0f
                    }
                }

            } else {
                // Show check mark
                newMessageCheck?.visibility = View.VISIBLE
            }

        } else {
            subtitle = Messenger.conversationEmptyText
        }
        holder.layout.findViewById<TextView>(R.id.conversation_subtitle).text = subtitle


        // Set icon
        var icon = Html.fromHtml(getFirstParticipantImageUrl(dataset.get(position).participants)).toString()
//        if (!dataset.get(position).messages.isEmpty()) {
//            // Set icon to Message's sender
//            icon = dataset.get(position).messages.first().sender.profileImageUrl
//        }
//        // Set icon to first participant's if it's the same as current user's
//        if (icon.equals(prefs?.currentUser?.profile?.profileImageUrl, ignoreCase = true)) icon =
//            getFirstParticipantImageUrl(dataset.get(position).participants)
        // Load profile image
        val iconView = holder.layout.findViewById<ImageView>(R.id.conversation_icon)

        if(dataset.get(position).participants.size == 1) {
            // Get owner's profile image
            icon = Html.fromHtml(getSingleParticipantImageUrl(dataset.get(position).participants)).toString()
            Picasso.get().load(icon).into(holder.layout.findViewById<ImageView>(R.id.conversation_icon))
        }

        if(dataset.get(position).participants.size == 2) Picasso.get().load(icon).into(
            holder.layout.findViewById<ImageView>(
                R.id.conversation_icon
            )
        )

        if(dataset.get(position).participants.size > 2) {
            // MultiImageView
            // Get latest profile images for the first 4 users that does not include the current user
            var participants = dataset.get(position).participants.filter { p -> p.user.userId != prefs?.userId}.take(4)
            Log.d(TAG, "title $title")
//            Log.d(TAG, "participants $participants")
            val urlList = getLatestProfileImages(participants)
            Log.d(TAG, "images $urlList")

            // Get image bitmaps
            // LinkedHashMap to preserve ordering
            val bitmapMap = linkedMapOf<String, Bitmap?>()
            for(url in urlList) {
                bitmapMap.put(url, null)
                Glide.with(context)
                    .asBitmap()
                    .load(url)
                    .into(object : CustomTarget<Bitmap>() {
                        override fun onResourceReady(
                            bitmap: Bitmap,
                            transition: Transition<in Bitmap>?
                        ) {
                            // Add bitmap to map
                            bitmapMap.put(url, bitmap)
                            if (areBitmapsLoaded(bitmapMap)) {
                                // All bitmaps are loaded
                                // Create multiimageview
                                Log.d(TAG, "title $title")
                                Log.d(TAG, "bitmaps loaded ${bitmapMap.values}")

                                val bitmapGrid =
                                    bitmapsToGrid(bitmapMap.values.toList() as List<Bitmap>)
                                iconView.setImageBitmap(bitmapGrid)
                            }
                        }

                        override fun onLoadCleared(placeholder: Drawable?) {
                            // this is called when imageView is cleared on lifecycle call or for
                            // some other reason.
                            // if you are referencing the bitmap somewhere else too other than this imageView
                            // clear it here as you can no longer have the bitmap
                        }
                    })
            }
        }


        holder.layout.setOnClickListener { view ->
            // Hide new message dot
//            Log.d(TAG, "timeCreated ${dataset.get(position).timeCreated}")
//            Log.d(TAG, "currentTimeMillis ${System.currentTimeMillis()}")
            prefs?.setConversationLastRead(
                dataset.get(position).conversationId,
                System.currentTimeMillis()
            )
            cardView.strokeWidth = 0
            newMessageDot.visibility = View.GONE

            if(fragment == null) {
                // Communicate with host Activity to show MessageFragment
                (context as MessengerActivity).showMessageFragment(dataset.get(position))
            } else if(fragment is com.koder.ellen.screen.ConversationScreen) {
                fragment.sendClick(dataset.get(position), position)
            }
        }

        if(fragment is com.koder.ellen.screen.ConversationScreen && Messenger.conversationLongClickToDelete) {
            holder.layout.setOnLongClickListener { view ->
                fragment.onLongClick(dataset.get(position), position)
                true
            }
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataset.size

    private fun areBitmapsLoaded(bitmapMap: LinkedHashMap<String, Bitmap?>): Boolean {
        return !bitmapMap.containsValue(null)
    }

    fun getParticipantsList(conversation: Conversation): ArrayList<User> {
        val list = ArrayList<User>()
        for(participant in conversation.participants) {
            val user = participant.user
            // Set Owner
            if (participant.role == 100) user.role = 100
            list.add(participant.user)
        }
        return list
    }

    fun getTodayYestDateFromMilli(msgTimeMillis: Long): String {
        val messageTime = Calendar.getInstance()
        messageTime.timeInMillis = msgTimeMillis
        val now = Calendar.getInstance()
        val strTimeFormat = "h:mm aa"
        val strDateFormat = "M/d/yy"
        val strDayFormat = "EEE"
        return if (now[Calendar.DATE] === messageTime[Calendar.DATE] &&
                now[Calendar.MONTH] === messageTime[Calendar.MONTH]
                &&
                now[Calendar.YEAR] === messageTime[Calendar.YEAR]) {
            DateFormat.format(strTimeFormat, messageTime).toString()
        } else if (now[Calendar.DATE] - messageTime[Calendar.DATE] === 1
                &&
                now[Calendar.MONTH] === messageTime[Calendar.MONTH]
                &&
                now[Calendar.YEAR] === messageTime[Calendar.YEAR]) {
            "Yesterday"
        } else if(now[Calendar.DATE] - messageTime[Calendar.DATE] < 7
                &&
                now[Calendar.MONTH] === messageTime[Calendar.MONTH]
                &&
                now[Calendar.YEAR] === messageTime[Calendar.YEAR]) {
            DateFormat.format(strDayFormat, messageTime).toString()
        } else {
            DateFormat.format(strDateFormat, messageTime).toString()
        }
    }

    fun getTimeAgo(msgTimeMillis: Long): String {
        val timeMillis = System.currentTimeMillis()
        val diffMillis = timeMillis - msgTimeMillis
        val diffSecs = diffMillis/1000
        return if (diffSecs.toInt() == 0) {
            "1s"
        } else if (diffSecs < 60) {
            "${diffSecs}s"
        } else if (diffSecs < 3600) {
            "${getFloor(diffSecs / 60)}m"
        } else if (diffSecs < 86400) {
            "${getFloor(diffSecs / 3600)}h"
        } else if (diffSecs < 604800) {
            "${getFloor(diffSecs / 86400)}d"
        } else if (diffSecs < (604800 * 4)) {
            "${getFloor(diffSecs / 604800)}w"
        } else {
            "${getFloor(diffSecs / (604800 * 4))}M"
        }
    }

    private fun getFloor(value: Long): Int {
        return floor((value).toDouble()).toInt()
    }

    // Return participants in format Participant1, Participant2, ...
    private fun getTitleByParticipants(participants: MutableList<Participant>): String {
        var title = ""

        // If the only participant is the sender (myself)
        if(participants.size == 1 && participants.first().user.userId.equals(
                prefs?.externalUserId,
                ignoreCase = true
            ))
            return "Me"

        for (participant in participants) {
            if(participant.user.displayName != null) {
                if (participant.user.userId == prefs?.userId) continue
                if (title.isEmpty()) {
                    title += participant.user.displayName
                    continue
                }
                title += ", ${participant.user.displayName}"
            }
        }
        return title
    }

    // Get image url when there is only 1 participant (owner)
    private fun getSingleParticipantImageUrl(participants: MutableList<Participant>): String {
        val participant = participants.first()
        // Get cached
        val cachedProfile = Messenger.userProfileCache.get(participant.user.userId.toLowerCase())
        if(cachedProfile != null) {
            return cachedProfile.photoUrl
        }
        return participant.user.profileImageUrl
    }

    // Get image url of the participant that isn't the current user
    private fun getFirstParticipantImageUrl(participants: MutableList<Participant>): String {
        for (participant in participants) {
            if (participant.user.userId != prefs?.userId) {
                // Get cached
                val cachedProfile = Messenger.userProfileCache.get(participant.user.userId.toLowerCase())
                if(cachedProfile != null) {
                    return cachedProfile.photoUrl
                }
                return participant.user.profileImageUrl
            }
        }
        return prefs?.currentUser?.profile?.profileImageUrl!! // Shouldn't reach this point
    }


    private fun convertDateToLong(date: String): Long {
        val df = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        df.setTimeZone(TimeZone.getTimeZone("UTC"))
        return df.parse(date).time
    }

    private fun getLatestProfileImages(participants: List<Participant>): List<String> {
        val imageList = mutableListOf<String>()
        for(participant in participants) {
            // Get cached
            val cachedProfile = Messenger.userProfileCache.get(participant.user.userId.toLowerCase())
            if(cachedProfile != null) {
                imageList.add(cachedProfile.photoUrl)
            } else {
                imageList.add(participant.user.profileImageUrl)
            }
        }
        return imageList.toList()
    }

    private fun prefixSubtitle(conversation: Conversation): String {
        if(conversation.messages.isEmpty()) return ""

        val message = conversation.messages.first()
        var subtitle = message.body
        when {
            conversation.participants.size == 2 -> {
                if(message.sender.userId.equals(prefs?.userId, ignoreCase = true)) {
                    // Sender is current user
                    subtitle = "You: ${subtitle}"
                }
            }
            conversation.participants.size > 2 -> {
                if(message.sender.userId.equals(prefs?.userId, ignoreCase = true)) {
                    // Sender is current user
                    subtitle = "You: ${subtitle}"
                } else {
                    // Sender is not current user
                    subtitle = "${message.sender.displayName}: ${subtitle}"
                }
            }
        }
        return subtitle
    }

    // Bitmaps are 100x100
    fun bitmapsToGrid(b: List<Bitmap>): Bitmap? {
        var drawnBitmap: Bitmap? = null
        var width = 100
        var height = 100
        val scaledBitmaps = mutableListOf<Bitmap>()
        when(b.size) {
            1 -> {
                scaledBitmaps.add(createScaledBitmap(b[0], 100, 100, false))
            }
            2 -> {
//                width = 100 * 2
                width = 100


//                scaledBitmaps.add(createScaledBitmap(b[0], 100, 100, false))
                scaledBitmaps.add(applyCrop(createScaledBitmap(b[0], 100, 100, false), 25, 0, 25, 0) as Bitmap)
//                scaledBitmaps.add(createScaledBitmap(b[1], 100, 100, false))
                scaledBitmaps.add(applyCrop(createScaledBitmap(b[1], 100, 100, false), 25, 0, 25, 0) as Bitmap)
            }
            3 -> {
                width = 100 * 2
                height = 100 * 2

                scaledBitmaps.add(createScaledBitmap(b[0], 200, 200, false))
                scaledBitmaps.add(createScaledBitmap(b[1], 100, 100, false))
                scaledBitmaps.add(createScaledBitmap(b[2], 100, 100, false))
            }
            4 -> {
                width = 100 * 2
                height = 100 * 2

                scaledBitmaps.add(createScaledBitmap(b[0], 100, 100, false))
                scaledBitmaps.add(createScaledBitmap(b[1], 100, 100, false))
                scaledBitmaps.add(createScaledBitmap(b[2], 100, 100, false))
                scaledBitmaps.add(createScaledBitmap(b[3], 100, 100, false))
            }
        }
        try {
            drawnBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(drawnBitmap)
            // JUST CHANGE TO DIFFERENT Bitmaps and coordinates .
            when(b.size) {
                1 -> {
                    canvas.drawBitmap(scaledBitmaps[0], 0f, 0f, null)
                }
                2 -> {

                    canvas.drawBitmap(scaledBitmaps[0], 0f, 0f, null)
//                    addWhiteBorder(scaledBitmaps[0], 4.px)?.let {
//                        canvas.drawBitmap(
//                            it,
//                            -5f,
//                            -4.px.toFloat(),
//                            null
//                        )
//                    }
//                    canvas.drawBitmap(scaledBitmaps[1], 100f, 0f, null)
                    canvas.drawBitmap(scaledBitmaps[1], 50f, 0f, null)
//                    addWhiteBorder(scaledBitmaps[1], 4.px)?.let {
//                        canvas.drawBitmap(
//                            it,
//                            45f,
//                            -4.px.toFloat(),
//                            null
//                        )
//                    }
                }
                3 -> {

//                    addWhiteBorder(scaledBitmaps[0], 4.px)?.let {
//                        canvas.drawBitmap(
//                            it,
//                            -50f,
//                            -4.px.toFloat(),
//                            null
//                        )
//                    }

//                    addWhiteBorder(scaledBitmaps[1], 4.px)?.let {
//                        canvas.drawBitmap(
//                            it,
//                            100f,
//                            -4.px.toFloat(),
//                            null
//                        )
//                    }

//                    addWhiteBorder(scaledBitmaps[2], 4.px)?.let {
//                        canvas.drawBitmap(
//                            it,
//                            100f,
//                            100f,
//                            null
//                        )
//                    }

                    canvas.drawBitmap(scaledBitmaps[0], -50f, 0f, null)
                    canvas.drawBitmap(scaledBitmaps[1], 100f, 0f, null)
                    canvas.drawBitmap(scaledBitmaps[2], 100f, 100f, null)
                }
                4 -> {
                    canvas.drawBitmap(scaledBitmaps[0], 0f, 0f, null)
                    canvas.drawBitmap(scaledBitmaps[1], 100f, 0f, null)
                    canvas.drawBitmap(scaledBitmaps[2], 0f, 100f, null)
                    canvas.drawBitmap(scaledBitmaps[3], 100f, 100f, null)
                }
            }
//            canvas.drawBitmap(b[0], 0f, 0f, null)
//            canvas.drawBitmap(b[1], 200f, 300f, null)
//            canvas.drawBitmap(b[0], 100f, 200f, null)
//            canvas.drawBitmap(b[0], 300f, 350f, null)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return drawnBitmap
    }

    private fun addWhiteBorder(bmp: Bitmap, borderSize: Int): Bitmap? {
        val bmpWithBorder =
            Bitmap.createBitmap(bmp.width + borderSize * 2, bmp.height + borderSize * 2, bmp.config)
        val canvas = Canvas(bmpWithBorder)
        canvas.drawColor(Color.WHITE)
        canvas.drawBitmap(bmp, borderSize.toFloat(), borderSize.toFloat(), null)
        return bmpWithBorder
    }

    private fun applyCrop(
        bitmap: Bitmap,
        leftCrop: Int,
        topCrop: Int,
        rightCrop: Int,
        bottomCrop: Int
    ): Bitmap? {
        val cropWidth = bitmap.width - rightCrop - leftCrop
        val cropHeight = bitmap.height - bottomCrop - topCrop
        return Bitmap.createBitmap(bitmap, leftCrop, topCrop, cropWidth, cropHeight)
    }

    // Extensions for dp-px conversion
    val Int.dp: Int
        get() = (this / Resources.getSystem().displayMetrics.density).toInt()
    val Int.px: Int
        get() = (this * Resources.getSystem().displayMetrics.density).toInt()
}