package com.services


import com.models.Property
import com.database.DatabaseManager
import org.slf4j.LoggerFactory

object PropertyController {
    private val logger = LoggerFactory.getLogger(PropertyController::class.java)
    suspend fun uploadProperty(ownerId: String, property: Property): Property {
        val existingOwner = DatabaseManager.getOwnerById(ownerId)
        if (existingOwner == null) {
            throw IllegalArgumentException("Owner with this ID does not exist.")
        }

        val propertyWithOwner = property.copy(ownerId = ownerId)

        val insertedProperty = DatabaseManager.insertProperty(propertyWithOwner)

        return if (insertedProperty != null) {
            // Insertion successful, return the inserted property
            insertedProperty
        } else {
            // Insertion failed, throw an exception or handle it as needed
            throw IllegalStateException("Failed to insert property into database.")

        }
    }

    suspend fun getPropertiesByOwnerId(ownerId: String): List<Property> {
        return DatabaseManager.getPropertiesByOwnerId(ownerId)
    }

    suspend fun getPropertyById(propertyId: String): Property? {
        val property = DatabaseManager.getPropertyById(propertyId)
            ?: throw IllegalArgumentException("Property with this ID does not exist.")
        logger.info("Property found: ${property.id}")
        return property
    }

    suspend fun updateAvailability(propertyId: String, isAvailable: Boolean): Boolean? {
        val property = DatabaseManager.getPropertyById(propertyId)
            ?: throw IllegalArgumentException("Property with this ID does not exist.")

        val updatedProperty = property.copy(available = isAvailable)

        return DatabaseManager.updateProperty(updatedProperty)
    }

    suspend fun deleteProperty(propertyId: String): Boolean {
        return DatabaseManager.deletePropertyById(propertyId)
    }

    suspend fun updateProperty(propertyId: String, property: Property): Boolean? {
        val existingProperty = DatabaseManager.getPropertyById(propertyId)
            ?: throw IllegalArgumentException("Property with this ID does not exist.")

        val updatedProperty = existingProperty.copy(
            id = propertyId,
            ownerId = property.ownerId,
            title = property.title,
            address = property.address,
            latitude = property.latitude,
            longitude = property.longitude,
            type = property.type,
            canContainRoommates = property.canContainRoommates,
            CurrentRoommatesIds = property.CurrentRoommatesIds,
            roomsNumber = property.roomsNumber,
            bathrooms = property.bathrooms,
            floor = property.floor,
            size = property.size,
            pricePerMonth = property.pricePerMonth,
            features = property.features,
            photos = property.photos
        )

        return DatabaseManager.updateProperty(updatedProperty)
    }
}
