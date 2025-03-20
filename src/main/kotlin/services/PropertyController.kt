package com.services


import com.models.Property
import com.database.DatabaseManager

class PropertyController {

    private val propertyCollection = DatabaseManager.getPropertiesCollection()
        ?: throw IllegalStateException("Properties collection not initialized")

    suspend fun uploadApartment(ownerId: String, property: Property): Result<Property> {
        if (ownerId.isBlank()) {
            return Result.failure(Exception("Owner ID cannot be null or empty"))
        }

        val propertyWithOwner = property.copy(ownerId = ownerId)

        return try {
            propertyCollection.insertOne(propertyWithOwner)
            Result.success(propertyWithOwner)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
