package com

import com.database.DatabaseManager
import com.models.RoommateUser
import com.services.UserService
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class UserServiceTest {

    @Test
    fun testRegisterAndFetchRoommate() {
        runBlocking {

            val dummyUser = RoommateUser(
                id = "",
                email = "testuser2@example.com",
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
    fun testTestRoute() = testApplication {
        application {
            module()
        }
        client.get("/api/test").apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals("It works!", bodyAsText())
        }
    }
}

