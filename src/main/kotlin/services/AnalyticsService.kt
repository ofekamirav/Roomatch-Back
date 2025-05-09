package com.services

import com.database.DatabaseManager
import com.models.AnalyticsResponse
import com.models.PropertyMatchAnalytics

object AnalyticsService {

    // Function to get analytics data for owner
    suspend fun getAnalyticsData(ownerId: String): AnalyticsResponse? {
        // Get Owner's properties
        val properties = DatabaseManager.getPropertiesByOwnerId(ownerId)
        if (properties.isEmpty()) {
            return null
        }
        val matchesPerProperty = mutableListOf<PropertyMatchAnalytics>()
        var totalMatches = 0
        var totalScore = 0.0
        var uniqueRoommates = 0
        properties.forEach { property ->
            val matchCount = DatabaseManager.getMatchCountByPropertyId(property.id.toString())
            val averageMatchScore = DatabaseManager.getAverageMatchScoreByPropertyId(property.id.toString())

            // Update totals
            totalMatches += matchCount
            totalScore += averageMatchScore * matchCount

            // Get unique roommates for this property
            val roommates = DatabaseManager.getRoommateNumByPropertyId(property.id.toString())
            uniqueRoommates += roommates


            matchesPerProperty.add(
                PropertyMatchAnalytics(
                    propertyId = property.id.toString(),
                    title = property.title ?: "",
                    matchCount = matchCount,
                    averageMatchScore = averageMatchScore
                )
            )
        }
        val overallAverageScore = if (totalMatches > 0) totalScore / totalMatches else 0.0

        return AnalyticsResponse(
            ownerId = ownerId,
            totalMatches = totalMatches,
            uniqueRoommates = uniqueRoommates,
            averageMatchScore = overallAverageScore,
            matchesPerProperty = matchesPerProperty,
        )
    }
}