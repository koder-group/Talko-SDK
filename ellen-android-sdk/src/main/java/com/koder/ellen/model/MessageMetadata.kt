package com.koder.ellen.model

data class MessageMetadata(
    var localReferenceId: String = "",
    var error: Boolean = false,
    var errorMessage: String = "",
    var statusMessage: String = "",
    var classId: String = "",
    var entityId: String = "",
    var entityType: String = "",
    var title: String = ""
)