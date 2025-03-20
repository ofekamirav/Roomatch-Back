package com.models

import kotlinx.serialization.Serializable

@Serializable
data class Match(
    val seekerId: String,
    val propertyId: String,
    val roommateMatches: List<RoommateMatch> = emptyList(),
    val propertyMatchScore: Int,

    )

@Serializable
data class RoommateMatch(
    val roommateId: String,
    val matchScore: Int,
)


