package com.koder.ellen.ui.conversation

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Bitmap.createScaledBitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.material.card.MaterialCardView
import com.koder.ellen.Messenger.Companion.prefs
import com.koder.ellen.MessengerActivity
import com.koder.ellen.R
import com.koder.ellen.model.Conversation
import com.koder.ellen.model.Participant
import com.koder.ellen.model.User
import java.text.SimpleDateFormat
import java.util.*


internal class ConversationAdapter(private val context: Context, private val dataset: MutableList<Conversation>, private val fragment: Fragment? = null) :
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
        return MyViewHolder(layout)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
//        holder.textView.text = myDataset[position]
//        holder.textView.setOnClickListener { view ->
//            Toast.makeText(holder.textView.context, holder.textView.text, Toast.LENGTH_SHORT).show()
//        }
//
        val cardView = holder.layout.findViewById<MaterialCardView>(R.id.conversation_icon_layout)
        val newMessageDot = holder.layout.findViewById<ImageView>(R.id.new_message_dot)
        cardView.strokeWidth = 0
        newMessageDot.visibility = View.GONE

        // Set title
        var title = ""
        if (dataset.get(position).title.isNullOrBlank()) {
            // Set title to participant(s) display names
            title = getTitleByParticipants(dataset.get(position).participants)
        } else {
            title = dataset.get(position).title
        }
        holder.layout.findViewById<TextView>(R.id.conversation_title).text = title

        // Set date
        var date = ""
        if (dataset.get(position).messages.isEmpty()) {
//            holder.layout.findViewById<ImageView>(R.id.conversation_arrow).visibility =
//                View.INVISIBLE
        } else {
            date = getTodayYestDateFromMilli(dataset.get(position).messages.first().timeCreated!!.toLong())
        }
        holder.layout.findViewById<TextView>(R.id.conversation_date).text = date

        // Set subtitle
        var subtitle = ""
//        var latestMessageCreated: Long = 0
        if (!dataset.get(position).messages.isEmpty()) {
            subtitle = dataset.get(position).messages.first().body

            // Set new message indicator
            // If latest message is newer than last read
            val latestMessageCreated = dataset.get(position).messages.first().timeCreated.toLong()
//            Log.d(TAG, "latestMessageCreated ${latestMessageCreated}")

            val lastRead = prefs?.getConversationLastRead(dataset.get(position).conversationId) ?: 0

            if(latestMessageCreated > lastRead) {
                // Show new message indicator
                newMessageDot.visibility = View.VISIBLE
                cardView.strokeWidth = 2.px
            }
//        val notificationDot = holder.layout.findViewById<ImageView>(R.id.notification_dot)
//
//        notificationDot.visibility = View.VISIBLE
        }
        holder.layout.findViewById<TextView>(R.id.conversation_subtitle).text = subtitle


        // Set icon
        var icon = getFirstParticipantImageUrl(dataset.get(position).participants)
        if (!dataset.get(position).messages.isEmpty()) {
            // Set icon to Message's sender
            icon = dataset.get(position).messages.first().sender.profileImageUrl
        }
        // Set icon to first participant's if it's the same as current user's
        if (icon.equals(prefs?.currentUser?.profile?.profileImageUrl, ignoreCase = true)) icon =
            getFirstParticipantImageUrl(dataset.get(position).participants)
        // Load profile image
        val iconView = holder.layout.findViewById<ImageView>(R.id.conversation_icon)
