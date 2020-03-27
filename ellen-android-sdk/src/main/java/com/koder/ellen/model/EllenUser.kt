package com.koder.ellen.model

data class EllenUser(
    var dateCreated: Long = 0,
    var externalIdentifier: String = "",
    var profile: UserProfile = UserProfile(),
    var state: Int = 0,
    var statistics: UserStatistics = UserStatistics(),
    var tenantId: String = "",
    var tokenSalt: String = "",
    var userId: String = ""
)

data class UserProfile(
    var displayName: String = "",
    var profileImageUrl: String = "")

data class UserStatistics(
    var totalMessagesSent: Int = 0,
    var totalMessagesBanned: Int = 0,
    var totalMessagesModerated: Int = 0,
    var totalMessagesReported: Int = 0,
    var totalMessagesSilenced: Int = 0
)