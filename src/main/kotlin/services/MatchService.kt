package com.services

import com.database.DatabaseManager
import com.models.*

object MatchService {

    private val userPreferencesCache: MutableMap<String, UserMatchPreference> = mutableMapOf()
    private val userMatchFeed: MutableMap<String, Pair<List<Match>, Int>> = mutableMapOf()


    suspend fun performMatch(seekerId: String): List<Match>? {
        val seeker = DatabaseManager.getRoommateById(seekerId) ?: return null

        // Get user preferences, create if doesn't exist
        val userPreference = getUserMatchPreference(seekerId)

        // Filter available properties based on available slots and seeker preferences
        val potentialProperties = DatabaseManager.getAllAvailableProperties().filter { property ->
            when (property.type) {
                PropertyType.ROOM -> property.CurrentRoommatesIds.size < property.canContainRoommates!! // Room: must have a slot available
                PropertyType.APARTMENT -> property.CurrentRoommatesIds.isEmpty() // Apartment: must be empty
            } && (property.pricePerMonth == null || property.pricePerMonth in seeker.minPrice..seeker.maxPrice) && (property.size == null || property.size in seeker.minPropertySize..seeker.maxPropertySize)
        }

        val matches: MutableList<Match> = mutableListOf()

        for (property in potentialProperties) {
            if (shouldSkipProperty(property,userPreference)) {
                //continue to the next property in the loop
                continue;
            }

            // Calculate property match score
            val propertyMatchScore = calculatePropertyMatchScore(seeker, property, userPreference)

            // Filter roommates
            val filteredRoommates = filterPotentialRoommates(seeker);
            val propertyRoommateMatches = updateRoommatesMatches(filteredRoommates,seeker,userPreference);


            if(propertyMatchScore >= 55 && propertyRoommateMatches.size == seeker.roommatesNumber){
                // Create Match object
                val match = Match(
                    seekerId = seekerId,
                    propertyId = property.id,
                    roommateMatches = propertyRoommateMatches,
                    propertyMatchScore = propertyMatchScore
                )
                matches.add(match)
            }
        }

        return matches
    }

    private suspend fun getUserMatchPreference(userId: String): UserMatchPreference {
        return userPreferencesCache.getOrPut(userId) {
            DatabaseManager.getUserMatchPreferenceByUserId(userId) ?: UserMatchPreference(userId)
        }
    }

    private fun calculatePropertyMatchScore(seeker: RoommateUser, property: Property, userPreference: UserMatchPreference): Int{
        var score = 0
        // Condo preferences
        for (preference in seeker.condoPreference) {
            if (preference in property.features) {
                score += 10
            }
        }

        if (property.id in userPreference.likePropertyIds)
            score -= 50

        if (property.id in userPreference.dislikePropertyIds)
            score += 50

        return score
    }

    private fun calculateRoommateMatchScore(seeker: RoommateUser, roommate: RoommateUser,userPreference:UserMatchPreference): Int{
        var score = 0
        // Condo preferences
        for (preference in seeker.condoPreference) {
            if (preference in roommate.condoPreference) {
                score += 10
            }
        }

        //Roommate preferences
        for (preference in seeker.lookingForRoomies) {
            if (preference.isDealbreaker) {
                if (preference.attribute in roommate.attributes) {
                    score -= 50
                }
            } else {
                if (preference.attribute in roommate.attributes) {
                    score += 10
                }
            }
        }

        if (roommate.id in userPreference.likeRoommateIds)
            score -= 50

        if (roommate.id in userPreference.dislikeRoommateIds)
            score += 50

        return score
    }

    //Filter the property if inside are users that dislike
    private suspend fun shouldSkipProperty(property: Property, userPreference: UserMatchPreference): Boolean {
        if(property.type == PropertyType.ROOM){
            // Get roommates in property
            val roommatesInProperty = DatabaseManager.getRoommatesByIds(property.CurrentRoommatesIds)

            for(roommate in roommatesInProperty){
                if(roommate.id in userPreference.dislikeRoommateIds){
                    // Skip this property if any of the roommates are disliked
                    //don't add this property to the matches
                    return true
                }
            }
        }
        return false;
    }

    //Filter Roomates and save in list to upload to response
    private suspend fun filterPotentialRoommates(seeker:RoommateUser) :  List<RoommateUser> {
        val filteredRoommates = DatabaseManager.getAllRoommates().filter { roommate ->
            roommate.id != seeker.id && (roommate.roommatesNumber == seeker.roommatesNumber)
        }
        return filteredRoommates;
    }

    //Filter Roomates and save in list to upload to response
    private fun updateRoommatesMatches(roommates:List<RoommateUser>,seeker:RoommateUser,userPreference:UserMatchPreference) :  MutableList<RoommateMatch> {
        val roommateMatches: MutableList<RoommateMatch> = mutableListOf()
        for (roommate in roommates) {
            val roommateMatchScore = calculateRoommateMatchScore(seeker, roommate, userPreference)
            if(roommateMatchScore >= 55)
                roommateMatches.add(RoommateMatch(roommate.id, roommateMatchScore))
        }
        return roommateMatches;
    }

    //LikeMatch so we can save the match in the user db
    suspend fun LikeMatch(seekerId: String, matchId: String): Boolean {
        val match = DatabaseManager.getMatchById(matchId) ?: return false
        if (match.seekerId != seekerId) return false

        val saved = DatabaseManager.insertMatch(match)

        return saved != null
    }


    suspend fun getMatchBatch(seekerId: String, limit: Int): List<Match> {
        val allMatches = performMatch(seekerId) ?: return emptyList()
        val userPreference = getUserMatchPreference(seekerId)

        val unseenMatches = allMatches.filterNot { match ->
            userPreference.likePropertyIds.contains(match.propertyId) ||
                    userPreference.dislikePropertyIds.contains(match.propertyId) ||
                    match.roommateMatches.any { userPreference.likeRoommateIds.contains(it.roommateId) ||
                            userPreference.dislikeRoommateIds.contains(it.roommateId) }
        }

        return unseenMatches.take(limit)
    }

    suspend fun getNextMatchFromFeed(seekerId: String): Match? {
        val (batch, index) = userMatchFeed[seekerId] ?: run {
            val newBatch = getMatchBatch(seekerId, 5)
            if (newBatch.isEmpty()) return null
            userMatchFeed[seekerId] = newBatch to 1 // index starts at 1
            return newBatch[0]
        }

        if (index >= batch.size) {
            val newBatch = getMatchBatch(seekerId, 5)
            if (newBatch.isEmpty()) return null
            userMatchFeed[seekerId] = newBatch to 1
            return newBatch[0]
        }

        val nextMatch = batch[index]
        userMatchFeed[seekerId] = batch to (index + 1)
        return nextMatch
    }





}