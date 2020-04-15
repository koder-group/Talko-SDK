package com.koder.ellenlibrary

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.koder.ellen.model.Conversation
import com.koder.ellen.screen.ConversationScreen

class ParentFragment: Fragment() {

    companion object {
        const val TAG = "ParentFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_frame, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

//        if (savedInstanceState == null) {
            val conversationScreen = ConversationScreen()
            getChildFragmentManager().beginTransaction().replace(
                R.id.frame_layout,
                conversationScreen,
                resources.getString(R.string.conversations)
            ).commit()

//        }

        // Conversation Screen click listener
        ConversationScreen.setItemClickListener(object: ConversationScreen.OnItemClickListener() {
            override fun OnItemClickListener(conversation: Conversation, position: Int) {
                Log.d(TAG, "OnItemClickListener")
                Log.d(TAG, "Conversation ${conversation}")
                Log.d(TAG, "Position ${position}")

                // Show Message Screens
//                val bundle = Bundle()
//                val messageScreen = MessageScreen()
//                bundle.putString("CONVERSATION_ID", conversation.conversationId)
//                messageScreen.setArguments(bundle)
//                getSupportFragmentManager().beginTransaction().replace(R.id.screenFrame, messageScreen, resources.getString(R.string.message)).addToBackStack(resources.getString(R.string.message)).commit()
            }
        })
    }
}