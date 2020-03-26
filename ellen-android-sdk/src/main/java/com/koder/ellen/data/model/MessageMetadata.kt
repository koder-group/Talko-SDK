package com.koder.ellen.data.model

data class MessageMetadata(
    var localReferenceId: String = "",
    var error: Boolean = false,
    var errorMessage: String = "",
    var statusMessage: String = ""
)