package com.koder.ellen.model

data class ClientConfiguration(
    val publishKey: String,
    val subscribeKey: String,
    val baseEndpoint: String,
    val resourceLanguage: String,
    val secretKey: String
)