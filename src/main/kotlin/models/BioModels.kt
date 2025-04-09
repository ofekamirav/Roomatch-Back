package com.models

import kotlinx.serialization.Serializable

@Serializable
data class BioRequest(
    val fullName: String,
    val hobbies: List<Hobby>,
    val attributes: List<Attribute>,
    val work: String
)

@Serializable
data class BioResponse(
    val generatedBio: String
)
