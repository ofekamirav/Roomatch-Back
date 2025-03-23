package com.database

import com.models.*
import org.bson.types.ObjectId
import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.reactivestreams.KMongo
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.eq
import org.litote.kmongo.`in`
import org.slf4j.LoggerFactory



object DatabaseManager {
    private var client = KMongo.createClient("mongodb://localhost:27017")
    private var database: CoroutineDatabase? = null
    private var roommatesCollection: CoroutineCollection<RoommateUser>? = null
    private var ownersCollection: CoroutineCollection<PropertyOwnerUser>? = null
    private var propertiesCollection: CoroutineCollection<Property>? = null
    private var matchesCollection: CoroutineCollection<Match>? = null
    private val logger = LoggerFactory.getLogger(DatabaseManager::class.java)

    init {
        try {
            connect()
            roommatesCollection = database?.getCollection("roommates")
            ownersCollection = database?.getCollection("owners")
            propertiesCollection = database?.getCollection("properties")
            matchesCollection = database?.getCollection("matches")
        } catch (e: Exception) {
            logger.error("Error in init database", e)
        }
    }

    private fun connect() {
        try {
            database = client.getDatabase("RooMatchDB").coroutine
        } catch (e: Exception) {
            logger.error("Error connecting to mongoDB", e)
        }
    }


//-------------------------Roommates---------------------------------------------------------------
    fun getRoommatesCollection(): CoroutineCollection<RoommateUser>? {
        return roommatesCollection
    }

    suspend fun getAllRoommates(): List<RoommateUser> {
        try {
            val roommates = roommatesCollection?.find()?.toList()
            if (roommates == null) {
                logger.warn("No roommates found")
            }
            return roommates ?: emptyList()
        } catch (e: Exception) {
            logger.error("Error in getAllRoommates", e)
        }
        return emptyList()
    }


    suspend fun getRoommateById(id: String): RoommateUser? {
        return try {
            roommatesCollection?.findOneById(id)
        } catch (e: Exception) {
            logger.error("Error fetching roommate by ID", e)
            null
        }
    }

    suspend fun getRoommatesByIds(ids: List<String>): List<RoommateUser> {
        return try {
            roommatesCollection?.find(RoommateUser::id `in` ids)?.toList() ?: emptyList()
        } catch (e: Exception) {
            logger.error("Error fetching roommates by IDs", e)
            emptyList()
        }
    }


    suspend fun insertRoommate(user: RoommateUser): RoommateUser? {
        return try {
            val insertedUser = user.copy(id = ObjectId().toHexString())
            roommatesCollection?.insertOne(user)
            insertedUser
        } catch (e: Exception) {
            logger.error("Error in insert user", e)
            null
        }
    }




//-------------------------Owners----------------------------------------------------------------



    fun getOwnersCollection(): CoroutineCollection<PropertyOwnerUser>? {
        return ownersCollection
    }


    suspend fun insertOwner(user: PropertyOwnerUser): PropertyOwnerUser? {
        return try {
            ownersCollection?.insertOne(user)
            user
        } catch (e: Exception) {
            logger.error("Error in insert user", e)
            null
        }
    }



    suspend fun getOwnerByEmail(email: String): PropertyOwnerUser? {
        try {
            val owner = ownersCollection?.findOne(PropertyOwnerUser::email eq email)
            if (owner == null) {
                logger.warn("Owner not found with email: $email")
            }
            return owner
        } catch (e: Exception) {
            logger.error("Error in getOwnerByEmail", e)
        }
        return null
    }


    suspend fun getOwnerById(id: String): PropertyOwnerUser? {
       return try {
            ownersCollection?.findOneById(id)
        } catch (e: Exception) {
           logger.error("Error fetching owner by ID", e)
           null
        }
    }




//-------------------------Properties---------------------------------------------------------------


    fun getPropertiesCollection(): CoroutineCollection<Property>? {
        return propertiesCollection
    }

    suspend fun getAllAvailableProperties(): List<Property> {
        try {
            val properties = propertiesCollection?.find(Property::available eq true)?.toList()
            if (properties == null) {
                logger.warn("No available properties found")
            }
            return properties ?: emptyList()
        } catch (e: Exception) {
            logger.error("Error in getAllAvailableProperties", e)
        }
        return emptyList()
    }



    suspend fun getPropertyById(id: String): Property? {
        return try {
            propertiesCollection?.findOneById(id)
        } catch (e: Exception) {
            logger.error("Error fetching property by ID", e)
            null
        }
    }


    suspend fun insertProperty(property: Property): Property? {
        return try {
            propertiesCollection?.insertOne(property)
            property
        } catch (e: Exception) {
            logger.error("Error inserting property", e)
            null
        }
    }

    suspend fun getPropertiesByOwnerId(ownerId: String): List<Property> {
        try {
            val properties = propertiesCollection?.find(Property::ownerId eq ownerId)?.toList()
            if (properties == null) {
                logger.warn("Properties not found for owner with id: $ownerId")
            }
            return properties ?: emptyList()
        } catch (e: Exception) {
            logger.error("Error in getPropertiesByOwnerId", e)
        }
        return emptyList()
    }


//---------------------------Matches------------------------------------------------------------------------------

    fun getMatchesCollection(): CoroutineCollection<Match>? {
        return matchesCollection
    }

    suspend fun insertMatch(match: Match): Match? {
        return try {
            matchesCollection?.insertOne(match)
            match
        } catch (e: Exception) {
            logger.error("Error inserting match", e)
            null
        }
    }

    suspend fun getMatchById(id: String): Match? {
        return try {
            matchesCollection?.findOneById(id)
        } catch (e: Exception) {
            logger.error("Error fetching match by ID", e)
            null
        }
    }


}