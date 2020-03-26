package com.koder.ellen.core

import com.koder.ellen.model.Conversation

class Utils {
    companion object {
        fun filterConversationsByState(list: MutableList<Conversation>, state: Int): MutableList<Conversation> {
            val filteredList = list.filter { it.state == state }
            return filteredList.toMutableList()
        }
    }
}