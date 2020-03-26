package com.koder.ellen.data.model

import java.util.*

data class Message(
    var messageId: String? = null,
    var conversationId: String = "",
    var tenantId: String? = null,
    var body: String = "",
    val sender: User,
    var state: Int? = null,
    var metadata: MessageMetadata,
    var accessPolicy: Any? = null,
    var timeCreated: Number = Calendar.getInstance().timeInMillis,
    var media: ConversationMedia? = null,
    var reactionSummary: ReactionSummary? = null,
    var mentions: MutableList<Mention> = mutableListOf()
)