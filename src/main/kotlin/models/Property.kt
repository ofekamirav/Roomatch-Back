package com.models

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

@Serializable
data class Property(
    @BsonId @Contextual val id: String? = null,
    val ownerId: String?=null,
    val available: Boolean?=null,
    val type: PropertyType,
    val address: String?=null,
    val latitude: Double?=null,
    val longitude: Double?=null,
    val title: String?=null,
    val canContainRoommates: Int?=null,
    var CurrentRoommatesIds: List<String> = emptyList(),
    val roomsNumber: Int?=null,
    val bathrooms: Int?=null,
    val floor: Int?=null,
    val size: Int?=null,
    val pricePerMonth: Int?=null,
    val features: List<CondoPreference> = emptyList(),
    val photos: List<String> = emptyList()
)

@Serializable
enum class PropertyType {
    ROOM,
    APARTMENT
}