package com.database

import com.config.AppConfig
import com.models.*
import com.mongodb.client.model.Filters.eq
import com.mongodb.client.model.Updates
import org.bson.types.ObjectId
import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.reactivestreams.KMongo
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.eq
import org.litote.kmongo.`in`
import org.slf4j.LoggerFactory
import java.net.URLEncoder
import org.litote.kmongo.*


object DatabaseManager {
    private val isTestEnv = AppConfig.isTestEnv

    private val mongoDb: String = AppConfig.mongoDatabase ?: "RooMatchDB" // fallback

    private val connectionString: String = AppConfig.mongoConnectionString
    private val logger = LoggerFactory.getLogger(DatabaseManager::class.java)

    private val database: CoroutineDatabase by lazy {
        logger.info("Initializing MongoDB connection to DB: '${AppConfig.mongoDatabase}'...")
        try{
            val client = KMongo.createClient(connectionString).coroutine
            val db = client.getDatabase(mongoDb)
            logger.info("Successfully connected to MongoDB.")
            db
        }catch (e: Exception) {
            logger.error("!!! FAILED to connect to MongoDB !!! Check your MONGO_CONNECTION_STRING.", e)
            throw e
        }
    }

    val roommatesCollection: CoroutineCollection<RoommateUser> by lazy {
        database.getCollection(if (isTestEnv) "roommates_test" else "roommates")
    }
    val ownersCollection: CoroutineCollection<PropertyOwnerUser> by lazy {
        database.getCollection(if (isTestEnv) "owners_test" else "owners")
    }
    val propertiesCollection: CoroutineCollection<Property> by lazy {
        database.getCollection(if (isTestEnv) "properties_test" else "properties")
    }
    val matchesCollection: CoroutineCollection<Match> by lazy {
        database.getCollection(if (isTestEnv) "matches_test" else "matches")
    }
    val disLikeCollection: CoroutineCollection<DisLike> by lazy {
        database.getCollection(if (isTestEnv) "dislikes_test" else "dislikes")
    }


    //-------------------------Roommates---------------------------------------------------------------
    suspend fun updateRoommate(user: RoommateUser): RoommateUser? {
        return try {
            roommatesCollection.updateOneById(user.id.toString(), user)
            logger.info("Roommate updated successfully: ${user.email}")
            user
        } catch (e: Exception) {
            logger.error("Error updating roommate", e)
            null
        }
    }

    suspend fun getAllRoommates(): List<RoommateUser> {
        try {
            val roommates = roommatesCollection.find().toList()
            if (roommates == null) {
                logger.warn("No roommates found")
            }
            return roommates ?: emptyList()
        } catch (e: Exception) {
            logger.error("Error in getAllRoommates", e)
        }
        return emptyList()
    }


    suspend fun getRoommateById(id: String?): RoommateUser? {
        return try {
            if (id == null) {
                logger.warn("ID is null")
                return null
            }
            roommatesCollection.findOneById(id)
        } catch (e: Exception) {
            logger.error("Error fetching roommate by ID", e)
            null
        }
    }

    suspend fun getRoomateByEmail(email: String): RoommateUser? {
        return try { roommatesCollection.findOne(RoommateUser::email eq email)
        } catch (e: Exception) {
            logger.error("Error finding roommate by email", e)
            null
        }
    }

    suspend fun getRoommatesByIds(ids: List<String>): List<RoommateUser> {
        return try {
            roommatesCollection.find(RoommateUser::id `in` ids).toList()
        } catch (e: Exception) {
            logger.error("Error fetching roommates by IDs", e)
            emptyList()
        }
    }

    suspend fun insertRoommate(user: RoommateUser): RoommateUser? {
        return try {
            val generatedId = ObjectId().toHexString()
            val userWithId = user.copy(id = generatedId)

            val result = roommatesCollection.insertOne(userWithId)

            if (result.wasAcknowledged() == true) {
                userWithId
            } else {
                logger.error("Insertion not acknowledged for user: ${user.email}")
                null
            }
        } catch (e: Exception) {
            logger.error("Error inserting user ${user.email}", e)
            null
        }
    }

    // Set reset token for roommate user
    suspend fun setRoommateResetToken(email: String, token: String, expiration: Long): Boolean {
        return try {
            val updateResult = roommatesCollection.updateOne(
                RoommateUser::email eq email,
                set(
                    RoommateUser::resetToken setTo token,
                    RoommateUser::resetTokenExpiration setTo expiration
                )
            )
            updateResult.modifiedCount == 1L
        } catch (e: Exception) {
            logger.error("Error setting reset token for roommate", e)
            false
        }
    }

