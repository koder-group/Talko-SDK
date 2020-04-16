package com.koder.ellen.ui.message

import android.content.Context
import android.os.Handler
import android.util.Log
import android.view.*
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.koder.ellen.Messenger.Companion.prefs
import com.koder.ellen.R
import com.koder.ellen.model.User
import com.koder.ellen.screen.MessageInfoScreen


internal class MessageInfoAdapter(private val context: Context, private val currentUser: User? = null, private val dataset: MutableList<User>, private val fragment: Fragment? = null) :
    RecyclerView.Adapter<MessageInfoAdapter.MyViewHolder>() {

    val TAG = "MessageInfoAdapter"

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
            .inflate(R.layout.item_message_info, parent, false) as ConstraintLayout
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

        val itemTitle = holder.layout.findViewById<TextView>(R.id.item_title)
        val ownerView = holder.layout.findViewById<TextView>(R.id.owner_view)

        // Participant
        val user = dataset.get(position) as User
        itemTitle.text = "${user.displayName}"

        // Set Owner
        Log.d(TAG, "${user}")
        if(user.role == 100) ownerView.visibility = View.VISIBLE

//                val userId = (dataset.get(position) as Array<String>)[1]
        holder.layout.setOnClickListener {
            Log.d(TAG, "${user}")

            val dialog = BottomSheetDialog(it.context)
//                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setTitle("Title")
            dialog.setContentView(R.layout.dialog_bottom_sheet)
            val nameView = dialog.findViewById<TextView>(R.id.name_view)
            val promoteView = dialog.findViewById<TextView>(R.id.promote_view)
            val removeModeratorView = dialog.findViewById<TextView>(R.id.remove_moderator_view)
            val removeView = dialog.findViewById<TextView>(R.id.remove_view)

            // Do not allow user to promote or remove themself
            if(user.userId.equals(prefs?.externalUserId, ignoreCase = true)) {
                promoteView?.visibility = View.GONE
                removeView?.visibility = View.GONE
            }

            // Hide Promote to moderator if user is owner or moderator
            // participantRoleEnumeration
            // 0 = default
            // 10 = moderator
            // 100 = owner
            if(user.role == 10 || user.role == 100) {
                promoteView?.visibility = View.GONE
            }

            // Hide Remove if Owner
            if(user.role == 100) {
                removeView?.visibility = View.GONE
            }

            // If current user is a regular user, hide actions
            if(currentUser?.role == 0) {
                promoteView?.visibility = View.GONE
                removeView?.visibility = View.GONE
            }

            // If current user is owner, show promote/remove Moderator
            if(currentUser?.role == 100) {
                when(user.role) {
                    0 -> {
                        promoteView?.visibility = View.VISIBLE
                        removeModeratorView?.visibility = View.GONE
                    }
                    10 -> {
                        promoteView?.visibility = View.GONE
                        removeModeratorView?.visibility = View.VISIBLE
                    }
                }
            }

            promoteView!!.setOnClickListener {
//                (context as MessageInfoActivity).promoteToModerator(user) // TODO

                Handler().postDelayed({
                    if(fragment is MessageInfoFragment) fragment?.addModerator(user)
                    if(fragment is MessageInfoScreen) fragment?.addModerator(user)
                    dialog.dismiss()
                }, 200)
            }

            removeModeratorView!!.setOnClickListener {
                Handler().postDelayed({
                    if(fragment is MessageInfoFragment) fragment?.removeModerator(user)
                    if(fragment is MessageInfoScreen) fragment?.removeModerator(user)
                    dialog.dismiss()
                }, 200)
            }

            removeView!!.setOnClickListener {
//                (context as MessageInfoActivity).removeFromConversation(user) // TODO

                Handler().postDelayed({
                    if(fragment is MessageInfoFragment) fragment?.removeFromConversation(user)
                    if(fragment is MessageInfoScreen) fragment?.removeFromConversation(user)
                    dialog.dismiss()
                }, 200)
            }

            nameView!!.text = itemTitle.text
            dialog.show()
        }


    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataset.size

    fun dpAsPixels(sizeInDp: Int): Int {
        val scale = context.getResources().getDisplayMetrics().density
        return (sizeInDp*scale + 0.5f).toInt()
    }
}