package com.models

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId
import org.litote.kmongo.Id

@Serializable
data class RoommateUser(
    @BsonId @Contextual val id: String = ObjectId().toHexString(), // MongoDB-generated ID
    val email: String,
    val fullName: String,
    val phoneNumber: String,
    val password: String,
    val refreshToken: String?=null,
    val profilePicture: String?=null,
    val gender: Gender,
    val birthDate: String,
    val work: String,
    val attributes: List<Attribute>,
    val hobbies: List<Hobby>,
    val lookingForRoomies: List<LookingForRoomiesPreference>,
    val lookingForCondo: List<LookingForCondoPreference>,
    val roommatesNumber: Int,
    val minPropertySize: Int,
    val maxPropertySize: Int,
    val minPrice: Int,
    val maxPrice: Int,
    val personalBio: String? = null,
    val preferredRadiusKm: Int = 10,
    val latitude: Double? = null,
    val longitude: Double? = null,
)

@Serializable
enum class Gender {
    MALE,
    FEMALE,
    OTHER
}

@Serializable
enum class Attribute {
    SMOKER,
    STUDENT,
    PETLOVER,
    HASPET,
    VEGGIE,
    CLEAN,
    NIGHTJOB,
    RELATIONSHIP,
    KOSHER,
    JEWISH,
    MUSLIM,
    CHRISTIAN,
    REMOTEJOB,
    ATHEIST,
    QUIET
}

@Serializable
enum class Hobby{
    MUSICIAN,
    SPORT,
    COOKER,
    PARTY,
    TV,
    GAMER,
    ARTIST,
    DANCER,
    WRITER,
    YOGA,
    READER,
    TRAVELER
}


@Serializable
enum class CondoPreference {
    BALCONY,
    ELEVATOR,
    PET_ALLOWED,
    SHELTER,
    FURNISHED,
    PARKING,
    ROOFTOP,
    GARDEN,
    GYM
}

@Serializable
data class LookingForCondoPreference(
    val preference: CondoPreference,
    val weight: Double,
    val setWeight: Boolean = false,
)

@Serializable
data class LookingForRoomiesPreference(
    val attribute: Attribute,
    val weight: Double,
    val setWeight: Boolean,
)
