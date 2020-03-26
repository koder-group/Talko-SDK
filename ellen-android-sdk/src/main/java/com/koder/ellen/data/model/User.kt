package com.koder.ellen.data.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

data class User(
    val tenantId: String,
    val userId: String,
    val displayName: String,
    val profileImageUrl: String,
    var role: Int = 0
)
