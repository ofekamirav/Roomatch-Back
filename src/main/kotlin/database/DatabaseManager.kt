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
import io.github.cdimascio.dotenv.dotenv
import java.net.URLEncoder
import org.litote.kmongo.*


object DatabaseManager {

    private val dotenv = dotenv()
    private val mongoUser: String = URLEncoder.encode(dotenv["MONGODB_USER"], "UTF-8") ?: error("MONGODB_USER is not set in .env")
    private val mongoPassword: String = URLEncoder.encode(dotenv["MONGODB_PASSWORD"],"UTF-8") ?: error("MONGODB_PASSWORD is not set in .env")
    private val mongoCluster: String = dotenv["MONGODB_CLUSTER"] ?: error("MONGODB_CLUSTER is not set in .env")

    private val connectionString: String = "mongodb+srv://$mongoUser:$mongoPassword@$mongoCluster/?retryWrites=true&w=majority&appName=Cluster0"
    private var client = KMongo.createClient(connectionString)
    private var database: CoroutineDatabase? = null
    private var roommatesCollection: CoroutineCollection<RoommateUser>? = null
    private var ownersCollection: CoroutineCollection<PropertyOwnerUser>? = null
    private var propertiesCollection: CoroutineCollection<Property>? = null
    private var matchesCollection: CoroutineCollection<Match>? = null
    private var disLikeCollection: CoroutineCollection<DisLike>? = null
    private val logger = LoggerFactory.getLogger(DatabaseManager::class.java)