    suspend fun findRoommateByEmailAndOtp(email: String, otpCode: String): RoommateUser? {
        return try {
            roommatesCollection.findOne(
                and(
                    RoommateUser::email eq email,
                    RoommateUser::resetToken eq otpCode,
                    RoommateUser::resetTokenExpiration gt System.currentTimeMillis()
                )
            )
        } catch (e: Exception) {
            logger.error("Error finding roommate by email and reset token", e)
            null
        }
    }

    suspend fun clearRoommateResetToken(email: String): Boolean {
        return try {
            val updateResult = roommatesCollection.updateOne(
                RoommateUser::email eq email,
                combine(
                    setValue(RoommateUser::resetToken, null),
                    setValue(RoommateUser::resetTokenExpiration, null)
                )
            )
            updateResult.modifiedCount == 1L
        } catch (e: Exception) {
            logger.error("Error clearing roommate reset token", e)
            false
        }
    }

    // Update roommate's password and clear reset token
    suspend fun resetRoommatePassword(email: String, hashedPassword: String): Boolean {
        return try {
            val updateResult = roommatesCollection.updateOne(
                RoommateUser::email eq email,
                combine(
                    setValue(RoommateUser::password, hashedPassword),
                    setValue(RoommateUser::resetToken, null),
                    setValue(RoommateUser::resetTokenExpiration, null)
                )
            )
            updateResult.modifiedCount == 1L
        } catch (e: Exception) {
            logger.error("Error resetting roommate password", e)
            false
        }
    }





//-------------------------Owners----------------------------------------------------------------


    suspend fun insertOwner(user: PropertyOwnerUser): PropertyOwnerUser? {
        return try {
            val generatedId = ObjectId().toHexString()
            val userWithId = user.copy(id = generatedId)

            val result = ownersCollection.insertOne(userWithId)

            if (result.wasAcknowledged() == true) {
                userWithId
            } else {
                logger.error("Insertion not acknowledged for owner: ${user.email}")
                null
            }
        } catch (e: Exception) {
            logger.error("Error inserting owner ${user.email}", e)
            null
        }
    }



    suspend fun getOwnerByEmail(email: String): PropertyOwnerUser? {
        try {
            val owner = ownersCollection.findOne(PropertyOwnerUser::email eq email)
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
            ownersCollection.findOneById(id)
        } catch (e: Exception) {
            logger.error("Error fetching owner by ID", e)
            null
        }
    }

    suspend fun updateOwner(user: PropertyOwnerUser): PropertyOwnerUser? {
        return try {
            ownersCollection.updateOneById(user.id.toString(), user)
            user
        } catch (e: Exception) {
            logger.error("Error updating owner", e)
            null
        }
    }

    // Set reset token for property owner user
    suspend fun setOwnerResetToken(email: String, token: String, expiration: Long): Boolean {
        return try {
            val updateResult = ownersCollection.updateOne(
                PropertyOwnerUser::email eq email,
                set(
                    PropertyOwnerUser::resetToken setTo token,
                    PropertyOwnerUser::resetTokenExpiration setTo expiration
                )
            )
            updateResult.modifiedCount == 1L
        } catch (e: Exception) {
            logger.error("Error setting reset token for owner", e)
            false
        }
    }

    suspend fun findOwnerByEmailAndOtp(email: String, otpCode: String): PropertyOwnerUser? {
        return try {
            ownersCollection.findOne(
                and(
                    PropertyOwnerUser::email eq email,
                    PropertyOwnerUser::resetToken eq otpCode,
                    PropertyOwnerUser::resetTokenExpiration gt System.currentTimeMillis()
                )
            )
        } catch (e: Exception) {
            logger.error("Error finding owner by email and reset token", e)
            null
        }
    }

    // Update owner's password and clear reset token
    suspend fun resetOwnerPassword(email: String, hashedPassword: String): Boolean {
        return try {
            val updateResult = ownersCollection.updateOne(
                PropertyOwnerUser::email eq email,
                combine(
                    setValue(PropertyOwnerUser::password, hashedPassword),
                    setValue(PropertyOwnerUser::resetToken, null),
                    setValue(PropertyOwnerUser::resetTokenExpiration, null)
                )
            )
            updateResult.modifiedCount == 1L
        } catch (e: Exception) {
            logger.error("Error resetting owner password", e)
            false
        }
    }

