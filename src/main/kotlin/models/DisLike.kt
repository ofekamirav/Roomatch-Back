package com.models

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

@Serializable
data class DisLike(
    @BsonId @Contextual val id: String? = null,
    val seekerId: String,
    var dislikedRoommatesIds: List<String?> = emptyList(),
    var dislikedPropertiesIds: List<String?> = emptyList(),
    var seenMatches: List<SeenMatch> = emptyList()
)

@Serializable
data class SeenMatch(
    val propertyId: String,
    val roommateIds: List<String>
)
