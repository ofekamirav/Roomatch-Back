package com.models

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

@Serializable
data class Match(
    @BsonId @Contextual val id: String? = null,
    val seekerId: String,
    val propertyId: String,
    val roommateMatches: List<RoommateMatch> = emptyList(),
    val propertyMatchScore: Int,
    )