//        Picasso.get().load(icon).into(holder.layout.findViewById<ImageView>(R.id.conversation_icon))

        // MultiImageView
        var count = 0;
        val bitmapList = mutableListOf<Bitmap>()
        for(participant in dataset.get(position).participants) {
            participant.user.profileImageUrl?.let {
                if(!participant.user.userId.equals(prefs?.externalUserId, ignoreCase = true) && count < 4) {
                    count++

//                    Log.d(TAG, "participant ${participant.user.displayName}")

//                Bitmap mBitmap = Picasso.get().load(link).get();
//                val bitmap = Picasso.get().load(participant.user.profileImageUrl).get()
//                multiImageView.addImage(bitmap)

//                    Picasso.get().load(participant.user.profileImageUrl).into(object: Target {    // TODO To Glide
//                        override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
//                        }
//
//                        override fun onBitmapFailed(e: java.lang.Exception?, errorDrawable: Drawable?) {
//                        }
//
//                        override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
//                            try {
//                                bitmap?.let {
////                                val scaled = createScaledBitmap(bitmap, 100, 100, false)
////                                Log.d(TAG, "bitmap ${bitmap.width} x ${bitmap.height}")
////                                Log.d(TAG, "scaled ${scaled.width} x ${scaled.height}")
//                                    bitmapList.add(bitmap)
//                                    val bitmapGrid = bitmapsToGrid(bitmapList)
//                                    iconView.setImageBitmap(bitmapGrid)
//                                }
//                            } catch (e: Exception) {
//                            }
//                        }
//                    })    // TODO To Glide
                    Glide.with(context)
                            .asBitmap()
                            .load(participant.user.profileImageUrl)
                            .into(object : CustomTarget<Bitmap>(){
                                override fun onResourceReady(bitmap: Bitmap, transition: Transition<in Bitmap>?) {
//                    imageView.setImageBitmap(resource)
//                                    val scaled = createScaledBitmap(bitmap, 100, 100, false)
//                                Log.d(TAG, "bitmap ${bitmap.width} x ${bitmap.height}")
//                                Log.d(TAG, "scaled ${scaled.width} x ${scaled.height}")
                                    bitmapList.add(bitmap)
                                    val bitmapGrid = bitmapsToGrid(bitmapList)
                                    iconView.setImageBitmap(bitmapGrid)
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
        }


        holder.layout.setOnClickListener { view ->
//            Toast.makeText(holder.layout.context, "${position}", Toast.LENGTH_SHORT).show()
//            // TODO
//            val intent = Intent(context, MessageActivity::class.java).apply {
//                Log.d(TAG, "${dataset.get(position)}")
//                putExtra("CONVERSATION_ID", dataset.get(position).conversationId)
//                putExtra("CONVERSATION_TITLE", title)
//                putExtra("CONVERSATION_DESCRIPTION", "")
//                putExtra("PARTICIPANT_ID", "")
//                putExtra("CONVERSATION_PARTICIPANTS", getParticipantsList(dataset.get(position)))
//            }
//            context.startActivity(intent) // TODO

            // Hide new message dot
//            Log.d(TAG, "timeCreated ${dataset.get(position).timeCreated}")
//            Log.d(TAG, "currentTimeMillis ${System.currentTimeMillis()}")
            prefs?.setConversationLastRead(dataset.get(position).conversationId, System.currentTimeMillis())
            cardView.strokeWidth = 0
            newMessageDot.visibility = View.GONE

            if(fragment == null) {
//                prefs?.setConversationLastRead(dataset.get(position).conversationId, System.currentTimeMillis())  // TODO UI Screens
//                cardView.strokeWidth = 0
//                newMessageDot.visibility = View.GONE

                // Communicate with host Activity to show MessageFragment
                (context as MessengerActivity).showMessageFragment(dataset.get(position))
            } else if(fragment is com.koder.ellen.screen.ConversationScreen) {
                fragment.sendClick(dataset.get(position), position)
            }


        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataset.size

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

    // Return participants in format Participant1, Participant2, ...
    private fun getTitleByParticipants(participants: MutableList<Participant>): String {
        var title = ""

        // If the only participant is the sender (myself)
        if(participants.size == 1 && participants.first().user.userId.equals(prefs?.externalUserId, ignoreCase = true))
            return "Me"

        for (participant in participants) {
            if(participant.user.displayName != null) {
                if (participant.user.displayName.equals(prefs?.currentUser?.profile?.displayName, ignoreCase = true)) continue
                if (title.isEmpty()) {
                    title += participant.user.displayName
                    continue
                }
                title += ", ${participant.user.displayName}"
            }
        }
        return title
    }

    private fun getFirstParticipantImageUrl(participants: MutableList<Participant>): String {
        for (participant in participants) {
            if(participant.user.displayName != null) {
                if (!participant.user.displayName.equals(prefs?.currentUser?.profile?.displayName, ignoreCase = true)) {
//                Log.d(TAG, "participant ${participant.user.displayName}")
                    return participant.user.profileImageUrl
                }
            }
        }
        return prefs?.currentUser?.profile?.profileImageUrl!! // Shouldn't reach this point
    }

    private fun convertDateToLong(date: String): Long {
        val df = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        df.setTimeZone(TimeZone.getTimeZone("UTC"))
        return df.parse(date).time
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
                width = 100 * 2

                scaledBitmaps.add(createScaledBitmap(b[0], 100, 100, false))
                scaledBitmaps.add(createScaledBitmap(b[1], 100, 100, false))
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
                    canvas.drawBitmap(scaledBitmaps[1], 100f, 0f, null)
                }
                3 -> {
                    canvas.drawBitmap(scaledBitmaps[0], 0f, 0f, null)
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

    // Extensions for dp-px conversion
    val Int.dp: Int
        get() = (this / Resources.getSystem().displayMetrics.density).toInt()
    val Int.px: Int
        get() = (this * Resources.getSystem().displayMetrics.density).toInt()
}