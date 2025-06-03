package com

import com.database.DatabaseManager
import com.models.CondoPreference
import com.models.Property
import com.models.PropertyOwnerUser
import com.models.PropertyType
import com.services.PropertyController
import com.services.UserService
import kotlinx.coroutines.runBlocking
import kotlin.test.*

class PropertyControllerTest {

    private lateinit var ownerId: String

    @BeforeTest
    fun setup() = runBlocking {
        val email = "owner_${System.currentTimeMillis()}@test.com"
        val dummyOwner = PropertyOwnerUser(
            id = "",
            email = email,
            fullName = "Test Owner",
            phoneNumber = "0501234567",
            birthDate = "1990-01-01",
            password = "ownerpass",
            profilePicture = null,
            resetToken = null,
            resetTokenExpiration = null,
            refreshToken = null
        )

        val response = UserService.registerPropertyOwner(dummyOwner)
        ownerId = response!!.userId!!
    }

    @Test
    fun testUploadAndFetchProperty() = runBlocking {
        val property = Property(
            id = "",
            ownerId = "", // Will be set in controller
            title = "Test Apartment",
            address = "123 Test St",
            latitude = 32.0853,
            longitude = 34.7818,
            type = PropertyType.APARTMENT,
            canContainRoommates = 3,
            CurrentRoommatesIds = listOf(),
            roomsNumber = 3,
            bathrooms = 1,
            floor = 2,
            size = 80,
            pricePerMonth = 4500,
            features = listOf(CondoPreference.ELEVATOR, CondoPreference.BALCONY),
            photos = listOf(),
            available = true
        )

        val uploaded = PropertyController.uploadProperty(ownerId, property)
        assertNotNull(uploaded)
        assertEquals(ownerId, uploaded.ownerId)

        val fetched = PropertyController.getPropertyById(uploaded.id!!)
        assertEquals(uploaded.title, fetched?.title)
    }

    @Test
    fun testUpdateAvailability() = runBlocking {
        val property = PropertyController.uploadProperty(ownerId, Property(
            id = "",
            ownerId = "",
            title = "For Availability Test",
            address = "Test Address",
            latitude = 0.0,
            longitude = 0.0,
            type = PropertyType.ROOM,
            canContainRoommates = 1,
            CurrentRoommatesIds = listOf(),
            roomsNumber = 1,
            bathrooms = 1,
            floor = 1,
            size = 30,
            pricePerMonth = 2000,
            features = emptyList(),
            photos = emptyList(),
            available = true
        ))

        val result = PropertyController.updateAvailability(property.id!!, false)
        assertTrue(result == true)

        val updated = PropertyController.getPropertyById(property.id)
        assertFalse(updated?.available ?: true)
    }

    @Test
    fun testDeleteProperty() = runBlocking {
        val property = PropertyController.uploadProperty(ownerId, Property(
            id = "",
            ownerId = "",
            title = "To Delete",
            address = "Delete St",
            latitude = 0.0,
            longitude = 0.0,
            type = PropertyType.APARTMENT,
            canContainRoommates = 2,
            CurrentRoommatesIds = listOf(),
            roomsNumber = 2,
            bathrooms = 1,
            floor = 0,
            size = 60,
            pricePerMonth = 2500,
            features = emptyList(),
            photos = emptyList(),
            available = true
        ))

        val deleted = PropertyController.deleteProperty(property.id!!)
        assertTrue(deleted)

        try {
            PropertyController.getPropertyById(property.id)
            fail("Should have thrown exception")
        } catch (e: IllegalArgumentException) {
            assertEquals("Property with this ID does not exist.", e.message)
        }
    }

    @Test
    fun testUpdatePropertyDetails() = runBlocking {
        val original = PropertyController.uploadProperty(ownerId, Property(
            id = "",
            ownerId = "",
            title = "Original Title",
            address = "Original Address",
            latitude = 0.0,
            longitude = 0.0,
            type = PropertyType.APARTMENT,
            canContainRoommates = 2,
            CurrentRoommatesIds = listOf(),
            roomsNumber = 2,
            bathrooms = 1,
            floor = 3,
            size = 70,
            pricePerMonth = 3000,
            features = listOf(CondoPreference.PARKING),
            photos = listOf(),
            available = true
        ))

        val updated = original.copy(
            title = "Updated Title",
            pricePerMonth = 3500,
            features = listOf(CondoPreference.PET_VERIFY, CondoPreference.GARDEN)
        )

        val success = PropertyController.updateProperty(original.id!!, updated)
        assertTrue(success == true)

        val result = PropertyController.getPropertyById(original.id)
        assertNotNull(result)
        assertEquals("Updated Title", result.title)
        assertEquals(3500, result.pricePerMonth)
        assertTrue(result.features.containsAll(listOf(CondoPreference.PET_VERIFY, CondoPreference.GARDEN)))}

    @Test
    fun testGetPropertiesByOwnerId() = runBlocking {
        // Upload two properties under the same owner
        repeat(2) {
            PropertyController.uploadProperty(ownerId, Property(
                id = "",
                ownerId = "",
                title = "Owner Property $it",
                address = "Somewhere $it",
                latitude = 0.0,
                longitude = 0.0,
                type = PropertyType.APARTMENT,
                canContainRoommates = 2,
                CurrentRoommatesIds = listOf(),
                roomsNumber = 2,
                bathrooms = 1,
                floor = it,
                size = 60,
                pricePerMonth = 2500 + (it * 100),
                features = emptyList(),
                photos = emptyList(),
                available = true
            ))
        }

        val properties = PropertyController.getPropertiesByOwnerId(ownerId)
        assertTrue(properties.size >= 2)
        assertTrue(properties.all { it.ownerId == ownerId })
    }
    @AfterTest
    fun cleanTestDb() {
        TestDatabaseCleaner.cleanAllCollections()
        println("✅ Test finished: everything cleaned successfully.")
    }
}

