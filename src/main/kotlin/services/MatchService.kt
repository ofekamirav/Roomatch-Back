package com.services

import com.database.DatabaseManager
import com.models.*
import com.utils.MatchWeights
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import org.slf4j.LoggerFactory
import java.util.*

object MatchService {

    private val logger = LoggerFactory.getLogger(MatchService::class.java)
    val userMatchCache: MutableMap<String, List<Match>> = mutableMapOf()
    val userSwipes: MutableMap<String, MutableSet<String>> = mutableMapOf()
    private val coroutineScope = CoroutineScope(Dispatchers.IO)


    suspend fun performMatch(seekerId: String): List<Match>? {
        // Retrieve seeker information; exit if seeker doesn't exist
        val seeker = DatabaseManager.getRoommateById(seekerId) ?: return null

        // Fetch all available properties asynchronously
        val allPropertiesDeferred = coroutineScope.async { DatabaseManager.getAllAvailableProperties() }

        // Fetch all potential roommates asynchronously
        val allRoommatesDeferred = coroutineScope.async { DatabaseManager.getAllRoommates() }

        // Fetch previously disliked matches by this seeker asynchronously
        val dislikedMatchesDeferred = coroutineScope.async { DatabaseManager.getDislikedMatchesBySeekerId(seekerId) }

        // Await the fetched data
        val allProperties = allPropertiesDeferred.await()
        val allRoommates = allRoommatesDeferred.await()
        val dislikedMatches = dislikedMatchesDeferred.await()

        // Create a set of property IDs from the disliked matches for efficient lookup
        val dislikedPropertyIds = dislikedMatches.map { it.propertyId }.toSet()

        // Filter potential properties based on user's dislikes and preferences
        val potentialProperties = allProperties.filter { property ->
            property.id !in dislikedPropertyIds && // Exclude properties previously disliked by the user
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

        val matches: MutableList<Match> = mutableListOf()

        for (property in potentialProperties) {

            //Filter out roommates that already live in this ROOM
            val roommatesInProperty = if (property.type == PropertyType.ROOM) property.CurrentRoommatesIds else emptyList()

            val potentialRoommates = allRoommates.filter { roommate ->
                roommate.id != seekerId &&
                        (roommate.roommatesNumber == seeker.roommatesNumber) &&
                        !roommatesInProperty.contains(roommate.id) // Exclude existing roommate
            }

            // Create Random list of roommates
            val random = Random()
            val shuffledRoommates = potentialRoommates.shuffled(random)

            // Calculate roommate match score
            val roommateMatches = shuffledRoommates.map { roommate ->
                val roommateMatchScore = calculateRoommateMatchScore(seeker, roommate)
                logger.info("Roommate match score: $roommateMatchScore")
                RoommateMatch(roommate.id, roommate.fullName, roommateMatchScore)
            }.filter { it.matchScore >= 55 }

            val combineRoommates = roommateMatches.take(seeker.roommatesNumber)

            //if(combineRoommates.size<seeker.roommatesNumber) continue


            val match = Match(
                    seekerId = seekerId,
                    propertyId = property.id,
                    roommateMatches = combineRoommates,
                    propertyMatchScore = calculatePropertyMatchScore(seeker, property)
                )
                matches.add(match)
            }

        userMatchCache[seekerId] = matches
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



    suspend fun getNextMatchForSwipe(seekerId: String): Match? {
        logger.info("getNextMatchForSwipe: seekerId = $seekerId")

        //Attempt to get from cache, if empty, perform a match to set the value in cache
        var matches = userMatchCache[seekerId]

        if (matches == null) {
            logger.info("getNextMatchForSwipe: No matches in cache, performing new match")
            matches = performMatch(seekerId)
        }
        //If matches is still empty, return null
        if (matches == null || matches.isEmpty()) {
            logger.info("getNextMatchForSwipe: No matches found after performing match")
            userMatchCache.remove(seekerId)
            return null
        }

        //Get the first match before removing it
        val match = matches.first()
        logger.info("getNextMatchForSwipe: Returning match with id = ${match.id}")

        //Update the cache, removeing the first one
        userMatchCache[seekerId] = matches.drop(1)

        return match
    }

    //Clear the feed for the user
    fun clearUserFeed(seekerId: String) {
        userMatchCache.remove(seekerId)
        logger.info("Cleared match feed for seeker $seekerId")
    }



}