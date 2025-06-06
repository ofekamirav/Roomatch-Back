
package com.services

import com.config.AppConfig
import com.database.DatabaseManager
import com.models.*
import com.utils.Hashing
import com.utils.JWTUtils
import org.litote.kmongo.eq
import org.litote.kmongo.setValue
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.exceptions.IncompleteRegistrationException
import org.slf4j.LoggerFactory


object UserService {

    private val jsonFactory = GsonFactory.getDefaultInstance()
    private val httpTransport = GoogleNetHttpTransport.newTrustedTransport()
    private val logger = LoggerFactory.getLogger(UserService::class.java)

    //Register a roommate user
    suspend fun registerRoommate(user: RoommateUser): UserResponse? {
        val existingUser = DatabaseManager.roommatesCollection.findOne(RoommateUser::email eq user.email)
        if (existingUser != null) {
            throw IllegalArgumentException("User with this email already exists.")
        }

        val hashedPassword = Hashing.hashPassword(user.password)
        val accessToken = JWTUtils.generateToken(user.email)
        val refreshToken = JWTUtils.generateRefreshToken(user.email)
        val roommateType = "Roommate"

        val newUser = user.copy(password = hashedPassword, refreshToken = refreshToken)

        val insertedUser = DatabaseManager.insertRoommate(newUser)

        return if (insertedUser != null) {
            logger.info("Roommate registered successfully: ${insertedUser.email}")
            UserResponse(
                token = accessToken,
                refreshToken = refreshToken,
                userId = insertedUser.id,
                userType = roommateType
            )
        } else{
            logger.error("Failed to register roommate: ${user.email}")
            null
        }
    }

    suspend fun registerPropertyOwner(user: PropertyOwnerUser): UserResponse? {
        val existingUser = DatabaseManager.ownersCollection.findOne(PropertyOwnerUser::email eq user.email)
        if (existingUser != null) {
            throw IllegalArgumentException("User with this email already exists.")
        }

        val hashedPassword = Hashing.hashPassword(user.password)
        val accessToken = JWTUtils.generateToken(user.email)
        val refreshToken = JWTUtils.generateRefreshToken(user.email)
        val ownerType = "PropertyOwner"

        val newUser = user.copy(password = hashedPassword, refreshToken = refreshToken)

        val insertedUser = DatabaseManager.insertOwner(newUser)

        return if (insertedUser != null) {
            logger.info("Property owner registered successfully: ${insertedUser.email}")
            UserResponse(
                token = accessToken,
                refreshToken = refreshToken,
                userId = insertedUser.id,
                userType = ownerType
            )
        } else{
            logger.error("Failed to register property owner: ${user.email}")
            null
        }
    }

    suspend fun login(email: String, password: String): UserResponse {
        val roommate = getRoomateByEmail(email)
        val owner = if (roommate == null) DatabaseManager.getOwnerByEmail(email) else null

        if (roommate == null && owner == null) {
            throw IllegalArgumentException("User is not exists")
        }

        val userType = if (roommate != null) "Roommate" else "PropertyOwner"
        val passwordHash = roommate?.password ?: owner?.password!!

        if (!Hashing.verifyPassword(password, passwordHash)) {
            throw IllegalArgumentException("Invalid password")
        }

        val accessToken = JWTUtils.generateToken(email)
        val refreshToken = JWTUtils.generateRefreshToken(email)

        when (userType) {
            "Roommate" -> {
                DatabaseManager.roommatesCollection.updateOne(RoommateUser::email eq email, setValue(RoommateUser::refreshToken, refreshToken))
            }
            "PropertyOwner" -> {
                DatabaseManager.ownersCollection.updateOne(PropertyOwnerUser::email eq email, setValue(PropertyOwnerUser::refreshToken, refreshToken))
            }
        }
        logger.info("User logged in successfully: $email")

        return UserResponse(
            token = accessToken,
            refreshToken = refreshToken,
            userId = roommate?.id ?: owner?.id,
            userType = userType
        )
    }

    suspend fun requestPasswordReset(email: String, userType: String): String {
        val userExists: Boolean

        when (userType) {
            "Roommate" -> {
                val roommate = DatabaseManager.getRoomateByEmail(email)
                userExists = roommate != null
            }
            "PropertyOwner" -> {
                val owner = DatabaseManager.getOwnerByEmail(email)
                userExists = owner != null
            }
            else -> return "INVALID_USER_TYPE"
        }

        if (!userExists) {
            return "USER_NOT_FOUND"
        }

        val resetToken = JWTUtils.generateToken(email)
        val expiration = System.currentTimeMillis() + 3600000

        val tokenSetSuccessfully = when (userType) {
            "Roommate" -> DatabaseManager.setRoommateResetToken(email, resetToken, expiration)
            "PropertyOwner" -> DatabaseManager.setOwnerResetToken(email, resetToken, expiration)
            else -> false
        }

        return if (tokenSetSuccessfully) {
            "SUCCESS"
        } else {
            "TOKEN_SET_FAILED"
        }
    }

