package com.models

import kotlinx.serialization.Serializable

@Serializable
data class Property(
    val id: String?=null,
    val ownerId: String?=null,
    val type: PropertyType,
    val address: String?=null,
    val title: String?=null,
    val roommates: Int?=null,
    val roomNumber: Int?=null,
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