    suspend fun clearOwnerResetToken(email: String): Boolean {
        return try {
            val updateResult = ownersCollection.updateOne(
                PropertyOwnerUser::email eq email,
                combine(
                    setValue(PropertyOwnerUser::resetToken, null),
                    setValue(PropertyOwnerUser::resetTokenExpiration, null)
                )
            )
            updateResult.modifiedCount == 1L
        } catch (e: Exception) {
            logger.error("Error clearing owner reset token", e)
            false
        }
    }





//-------------------------Properties---------------------------------------------------------------


    suspend fun getAllAvailableProperties(): List<Property> {
        try {
            val properties = propertiesCollection.find(Property::available eq true).toList()
            if (properties == null) {
                logger.warn("No available properties found")
            }
            return properties
        } catch (e: Exception) {
            logger.error("Error in getAllAvailableProperties", e)
        }
        return emptyList()
    }

    suspend fun updateProperty(property: Property): Boolean? {
        return try {
            val updatedProperty = propertiesCollection.updateOneById(property.id.toString(), property)
            if (updatedProperty != null) {
                logger.info("Property updated successfully: ${property.title}")
                true
            } else {
                logger.error("Failed to update property: ${property.title}")
                false
            }
        } catch (e: Exception) {
            logger.error("Error updating property", e)
            null
        }
    }

    suspend fun deletePropertyById(id: String): Boolean {
        return try {
            val result = propertiesCollection.deleteOneById(id)
            result.wasAcknowledged() == true
        } catch (e: Exception) {
            logger.error("Error deleting property by ID", e)
            false
        }
    }



    suspend fun getPropertyById(id: String): Property? {
        try {
            val property = propertiesCollection.findOne(Property::id eq id)
            logger.info("Property found: $id")
            return property
        } catch (e: Exception) {
            logger.error("Error fetching property by ID", e)
            return null
        }
    }


    suspend fun insertProperty(property: Property): Property? {
        return try {
            val generatedId = ObjectId().toHexString()
            val propertywithid = property.copy(id = generatedId)

            val result = propertiesCollection.insertOne(propertywithid)

            if (result.wasAcknowledged() == true) {
                propertywithid
            } else {
                logger.error("Insertion not acknowledged for property: ${propertywithid.title}")
                null
            }
        } catch (e: Exception) {
            logger.error("Error inserting property", e)
            null
        }
    }

