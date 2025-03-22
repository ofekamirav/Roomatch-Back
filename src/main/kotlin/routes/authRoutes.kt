package com.routes

import com.utils.JWTUtils
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.litote.kmongo.eq
import com.database.DatabaseManager
import com.models.RoommateUser

fun Routing.configureAuthRoutes() {

    route("/auth") {

        post("/refresh") {
            try {
                val request = call.receive<Map<String, String>>()
                val refreshToken = request["refreshToken"] ?: return@post call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Refresh Token is required")
                )

                //Verify the refresh token
                val decodedJWT = JWTUtils.verifyRefreshToken(refreshToken).verify(refreshToken)
                val email = decodedJWT.getClaim("email").asString()

                //Check if the user exists and the refresh token is valid
                val user = DatabaseManager.getRoommatesCollection()?.findOne(RoommateUser::email eq email)

                if (user == null || user.refreshToken != refreshToken) {
                    return@post call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid Refresh Token"))
                }

                //Generate a new access token
                val newAccessToken = JWTUtils.generateToken(user.email)

                val response = mapOf("token" to newAccessToken)
                call.respond(HttpStatusCode.OK, response)

            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("error" to (e.message ?: "Internal server error"))
                )
            }
        }
    }
}
