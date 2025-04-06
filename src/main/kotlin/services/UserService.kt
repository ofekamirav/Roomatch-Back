package com.services

import com.database.DatabaseManager
import com.models.*
import com.utils.Hashing
import com.utils.JWTUtils
import org.litote.kmongo.MongoOperator
import org.litote.kmongo.eq


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



}