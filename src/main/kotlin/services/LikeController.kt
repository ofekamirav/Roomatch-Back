package com.services
import com.database.DatabaseManager
import com.models.Match
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

    suspend fun getMatchesBySeekerId(seekerId: String): List<Match> {
        return DatabaseManager.getMatchesBySeekerId(seekerId)
    }
}
