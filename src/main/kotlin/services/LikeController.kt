package com.services
import com.database.DatabaseManager
import com.models.DisLike
import com.models.Match
import com.models.Property
import com.models.RoommateUser
import org.slf4j.LoggerFactory

object LikeController {

    private val logger = LoggerFactory.getLogger(LikeController::class.java)

    suspend fun saveMatch(match: Match): Result<Match> {
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

    //Like Property--dislike the roomies
    suspend fun likeProperty(match: Match): Boolean {
        val dislikeRoomiesFromMatch = mutableListOf<RoommateUser>()

        match.roommateMatches.forEach { roommateMatch ->
            val roommate = DatabaseManager.getRoommateById(roommateMatch.roommateId)
            if (roommate != null) {
                dislikeRoomiesFromMatch.add(roommate)
            }
        }

        if(dislikeRoomiesFromMatch.isNotEmpty()){
            val currentDislikeRoommies = DatabaseManager.getDislikeRoommatesIds(match.seekerId)
            val newDislikeRoommies = currentDislikeRoommies + dislikeRoomiesFromMatch.map { it.id }
            val updatedDislike = DatabaseManager.updateDislikeRoommates(match.seekerId, newDislikeRoommies)
            if (updatedDislike != null) {
                logger.info("Dislike roomies updated successfully: ${match.seekerId}")
                return true
            } else {
                val errorMsg = "Failed to update dislike roomies"
                logger.error(errorMsg)
                return false
            }
        }
        return false
    }

    //Like Roommate--dislike the properties
    suspend fun likeRoommate(match: Match): Boolean {
        try {
            val currentDislikeProperties = DatabaseManager.getDislikePropertiesIds(match.seekerId)
            val newDislikeProperties = currentDislikeProperties + match.propertyId
            val updatedDislike = DatabaseManager.updateDislikeProperties(match.seekerId, newDislikeProperties)
            if (updatedDislike != null) {
                logger.info("Dislike properties updated successfully: ${match.seekerId}")
                return true
            } else {
                val errorMsg = "Failed to update dislike properties"
                logger.error(errorMsg)
                return false
            }
        } catch (e: Exception) {
            logger.error("Error while updating dislike properties: ${e.message}")
            throw e
        }
    }


    suspend fun getMatchesBySeekerId(seekerId: String): List<Match> {
        return DatabaseManager.getMatchesBySeekerId(seekerId)
    }
}
