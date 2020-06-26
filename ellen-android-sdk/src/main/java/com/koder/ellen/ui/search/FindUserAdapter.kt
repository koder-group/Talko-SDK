package com.koder.ellen.ui.search

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.koder.ellen.MessengerActivity
import com.squareup.picasso.Picasso
import com.koder.ellen.R
import com.koder.ellen.model.User
import com.koder.ellen.screen.AddParticipantScreen


internal class FindUserAdapter(private val context: Context, private val dataset: MutableList<User>, private val fragment: Fragment? = null) :
    RecyclerView.Adapter<FindUserAdapter.MyViewHolder>() {

    val TAG = "FindUserAdapter"

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
            .inflate(R.layout.item_users, parent, false) as ConstraintLayout
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
        val userProfileImage = holder.layout.findViewById<ImageView>(R.id.user_profile_image)
        val userName = holder.layout.findViewById<TextView>(R.id.user_name)

        Picasso.get().load(user.profileImageUrl).into(userProfileImage)
        userName.text = user.displayName

        // Dark mode
        val mode = context?.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)
        when (mode) {
            Configuration.UI_MODE_NIGHT_YES -> { userName.setTextColor(context?.resources!!.getColor(R.color.dmTextHigh)) }
        }

        holder.layout.setOnClickListener { view -> {}
//            Toast.makeText(holder.layout.context, "${position}", Toast.LENGTH_SHORT).show()
//            Log.d(TAG, "${view.isSelected}")
//            view.setSelected(true)

            Log.d(TAG, "user.userId ${user.userId}")
//            val publicId = getPublicIdFromImageUrl(user.profileImageUrl)  // TODO

//            Log.d(TAG, "publicIdFromImageUrl ${publicId}")

            // TODO Uncomment
//            (context as MainActivity).createConversationFromSearch(user.userId)

            // Communicate with host Activity to show MessageFragment
//            (context as MainActivity).showMessageFragment(dataset.get(position))

//            // Add Participant
            if(fragment is FindUserFragment)
                fragment?.messageViewModel?.addParticipant(user.userId, (context as MessengerActivity).getCurrentConversation()?.conversationId!!)

            if(fragment is AddParticipantScreen)
                fragment?.sendClick(user.userId, position)
//            fragment?.finishFragment()
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataset.size

    private fun getPublicIdFromImageUrl(imageUrl: String): String {
        val filename = imageUrl.split("/").last()
        val publicId = filename.split(".").first()
        return publicId
    }
}