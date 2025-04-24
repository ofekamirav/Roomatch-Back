package com.services

import com.database.DatabaseManager
import com.models.DisLike
import com.models.Match
import org.litote.kmongo.eq
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo
import org.litote.kmongo.coroutine.CoroutineCollection
import org.slf4j.LoggerFactory

object LikeController {

    private val logger = LoggerFactory.getLogger(LikeController::class.java)

    private val dislikesCollection: CoroutineCollection<DisLike> by lazy {
        val client = KMongo.createClient().coroutine
        val db = client.getDatabase("RooMatchDB")
        db.getCollection()
    }

    suspend fun saveMatch(match: Match): Result<Match> {
        val seekerId = match.seekerId
        val likedPropertyId = match.propertyId
        val roommateMatches = match.roommateMatches

        try {
            if (roommateMatches.isNotEmpty()) {
                // if user like roommates  → add property to dislikedPropertiesIds
                val allProperties = DatabaseManager.getAllAvailableProperties()
                val relatedProperties = allProperties.filter { prop ->
                    roommateMatches.any { it.roommateId in prop.CurrentRoommatesIds }
                }
                val propertyIdsToAdd = relatedProperties.map { it.id }

                val existingDislike = dislikesCollection.findOne(DisLike::seekerId eq seekerId)
                if (existingDislike != null) {
                    val updated = existingDislike.copy(
                        dislikedPropertiesIds = (existingDislike.dislikedPropertiesIds + propertyIdsToAdd).distinct()
                    )
                    dislikesCollection.updateOneById(updated.id, updated)
                } else {
                    val newDislike = DisLike(
                        seekerId = seekerId,
                        dislikedPropertiesIds = propertyIdsToAdd
                    )
                    dislikesCollection.insertOne(newDislike)
                }

            } else if (!likedPropertyId.isNullOrBlank()) {
                // if user like property → add roommates to dislikedRoommatesIds
                val property = DatabaseManager.getPropertyById(likedPropertyId)
                val roommatesToDislike = property?.CurrentRoommatesIds?.filter { it != seekerId } ?: emptyList()

                val existingDislike = dislikesCollection.findOne(DisLike::seekerId eq seekerId)
                if (existingDislike != null) {
                    val updated = existingDislike.copy(
                        dislikedRoommatesIds = (existingDislike.dislikedRoommatesIds + roommatesToDislike).distinct()
                    )
                    dislikesCollection.updateOneById(updated.id, updated)
                } else {
                    val newDislike = DisLike(
                        seekerId = seekerId,
                        dislikedRoommatesIds = roommatesToDislike
                    )
                    dislikesCollection.insertOne(newDislike)
                }
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
        } catch (e: Exception) {
            logger.error("Exception while saving match and processing dislike logic", e)
            return Result.failure(e)
        }
    }

    suspend fun getMatchesBySeekerId(seekerId: String): List<Match> {
        return DatabaseManager.getMatchesBySeekerId(seekerId)
    }
}
