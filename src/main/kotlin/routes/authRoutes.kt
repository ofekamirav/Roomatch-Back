package com.routes

import com.utils.JWTUtils
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import com.database.DatabaseManager
import com.models.PasswordResetRequest
import com.models.ResetPassword
import com.services.UserService

fun Routing.configureAuthRoutes() {

    route("/auth") {

        post("/refresh") {
            try {
                val request = call.receive<Map<String, String>>() // receives access and refresh tokens
                val refreshToken = request["refreshToken"] ?: return@post call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Refresh Token is required")
                )

                //Verify the refresh token
                val decodedJWT = JWTUtils.verifyRefreshToken(refreshToken).verify(refreshToken)
                val email = decodedJWT.getClaim("email").asString()

                //Check if the user exists and the refresh token is valid
                val roomate = DatabaseManager.getRoomateByEmail(email)
                val owner = if (roomate != null) {
                    DatabaseManager.getOwnerByEmail(email)
                }else null
                if ((roomate?.refreshToken ?: owner?.refreshToken) != refreshToken) {
                    return@post call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid Refresh Token"))
                }

                // Generate new tokens
                val newAccessToken = JWTUtils.generateToken(email)
                val newRefreshToken = JWTUtils.generateRefreshToken(email)

                if (roomate != null){
                    JWTUtils.updateRefreshToken(roomate, newRefreshToken)
                } else if (owner != null){
                    JWTUtils.updateRefreshToken(owner, newRefreshToken)
                }


                call.respond(HttpStatusCode.OK, mapOf(
                    "token" to newAccessToken,
                    "refreshToken" to newRefreshToken,
                ))

            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("error" to (e.message ?: "Internal error"))
                )
            }
        }

        // Request Password Reset
        post("/request-password-reset") {
            try {
                val request = call.receive<PasswordResetRequest>()
                val success = UserService.requestPasswordReset(request.email, request.userType)

                if (success) {
                    call.respond(HttpStatusCode.OK, mapOf("message" to "Password reset token generated successfully."))
                } else {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Unable to generate reset token. Check email or user type."))
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to (e.message ?: "Internal server error")))
            }
        }

        // Reset Password
        post("/reset-password") {
            try {
                val request = call.receive<ResetPassword>()
                val success = UserService.resetPassword(request.token, request.newPassword, request.userType)

                if (success) {
                    call.respond(HttpStatusCode.OK, mapOf("message" to "Password reset successfully."))
                } else {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid or expired token."))
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to (e.message ?: "Internal server error")))
            }
        }
    }
}
