package com.koder.ellen.model

data class Participant(
    val user: User,
    var state: Int = 0,
    val role: Int = 0
) {
    enum class ParticipantRole(val value: Int) {
        MEMBER(0),
        MODERATOR(10),
        OWNER(100)
    }
    enum class ParticipantState(val value: Int) {
        ACTIVE(0),
        SILENCED(10),
        BANNED(20)
    }
}