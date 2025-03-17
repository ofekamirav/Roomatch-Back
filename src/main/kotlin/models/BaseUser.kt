package com.models

import kotlinx.serialization.Serializable

@Serializable
open class BaseUser(
    open val id: String? = null,
    open val userType: String = "User",
    open val email: String = "",
    open val password: String = "",
    open val profileImage: String? = null,
    open val token: String? = null
)
