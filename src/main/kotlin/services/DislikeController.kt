    import com.database.DatabaseManager
    import com.models.DisLike
    import com.models.Match
    import org.slf4j.LoggerFactory

    object DislikeController {
        private val logger = LoggerFactory.getLogger(DislikeController::class.java)

        suspend fun swipeLeft(match: Match): Boolean {
            try {
                // Fetch existing dislike entry for the seeker (if any)
                val existing = DatabaseManager.getDislikeBySeekerId(match.seekerId)

                val dislikedPropertyId = match.propertyId
                val dislikedRoommateIds = match.roommateMatches.map { it.roommateId }

                if (existing != null) {
                    val updated = existing.copy(
                        dislikedPropertiesIds = existing.dislikedPropertiesIds + dislikedPropertyId,
                        dislikedRoommatesIds = existing.dislikedRoommatesIds + dislikedRoommateIds
                    )
                    return DatabaseManager.updateDislike(updated)
                } else {
                    val newDislike = DisLike(
                        seekerId = match.seekerId,
                        dislikedPropertiesIds = listOf(dislikedPropertyId),
                        dislikedRoommatesIds = dislikedRoommateIds
                    )
                    return DatabaseManager.insertDislike(newDislike)
                }
            } catch (e: Exception) {
                logger.error("Error in swipeLeft", e)
                return false
            }
        }
    }

