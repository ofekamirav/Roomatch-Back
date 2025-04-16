package com.utils

import com.utils.JWTUtils
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*

fun Application.configureMiddleware() {
    install(Authentication) {
        jwt("auth-jwt") {
            verifier(JWTUtils.verifyToken("")) // this uses your JWTUtils verifier
            validate { credential ->
                val email = credential.payload.getClaim("email").asString()
                if (!email.isNullOrBlank()) {
                    JWTPrincipal(credential.payload)
                } else {
                    null // reject
                }
            }
            challenge { _, _ ->  // authentication fails
                val errorResponse = if (call.request.headers["Authorization"].isNullOrBlank()) {
                    mapOf("error" to "Token Missing")
                } else {
                    mapOf("error" to "Token expired")
                }
                call.respond(HttpStatusCode.Unauthorized, errorResponse)
            }
        }
    }
}