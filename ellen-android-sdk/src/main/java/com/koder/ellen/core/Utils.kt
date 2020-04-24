package com.koder.ellen.core

import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RoundRectShape
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.koder.ellen.model.Conversation
import okhttp3.MediaType.Companion.toMediaType
import java.text.SimpleDateFormat
import java.util.*


internal class Utils {
    companion object {
        val MEDIA_TYPE_JSON = "application/json; charset=utf-8".toMediaType()

        fun filterConversationsByState(list: MutableList<Conversation>, state: Int): MutableList<Conversation> {
            val filteredList = list.filter { it.state == state }
            return filteredList.toMutableList()
        }

        fun sortConversationsByLatestMessage(conversations: MutableList<Conversation>): MutableList<Conversation> {

            val latestMessageMap: MutableMap<Conversation, Long> = mutableMapOf()
            val noMessageMap: MutableMap<Conversation, Long> = mutableMapOf()
            val sortedConversationList = mutableListOf<Conversation>()

            for (conversation in conversations) {
                if(conversation.messages.isEmpty()) {
                    noMessageMap.put(conversation, conversation.timeCreated.toLong())
                    continue
                }
                latestMessageMap.put(conversation, conversation.messages.first().timeCreated!!.toLong()) // First message should be latest, from sort: -1
            }

            val sortedMessageMap = sortByDescending(latestMessageMap)
            val sortedNoMessageMap = sortByDescending(noMessageMap)

            addConversationsToList(sortedNoMessageMap, sortedConversationList)
            addConversationsToList(sortedMessageMap, sortedConversationList)

            return sortedConversationList
        }

        // Return shape drawable with corner radius and background color
        // radius in pixels
        // color in hex string #FFFFFF
        fun getShape(topLeft: Float, topRight: Float, bottomRight: Float, bottomLeft: Float, color: String): ShapeDrawable {
            val shape = ShapeDrawable(RoundRectShape(floatArrayOf(topLeft, topLeft, topRight, topRight, bottomRight, bottomRight, bottomLeft, bottomLeft), null, null))
            shape.getPaint().setColor(Color.parseColor(color))
            return shape
        }

        fun convertDateToLong(date: String): Long {
            val df = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            df.setTimeZone(TimeZone.getTimeZone("UTC"))
            return df.parse(date).time
        }

        private fun sortByDescending(messageMap: MutableMap<Conversation, Long>): Map<Conversation, Long> {
            return messageMap.toList().sortedByDescending { (_, value) -> value }.toMap()
        }

        private fun addConversationsToList(map: Map<Conversation, Long>, list: MutableList<Conversation>) {
            for (entry in map) {
                list.add(entry.key)
            }
        }
    }
}