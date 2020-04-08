package com.koder.ellen.ui.message

import android.content.Context
import android.content.res.Resources
import android.view.*
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.koder.ellen.R
import com.koder.ellen.model.Message


internal class MediaAdapter(private val context: Context, private val dataset: MutableList<Message>, private val fragment: MessageFragment?) :
    RecyclerView.Adapter<MediaAdapter.MyViewHolder>() {

    val TAG = "MessageAdapter"

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
            .inflate(R.layout.item_media, parent, false) as RelativeLayout
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
//        val mediaItemLayout = holder.layout.findViewById<RelativeLayout>(R.id.media_item_layout)
//
//        if (position == 0){
//            val params = mediaItemLayout.layoutParams as RecyclerView.LayoutParams
//            params.leftMargin = 8.px
//            mediaItemLayout.layoutParams = params
//        }
//        if (position == dataset.size-1) {
//            val params = mediaItemLayout.layoutParams as RecyclerView.LayoutParams
//            params.rightMargin = 8.px
//            mediaItemLayout.layoutParams = params
//        }


        val message = dataset.get(position)
        val closeView = holder.layout.findViewById<ImageView>(R.id.close_button_view)
        closeView.setOnClickListener {
            fragment?.removeMediaItem(message)
        }

        val mediaView = holder.layout.findViewById<ImageView>(R.id.media_item_image)
        Glide.with(context)
                .asBitmap()
                .load(message.media?.thumbnail?.source)
                .into(mediaView)

    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataset.size

    // Extensions for dp-px conversion
    val Int.dp: Int
        get() = (this / Resources.getSystem().displayMetrics.density).toInt()
    val Int.px: Int
        get() = (this * Resources.getSystem().displayMetrics.density).toInt()
}