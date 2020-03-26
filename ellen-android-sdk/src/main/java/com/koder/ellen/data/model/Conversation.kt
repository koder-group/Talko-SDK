package com.koder.ellen.data.model

data class Conversation(
    val conversationId: String,
    val tenantId: String,
    var title: String = "",
    var description: String = "",
    val type: Int,
    val participants: MutableList<Participant>,
    val state: Int,
    val metadata: Any,
    val accessPolicy: Any,
    var timeCreated: Number,
    var messages: MutableList<Message>
)