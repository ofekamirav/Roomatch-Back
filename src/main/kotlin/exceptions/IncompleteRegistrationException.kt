package com.exceptions

data class IncompleteRegistrationException(
    val email: String,
    val fullName: String,
    val profilePicture: String?
) : Exception("User not found. Needs registration.")
