package com.models

import com.models.BaseUser
import kotlinx.serialization.SerialName

data class PropertyOwnerUser(
    val firstName: String,
    val lastName: String,
    val phoneNumber: String,
    @SerialName("roommate_id") override val id: String? = null,
    @SerialName("roommate_user_type") override val userType: String = "PropertyOwner",
    @SerialName("roommate_email") override val email: String,
    @SerialName("roommate_password") override val password: String,
    @SerialName("roommate_profile_image") override val profileImage: String? = null,
    @SerialName("roommate_token") override val token: String? = null

) : BaseUser(
    id = id,
    userType = userType,
    email = email,
    password = password,
    profileImage = profileImage,
    token = token
)
