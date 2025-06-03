package com.services

import com.database.DatabaseManager
import com.models.*
import com.utils.MatchWeights
import com.utils.haversine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import org.bson.types.ObjectId
import org.slf4j.LoggerFactory
import java.util.*
import com.models.DisLike

object MatchService {

    private val logger = LoggerFactory.getLogger(MatchService::class.java)
    val userMatchCache: MutableMap<String, List<Match>> = mutableMapOf()
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    //Calculate a Match between a seeker and a property and roommates
    suspend fun performMatch(seekerId: String, limit: Int): List<Match> {
        logger.info("performMatch: seekerId = $seekerId, limit = $limit")
        val seeker = DatabaseManager.getRoommateById(seekerId)
        if (seeker == null) {
            logger.warn("Seeker not found for seekerId: $seekerId")
            return emptyList()
        }

        val allPropertiesDeferred = coroutineScope.async { DatabaseManager.getAllAvailableProperties() }
        val allRoommatesDeferred = coroutineScope.async { DatabaseManager.getAllRoommates() }

        //wait for the properties and roommates to be fetched
        val allProperties = allPropertiesDeferred.await()
        val allRoommates = allRoommatesDeferred.await()

        var dislike = DatabaseManager.getDisLikeBySeekerId(seekerId)
        if (dislike == null) {
            logger.info("No dislike data found for seekerId: $seekerId")
            val newDislike = DisLike(
                seekerId = seekerId,
                dislikedPropertiesIds = emptyList(),
                dislikedRoommatesIds = emptyList(),
                seenMatches = emptyList()
            )
            DatabaseManager.insertDislike(newDislike)
            dislike = newDislike
        } else {
            logger.info("Dislike data found for seekerId: $seekerId, dislikedPropertiesIds: ${dislike.dislikedPropertiesIds}, dislikedRoommatesIds: ${dislike.dislikedRoommatesIds}")
        }
        val dislikedProperties = dislike?.dislikedPropertiesIds ?: emptyList()
        val dislikedRoommates = dislike?.dislikedRoommatesIds ?: emptyList()
        val seenMatches = dislike?.seenMatches ?: emptyList()


        val locationProperties = allProperties.filter { property ->
            val propLat = property.latitude
            val propLon = property.longitude
            if (propLat != null && propLon != null && seeker.latitude != null && seeker.longitude != null) {
                haversine(seeker.latitude, seeker.longitude, propLat, propLon) <= seeker.preferredRadiusKm
            } else false
        }
        logger.info("Preferred location properties count: ${locationProperties.size}")

        // Filter out disliked properties
        val filteredProperties = locationProperties.filter { property ->
            !dislikedProperties.contains(property.id)
        }

        // Filter out disliked roommates
        val filteredRoommates = allRoommates.filter { roommate ->
            !dislikedRoommates.contains(roommate.id)
        }

        val potentialProperties = filteredProperties.filter { property ->
            when (property.type) {
                PropertyType.ROOM ->
                    (property.canContainRoommates == null || property.CurrentRoommatesIds.size < property.canContainRoommates) &&
                            (property.pricePerMonth == null || property.pricePerMonth in seeker.minPrice..seeker.maxPrice) &&
                            (property.size == null || property.size in seeker.minPropertySize..seeker.maxPropertySize)

                PropertyType.APARTMENT ->
                    property.CurrentRoommatesIds.isEmpty() &&
                            (property.pricePerMonth == null || property.pricePerMonth in seeker.minPrice..seeker.maxPrice) &&
                            (property.size == null || property.size in seeker.minPropertySize..seeker.maxPropertySize)
            }
        }

        logger.info("Potential properties count: ${potentialProperties.size}")

        val matches = mutableListOf<Match>()
        val random = Random()

        for (property in potentialProperties) {
            if (matches.size >= limit) break

            //Filter out roommates that already live in this ROOM
            val roommatesInProperty =
                if (property.type == PropertyType.ROOM) property.CurrentRoommatesIds else emptyList()

            val potentialRoommates = filteredRoommates.filter { roommate ->
                roommate.id != seekerId &&
                        !roommatesInProperty.contains(roommate.id) // Exclude existing roommate
            }

            // Create Random list of roommates
            val shuffledRoommates = potentialRoommates.shuffled(random)

            // Calculate roommate match score
            val roommateMatches = shuffledRoommates.map { roommate ->
                val roommateMatchScore = calculateRoommateMatchScore(seeker, roommate)
                logger.info("Roommate match score: $roommateMatchScore")
                RoommateMatch(roommate.id, roommate.fullName, roommateMatchScore, roommate.profilePicture ?: "")
            }
                .filter { it.matchScore >= 50 }
                .sortedByDescending { it.matchScore } // Sort by match score in descending order

            val combineRoommates = roommateMatches.take(seeker.roommatesNumber)
            logger.info("combineRoommates: $combineRoommates")

            if (property.id != null && combineRoommates.isNotEmpty()) {
                val isAlreadySeen = seenMatches.any { seen ->
                    seen.propertyId == property.id &&
                            seen.roommateIds.toSet() == combineRoommates.mapNotNull { it.roommateId }.toSet()
                }
                if (isAlreadySeen) continue

                val match = Match(
                    id = ObjectId().toHexString(),
                    seekerId = seekerId,
                    propertyId = property.id,
                    roommateMatches = combineRoommates,
                    propertyMatchScore = calculatePropertyMatchScore(seeker, property),
                    propertyTitle = property.title ?: "",
                    propertyPrice = property.pricePerMonth ?: 0,
                    propertyAddress = property.address ?: "",
                    propertyPhoto = property.photos.firstOrNull() ?: ""
                )
                // Save the match to seen matches
                val seenMatch = SeenMatch(
                    propertyId = property.id,
                    roommateIds = combineRoommates.mapNotNull { it.roommateId }
                )
                DatabaseManager.appendSeenMatch(match.seekerId, seenMatch)
                // Only add if property score meets a minimum threshold
                if (match.propertyMatchScore >= 50) { // New filter for property match quality
                    matches.add(match)
                }
            }
        }

        logger.info("performMatch: matches.size = ${matches.size}")

        return matches
    }