    init {
        try {
            connect()
            roommatesCollection = database?.getCollection("roommates")
            ownersCollection = database?.getCollection("owners")
            propertiesCollection = database?.getCollection("properties")
            matchesCollection = database?.getCollection("matches")
            disLikeCollection = database?.getCollection("dislikes")
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

    suspend fun getRoomateByEmail(email: String): RoommateUser? {
        return try {
            roommatesCollection?.findOne(RoommateUser::email eq email)
        } catch (e: Exception) {
            logger.error("Error finding roommate by email", e)
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

    // Set reset token for roommate user
    suspend fun setRoommateResetToken(email: String, token: String, expiration: Long): Boolean {
        return try {
            val updateResult = roommatesCollection?.updateOne(
                RoommateUser::email eq email,
                set(
                    RoommateUser::resetToken setTo token,
                    RoommateUser::resetTokenExpiration setTo expiration
                )
            )
            updateResult?.modifiedCount == 1L
        } catch (e: Exception) {
            logger.error("Error setting reset token for roommate", e)
            false
        }
    }

    // Find roommate by reset token
    suspend fun findRoommateByResetToken(token: String): RoommateUser? {
        return try {
            roommatesCollection?.findOne(
                and(
                    RoommateUser::resetToken eq token,
                    RoommateUser::resetTokenExpiration gt System.currentTimeMillis()
                )
            )
        } catch (e: Exception) {
            logger.error("Error finding roommate by reset token", e)
            null
        }
    }

    // Update roommate's password and clear reset token
    suspend fun resetRoommatePassword(email: String, hashedPassword: String): Boolean {
        return try {
            val updateResult = roommatesCollection?.updateOne(
                RoommateUser::email eq email,
                combine(
                    setValue(RoommateUser::password, hashedPassword),
                    setValue(RoommateUser::resetToken, null),
                    setValue(RoommateUser::resetTokenExpiration, null)
                )
            )
            updateResult?.modifiedCount == 1L
        } catch (e: Exception) {
            logger.error("Error resetting roommate password", e)
            false
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

    suspend fun updateOwner(user: PropertyOwnerUser): PropertyOwnerUser? {
        return try {
            ownersCollection?.updateOneById(user.id, user)
            user
        } catch (e: Exception) {
            logger.error("Error updating owner", e)
            null
        }
    }

    // Set reset token for property owner user
    suspend fun setOwnerResetToken(email: String, token: String, expiration: Long): Boolean {
        return try {
            val updateResult = ownersCollection?.updateOne(
                PropertyOwnerUser::email eq email,
                set(
                    PropertyOwnerUser::resetToken setTo token,
                    PropertyOwnerUser::resetTokenExpiration setTo expiration
                )
            )
            updateResult?.modifiedCount == 1L
        } catch (e: Exception) {
            logger.error("Error setting reset token for owner", e)
            false
        }
    }

    // Find owner by reset token
    suspend fun findOwnerByResetToken(token: String): PropertyOwnerUser? {
        return try {
            ownersCollection?.findOne(
                and(
                    PropertyOwnerUser::resetToken eq token,
                    PropertyOwnerUser::resetTokenExpiration gt System.currentTimeMillis()
                )
            )
        } catch (e: Exception) {
            logger.error("Error finding owner by reset token", e)
            null
        }
    }

    // Update owner's password and clear reset token
    suspend fun resetOwnerPassword(email: String, hashedPassword: String): Boolean {
        return try {
            val updateResult = ownersCollection?.updateOne(
                PropertyOwnerUser::email eq email,
                combine(
                    setValue(PropertyOwnerUser::password, hashedPassword),
                    setValue(PropertyOwnerUser::resetToken, null),
                    setValue(PropertyOwnerUser::resetTokenExpiration, null)
                )
            )
            updateResult?.modifiedCount == 1L
        } catch (e: Exception) {
            logger.error("Error resetting owner password", e)
            false
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

    suspend fun getMatchesBySeekerId(seekerId: String): List<Match> {
        return try {
            matchesCollection?.find(Match::seekerId eq seekerId)?.toList() ?: emptyList()
        } catch (e: Exception) {
            logger.error("Error fetching matches by seeker ID", e)
            emptyList()
        }
    }


//------------------------------Dislikes--------------------------------------------------------------


    suspend fun insertDisLike(dislike: DisLike): DisLike? {
        return try {
            disLikeCollection?.insertOne(dislike)
            dislike
        } catch (e: Exception) {
            logger.error("Error inserting dislike", e)
            null
        }
    }

    suspend fun getDisLikeBySeekerId(seekerId: String): DisLike? {
        return try {
            disLikeCollection?.findOne(DisLike::seekerId eq seekerId)
        } catch (e: Exception) {
            logger.error("Error fetching dislike by seeker ID", e)
            null
        }
    }

    suspend fun getDislikeRoommatesIds(seekerId: String): List<String> {
        return try {
            val disLike = disLikeCollection?.findOne(DisLike::seekerId eq seekerId)
            disLike?.dislikedRoommatesIds ?: emptyList()
        } catch (e: Exception) {
            logger.error("Error fetching disliked roommates IDs", e)
            emptyList()
        }
    }

    suspend fun getDislikePropertiesIds(seekerId: String): List<String> {
        return try {
            val disLike = disLikeCollection?.findOne(DisLike::seekerId eq seekerId)
            disLike?.dislikedPropertiesIds ?: emptyList()
        } catch (e: Exception) {
            logger.error("Error fetching disliked properties IDs", e)
            emptyList()
        }
    }

    suspend fun updateDislikeRoommates(seekerId: String, dislikedRoommatesIds: List<String>): DisLike? {
        return try {
            val disLike = disLikeCollection?.findOne(DisLike::seekerId eq seekerId)
            if (disLike != null) {
                disLike.dislikedRoommatesIds = dislikedRoommatesIds
                disLikeCollection?.updateOneById(disLike.id, disLike)
                disLike
            } else {
                null
            }
        } catch (e: Exception) {
            logger.error("Error updating disliked roommates IDs", e)
            null
        }
    }

    suspend fun updateDislikeProperties(seekerId: String, dislikedPropertiesIds: List<String>): DisLike? {
        return try {
            val disLike = disLikeCollection?.findOne(DisLike::seekerId eq seekerId)
            if (disLike != null) {
                disLike.dislikedPropertiesIds = dislikedPropertiesIds
                disLikeCollection?.updateOneById(disLike.id, disLike)
                disLike
            } else {
                null
            }
        } catch (e: Exception) {
            logger.error("Error updating disliked properties IDs", e)
            null
        }
    }


}