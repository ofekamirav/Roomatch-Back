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

    //Calculate a Match between a seeker and a property and roommates
    suspend fun performMatch(seekerId: String): List<Match>? {
        val seeker = DatabaseManager.getRoommateById(seekerId)
        if (seeker == null) {
            logger.warn("Seeker not found for seekerId: $seekerId")
            return null
        }

        val allPropertiesDeferred = coroutineScope.async { DatabaseManager.getAllAvailableProperties() }
        val allRoommatesDeferred = coroutineScope.async { DatabaseManager.getAllRoommates() }

        //wait for the properties and roommates to be fetched
        val allProperties = allPropertiesDeferred.await()
        val allRoommates = allRoommatesDeferred.await()

        // Filter available properties based on available slots and seeker preferences
        val potentialProperties = allProperties.filter { property ->
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


    // calculatePropertyMatchScore function
    private fun calculatePropertyMatchScore(seeker: RoommateUser, property: Property): Int {
        var score = 0.0
        var maxScore = 0.0

        val allCondoPreferences = MatchWeights.condoPreferenceWeights.map { (attribute, defaultWeight) ->
            seeker.lookingForCondo.find { it.attribute == attribute && it.setWeight }
                ?: LookingForCondoPreference(attribute, defaultWeight, setWeight = false)
        }


        for (preference in allCondoPreferences) {
            val isDealbreaker = preference.weight == 1.0
            val preferenceScore = if (isDealbreaker)
                MatchWeights.dealbreakerWeight
            else
                MatchWeights.preferenceWeight * preference.weight

            maxScore += preferenceScore

            if (preference.attribute in property.features) {
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

        logger.info("calculatePropertyMatchScore: score= $normalizedScore")
        return normalizedScore.toInt().coerceAtMost(100)
    }




    // calculateRoommateMatchScore function
    private fun calculateRoommateMatchScore(seeker: RoommateUser, roommate: RoommateUser): Int {
        var score = 0.0
        var maxScore = 0.0

        // If user didn't choose a wight, fallback to default weights
        val allRoomiePreferences = MatchWeights.attributeWeights.map { (attribute, defaultWeight) ->
            seeker.lookingForRoomies.find { it.attribute == attribute && it.setWeight }
                ?: LookingForRoomiesPreference(attribute, defaultWeight, setWeight = false)
        }

        for (preference in allRoomiePreferences) {
            val isDealbreaker = preference.weight == 1.0
            val preferenceScore = if (isDealbreaker)
                MatchWeights.dealbreakerWeight
            else
                MatchWeights.preferenceWeight * preference.weight

            maxScore += preferenceScore

            if (preference.attribute in roommate.attributes) {
                score += preferenceScore
            } else if (isDealbreaker) {
                return 0
            }
        }

        // Condo preference matching between seeker and roommate
        val allCondoPreferences = MatchWeights.condoPreferenceWeights.map { (attribute, defaultWeight) ->
            seeker.lookingForCondo.find { it.attribute == attribute && it.setWeight }
                ?: LookingForCondoPreference(attribute, defaultWeight, setWeight = false)
        }

        for (preference in allCondoPreferences) {
            val isDealbreaker = preference.weight == 1.0
            val preferenceScore = if (isDealbreaker)
                MatchWeights.dealbreakerWeight
            else
                MatchWeights.preferenceWeight * preference.weight

            maxScore += preferenceScore

            if (preference.attribute in roommate.lookingForCondo.map { it.attribute }) {
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