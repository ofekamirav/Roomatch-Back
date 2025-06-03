package com

import com.models.*
import com.services.MatchService
import com.services.UserService
import com.services.PropertyController
import kotlinx.coroutines.runBlocking
import kotlin.test.*

class MatchServiceTest {

    private lateinit var seekerId: String

    @BeforeTest
    fun setup() {
        runBlocking {

            // Register a roommate seeker
            val email = "seeker_${System.currentTimeMillis()}@test.com"
            val user = RoommateUser(
                id = "",
                email = email,
                fullName = "Seeker User",
                phoneNumber = "0501111111",
                birthDate = "1999-01-01",
                password = "seekerpass",
                gender = Gender.MALE,
                attributes = listOf(Attribute.CLEAN),
                hobbies = listOf(Hobby.MUSICIAN),
                lookingForRoomies = listOf(LookingForRoomiesPreference(Attribute.CLEAN, 1.0, true)),
                lookingForCondo = listOf(LookingForCondoPreference(CondoPreference.ELEVATOR, 1.0, true)),
                roommatesNumber = 1,
                minPropertySize = 10,
                maxPropertySize = 100,
                minPrice = 2000,
                maxPrice = 4000,
                personalBio = "Looking for matches",
                profilePicture = null,
                latitude = 32.0,
                longitude = 34.0,
                preferredRadiusKm = 10,
                work = "Dev"
            )
            seekerId = UserService.registerRoommate(user)!!.userId!!

            // Register a matching roommate
            val otherRoommate = user.copy(
                email = "roommate_${System.currentTimeMillis()}@test.com",
                fullName = "Matchable Roommate"
            )
            UserService.registerRoommate(otherRoommate)

            // Upload a matching property
            val owner = PropertyOwnerUser(
                id = "",
                email = "owner_${System.currentTimeMillis()}@test.com",
                fullName = "Owner Test",
                phoneNumber = "0501234567",
                birthDate = "1990-01-01",
                password = "ownerpass",
                profilePicture = null,
                resetToken = null,
                resetTokenExpiration = null,
                refreshToken = null
            )
            val ownerId = UserService.registerPropertyOwner(owner)!!.userId!!

            val property = Property(
                id = "",
                ownerId = "",
                title = "Matching Apartment",
                address = "Matching St",
                latitude = 32.001,
                longitude = 34.001,
                type = PropertyType.APARTMENT,
                canContainRoommates = 1,
                CurrentRoommatesIds = listOf(),
                roomsNumber = 2,
                bathrooms = 1,
                floor = 2,
                size = 70,
                pricePerMonth = 3000,
                features = listOf(CondoPreference.ELEVATOR),
                photos = listOf(),
                available = true
            )
            PropertyController.uploadProperty(ownerId, property)
        }
    }
    @Test
    fun testPerformMatchReturnsAtLeastOneResult() = runBlocking {
        val matches = MatchService.performMatch(seekerId, 5)
        assertTrue(matches.isNotEmpty(), "Expected at least one match")
        val match = matches.first()
        assertEquals(seekerId, match.seekerId)
        assertTrue(match.roommateMatches.isNotEmpty())
        assertTrue(match.propertyMatchScore > 0)
    }
    @AfterTest
    fun cleanTestDb() {
        TestDatabaseCleaner.cleanAllCollections()
        println("✅ Test finished: everything cleaned successfully.")
    }
}

