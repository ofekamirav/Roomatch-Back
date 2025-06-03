package com

import com.database.DatabaseManager
import com.models.PropertyOwnerUser
import com.models.RoommateUser
import com.services.UserService
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import kotlinx.coroutines.runBlocking
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.fail

class UserServiceTest {

    private fun uniqueEmail(prefix: String): String =
        "$prefix${System.currentTimeMillis()}@test.com"

    @Test
    fun testRegisterAndFetchRoommate() {
        runBlocking {
            val email = uniqueEmail("roommate")


            val dummyUser = RoommateUser(
                id = "",
                email = email,
                fullName = "Test User",
                phoneNumber = "1234567890",
                birthDate = "1990-01-01",
                password = "secure123",
                gender = null,
                attributes = emptyList(),
                hobbies = emptyList(),
                lookingForRoomies = emptyList(),
                lookingForCondo = emptyList(),
                roommatesNumber = 2,
                minPropertySize = 50,
                maxPropertySize = 150,
                minPrice = 2000,
                maxPrice = 4000,
                personalBio = "Testing",
                profilePicture = null,
                latitude = 0.0,
                longitude = 0.0,
                preferredRadiusKm = 10,
                work = "Student" // ✅ ADD THIS
            )


            val response = UserService.registerRoommate(dummyUser)
            assertNotNull(response, "User registration failed")

            val fetchedUser = UserService.getRoomateByEmail(dummyUser.email)
            assertNotNull(fetchedUser, "User was not found after registration")
        }
    }

    @Test
    fun testLoginSuccess() = runBlocking {
        val email = "testuser@example.com"
        val password = "secure123"

        UserService.registerRoommate(RoommateUser(
            id = "",
            email = email,
            fullName = "Login Test User",
            phoneNumber = "000",
            birthDate = "1990-01-01",
            password = password,
            gender = null,
            attributes = listOf(),
            hobbies = listOf(),
            lookingForRoomies = listOf(),
            lookingForCondo = listOf(),
            roommatesNumber = 1,
            minPropertySize = 20,
            maxPropertySize = 100,
            minPrice = 1000,
            maxPrice = 3000,
            personalBio = "bio",
            profilePicture = null,
            latitude = 0.0,
            longitude = 0.0,
            preferredRadiusKm = 5,
            work = "QA"
        ))
        val response = UserService.login(email, password)
        assertNotNull(response)
        assertEquals(email, DatabaseManager.getRoomateByEmail(email)?.email)
    }

    @Test
    fun testDuplicateRegistrationFails() = runBlocking {
        val email = "testuser@example.com"

        val dummyUser = RoommateUser(
            id = "",
            email = email,
            fullName = "Duplicate User",
            phoneNumber = "0000000000",
            birthDate = "1990-01-01",
            password = "secure123",
            work = "Student",
            gender = null,
            attributes = emptyList(),
            hobbies = emptyList(),
            lookingForRoomies = emptyList(),
            lookingForCondo = emptyList(),
            roommatesNumber = 1,
            minPropertySize = 50,
            maxPropertySize = 100,
            minPrice = 1000,
            maxPrice = 2000,
            personalBio = "Test Bio",
            profilePicture = null,
            latitude = 0.0,
            longitude = 0.0,
            preferredRadiusKm = 10
        )

        // ✅ Step 1: Register once (should succeed)
        val result = UserService.registerRoommate(dummyUser)
        assertNotNull(result)

        // ✅ Step 2: Try to register again with same email
        try {
            UserService.registerRoommate(dummyUser)
            fail("Expected IllegalArgumentException for duplicate email")
        } catch (e: IllegalArgumentException) {
            assertEquals("User with this email already exists.", e.message)
        }
    }

    @Test
    fun testRegisterAndFetchOwner(): Unit = runBlocking {
        val email = uniqueEmail("owner")

        val dummyOwner = PropertyOwnerUser(
            id = "",
            email = email,
            fullName = "Test Owner",
            phoneNumber = "1112223333",
            birthDate = "1980-02-02",
            password = "ownerpass123",
            profilePicture = null,
            resetToken = null,
            resetTokenExpiration = null,
            refreshToken = null
        )

        val response = UserService.registerPropertyOwner(dummyOwner)
        assertNotNull(response, "Property owner registration failed")

        val fetchedOwner = UserService.getOwnerById(response.userId!!)
        assertNotNull(fetchedOwner, "Owner was not found after registration")
    }

