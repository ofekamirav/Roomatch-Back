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
    suspend fun registerRoommate(user: RoommateUser): Map<String, Any> {
        val existingUser = DatabaseManager.getRoommatesCollection()?.findOne(RoommateUser::email eq user.email)
        if (existingUser != null) {
            return mapOf("error" to "User with this email already exists.")
        }

        // Hashing password
        val hashedPassword = Hashing.hashPassword(user.password)
        val accessToken = JWTUtils.generateToken(user.email)
        val refreshToken = JWTUtils.generateRefreshToken(user.email)
        val roommateType = "Roommate"

        val newUser = user.copy(password = hashedPassword, refreshToken = refreshToken)

        val insertedUser = DatabaseManager.insertRoommate(newUser)

        return if (insertedUser != null) {
            mapOf(
                "token" to accessToken,
                "refreshToken" to refreshToken,
                "userId" to insertedUser.id,
                "userType" to roommateType
            )
        } else {
            mapOf("error" to "Failed to create user.")
        }
    }

    //Register a property owner user
    suspend fun registerPropertyOwner(user: PropertyOwnerUser): Map<String, Any> {
        val existingUser = DatabaseManager.getOwnersCollection()?.findOne(PropertyOwnerUser::email eq user.email)
        if (existingUser != null) {
            return mapOf("error" to "User with this email already exists.")
        }

        // Hashing password
        val hashedPassword = Hashing.hashPassword(user.password)
        val accessToken = JWTUtils.generateToken(user.email)
        val refreshToken = JWTUtils.generateRefreshToken(user.email)
        val ownerType = "PropertyOwner"

        val newUser = user.copy(password = hashedPassword, refreshToken = refreshToken)

        val insertedUser = DatabaseManager.insertOwner(newUser)

        return if (insertedUser != null) {
            mapOf(
                "token" to accessToken,
                "refreshToken" to refreshToken,
                "userId" to insertedUser.id,
                "userType" to ownerType
            )
        } else {
            mapOf("error" to "Failed to create user.")
        }
    }

    suspend fun getRoommateById(id: String): RoommateUser? {
        return DatabaseManager.getRoommateById(id)
    }

    suspend fun getRoomateByEmail(email: String): RoommateUser? {
        return DatabaseManager.getRoomateByEmail(email)
    }

    suspend fun login(email: String, password: String): Map<String, Any> {
        val roommate = getRoomateByEmail(email)
        val owner = if (roommate == null) {
            DatabaseManager.getOwnerByEmail(email)
        } else null

        if (roommate == null && owner == null) {
            return mapOf("error" to "User not found.")
        }

        val userType = if (roommate != null) "Roommate" else "PropertyOwner"
        val passwordHash = roommate?.password ?: owner?.password!!

        if (!Hashing.verifyPassword(password, passwordHash)) {
            return mapOf("error" to "Invalid password.")
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

        return mapOf(
            "token" to accessToken,
            "refreshToken" to refreshToken,
            "userId" to (roommate?.id ?: owner?.id.orEmpty()),
            "userType" to userType
        )
    }

}