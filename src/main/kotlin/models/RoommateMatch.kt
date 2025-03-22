package com.models

import kotlinx.serialization.Serializable

@Serializable
data class RoommateMatch(
    val roommateId: String,
    val matchScore: Int,
)