    @Test
    fun testUpdateRoommate() = runBlocking {
        val email = uniqueEmail("roommate")

        // First register
        val user = RoommateUser(
            id = "",
            email = email,
            fullName = "Before Update",
            phoneNumber = "123",
            birthDate = "2000-01-01",
            password = "pass123",
            gender = null,
            attributes = listOf(),
            hobbies = listOf(),
            lookingForRoomies = listOf(),
            lookingForCondo = listOf(),
            roommatesNumber = 1,
            minPropertySize = 20,
            maxPropertySize = 100,
            minPrice = 1500,
            maxPrice = 3000,
            personalBio = "test",
            profilePicture = null,
            latitude = 0.0,
            longitude = 0.0,
            preferredRadiusKm = 5,
            work = "DJ"
        )

        val response = UserService.registerRoommate(user)
        val updatedUser = user.copy(fullName = "Updated Name", password = "newpass123")

        val result = UserService.updateRoommate(response!!.userId!!, updatedUser)
        assertNotNull(result)
        assertEquals("Updated Name", result.fullName)
    }


    @Test
    fun testUpdateOwner() = runBlocking {
        val email = uniqueEmail("owner")
        val owner = PropertyOwnerUser(
            id = "",
            email = email,
            fullName = "Owner Before",
            phoneNumber = "000",
            birthDate = "1970-01-01",
            password = "originalpass",
            profilePicture = null,
            resetToken = null,
            resetTokenExpiration = null,
            refreshToken = null
        )

        val response = UserService.registerPropertyOwner(owner)
        val updated = owner.copy(fullName = "Owner Updated", password = "updatedpass")

        val result = UserService.updateOwner(response!!.userId!!, updated)
        assertNotNull(result)
        assertEquals("Owner Updated", result.fullName)
    }

    @Test
    fun testLoginRoommateSuccess() = runBlocking {
        val email = uniqueEmail("roommate")
        val password = "mypassword"

        val user = RoommateUser(
            id = "",
            email = email,
            fullName = "Login Test",
            phoneNumber = "123",
            birthDate = "2000-01-01",
            password = password,
            gender = null,
            attributes = listOf(),
            hobbies = listOf(),
            lookingForRoomies = listOf(),
            lookingForCondo = listOf(),
            roommatesNumber = 1,
            minPropertySize = 10,
            maxPropertySize = 200,
            minPrice = 1000,
            maxPrice = 5000,
            personalBio = "Login bio",
            profilePicture = null,
            latitude = 0.0,
            longitude = 0.0,
            preferredRadiusKm = 5,
            work = "QA"
        )

        UserService.registerRoommate(user)
        val response = UserService.login(email, password)
        assertNotNull(response)
        assertEquals("Roommate", response.userType)
    }

    @Test
    fun testGetRoommateById() = runBlocking {
        val email = uniqueEmail("roommate")

        val user = RoommateUser(
            id = "",
            email = email,
            fullName = "Roommate ID Test",
            phoneNumber = "999",
            birthDate = "1995-12-12",
            password = "idpass",
            gender = null,
            attributes = listOf(),
            hobbies = listOf(),
            lookingForRoomies = listOf(),
            lookingForCondo = listOf(),
            roommatesNumber = 2,
            minPropertySize = 30,
            maxPropertySize = 120,
            minPrice = 2500,
            maxPrice = 4500,
            personalBio = "Testing by ID",
            profilePicture = null,
            latitude = 0.0,
            longitude = 0.0,
            preferredRadiusKm = 8,
            work = "Engineer"
        )

        val response = UserService.registerRoommate(user)
        val result = UserService.getRoommateById(response!!.userId!!)
        assertNotNull(result)
        assertEquals(email, result.email)
    }

    @Test
    fun testGetOwnerById() = runBlocking {
        val email = uniqueEmail("owner")
        val owner = PropertyOwnerUser(
            id = "",
            email = email,
            fullName = "Owner ID Test",
            phoneNumber = "888",
            birthDate = "1985-05-05",
            password = "idownerpass",
            profilePicture = null,
            resetToken = null,
            resetTokenExpiration = null,
            refreshToken = null
        )

        val response = UserService.registerPropertyOwner(owner)
        val result = UserService.getOwnerById(response!!.userId!!)
        assertNotNull(result)
        assertEquals(email, result.email)
    }

    @AfterTest
    fun cleanTestDb() {
        TestDatabaseCleaner.cleanAllCollections()
        println("✅ Test finished: everything cleaned successfully.")
    }

}


