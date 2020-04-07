package com.koder.ellen.ui.message

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RoundRectShape
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.text.*
import android.text.format.DateFormat
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.transition.Slide
import android.transition.TransitionManager
import android.util.Log
import android.view.*
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.PopupMenu
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.text.HtmlCompat
import androidx.core.text.HtmlCompat.FROM_HTML_MODE_COMPACT
import androidx.core.widget.PopupWindowCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.card.MaterialCardView
import com.google.android.material.internal.VisibilityAwareImageButton
import com.koder.ellen.Messenger
import com.koder.ellen.Messenger.Companion.prefs
import com.koder.ellen.R
import com.koder.ellen.model.Message
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import kotlinx.android.synthetic.main.dialog_message_actions.view.*
import java.io.InputStream
import java.util.*


class MessageAdapter(private val context: Context, private val dataset: MutableList<Message>, private val fragment: MessageFragment?) :
    RecyclerView.Adapter<MessageAdapter.MyViewHolder>() {

    val TAG = "MessageAdapter"

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
            .inflate(R.layout.item_message, parent, false) as ConstraintLayout
        // set the view's size, margins, paddings and layout parameters
        // ...
        val senderBody = layout.findViewById<TextView>(R.id.sender_body)
        val senderMediaLayout = layout.findViewById<MaterialCardView>(R.id.sender_media_layout)
        val senderRadius = Messenger.senderMessageRadius.px.toFloat()
        senderMediaLayout.radius = senderRadius
        senderBody.background = getShape(senderRadius, Messenger.senderBackgroundColor)

        val selfBody = layout.findViewById<TextView>(R.id.self_body)
        val selfMediaLayout = layout.findViewById<MaterialCardView>(R.id.self_media_layout)
        val selfRadius = Messenger.selfMessageRadius.px.toFloat()
        selfMediaLayout.radius = selfRadius
        selfBody.background = getShape(selfRadius, Messenger.selfBackgroundColor)

        return MyViewHolder(layout)
    }

    // Return shape drawable with corner radius and background color
    // radius in pixels
    // color in hex string #FFFFFF
    private fun getShape(radius: Float, color: String): ShapeDrawable {
        val shape = ShapeDrawable(RoundRectShape(floatArrayOf(radius, radius, radius, radius, radius, radius, radius, radius), null, null))
        shape.getPaint().setColor(Color.parseColor(color))
        return shape
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
        val message = dataset.get(position)
        val prevMessage = if (position - 1 >= 0) dataset.get(position - 1) else null

        // Timestamp message grouping
        val timestamp = holder.layout.findViewById<TextView>(R.id.timestamp)
        timestamp.visibility = View.GONE
        // Group by hour-elapsed
        if (position == 0 || (prevMessage != null && hourTimeElapsed(prevMessage, message))) {
            timestamp.text = HtmlCompat.fromHtml(
                getTodayYestDateFromMilli(message.timeCreated.toLong()),
                FROM_HTML_MODE_COMPACT
            )
            timestamp.visibility = View.VISIBLE
        }

        val selfLayout = holder.layout.findViewById<ConstraintLayout>(R.id.self_layout)
        val selfBody = holder.layout.findViewById<TextView>(R.id.self_body)
        val selfMediaLayout = holder.layout.findViewById<MaterialCardView>(R.id.self_media_layout)
        val selfMediaLoading = holder.layout.findViewById<ProgressBar>(R.id.self_media_loading)
        val selfMedia = holder.layout.findViewById<ImageView>(R.id.self_media)
        val selfDelivered = holder.layout.findViewById<TextView>(R.id.self_delivered)
//        val selfDelivered = holder.layout.findViewById<ImageView>(R.id.self_delivered)
        val selfError = holder.layout.findViewById<ImageView>(R.id.self_body_error)
        val selfTimestamp = holder.layout.findViewById<TextView>(R.id.self_timestamp)

        val senderLayout = holder.layout.findViewById<ConstraintLayout>(R.id.sender_layout)
        val senderIconLayout = holder.layout.findViewById<MaterialCardView>(R.id.sender_icon_layout)
        val senderName = holder.layout.findViewById<TextView>(R.id.sender_name)
        val senderBody = holder.layout.findViewById<TextView>(R.id.sender_body)
        val senderMediaLayout =
            holder.layout.findViewById<MaterialCardView>(R.id.sender_media_layout)
        val senderMedia = holder.layout.findViewById<ImageView>(R.id.sender_media)
        val senderTimestamp = holder.layout.findViewById<TextView>(R.id.sender_timestamp)

        selfBody.visibility = View.GONE
        selfMediaLayout.visibility = View.GONE

        senderBody.visibility = View.GONE
        senderMediaLayout.visibility = View.GONE

        // Thumbnail click listeners for expanded image
        selfMedia.setOnClickListener {
//            (context as MessageActivity).showExpandedImage(   // TODO
            fragment?.showExpandedImage(
                it as ImageView,
                message.media!!.content.source
            )
        }
        senderMedia.setOnClickListener {
//            (context as MessageActivity).showExpandedImage(   // TODO
            fragment?.showExpandedImage(
                it as ImageView,
                message.media!!.content.source
            )
        }

        // Message body click listeners for reactions popup menu
        selfBody.setOnLongClickListener {
            Log.d(TAG, "selfBody OnLongClick")
//            addPopup(it, message)
            showBottomSheetDialog(it, message)
            true
        }
        selfMedia.setOnLongClickListener {
            Log.d(TAG, "selfMedia OnLongClick")
//            addPopup(it, message)
            showBottomSheetDialog(it, message)
            true
        }
        senderBody.setOnLongClickListener {
            Log.d(TAG, "senderBody OnLongClick")
//            addPopup(it, message)
            showBottomSheetDialog(it, message)
            true
        }
        senderMedia.setOnLongClickListener {
            Log.d(TAG, "senderMedia OnLongClick")
//            addPopup(it, message)
            showBottomSheetDialog(it, message)
            true
        }
//        Picasso.get().setLoggingEnabled(true)
        // Set Message
        // Check if message is from a sender or self
        if (message.sender.userId.equals(prefs?.externalUserId, ignoreCase = true)) {
            // Show self layout, hide sender
            selfLayout.visibility = View.VISIBLE
            senderLayout.visibility = View.GONE

//            Log.d(TAG, "message body media ${message.body} ${message.media}")
            message.media?.thumbnail?.let {
                // Show message media if not null
                // Check tag to prevent reload of image on Delivered status
//                selfMedia.tag?.let {
                    if (!message.metadata.localReferenceId.equals(
                            selfMedia.tag
                        )
                    ) {
                        Picasso.get().load(message.media?.thumbnail?.source).resize(0, 800)
                            .into(selfMedia)
                    }
//                }
                // Set tag to prevent reload of image on Delivered status
                selfMedia.tag = message.metadata.localReferenceId

                // Progress loader
                // If messageId is null/blank, message has not been sent
                // messageId is updated when message:published
                selfMediaLoading.visibility = View.GONE
                if(message.messageId.isNullOrBlank()) {
                    selfMediaLoading.visibility = View.VISIBLE
                }

                selfMediaLayout.visibility = View.VISIBLE
            }
            // Sender, body text
            if (!message.body.equals("Sent an image", ignoreCase = true)) {
                // Hide sender body if messaage is media
//                selfBody.text = message.body
//                selfBody.setText( // TODO
//                    HtmlCompat.fromHtml(
//                        processMentions(message),
//                        HtmlCompat.FROM_HTML_MODE_COMPACT
//                    )
//                ) // TODO
                selfBody.text = message.body
//                selfBody.makeLinks(mutableListOf<Pair<String, ViewOnClickListener>>)
                selfBody.makeLinks(getMentionPairs(message))
                selfBody.visibility = View.VISIBLE
            }

            // Self reactions
            val selfReactionsView = holder.layout.findViewById<TextView>(R.id.self_reactions)
            selfReactionsView.visibility = View.GONE
            selfReactionsView.text = ""
            message.reactionSummary?.reactioN_CODE_LIKE?.let {
                selfReactionsView.text = "\uD83D\uDC4D"
                if (message.reactionSummary?.reactioN_CODE_LIKE!!.count > 1) selfReactionsView.text =
                    "${selfReactionsView.text} ${message.reactionSummary?.reactioN_CODE_LIKE?.count}"
                selfReactionsView.visibility = View.VISIBLE
            }
            message.reactionSummary?.reactioN_CODE_DISLIKE?.let {
                if (!selfReactionsView.text.isBlank()) selfReactionsView.text =
                    "${selfReactionsView.text}    "
                selfReactionsView.text = "${selfReactionsView.text}\uD83D\uDC4E"
                if (message.reactionSummary?.reactioN_CODE_DISLIKE!!.count > 1) selfReactionsView.text =
                    "${selfReactionsView.text} ${message.reactionSummary?.reactioN_CODE_DISLIKE?.count}"
                selfReactionsView.visibility = View.VISIBLE
            }

            // Delivered status
            selfDelivered.visibility = View.GONE
            message.messageId?.let {
                // If latest message with messageId
                if (isLatestSelfMessage(message)) {
                    // Show Delivered status if messagedId exists
                    selfDelivered.visibility = View.VISIBLE   // TODO Uncomm
                }
            }

            // Self message error
            selfError.visibility = View.INVISIBLE
            if(message.metadata.error) {
                selfError.visibility = View.VISIBLE
            }

            // Self timestamp
            selfTimestamp.text = getTimeFromMilli(message.timeCreated.toLong())
        } else {
            // Sender
            // Show sender layout, hide self
            selfLayout.visibility = View.GONE
            senderLayout.visibility = View.VISIBLE

            // Icon
            if (!isSameSender(position)) {
                // Show icon
                val icon = holder.layout.findViewById<ImageView>(R.id.sender_icon)
                if(message.sender.profileImageUrl.isNotBlank()) {   // For User typing...
                    Picasso.get().load(message.sender.profileImageUrl).into(icon)
                    senderIconLayout.visibility = View.VISIBLE
                } else {
                    senderIconLayout.visibility = View.GONE
                }

                // Show display name
                senderName.text = message.sender.displayName
                senderName.visibility = View.VISIBLE

                // Sender's user profile link on icon
                icon.setOnClickListener {
                    fragment?.showProfile(message.sender)
                }
            } else {
                senderIconLayout.visibility = View.INVISIBLE
                senderName.visibility = View.GONE
            }

//            Log.d(TAG, "media ${message.messageId} ${message.media}")
            message.media?.thumbnail?.let {
                // Show message media if not null
                Picasso.get().load(message.media?.thumbnail?.source).resize(0, 800).into(senderMedia)
                senderMediaLayout.visibility = View.VISIBLE
            }
            // Sender, body text
            if (message.body.isNotBlank() && !message.body.equals("Sent an image", ignoreCase = true)) {   // message.body.isNotBlank() for User typing...
                // Hide sender body if messaage is media
//                senderBody.text = message.body
//                senderBody.setText(
//                    HtmlCompat.fromHtml(
//                        processMentions(message),
//                        HtmlCompat.FROM_HTML_MODE_COMPACT
//                    )
//                )
                senderBody.text = message.body
                senderBody.makeLinks(getMentionPairs(message))
                senderBody.visibility = View.VISIBLE
            }

            // Sender reactions
            val senderReactionsView = holder.layout.findViewById<TextView>(R.id.sender_reactions)
            senderReactionsView.visibility = View.GONE
            senderReactionsView.text = ""
            message.reactionSummary?.reactioN_CODE_LIKE?.let {
                senderReactionsView.text = "\uD83D\uDC4D"
                if (message.reactionSummary?.reactioN_CODE_LIKE!!.count > 1) senderReactionsView.text =
                    "${senderReactionsView.text} ${message.reactionSummary?.reactioN_CODE_LIKE?.count}"
                senderReactionsView.visibility = View.VISIBLE
            }
            message.reactionSummary?.reactioN_CODE_DISLIKE?.let {
                if (!senderReactionsView.text.isBlank()) senderReactionsView.text =
                    "${senderReactionsView.text}    "
                senderReactionsView.text = "${senderReactionsView.text}\uD83D\uDC4E"
                if (message.reactionSummary?.reactioN_CODE_DISLIKE!!.count > 1) senderReactionsView.text =
                    "${senderReactionsView.text} ${message.reactionSummary?.reactioN_CODE_DISLIKE?.count}"
                senderReactionsView.visibility = View.VISIBLE
            }

            // Sender timestamp
            senderTimestamp.text = getTimeFromMilli(message.timeCreated.toLong())
        }

        // Status messages, eg. User1 added someone to the conversation
        val statusMessageLayout = holder.layout.findViewById<TextView>(R.id.status_message)
        statusMessageLayout.visibility = View.GONE
        if(!message.metadata.statusMessage.isNullOrBlank()) {
            Log.d(TAG, "")
            timestamp.visibility = View.GONE
            selfLayout.visibility = View.GONE
            senderLayout.visibility = View.GONE

            statusMessageLayout.text = message.metadata.statusMessage
            statusMessageLayout.visibility = View.VISIBLE
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataset.size

    fun getTimeFromMilli(msgTimeMillis: Long): String {
        val messageTime = Calendar.getInstance()
        messageTime.timeInMillis = msgTimeMillis
        val strTimeFormat = "h:mm aa"
        return DateFormat.format(strTimeFormat, messageTime).toString()
    }

    fun getTodayYestDateFromMilli(msgTimeMillis: Long): String {
        val messageTime = Calendar.getInstance()
        messageTime.timeInMillis = msgTimeMillis
        val now = Calendar.getInstance()
        val strTimeFormat = "h:mm aa"
        val strDateFormat = "M/d/yy"
        val strDayFormat = "EEE"
        val shortDateFormat = "MMM d"
        return if (now[Calendar.DATE] === messageTime[Calendar.DATE] &&
            now[Calendar.MONTH] === messageTime[Calendar.MONTH]
            &&
            now[Calendar.YEAR] === messageTime[Calendar.YEAR]
        ) {
            "<b>Today,</b> ${DateFormat.format(strTimeFormat, messageTime)}"
        } else if (now[Calendar.DATE] - messageTime[Calendar.DATE] === 1
            &&
            now[Calendar.MONTH] === messageTime[Calendar.MONTH]
            &&
            now[Calendar.YEAR] === messageTime[Calendar.YEAR]
        ) {
            "<b>Yesterday,</b> ${DateFormat.format(strTimeFormat, messageTime)}"
        } else
        // Less than 7 days (a week)
//            if(now[Calendar.DATE] - messageTime[Calendar.DATE] < 7
//                &&
//                now[Calendar.MONTH] === messageTime[Calendar.MONTH]
//                &&
//                now[Calendar.YEAR] === messageTime[Calendar.YEAR])
        {
            "<b>${DateFormat.format(
                strDayFormat,
                messageTime
            )}, ${DateFormat.format(shortDateFormat, messageTime)},</b> ${DateFormat.format(
                strTimeFormat,
                messageTime
            )}"
        }
//        else {
////            DateFormat.format(strDateFormat, messageTime).toString()
//            "<b>${DateFormat.format(strDayFormat, messageTime)}, ${DateFormat.format(shortDateFormat, messageTime)}</b>"
//        }
    }

    // Returns true if more than an hour has elapsed between messages
    fun hourTimeElapsed(
        previousMessage: Message,
        message: Message
    ): Boolean {
        val prevMessageTime = Calendar.getInstance()
        prevMessageTime.timeInMillis = previousMessage.timeCreated.toLong()
        val messageTime = Calendar.getInstance()
        messageTime.timeInMillis = message.timeCreated.toLong()

        val timeDiffInMillis = messageTime.timeInMillis - prevMessageTime.timeInMillis
//        Log.d(TAG, "prevmsg ${prevMessageTime[Calendar.HOUR]} msg ${messageTime[Calendar.HOUR]}")
        val timeDiff =
            timeDiffInMillis / (1000 * 60 * 60f) //  1000 ms to sec, 60 sec to min, 60 min to hour
//        Log.d(TAG, "${timeDiff}")

        return timeDiff > 1.0
    }

    // Returns true if current message's sender is the same as the one following it
    fun isSameSender(position: Int): Boolean {
        if (position < dataset.size - 1 &&
            dataset.get(position).sender.userId.equals(dataset.get(position + 1).sender.userId, ignoreCase = true)
        ) {
            return true
        }
        return false
    }

