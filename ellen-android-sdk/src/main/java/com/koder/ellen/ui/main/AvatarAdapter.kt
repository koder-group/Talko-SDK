package com.koder.ellen.ui.main

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.recyclerview.widget.RecyclerView
import com.koder.ellen.MessengerActivity
import com.koder.ellen.R
import com.squareup.picasso.Picasso

internal class AvatarAdapter(private val context: Context, private val dataset: MutableList<String>) :
    RecyclerView.Adapter<AvatarAdapter.MyViewHolder>() {

    val TAG = "AvatarAdapter"

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder.
    // Each data item is just a string in this case that is shown in a TextView.
//    class MyViewHolder(val textView: TextView) : RecyclerView.ViewHolder(textView)
    class MyViewHolder(val layout: RelativeLayout) : RecyclerView.ViewHolder(layout)

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyViewHolder {
        // create a new view
//        val textView = LayoutInflater.from(parent.context)
//            .inflate(R.layout.item_conversations, parent, false) as TextView
        val layout = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_grid, parent, false) as RelativeLayout
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
        val avatarView = holder.layout.findViewById<ImageView>(R.id.item_image)
        Picasso.get().load(dataset.get(position)).into(avatarView)

        avatarView?.setOnClickListener {
            (context as MessengerActivity).setAvatar(dataset.get(position))
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataset.size
}