package com.koder.ellen.model

data class User(
    val tenantId: String,
    val userId: String,
    val displayName: String,
    val profileImageUrl: String,
    var role: Int = 0
)
