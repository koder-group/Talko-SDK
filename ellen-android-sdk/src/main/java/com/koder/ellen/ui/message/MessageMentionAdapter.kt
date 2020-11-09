package com.koder.ellen.ui.message

import android.content.Context
import android.os.Build
import android.os.Handler
import android.text.Html
import android.text.format.DateFormat
import android.transition.Slide
import android.transition.TransitionManager
import android.util.Log
import android.view.*
import android.widget.*
import androidx.annotation.Nullable
import androidx.appcompat.widget.PopupMenu
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.text.HtmlCompat
import androidx.core.text.HtmlCompat.FROM_HTML_MODE_COMPACT
import androidx.core.widget.PopupWindowCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.card.MaterialCardView
import com.google.android.material.internal.VisibilityAwareImageButton
import com.koder.ellen.Messenger
import com.koder.ellen.R
import com.koder.ellen.model.User
import com.koder.ellen.screen.MessageScreen
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item_message_mention.view.*
import java.util.*


internal class MessageMentionAdapter(private val context: Context, private val dataset: MutableList<User>, private val fragment: Fragment? = null) :
    RecyclerView.Adapter<MessageMentionAdapter.MyViewHolder>() {

    val TAG = "MessageMentionAdapter"

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
            .inflate(R.layout.item_message_mention, parent, false) as ConstraintLayout
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

        val user = dataset.get(position)

        val layout = holder.layout
        val name = holder.layout.mention_text
        val icon = holder.layout.mention_icon

        name.text = user.displayName

        // Get latest profile image url
        val imageUrl = getLatestProfileImageUrl(user)

        Picasso.get().load(imageUrl).into(icon)

        layout.setOnClickListener {
            Handler().postDelayed({
//                (context as MessageActivity).mentionUser(it as ConstraintLayout, user)    // TODO
                if(fragment is MessageFragment) {
                    fragment?.mentionUser(it as ConstraintLayout, user)
                }
                if(fragment is MessageScreen) {
                    fragment?.mentionUser(it as ConstraintLayout, user)
                }
            }, 200)
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataset.size

    private fun getLatestProfileImageUrl(user: User): String {
        val cachedProfile = Messenger.userProfileCache.get(user.userId.toLowerCase())
        if(cachedProfile != null) {
            return cachedProfile.photoUrl
        }
        return user.profileImageUrl
    }

    fun setData(newList: List<User>) {
        val diffCallback = DiffCallback(dataset, newList)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        dataset.clear()
        dataset.addAll(newList)
        diffResult.dispatchUpdatesTo(this)
    }

    class DiffCallback(private val oldList: List<User>, private val newList: List<User>) : DiffUtil.Callback() {

        override fun getOldListSize(): Int = oldList.size

        override fun getNewListSize(): Int = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].userId === newList.get(newItemPosition).userId
        }

//        @Parcelize
//        data class User(
//            val tenantId: String,
//            val userId: String,
//            val displayName: String,
//            val profileImageUrl: String,
//            var role: Int = 0
//        ) : Parcelable
        override fun areContentsTheSame(oldPosition: Int, newPosition: Int): Boolean {
            val (_, userId, _, _, _) = oldList[oldPosition]
            val (_, userId1, _, _, _) = newList[newPosition]

            return userId == userId1
        }

        @Nullable
        override fun getChangePayload(oldPosition: Int, newPosition: Int): Any? {
            return super.getChangePayload(oldPosition, newPosition)
        }
    }
}