    suspend fun getPropertiesByOwnerId(ownerId: String): List<Property> {
        try {
            val properties = propertiesCollection.find(Property::ownerId eq ownerId).toList()
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

    suspend fun insertMatch(match: Match): Match? {
        return try {
            val matchWithId = if (match.id?.isBlank() == true) {
                match.copy(id = ObjectId().toHexString())
            } else {
                match
            }
            val result = matchesCollection.insertOne(matchWithId)
            if (result.wasAcknowledged() == true) {
                matchWithId
            } else {
                logger.error("Insertion not acknowledged for match: ${matchWithId.id}")
                null
            }
        } catch (e: Exception) {
            logger.error("Error inserting match", e)
            null
        }
    }

    suspend fun getMatchById(id: String): Match? {
        return try {
            matchesCollection.findOneById(id)
        } catch (e: Exception) {
            logger.error("Error fetching match by ID", e)
            null
        }
    }

    suspend fun appendSeenMatch(seekerId: String, match: SeenMatch) {
        val filter = eq("seekerId", seekerId)
        val update = Updates.addToSet("seenMatches", match)
        disLikeCollection.updateOne(filter, update)
    }


    suspend fun getMatchesBySeekerId(seekerId: String): List<Match> {
        return try {
            matchesCollection.find(Match::seekerId eq seekerId).toList()
        } catch (e: Exception) {
            logger.error("Error fetching matches by seeker ID", e)
            emptyList()
        }
    }

    suspend fun getMatchesByPropertyId(propertyId: String): List<Match> {
        return try {
            matchesCollection.find(Match::propertyId eq propertyId).toList()
        } catch (e: Exception) {
            logger.error("Error fetching matches by property ID", e)
            emptyList()
        }
    }

    suspend fun getMatchCountByPropertyId(propertyId: String): Int {
        return try {
            matchesCollection.countDocuments(Match::propertyId eq propertyId).toInt()
        } catch (e: Exception) {
            logger.error("Error fetching match count by property ID", e)
            0
        }
    }

    suspend fun getAverageMatchScoreByPropertyId(propertyId: String): Double {
        return try {
            val matches = matchesCollection.find(Match::propertyId eq propertyId).toList()
            if (matches.isEmpty()) {
                0.0
            } else {
                matches.map { it.propertyMatchScore }.average()
            }
        } catch (e: Exception) {
            logger.error("Error fetching average match score by property ID", e)
            0.0
        }
    }

    suspend fun getRoommateNumByPropertyId(propertyId: String): Int {
        return try {
            val matches = matchesCollection.find(Match::propertyId eq propertyId).toList()
            if (matches.isEmpty()) {
                0
            } else {
                matches.flatMap { it.roommateMatches }.distinctBy { it.roommateId }.size
            }
        } catch (e: Exception) {
            logger.error("Error fetching roommate number by property ID", e)
            0
        }
    }

    suspend fun deleteMatch(matchId: String): Boolean {
        try {
            val result = matchesCollection.deleteOneById(matchId)
            if (result.wasAcknowledged() == true) {
                logger.info("Match deleted successfully: $matchId")
                return true
            } else {
                logger.error("Failed to delete match: $matchId")
            }
        } catch (e: Exception) {
            logger.error("Error deleting match by ID", e)
        }
        return false
    }


//------------------------------Dislikes--------------------------------------------------------------


    suspend fun getDisLikeBySeekerId(seekerId: String): DisLike? {
        return try {
            disLikeCollection.findOne(DisLike::seekerId eq seekerId)
        } catch (e: Exception) {
            logger.error("Error fetching dislike by seeker ID", e)
            null
        }
    }

    suspend fun getDislikeRoommatesIds(seekerId: String): List<String?> {
        return try {
            val disLike = disLikeCollection.findOne(DisLike::seekerId eq seekerId)
            disLike?.dislikedRoommatesIds ?: emptyList()
        } catch (e: Exception) {
            logger.error("Error fetching disliked roommates IDs", e)
            emptyList()
        }
    }

    suspend fun getDislikePropertiesIds(seekerId: String): List<String?> {
        return try {
            val disLike = disLikeCollection.findOne(DisLike::seekerId eq seekerId)
            disLike?.dislikedPropertiesIds ?: emptyList()
        } catch (e: Exception) {
            logger.error("Error fetching disliked properties IDs", e)
            emptyList()
        }
    }

    suspend fun updateDislikeRoommates(seekerId: String, dislikedRoommatesIds: List<String?>): DisLike? {
        return try {
            val disLike = disLikeCollection.findOne(DisLike::seekerId eq seekerId)
            if (disLike != null) {
                disLike.dislikedRoommatesIds = dislikedRoommatesIds
                disLikeCollection.updateOneById(disLike.id!!, disLike)
                disLike
            } else {
                logger.warn("Dislike record not found for seekerId: $seekerId")
                null
            }
        } catch (e: Exception) {
            logger.error("Error updating disliked roommates IDs", e)
            null
        }
    }

    suspend fun updateDislikeProperties(seekerId: String, dislikedPropertiesIds: List<String?>): DisLike? {
        return try {
            val disLike = disLikeCollection.findOne(DisLike::seekerId eq seekerId)
            if (disLike != null) {
                disLike.dislikedPropertiesIds = dislikedPropertiesIds
                disLikeCollection.updateOneById(disLike.id!!, disLike)
                disLike
            } else {
                null
            }
        } catch (e: Exception) {
            logger.error("Error updating disliked properties IDs", e)
            null
        }
    }

    // Get dislike record for a seeker
    suspend fun getDislikeBySeekerId(seekerId: String): DisLike? {
        return try {
            disLikeCollection.findOne(DisLike::seekerId eq seekerId)
        } catch (e: Exception) {
            logger.error("Error getting DisLike by seekerId", e)
            null
        }
    }

    // Insert a new dislike record
    suspend fun insertDislike(dislike: DisLike): Boolean {
        return try {
            disLikeCollection.insertOne(dislike)
            true
        } catch (e: Exception) {
            logger.error("Error inserting new DisLike", e)
            false
        }
    }

    // Update an existing dislike record
    suspend fun updateDislike(dislike: DisLike): Boolean {
        return try {
            disLikeCollection.replaceOne(DisLike::seekerId eq dislike.seekerId, dislike)
            true
        } catch (e: Exception) {
            logger.error("Error updating DisLike", e)
            false
        }
    }
}
