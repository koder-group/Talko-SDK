package com.koder.ellen.model

data class Conversation(
    val conversationId: String,
    val tenantId: String,
    var title: String = "",
    var description: String = "",
    val type: Int,
    val participants: MutableList<Participant>,
    val state: Int,
    val metadata: MessageMetadata,
    val accessPolicy: Any,
    var timeCreated: Number,
    var messages: MutableList<Message> = mutableListOf()
)