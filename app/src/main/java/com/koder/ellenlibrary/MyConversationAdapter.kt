package com.koder.ellenlibrary

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.koder.ellen.Messenger
import com.koder.ellen.model.Conversation
import com.koder.ellen.screen.ConversationScreen
import com.koder.ellen.ui.conversation.ConversationAdapter
import org.w3c.dom.Text

class MyConversationAdapter(
    private val context: Context,
    private val dataset: MutableList<Conversation>,
    private val fragment: Fragment
) : ConversationAdapter(context, dataset, fragment) {

    companion object {
        const val TAG = "MyConversationAdapter"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val layout = LayoutInflater.from(parent.context)
            .inflate(R.layout.my_item_conversations, parent, false) as ConstraintLayout
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

        return MyViewHolder(layout)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)

        Log.d(TAG, "${dataset[position].metadata}")

        // Badge
        val classBadge = holder.layout.findViewById<TextView>(R.id.class_badge)
        val passBadge = holder.layout.findViewById<TextView>(R.id.pass_badge)

        if(!dataset[position].metadata.classId.isNullOrEmpty() || !dataset[position].metadata.entityType.equals("Class", ignoreCase = true)) {
            // Class
            classBadge.visibility = View.VISIBLE
            passBadge.visibility = View.GONE
        }

        if(dataset[position].metadata.entityType.equals("Subscription", ignoreCase = true)) {
            // Pass
            classBadge.visibility = View.GONE
            passBadge.visibility = View.VISIBLE
        }

        val cardView = holder.layout.findViewById<MaterialCardView>(R.id.conversation_icon_layout)
        cardView.strokeWidth = 1.px
    }

    override fun showNewMessageIndicators(layout: View) {
        super.showNewMessageIndicators(layout)

        val title = layout.findViewById<TextView>(R.id.conversation_title)
        title.typeface = ResourcesCompat.getFont(context, R.font.product_sans_bold)
    }

    override fun hideNewMessageIndicators(layout: View) {
        super.hideNewMessageIndicators(layout)

        val title = layout.findViewById<TextView>(R.id.conversation_title)
        title.typeface = ResourcesCompat.getFont(context, R.font.product_sans_regular)
    }
}