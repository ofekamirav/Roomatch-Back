package com.services


import com.models.Property
import com.database.DatabaseManager
import org.litote.kmongo.MongoOperator

object PropertyController {

    suspend fun uploadProperty(ownerId: String, property: Property): Map<String, Any> {
        val existingOwner = DatabaseManager.getOwnerById(ownerId)
        if (existingOwner == null) {
            return mapOf("error" to "Owner does not exist.")
        }

        val propertyWithOwner = property.copy(ownerId = ownerId)

        val insertedProperty = DatabaseManager.insertProperty(propertyWithOwner)

        return if (insertedProperty != null) {
            mapOf(
                "propertyId" to (MongoOperator.id ?: ""),
                "message" to "Property successfully added."
            )
        } else {
            mapOf("error" to "Failed to create property.")
        }
    }

    suspend fun getPropertiesByOwnerId(ownerId: String): List<Property> {
        return DatabaseManager.getPropertiesByOwnerId(ownerId)
    }
}
