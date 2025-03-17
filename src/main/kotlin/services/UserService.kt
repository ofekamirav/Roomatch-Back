package com.services

import com.database.DatabaseManager
import com.models.RoommateUser
import com.utils.Hashing
import com.utils.JWTUtils


object UserService {

    //Register a roommate user
    suspend fun registerRoommate(roommateUser: RoommateUser): Map<String, Any>? {
        val existingUser = DatabaseManager.getUserByEmail(roommateUser.email)
        if (existingUser != null) {
            return mapOf("error" to "User with this email already exists.")

        }

        val hashedPassword = Hashing.hashPassword(roommateUser.password)
        val userToSave = roommateUser.copy(password = hashedPassword)

        val insertUser = DatabaseManager.insertUser(userToSave)

        if(insertUser== null){
            return mapOf("error" to "Error creating user")
        }
        val token = JWTUtils.generateToken(insertUser!!.email)

        return mapOf("token" to token, "user" to insertUser)

    }

}