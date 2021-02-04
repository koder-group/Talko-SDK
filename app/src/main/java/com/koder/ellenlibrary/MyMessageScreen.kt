package com.koder.ellenlibrary

import android.content.res.Configuration
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.graphics.drawable.DrawableCompat
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.koder.ellen.Messenger
import com.koder.ellen.model.Message
import com.koder.ellen.screen.ConversationScreen
import com.koder.ellen.screen.MessageScreen

class MyMessageScreen: MessageScreen() {

    companion object {
        const val TAG = "MyMessageScreen"
    }

    // 👍 😍 😉 🧐
    // REACTION_CODE_LIKE
    // REACTION_CODE_LOVE
    // REACTION_CODE_WINK
    // REACTION_CODE_NERDY

    override fun showBottomSheetDialog(view: View, message: Message) {
        val dialog = BottomSheetDialog(view.context)
//                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
//        dialog.setTitle("Title")
        dialog.setContentView(R.layout.my_dialog_message_actions)

        val likeBtn = dialog.findViewById<TextView>(R.id.like)
        val loveBtn = dialog.findViewById<TextView>(R.id.love)
        val winkBtn = dialog.findViewById<TextView>(R.id.wink)
        val nerdyBtn = dialog.findViewById<TextView>(R.id.nerdy)

        val copyText = dialog.findViewById<TextView>(R.id.copy_text)
        val saveMedia = dialog.findViewById<TextView>(R.id.save_media)
        val report = dialog.findViewById<TextView>(R.id.report)
        val delete = dialog.findViewById<TextView>(R.id.delete)
        val errorMessage = dialog.findViewById<TextView>(R.id.error_message)

        // Dark mode
        val mode = context?.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)
        when (mode) {
            Configuration.UI_MODE_NIGHT_YES -> {
                val background = dialog.findViewById<ConstraintLayout>(R.id.dialog_bottom_sheet)
                background?.background = resources.getDrawable(R.drawable.dialog_round_top_dark)

                copyText?.setTextColor(resources.getColor(R.color.dmTextHigh))
                var drawables = copyText?.compoundDrawables
                drawables?.let {
                    for (drawable in it) {
                        drawable?.let {
                            DrawableCompat.setTint(it, resources.getColor(R.color.dmTextMed))
                        }
                    }
                }

                saveMedia?.setTextColor(resources.getColor(R.color.dmTextHigh))
                drawables = saveMedia?.compoundDrawables
                drawables?.let {
                    for (drawable in it) {
                        drawable?.let {
                            DrawableCompat.setTint(it, resources.getColor(R.color.dmTextMed))
                        }
                    }
                }

                errorMessage?.setTextColor(resources.getColor(R.color.dmTextHigh))
            }
        }

        // Reaction, Like
        likeBtn!!.setOnClickListener {
            setReaction(
                message,
                it.tag.toString()
            )
            Handler().postDelayed({
                dialog.dismiss()
            }, 200)
        }

        loveBtn!!.setOnClickListener {
            setReaction(
                message,
                it.tag.toString()
            )
            Handler().postDelayed({
                dialog.dismiss()
            }, 200)
        }


        winkBtn!!.setOnClickListener {
            setReaction(
                message,
                it.tag.toString()
            )
            Handler().postDelayed({
                dialog.dismiss()
            }, 200)
        }


        nerdyBtn!!.setOnClickListener {
            setReaction(
                message,
                it.tag.toString()
            )
            Handler().postDelayed({
                dialog.dismiss()
            }, 200)
        }

        // Show Copy text or Save (Image)
        if (message.media == null || message.media?.content == null || message.media?.thumbnail == null) {
            // Show Copy text
            copyText!!.visibility = View.VISIBLE
            copyText!!.setOnClickListener {
                copyText(message, it)
                Handler().postDelayed({
                    dialog.dismiss()
                }, 200)
            }
        } else {
            // Show Save
            saveMedia!!.visibility = View.VISIBLE
            saveMedia!!.setOnClickListener {
                saveMedia(message, it)
                Handler().postDelayed({
                    dialog.dismiss()
                }, 200)
            }
        }

        if (!message.sender.userId.equals(Messenger.getUserId(), ignoreCase = true)) {
            // Report
            report!!.visibility = View.VISIBLE
            report!!.setOnClickListener {
                reportMessage(message, it)
                Handler().postDelayed({
                    dialog.dismiss()
                }, 200)
            }
        } else {
            // Delete
            delete!!.visibility = View.VISIBLE
            delete!!.setOnClickListener {
                deleteMessage(message, it)
                Handler().postDelayed({
                    dialog.dismiss()
                }, 200)
            }
        }

        // Error message
        if(!message.metadata.errorMessage.isNullOrBlank()) {
            likeBtn.visibility = View.GONE
            loveBtn.visibility = View.GONE
            winkBtn.visibility = View.GONE
            nerdyBtn.visibility = View.GONE
//            dislikeBtn.visibility = View.GONE
//            copyText!!.visibility = View.GONE
            copyText!!.visibility = View.VISIBLE

            errorMessage!!.text = message.metadata.errorMessage
            errorMessage!!.visibility = View.VISIBLE

            delete!!.visibility = View.VISIBLE
            delete!!.text = "Remove"
            delete!!.setOnClickListener {
                deleteMessageFromList(message)
                Handler().postDelayed({
                    dialog.dismiss()
                }, 200)
            }
        }

        dialog.show()
    }
}