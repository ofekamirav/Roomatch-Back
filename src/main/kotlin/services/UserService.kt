package com.services

import com.database.DatabaseManager
import com.models.*
import com.utils.Hashing
import com.utils.JWTUtils
import org.litote.kmongo.Data
import org.litote.kmongo.MongoOperator
import org.litote.kmongo.eq
import org.litote.kmongo.setValue



object UserService {

    //Register a roommate user
    suspend fun registerRoommate(user: RoommateUser): UserResponse? {
        val existingUser = DatabaseManager.getRoommatesCollection()?.findOne(RoommateUser::email eq user.email)
        if (existingUser != null) {
            throw IllegalArgumentException("User with this email already exists.")
        }

        val hashedPassword = Hashing.hashPassword(user.password)
        val accessToken = JWTUtils.generateToken(user.email)
        val refreshToken = JWTUtils.generateRefreshToken(user.email)
        val roommateType = "Roommate"

        val newUser = user.copy(password = hashedPassword, refreshToken = refreshToken)

        val insertedUser = DatabaseManager.insertRoommate(newUser) // This returns RoommateUser?

        return if (insertedUser != null) {
            // Insertion successful, create and return UserResponse
            UserResponse(
                token = accessToken,
                refreshToken = refreshToken,
                userId = insertedUser.id,
                userType = roommateType
            )
        } else {
            // Insertion failed, return null to indicate failure
            null
        }
    }

    //Register a property owner user
    suspend fun registerPropertyOwner(user: PropertyOwnerUser):  UserResponse? {
        val existingUser = DatabaseManager.getOwnersCollection()?.findOne(PropertyOwnerUser::email eq user.email)
        if (existingUser != null) {
            throw IllegalArgumentException("User with this email already exists.")
        }

        // Hashing password
        val hashedPassword = Hashing.hashPassword(user.password)
        val accessToken = JWTUtils.generateToken(user.email)
        val refreshToken = JWTUtils.generateRefreshToken(user.email)
        val ownerType = "PropertyOwner"

        val newUser = user.copy(password = hashedPassword, refreshToken = refreshToken)

        val insertedUser = DatabaseManager.insertOwner(newUser)


        return if (insertedUser != null) {
            // Insertion successful, create and return UserResponse
            UserResponse(
                token = accessToken,
                refreshToken = refreshToken,
                userId = insertedUser.id,
                userType = ownerType
            )
        } else {
            // Insertion failed, return null to indicate failure
            null
        }
    }

    suspend fun getRoommateById(id: String): RoommateUser? {
        return DatabaseManager.getRoommateById(id)
    }

    suspend fun getRoomateByEmail(email: String): RoommateUser? {
        return DatabaseManager.getRoomateByEmail(email)
    }

    suspend fun login(email: String, password: String): UserResponse {
        val roommate = getRoomateByEmail(email)
        val owner = if (roommate == null) {
            DatabaseManager.getOwnerByEmail(email)
        } else null

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
                DatabaseManager.getRoommatesCollection()
                    ?.updateOne(RoommateUser::email eq email, setValue(RoommateUser::refreshToken, refreshToken))
            }
            "PropertyOwner" -> {
                DatabaseManager.getOwnersCollection()
                    ?.updateOne(PropertyOwnerUser::email eq email, setValue(PropertyOwnerUser::refreshToken, refreshToken))
            }
        }

        return UserResponse(
            token = accessToken,
            refreshToken = refreshToken,
            userId = if (userType == "Roommate") roommate?.id else owner?.id,
            userType = userType
        )
    }

    //reset a user password

    suspend fun requestPasswordReset(email: String, userType: String): Boolean {
        val resetToken = JWTUtils.generateToken(email)  // Generates JWT token
        val expiration = System.currentTimeMillis() + 3600000  // 1 hour validity

        return when (userType) {
            "Roommate" -> DatabaseManager.setRoommateResetToken(email, resetToken, expiration)
            "PropertyOwner" -> DatabaseManager.setOwnerResetToken(email, resetToken, expiration)
            else -> false
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


}