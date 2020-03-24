package com.koder.ellen.data.model

data class ClientConfiguration(
    val publishKey: String,
    val subscribeKey: String,
    val baseEndpoint: String,
    val resourceLanguage: String,
    val secretKey: String
)