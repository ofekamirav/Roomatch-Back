    import com.database.DatabaseManager
    import com.models.DisLike
    import com.models.Match
    import org.slf4j.LoggerFactory

    object DislikeController {
        private val logger = LoggerFactory.getLogger(DislikeController::class.java)

        suspend fun swipeLeft(match: Match): Boolean {
            try {
                val existing = DatabaseManager.getDislikeBySeekerId(match.seekerId)

                val newPropertyId = match.propertyId?.takeIf {
                    existing?.dislikedPropertiesIds?.contains(it) != true
                }

                val newRoommateIds = match.roommateMatches.map { it.roommateId }
                    .filterNot { existing?.dislikedRoommatesIds?.contains(it) == true }

                if (existing != null) {
                    if (newPropertyId == null && newRoommateIds.isEmpty()) {
                        logger.info("Nothing new to add to dislike for ${match.seekerId}")
                        return true
                    }

                    val updated = existing.copy(
                        dislikedPropertiesIds = existing.dislikedPropertiesIds + listOfNotNull(newPropertyId),
                        dislikedRoommatesIds = existing.dislikedRoommatesIds + newRoommateIds
                    )
                    logger.info("Updated dislike for ${match.seekerId} with new propertyId=$newPropertyId and newRoommateIds=$newRoommateIds")
                    return DatabaseManager.updateDislike(updated)
                } else {
                    val newDislike = DisLike(
                        seekerId = match.seekerId,
                        dislikedPropertiesIds = listOfNotNull(newPropertyId),
                        dislikedRoommatesIds = newRoommateIds
                    )
                    return DatabaseManager.insertDislike(newDislike)
                }
            } catch (e: Exception) {
                logger.error("Error in swipeLeft for ${match.seekerId}", e)
                return false
            }
        }

    }

