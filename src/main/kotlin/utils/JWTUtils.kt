package com.utils

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.database.DatabaseManager
import com.models.PropertyOwnerUser
import com.models.RoommateUser
import org.litote.kmongo.eq
import org.litote.kmongo.setValue
import java.util.*

object JWTUtils {

    private const val SECRET = "SuperSecretKey"
    private const val REFRESH_SECRET = "SuperSecretRefreshKey"
    private const val EXPIRATION_TIME = 24 * 60 * 60 * 1000 // 24 Hours
    private const val REFRESH_EXPIRATION_TIME = 15 * 24 * 60 * 60 * 1000 // 15 Days

    fun generateToken(email: String): String {
        return JWT.create()
            .withSubject("Authentication")
            .withIssuer("roomatch")
            .withClaim("email", email)
            .withExpiresAt(Date(System.currentTimeMillis() + EXPIRATION_TIME))
            .sign(Algorithm.HMAC256(SECRET))
    }

    fun generateRefreshToken(email: String): String {
        return JWT.create()
            .withSubject("Authentication")
            .withIssuer("roomatch")
            .withClaim("email", email)
            .withExpiresAt(Date(System.currentTimeMillis() + REFRESH_EXPIRATION_TIME))
            .sign(Algorithm.HMAC256(REFRESH_SECRET))
    }

    fun verifyToken(token: String): JWTVerifier {
        return JWT.require(Algorithm.HMAC256(SECRET))
            .withIssuer("roomatch")
            .build()
    }

    fun verifyRefreshToken(token: String): JWTVerifier {
        return JWT.require(Algorithm.HMAC256(REFRESH_SECRET))
            .withIssuer("roomatch")
            .build()
    }

    private suspend fun updateRefreshToken(user: Any, refreshToken: String) {
        when (user) {
            is RoommateUser -> DatabaseManager.getRoommatesCollection()?.updateOne(
                RoommateUser::id eq user.id,
                setValue(RoommateUser::refreshToken, refreshToken)
            )
            is PropertyOwnerUser -> DatabaseManager.getOwnersCollection()?.updateOne(
                PropertyOwnerUser::id eq user.id,
                setValue(PropertyOwnerUser::refreshToken, refreshToken)
            )
        }
    }


}