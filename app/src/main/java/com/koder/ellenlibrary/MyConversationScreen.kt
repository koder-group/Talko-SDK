package com.koder.ellenlibrary

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.recyclerview.widget.RecyclerView
import com.koder.ellen.screen.ConversationScreen

class MyConversationScreen: ConversationScreen() {

    companion object {
        const val TAG = "MyConversationScreen"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
//        return super.onCreateView(inflater, container, savedInstanceState)
        val view = super.onCreateView(inflater, container, savedInstanceState)

        var recyclerView = getRecyclerView()
        val conversations = getConversations()
        val adapter = MyConversationAdapter(view.context, conversations, this@MyConversationScreen)
        setAdapter(adapter)
        recyclerView.adapter = adapter

        return view
    }

    override fun setEmptyPlaceholder(frame: FrameLayout) {
        val emptyView = LayoutInflater.from(activity).inflate(R.layout.my_placeholder, null)
        frame.addView(emptyView)
    }
}