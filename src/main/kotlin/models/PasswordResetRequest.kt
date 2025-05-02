package com.models

import kotlinx.serialization.Serializable

@Serializable
data class PasswordResetRequest(val email: String, val userType: String)

@Serializable
data class ResetPassword(val token: String, val newPassword: String, val userType: String)