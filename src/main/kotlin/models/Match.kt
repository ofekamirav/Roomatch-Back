package com.models

data class Match(
    val id: String,
    val seekerId: String,
    val propertyId: String,
    val roommateMatches: List<RoommateMatch> = emptyList(),
    val propertyMatchScore: Int,

    )

data class RoommateMatch(
    val roommateId: String,
    val matchScore: Int,
)


