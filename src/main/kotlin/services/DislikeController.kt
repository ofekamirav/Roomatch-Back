package com.services
import com.database.DatabaseManager
import com.models.Match
import org.slf4j.LoggerFactory

object DislikeController {

    private val logger = LoggerFactory.getLogger(DislikeController::class.java)

    suspend fun dislikeMatch(match: Match): Result<Match> {
        val result = DatabaseManager.insertDislikedMatch(match)
        return if (result != null) {
            logger.info("Disliked match successfully saved: ${match.id}")
            Result.success(result)
        } else {
            val errorMsg = "Failed to save disliked match"
            logger.error(errorMsg)
            Result.failure(Exception(errorMsg))
        }
    }

    suspend fun getDislikedMatches(seekerId: String): List<Match> {
        return DatabaseManager.getDislikedMatchesBySeekerId(seekerId)
    }
}