    private fun mapWeightToPoints(weight: Double): Double {
        return when (weight) {
            1.0 -> 100.0
            0.75 -> 60.0
            0.5 -> 30.0
            0.25 -> 10.0
            else -> 0.0
        }
    }

    // calculatePropertyMatchScore function
    private fun calculatePropertyMatchScore(seeker: RoommateUser, property: Property): Int {
        var score = 0.0
        var maxScore = 0.0
        val priceMatchPoints = 50.0
        val sizeMatchPoints = 40.0

        maxScore += priceMatchPoints
        if (property.pricePerMonth != null && property.pricePerMonth in seeker.minPrice..seeker.maxPrice) {
            score += priceMatchPoints
        }
        maxScore += sizeMatchPoints
        if (property.size != null && property.size in seeker.minPropertySize..seeker.maxPropertySize) {
            score += sizeMatchPoints
        }

        // filtering out condo preferences that are not set by the user
        val allCondoPreferences = MatchWeights.condoPreferenceWeights.map { (pref, defaultWeight) ->
            val userPref = seeker.lookingForCondo.find { it.preference == pref }

            if (userPref != null && userPref.setWeight && userPref.weight > 0.0) {
                // Use user-defined weight
                LookingForCondoPreference(pref, userPref.weight, true)
            } else {
                // Use default weight
                LookingForCondoPreference(pref, defaultWeight, false)
            }
        }

        for (preference in allCondoPreferences) {
            val preferenceScore = mapWeightToPoints(preference.weight)

            var currentPrefMaxContribution = preferenceScore

            val isDealbreaker = preference.weight == 1.0

            if (isDealbreaker) {
                currentPrefMaxContribution += MatchWeights.dealbreakerBonus
            }
            maxScore += currentPrefMaxContribution

            if (preference.preference in property.features) {
                score += preferenceScore
                if (isDealbreaker) {
                    score += MatchWeights.dealbreakerBonus // Award bonus if dealbreaker matches
                }

            } else if (isDealbreaker) {
                logger.info("Match Service: Property ${property.id} failed dealbreaker: ${preference.preference} for seeker ${seeker.fullName}")
                return 0 // Dealbreaker not met, score is 0
            }
        }

        val normalizedScore = if (maxScore > 0) {
            (score / maxScore) * 100
        } else {
            0.0
        }

        logger.info("calculatePropertyMatchScore: score= $normalizedScore")
        return normalizedScore.toInt().coerceAtMost(100)
    }


