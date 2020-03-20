package com.koder.ellen

import android.content.Context
import android.widget.Toast

class ToasterMessage {
    companion object {
        fun createToast(context: Context, message: String) {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
    }
}