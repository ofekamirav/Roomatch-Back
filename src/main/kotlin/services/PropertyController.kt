package com.services


import com.models.Property
import com.database.DatabaseManager

object PropertyController {

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
        return DatabaseManager.getPropertyById(propertyId)
    }
}
