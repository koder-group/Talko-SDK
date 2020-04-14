package com.koder.ellen.core

import android.graphics.Color
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RoundRectShape
import com.koder.ellen.model.Conversation
import okhttp3.MediaType.Companion.toMediaType

internal class Utils {
    companion object {
        val MEDIA_TYPE_JSON = "application/json; charset=utf-8".toMediaType()

        fun filterConversationsByState(list: MutableList<Conversation>, state: Int): MutableList<Conversation> {
            val filteredList = list.filter { it.state == state }
            return filteredList.toMutableList()
        }

        // Return shape drawable with corner radius and background color
        // radius in pixels
        // color in hex string #FFFFFF
        fun getShape(topLeft: Float, topRight: Float, bottomRight: Float, bottomLeft: Float, color: String): ShapeDrawable {
            val shape = ShapeDrawable(RoundRectShape(floatArrayOf(topLeft, topLeft, topRight, topRight, bottomRight, bottomRight, bottomLeft, bottomLeft), null, null))
            shape.getPaint().setColor(Color.parseColor(color))
            return shape
        }
    }
}