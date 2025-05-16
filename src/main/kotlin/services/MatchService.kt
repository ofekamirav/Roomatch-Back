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

        val dislike = DatabaseManager.getDisLikeBySeekerId(seekerId)
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
                    property.CurrentRoommatesIds.size < property.canContainRoommates!! &&
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
                        (roommate.roommatesNumber == seeker.roommatesNumber) &&
                        !roommatesInProperty.contains(roommate.id) // Exclude existing roommate
            }

            // Create Random list of roommates
            val shuffledRoommates = potentialRoommates.shuffled(random)

            // Calculate roommate match score
            val roommateMatches = shuffledRoommates.map { roommate ->
                val roommateMatchScore = calculateRoommateMatchScore(seeker, roommate)
                logger.info("Roommate match score: $roommateMatchScore")
                RoommateMatch(roommate.id, roommate.fullName, roommateMatchScore)
            }.filter { it.matchScore >= 25 } //needs to improve this

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
                    propertyMatchScore = calculatePropertyMatchScore(seeker, property)
                )
                matches.add(match)

                val newSeen = SeenMatch(property.id, combineRoommates.mapNotNull { it.roommateId })
                DatabaseManager.appendSeenMatch(seekerId, newSeen)
            }
        }

        logger.info("performMatch: matches.size = ${matches.size}")

        return matches
    }

    private fun mapWeightToPoints(weight: Double): Double {
        return when (weight) {
            1.0 -> 50.0  // Dealbreaker
            0.75 -> 15.0
            0.5 -> 10.0
            0.25 -> 5.0
            else -> 0.0
        }
    }

    // calculatePropertyMatchScore function
    private fun calculatePropertyMatchScore(seeker: RoommateUser, property: Property): Int {
        var score = 0.0
        var maxScore = 0.0

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
//            if (preferenceScore == 0.0) continue

            val isDealbreaker = preference.weight == 1.0
            maxScore += preferenceScore

            if (preference.preference in property.features) {
                score += preferenceScore
            } else if (isDealbreaker) { // If it's a dealbreaker and the preference doesn't match, return 0
                return 0
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
//            if (preferenceScore == 0.0) continue

            val isDealbreaker = preference.weight == 1.0
            maxScore += preferenceScore

            if (preference.attribute in roommate.attributes) {
                score += preferenceScore
                if (isDealbreaker){
                    score += MatchWeights.dealbreakerBonus
                }
            } else if (isDealbreaker) {
                return 0
            }
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
            if (preferenceScore == 0.0) continue

            val isDealbreaker = preference.weight == 1.0
            maxScore += preferenceScore

            if (preference.preference in roommate.lookingForCondo.map { it.preference }) {
                score += preferenceScore
            } else if (isDealbreaker) {
                return 0
            }
        }

        val normalizedScore = if (maxScore > 0) {
            (score / maxScore) * 100
        } else {
            0.0
        }

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