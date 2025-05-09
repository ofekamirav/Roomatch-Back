package com.models

data class AnalyticsResponse(
    val ownerId: String? = null,
    val totalMatches: Int,
    val uniqueRoommates: Int,
    val averageMatchScore: Double,
    val matchesPerProperty: List<PropertyMatchAnalytics>,
)

data class PropertyMatchAnalytics(
    val propertyId: String,
    val title: String,
    val matchCount: Int,
    val averageMatchScore: Double
)
