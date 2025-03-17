package com.utils

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import java.util.*

object JWTUtils {

    private const val SECRET = "SuperSecretKey"
    private const val EXPIRATION_TIME = 24 * 60 * 60 * 1000 // 24 Hours

    fun generateToken(email: String): String {
        return JWT.create()
            .withSubject("Authentication")
            .withIssuer("roomatch")
            .withClaim("email", email)
            .withExpiresAt(Date(System.currentTimeMillis() + EXPIRATION_TIME))
            .sign(Algorithm.HMAC256(SECRET))
    }

}