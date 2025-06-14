package com.models

import kotlinx.serialization.Serializable

@Serializable
data class PasswordResetRequest(
    val email: String,
    val userType: String,
)

@Serializable
data class ResetPassword(
    val otpCode: String,
    val email: String,
    val newPassword: String,
    val userType: String
)

@Serializable
data class PasswordResetRequestResult(
    val status: String,
    val otpCode: String? = null,
    val error: String? = null
)