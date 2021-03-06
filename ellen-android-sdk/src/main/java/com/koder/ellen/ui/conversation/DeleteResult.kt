package com.koder.ellen.ui.conversation

import com.koder.ellen.model.Conversation

/**
 * Delete result : Conversation and deleted result.
 */
internal data class DeleteResult(
    var conversation: Conversation,
    var deleted: Boolean = false
)