    // calculateRoommateMatchScore function
    private fun calculateRoommateMatchScore(seeker: RoommateUser, roommate: RoommateUser): Int {
        var score = 0.0
        var maxScore = 0.0
        val roommateNumberMatchPoints = 30.0

        if (roommate.roommatesNumber == seeker.roommatesNumber) {
            score += roommateNumberMatchPoints
        }
        maxScore += roommateNumberMatchPoints

        // Use user-defined preferences or fallback to default attributes
        val allRoomiePreferences = MatchWeights.attributeWeights.map { (attr, defaultWeight) ->
            val userPref = seeker.lookingForRoomies.find { it.attribute == attr }

            if (userPref != null && userPref.setWeight && userPref.weight > 0.0) {
                // Use user-defined weight
                LookingForRoomiesPreference(attr, userPref.weight, true)
            } else {
                // Use default weight
                LookingForRoomiesPreference(attr, defaultWeight, false)
            }
        }

        for (preference in allRoomiePreferences) {
            val preferenceScore = mapWeightToPoints(preference.weight)
            if (preferenceScore == 0.0) continue

            val isDealbreaker = preference.weight == 1.0

            val bonus = if (isDealbreaker) MatchWeights.dealbreakerBonus else 0.0

            if (preference.attribute in roommate.attributes) {
                score += preferenceScore + bonus
            } else if (isDealbreaker) {
                return 0
            }
            maxScore += preferenceScore + bonus
        }

        // Condo preferences matching between seeker and roommate
        val allCondoPreferences = MatchWeights.condoPreferenceWeights.map { (pref, defaultWeight) ->
            val userPref = seeker.lookingForCondo.find { it.preference == pref }

            if (userPref != null && userPref.setWeight && userPref.weight > 0.0) {
                // Use user-defined weight
                LookingForCondoPreference(pref, userPref.weight, true)
            } else {
                // Use default weight
                LookingForCondoPreference(pref, defaultWeight, false)
            }
        }

        for (preference in allCondoPreferences) {
            val preferenceScore = mapWeightToPoints(preference.weight)

            var bonus = if (preference.weight == 1.0) MatchWeights.dealbreakerBonus else 0.0

            val isDealbreaker = preference.weight == 1.0

            val roommateCaresAbout =
                roommate.lookingForCondo.any { it.preference == preference.preference && it.weight > 0.0 }

            if (roommateCaresAbout) {
                score += preferenceScore + bonus
                maxScore += preferenceScore + bonus

            } else if (isDealbreaker) {
                return 0
            }
        }

            // Hobby Matching
            // hobbies only increase your score range if they actually match

            val commonHobbies = seeker.hobbies.intersect(roommate.hobbies.toSet())
            val hobbyScore = commonHobbies.size * 20.0
            score += hobbyScore
            maxScore += hobbyScore

            val normalizedScore = if (maxScore > 0) {
                (score / maxScore) * 100 * 1.7
            } else {
                0.0
            }
            logger.info("Roommate: ${roommate.fullName}, rawScore=$score, maxScore=$maxScore, normalizedScore=$normalizedScore")

            return normalizedScore.toInt().coerceAtMost(100)
        }


    suspend fun getNextMatchesForSwipe(seekerId: String, limit: Int): List<Match> {
        return performMatch(seekerId, limit)
    }

    //Delete a match
    suspend fun deleteMatch(matchId: String): Boolean {
        return try {
            val result = DatabaseManager.deleteMatch(matchId)
            if (result) {
                logger.info("Match with ID $matchId deleted successfully")
            } else {
                logger.warn("Failed to delete match with ID $matchId")
            }
            result
        } catch (e: Exception) {
            logger.error("Error deleting match with ID $matchId", e)
            false
        }
    }
}
