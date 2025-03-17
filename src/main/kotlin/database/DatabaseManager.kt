package com.database

import com.models.BaseUser
import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.reactivestreams.KMongo
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.eq
import org.slf4j.LoggerFactory


object DatabaseManager {
    private var client = KMongo.createClient("mongodb://localhost:27017")
    private var database: CoroutineDatabase? = null
    private var usersCollection: CoroutineCollection<BaseUser>? = null
    private val logger = LoggerFactory.getLogger(DatabaseManager::class.java)

    init {
        try {
            connect()
            usersCollection = database?.getCollection("users")
        } catch (e: Exception) {
            logger.error("Error in init database", e)
        }
    }

    fun getUsersCollection(): CoroutineCollection<BaseUser>? {
        return usersCollection
    }

    private fun connect() {
        try {
            database = client.getDatabase("RooMatchDB").coroutine
        } catch (e: Exception) {
            logger.error("Error connecting to mongoDB", e)
        }
    }

    suspend fun insertUser(user: BaseUser): BaseUser? {
        return try {
            usersCollection?.insertOne(user)
            user
        } catch (e: Exception) {
            logger.error("Error in insert user", e)
            null
        }
    }

    suspend fun updateUser(user: BaseUser): BaseUser {
        usersCollection?.replaceOneById(user.id!!, user)
        return user
    }

    suspend fun getUserByEmail(email: String): BaseUser? {
        return usersCollection?.findOne(BaseUser::email eq email)
    }


    // ... פונקציות נוספות לגישה לנתונים ...
}