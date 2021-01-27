package com.koder.ellenlibrary

import android.view.LayoutInflater
import android.widget.FrameLayout
import com.koder.ellen.screen.ConversationScreen

class myConversationScreen: ConversationScreen() {

    companion object {
        const val TAG = "myConversationScreen"
    }

    override fun setEmptyPlaceholder(frame: FrameLayout) {
        val emptyView = LayoutInflater.from(activity).inflate(R.layout.my_placeholder, null)
        frame.addView(emptyView)
    }
}