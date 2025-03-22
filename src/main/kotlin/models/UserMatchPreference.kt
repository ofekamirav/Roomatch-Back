package com.models

import kotlinx.serialization.Contextual
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

data class UserMatchPreference(
    @BsonId @Contextual val id: String = ObjectId().toHexString(), // MongoDB-generated ID
    val userId: String,
    val dislikeRoommateIds: List<String> = mutableListOf(),
    val likeRoommateIds: List<String> = mutableListOf(),
    val likePropertyIds: List<String> = mutableListOf(),
    val dislikePropertyIds: List<String> = mutableListOf(),
    ){
    constructor(userId: String) : this(
        id = ObjectId().toHexString(),
        userId = userId,
        dislikeRoommateIds = mutableListOf(),
        likeRoommateIds = mutableListOf(),
        likePropertyIds = mutableListOf(),
        dislikePropertyIds = mutableListOf()
    )
}