    suspend fun resetPassword(token: String, newPassword: String, userType: String): Boolean {
        val hashedPassword = Hashing.hashPassword(newPassword)

        return when (userType) {
            "Roommate" -> {
                val user = DatabaseManager.findRoommateByResetToken(token)
                if (user != null) {
                    DatabaseManager.resetRoommatePassword(user.email, hashedPassword)
                } else false
            }
            "PropertyOwner" -> {
                val user = DatabaseManager.findOwnerByResetToken(token)
                if (user != null) {
                    DatabaseManager.resetOwnerPassword(user.email, hashedPassword)
                } else false
            }
            else -> false
        }
    }

    suspend fun getRoommateById(id: String): RoommateUser? {
        return DatabaseManager.getRoommateById(id)
    }

    suspend fun getRoomateByEmail(email: String): RoommateUser? {
        return DatabaseManager.getRoomateByEmail(email)
    }

    suspend fun getOwnerById(id: String): PropertyOwnerUser? {
        return DatabaseManager.getOwnerById(id)
    }

    suspend fun getAllRoommates(): List<RoommateUser> {
        return DatabaseManager.getAllRoommates()
    }

    suspend fun handleGoogleSignIn(idTokenString: String): UserResponse {
        val verifier = GoogleIdTokenVerifier.Builder(httpTransport, jsonFactory)
            .setAudience(listOf(
                AppConfig.googleWebClientId,// Web Client ID
                AppConfig.googleAndroidClientId, // Android Client ID
            ))
            .build()

        val idToken: GoogleIdToken? = verifier.verify(idTokenString)
        println("handleGoogleSignIn-ID Token: $idToken")

        if (idToken != null) {
            val payload = idToken.payload
            val email = payload.email
            val fullName = payload["name"] as String
            val picture = payload["picture"] as? String

            // Check if already exists as roommate
            val roommateUser = DatabaseManager.getRoomateByEmail(email)
            if (roommateUser != null) {
                val accessToken = JWTUtils.generateToken(email)
                val refreshToken = JWTUtils.generateRefreshToken(email)

                return UserResponse(
                    token = accessToken,
                    refreshToken = refreshToken,
                    userId = roommateUser.id,
                    userType = "Roommate"
                )
            }

            // Check if already exists as property owner
            val ownerUser = DatabaseManager.getOwnerByEmail(email)
            if (ownerUser != null) {
                val accessToken = JWTUtils.generateToken(email)
                val refreshToken = JWTUtils.generateRefreshToken(email)

                return UserResponse(
                    token = accessToken,
                    refreshToken = refreshToken,
                    userId = ownerUser.id,
                    userType = "PropertyOwner"
                )
            }

            // If user does not exist — return partial info to allow full registration
            throw IncompleteRegistrationException(
                email = email,
                fullName = fullName,
                profilePicture = picture
            )
        } else {
            throw IllegalArgumentException("Invalid ID token.")
        }
    }

    suspend fun updateRoommate(id: String, user: RoommateUser): RoommateUser? {
        val existingUser = DatabaseManager.getRoommateById(id)
            ?: throw IllegalArgumentException("User with this ID does not exist.")
        logger.info("Updating roommate with ID: $id")

        val updatedUser = existingUser.copy(
            id = id,
            email = user.email,
            fullName = user.fullName,
            phoneNumber = user.phoneNumber,
            birthDate = user.birthDate,
            password = user.password,
            profilePicture = user.profilePicture,
            resetToken = user.resetToken,
            resetTokenExpiration = user.resetTokenExpiration,
            refreshToken = user.refreshToken,
            attributes = user.attributes,
            longitude = user.longitude,
            latitude = user.latitude,
            lookingForRoomies = user.lookingForRoomies,
            lookingForCondo = user.lookingForCondo,
            maxPrice = user.maxPrice,
            minPrice = user.minPrice,
            maxPropertySize = user.maxPropertySize,
            minPropertySize = user.minPropertySize,
            personalBio = user.personalBio,
            preferredRadiusKm = user.preferredRadiusKm,
            hobbies = user.hobbies,
            roommatesNumber = user.roommatesNumber,
            gender = user.gender,
            work = user.work
        )

        return DatabaseManager.updateRoommate(updatedUser)
    }

    suspend fun updateOwner(id: String, user: PropertyOwnerUser): PropertyOwnerUser? {
        val existingUser = DatabaseManager.getOwnerById(id)
            ?: throw IllegalArgumentException("User with this ID does not exist.")

        val updatedUser = existingUser.copy(
            id = id,
            email = user.email,
            fullName = user.fullName,
            phoneNumber = user.phoneNumber,
            birthDate = user.birthDate,
            password = user.password,
            profilePicture = user.profilePicture,
            resetToken = user.resetToken,
            resetTokenExpiration = user.resetTokenExpiration,
            refreshToken = user.refreshToken
        )

        return DatabaseManager.updateOwner(updatedUser)
    }
}