//    private fun addPopup(view: View, message: Message) {
//        // Popup menu
//        val popup = PopupMenu(context, view)
//        //inflating menu from xml resource
//        popup.inflate(R.menu.popup_reactions)
//        //adding click listener
//        popup.setOnMenuItemClickListener(object : PopupMenu.OnMenuItemClickListener {
//            override fun onMenuItemClick(item: MenuItem): Boolean {
//                return when (item.getItemId()) {
//                    R.id.reaction_like -> { //handle menu1 click
//                        Log.d(TAG, "Liked messageId ${message.messageId}")
//                        Log.d(TAG, "Liked conversationId ${message.conversationId}")
//                        (context as MessageActivity).setReaction(
//                            message,
//                            context.getResources().getString(R.string.reaction_code_like)
//                        )
//                        true
//                    }
//                    R.id.reaction_dislike -> { //handle menu2 click
//                        Log.d(TAG, "Disliked ${message.messageId}")
//                        (context as MessageActivity).setReaction(
//                            message,
//                            context.getResources().getString(R.string.reaction_code_dislike)
//                        )
//                        true
//                    }
//                    else -> false
//                }
//            }
//        })
//        //displaying the popup
//        popup.show()
//    }

    private fun showBottomSheetDialog(view: View, message: Message) {
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

        // Reaction, Like
        likeBtn!!.setOnClickListener {
//            (context as MessageActivity).setReaction( // TODO
            fragment?.setReaction(
                message,
                context.getResources().getString(R.string.reaction_code_like)
            )
            Handler().postDelayed({
                dialog.dismiss()
            }, 200)
        }

        // Reaction, Dislike
        dislikeBtn!!.setOnClickListener {
//            (context as MessageActivity).setReaction( // TODO
            fragment?.setReaction(
                message,
                context.getResources().getString(R.string.reaction_code_dislike)
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
//                (context as MessageActivity).copyText(message, it)    // TODO
                fragment?.copyText(message, it)
                Handler().postDelayed({
                    dialog.dismiss()
                }, 200)
            }
        } else {
            // Show Save
            saveMedia!!.visibility = View.VISIBLE
            saveMedia!!.setOnClickListener {
//                (context as MessageActivity).saveMedia(message, it)   // TODO
                fragment?.saveMedia(message, it)
                Handler().postDelayed({
                    dialog.dismiss()
                }, 200)
            }
        }

        if (!message.sender.userId.equals(prefs?.externalUserId, ignoreCase = true)) {
            // Report
            report!!.visibility = View.VISIBLE
            report!!.setOnClickListener {
//                (context as MessageActivity).reportMessage(message, it)   // TODO
                fragment?.reportMessage(message, it)
                Handler().postDelayed({
                    dialog.dismiss()
                }, 200)
            }
        } else {
            // Delete
            delete!!.visibility = View.VISIBLE
            delete!!.setOnClickListener {
//                (context as MessageActivity).deleteMessage(message, it)   // TODO
                fragment?.deleteMessage(message, it)
                Handler().postDelayed({
                    dialog.dismiss()
                }, 200)
            }
        }

        // Error message
        if(!message.metadata.errorMessage.isNullOrBlank()) {
            likeBtn.visibility = View.GONE
            dislikeBtn.visibility = View.GONE
//            copyText!!.visibility = View.GONE
            copyText!!.visibility = View.VISIBLE

            errorMessage!!.text = message.metadata.errorMessage
            errorMessage!!.visibility = View.VISIBLE

            delete!!.visibility = View.VISIBLE
            delete!!.text = "Remove"
            delete!!.setOnClickListener {
//                (context as MessageActivity).deleteMessage(message, it)   // TODO
                fragment?.deleteMessageFromList(message)
                Handler().postDelayed({
                    dialog.dismiss()
                }, 200)
            }
        }

        dialog.show()
    }

    fun processMentions(message: Message): String {
        val words = message.body.split(" ").toMutableList()
        words.forEachIndexed { index, word ->
            val found = message.mentions.find { mention -> mention.mentionTextPattern.equals(word, ignoreCase = true) }
            found?.let {
                words.set(index, "<font color='#e3f2fd'>${word}</font>")
            }
        }
        return words.joinToString(separator = " ")
    }

    // selfBody.makeLinks(mutableListOf<Pair<String, ViewOnClickListener>>)
    // my_text_view.makeLinks(
    //        Pair("Terms of Service", View.OnClickListener {
    //            Toast.makeText(applicationContext, "Terms of Service Clicked", Toast.LENGTH_SHORT).show()
    //        }),
    //        Pair("Privacy Policy", View.OnClickListener {
    //            Toast.makeText(applicationContext, "Privacy Policy Clicked", Toast.LENGTH_SHORT).show()
    //        }))
    fun getMentionPairs(message: Message): MutableList<Pair<String, View.OnClickListener>> {
//        Log.d(TAG, "getMentionPairs ${message}")
        val pairs = mutableListOf<Pair<String, View.OnClickListener>>()
        for(mention in message.mentions) {
            val pair = Pair(mention.mentionTextPattern, View.OnClickListener { fragment?.showProfile(mention.user) })
            pairs.add(pair)
//            Log.d(TAG, "pair ${pair}")
        }
        return pairs
    }

    fun isLatestSelfMessage(message: Message): Boolean {
        val found =
            dataset.findLast { it.messageId != null && it.sender.userId.equals(message.sender.userId, ignoreCase = true) }
        return found?.metadata?.localReferenceId.equals(message.metadata.localReferenceId, ignoreCase = true)
    }

    fun TextView.makeLinks(links: MutableList<Pair<String, View.OnClickListener>>) {
//        Log.d(TAG, "this.text ${this.text}")
        val spannableString = SpannableString(this.text)
        for (link in links) {
            if(!this.text.contains(link.first)) {
                continue
            }

            val clickableSpan = object : ClickableSpan() {
                override fun onClick(view: View) {
                    Selection.setSelection((view as TextView).text as Spannable, 0)
                    view.invalidate()
                    link.second.onClick(view)
                }

                override fun updateDrawState(ds: TextPaint) {
                    super.updateDrawState(ds)
                    ds.setUnderlineText(false)  // Remove underline text
                }
            }

//            Log.d(TAG, "${this.text.toString()}")
//            Log.d(TAG, "${this.text.toString().indexOf(link.first)}")
            val startIndexOfLink = this.text.toString().indexOf(link.first)
            spannableString.setSpan(clickableSpan, startIndexOfLink, startIndexOfLink + link.first.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        this.movementMethod = LinkMovementMethod.getInstance() // without LinkMovementMethod, link can not click
        this.setText(spannableString, TextView.BufferType.SPANNABLE)
    }

    // Extensions for dp-px conversion
    val Int.dp: Int
        get() = (this / Resources.getSystem().displayMetrics.density).toInt()
    val Int.px: Int
        get() = (this * Resources.getSystem().displayMetrics.density).toInt()
}