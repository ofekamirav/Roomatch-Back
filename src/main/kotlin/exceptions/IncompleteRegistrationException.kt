package com.exceptions

import kotlinx.serialization.Serializable

@Serializable
data class IncompleteRegistrationException(
    val email: String,
    val fullName: String,
    val profilePicture: String?
) : Exception("User not found. Needs registration.")
