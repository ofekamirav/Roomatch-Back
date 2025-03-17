package com.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RoommateUser(
    val firstName: String,
    val lastName: String,
    val phoneNumber: String,
    val gender: Gender,
    val birthDate: String,
    val work: String,
    val attributes: List<Attribute>,
    val hobbies: List<Hobby>,
    val lookingForRoomies: List<Attribute>,
    val condoPreference: List<CondoPreference>,

    @SerialName("roommate_id") override val id: String? = null,
    @SerialName("roommate_user_type") override val userType: String = "Roommate",
    @SerialName("roommate_email") override val email: String,
    @SerialName("roommate_password") override val password: String,
    @SerialName("roommate_profile_image") override val profileImage: String? = null,
    @SerialName("roommate_token") override val token: String? = null

) : BaseUser(
    id = id,
    userType = userType,
    email = email,
    password = password,
    profileImage = profileImage,
    token = token
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
