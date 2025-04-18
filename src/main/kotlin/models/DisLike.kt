package com.models

import kotlinx.serialization.Contextual
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

data class DisLike(
    @BsonId @Contextual val id: String = ObjectId().toHexString(), // MongoDB-generated ID
    val seekerId: String,
    val dislikedRoommatesIds: List<String> = emptyList(),
    val dislikedPropertiesIds: List<String> = emptyList()
)
