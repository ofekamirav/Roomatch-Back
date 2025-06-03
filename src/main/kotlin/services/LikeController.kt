package com.services
import com.database.DatabaseManager
import com.models.*
import org.slf4j.LoggerFactory

object LikeController {

    private val logger = LoggerFactory.getLogger(LikeController::class.java)

    suspend fun saveMatch(match: Match): Result<Match> {
        val existing = DatabaseManager.getMatchById(match.id.toString())
        if (existing != null) {
            logger.info("Match already exists: ${match.id}")
            return Result.success(existing)
        }

        val insertedMatch = DatabaseManager.insertMatch(match)
        return if (insertedMatch != null) {
            logger.info("Match saved successfully: ${match.id}")
            Result.success(insertedMatch)
        } else {
            val errorMsg = "Failed to save match"
            logger.error(errorMsg)
            Result.failure(Exception(errorMsg))
        }
    }


    //Like Property--dislike the roommates
    suspend fun likeProperty(match: Match): Boolean {
        val currentDislikeRoommies = DatabaseManager.getDislikeRoommatesIds(match.seekerId)
        val newRoommateIds = match.roommateMatches.map { it.roommateId }
            .filterNot { currentDislikeRoommies.contains(it) }

        if (newRoommateIds.isEmpty()) {
            logger.info("No new roommates to dislike for seekerId=${match.seekerId}")
            return true
        }

        val updatedDislikeRoomies = (currentDislikeRoommies + newRoommateIds).distinct()
        val updatedDislike = DatabaseManager.updateDislikeRoommates(match.seekerId, updatedDislikeRoomies)

        return if (updatedDislike != null) {
            logger.info("Updated disliked roommates for ${match.seekerId}")
            true
        } else {
            logger.error("Failed to update disliked roommates for ${match.seekerId}")
            false
        }
    }

    //Like Roommate--dislike the properties
    suspend fun likeRoommate(match: Match): Boolean {
        val currentDislikeProperties = DatabaseManager.getDislikePropertiesIds(match.seekerId)
        val propertyId = match.propertyId

        if (propertyId == null || currentDislikeProperties.contains(propertyId)) {
            logger.info("Property already disliked or null for ${match.seekerId}")
            return true
        }

        val updatedDislikeProperties = currentDislikeProperties + propertyId
        val updatedDislike = DatabaseManager.updateDislikeProperties(match.seekerId, updatedDislikeProperties)

        return if (updatedDislike != null) {
            logger.info("Updated disliked properties for ${match.seekerId}")
            true
        } else {
            logger.error("Failed to update disliked properties for ${match.seekerId}")
            false
        }
    }



    suspend fun getMatchesBySeekerId(seekerId: String): List<Match> {
        return DatabaseManager.getMatchesBySeekerId(seekerId)
    }
}
