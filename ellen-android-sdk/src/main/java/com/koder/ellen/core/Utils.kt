package com.koder.ellen.core

import com.koder.ellen.model.Conversation
import okhttp3.MediaType.Companion.toMediaType

class Utils {
    companion object {
        val MEDIA_TYPE_JSON = "application/json; charset=utf-8".toMediaType()

        fun filterConversationsByState(list: MutableList<Conversation>, state: Int): MutableList<Conversation> {
            val filteredList = list.filter { it.state == state }
            return filteredList.toMutableList()
        }
    }
}