package com.koder.ellen.model

data class ReactionSummary(
    var reactioN_CODE_LIKE: Reaction? = null,
    var reactioN_CODE_DISLIKE: Reaction? = null,
    var reactioN_CODE_LOVE: Reaction? = null,
    var reactioN_CODE_WINK: Reaction? = null,
    var reactioN_CODE_NERDY: Reaction? = null
)