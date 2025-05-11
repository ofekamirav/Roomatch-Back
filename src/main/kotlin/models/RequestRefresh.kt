package com.models

import kotlinx.serialization.Serializable

@Serializable
data class RequestRefresh(
    val accessToken: String,
    val refreshToken: String
)

