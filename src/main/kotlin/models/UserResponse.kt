package com.models

import kotlinx.serialization.Serializable

@Serializable
data class UserResponse(
    val token: String,
    val refreshToken: String,
    val userId: String?,
    val userType: String,
)
