package com.models

import kotlinx.serialization.Serializable

@Serializable
data class PropertyOwnerUser(
    val email: String,
    val fullName: String,
    val phoneNumber: String,
    val password: String,
    val token: String?=null,
    val profilePicture: String?=null,
)
