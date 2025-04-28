package com.models

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId

@Serializable
data class PropertyOwnerUser(
    @BsonId @Contextual val id: String? = null,
    val email: String,
    val fullName: String,
    val phoneNumber: String,
    val birthDate: String,
    val password: String,
    val refreshToken: String?=null,
    val profilePicture: String?=null,
    val resetToken: String? = null,
    val resetTokenExpiration: Long? = null

)
