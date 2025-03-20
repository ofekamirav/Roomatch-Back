package com.models

import kotlinx.serialization.Serializable

@Serializable
data class RoommateUser(
    val email: String,
    val fullName: String,
    val phoneNumber: String,
    val password: String,
    val token: String?=null,
    val profilePicture: String?=null,
    val gender: Gender,
    val birthDate: String,
    val work: String,
    val attributes: List<Attribute>,
    val hobbies: List<Hobby>,
    val lookingForRoomies: List<Attribute>,
    val condoPreference: List<CondoPreference>,
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
    PET_LOVER,
    PET_OWNER,
    VEGETARIAN,
    CLEAN,
    NIGHT_WORKER,
    IN_RELATIONSHIP,
    KOSHER,
    JEWISH,
    MUSLIM,
    CHRISTIAN,
    REMOTE_WORKER,
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
    WRITER
}


@Serializable
enum class CondoPreference {
    BALCONY,
    ELEVATOR,
    PET_ALLOWED,
    SHELTER,
    FURNISHED,
    PARKING
}
