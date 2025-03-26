package com.services

import com.database.DatabaseManager
import com.models.*
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
        var score = 50
        var maxScore = 50
        val numPreferences = seeker.condoPreference.size

        maxScore += numPreferences * 20

        for (preference in seeker.condoPreference) {
            if (preference in property.features) {
                score += 20
            }
        }

        val normalizedScore = (score.toDouble() / maxScore.toDouble()) * 100
        logger.info("calculatePropertyMatchScore: score= $normalizedScore ")
        return normalizedScore.toInt().coerceAtMost(100) // Normalize to max 100

    }


    // calculateRoommateMatchScore function
    private fun calculateRoommateMatchScore(seeker: RoommateUser, roommate: RoommateUser): Int {
        var score = 0
        var maxScore = 0

        // Calculate score for lookingForRoomies preferences
        for (preference in seeker.lookingForRoomies) {
            val preferenceScore = if (preference.isDealbreaker) 50 else 10
            maxScore += preferenceScore
            if (preference.attribute in roommate.attributes) {
                score += preferenceScore
            } else if (preference.isDealbreaker) {
                return 0 // Dealbreaker logic remains
            }
        }

        // Calculate score for condo preferences
        val condoPreferencesMatch = seeker.condoPreference.intersect(roommate.condoPreference.toSet())
        val condoScore = condoPreferencesMatch.size * 5 // Example: 5 points per match
        score += condoScore
        maxScore += seeker.condoPreference.size * 5 // Update maxScore

        // Normalize the score
        val normalizedScore = if (maxScore > 0) {
            (score.toDouble() / maxScore.toDouble()) * 100
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