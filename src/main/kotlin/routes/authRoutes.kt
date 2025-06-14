package com.routes

import com.utils.JWTUtils
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import com.database.DatabaseManager
import com.models.PasswordResetRequest
import com.models.RequestRefresh
import com.models.ResetPassword
import com.services.EmailService
import com.services.UserService
import io.ktor.server.application.*

fun Routing.configureAuthRoutes() {

    route("/auth") {

        post("/refresh") {
            try {
                val request = call.receive<RequestRefresh>()
                val refreshToken = request.refreshToken

                // Verify the refresh token
                val decodedJWT = JWTUtils.verifyRefreshToken(refreshToken).verify(refreshToken)
                val email = decodedJWT.getClaim("email").asString()

                // Check if the user exists and the refresh token is valid
                val roommate = DatabaseManager.getRoomateByEmail(email)
                val owner = if (roommate != null) null else DatabaseManager.getOwnerByEmail(email)

                if ((roommate?.refreshToken ?: owner?.refreshToken) != refreshToken) {
                    return@post call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid Refresh Token"))
                }

                // Generate new tokens
                val newAccessToken = JWTUtils.generateToken(email)
                val newRefreshToken = JWTUtils.generateRefreshToken(email)

                if (roommate != null) {
                    JWTUtils.updateRefreshToken(roommate, newRefreshToken)
                } else if (owner != null) {
                    JWTUtils.updateRefreshToken(owner, newRefreshToken)
                }

                call.respond(HttpStatusCode.OK, RequestRefresh(
                    accessToken = newAccessToken,
                    refreshToken = newRefreshToken
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
                val email = request.email
                val userType = request.userType

                val resetResult = UserService.requestPasswordReset(email, userType)

                when (resetResult.status) {
                    "SUCCESS" -> {
                        val otpCode = resetResult.otpCode
                        if (otpCode != null) {
                            val emailSent = EmailService.sendPasswordResetEmail(email, otpCode)
                            if (emailSent) {
                                call.respond(HttpStatusCode.OK, mapOf("message" to "Password reset code sent to your email."))
                            } else {
                                application.log.error("Failed to send OTP email for $email. OTP generated but not sent.")
                                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Failed to send password reset email. Please try again later."))
                            }
                        } else {
                            application.log.error("Internal server error: OTP not generated for $email.")
                            call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Internal server error: OTP not generated."))
                        }
                    }
                    "USER_NOT_FOUND" -> {
                        call.respond(HttpStatusCode.NotFound, mapOf("error" to "User with the provided email and type not found."))
                    }
                    "INVALID_USER_TYPE" -> {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid user type provided."))
                    }
                    "TOKEN_SET_FAILED" -> {
                        call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Failed to set password reset code in database. Please try again later."))
                    }
                    else -> {
                        application.log.error("An unexpected status '${resetResult.status}' occurred during password reset request for email: $email.")
                        call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "An unexpected error occurred during password reset request."))
                    }
                }
            } catch (e: ContentTransformationException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid request body: ${e.message}"))
            }
            catch (e: Exception) {
                application.log.error("Error in /request-password-reset: ${e.localizedMessage}", e)
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to (e.message ?: "An internal server error occurred.")))
            }
        }

        // Reset Password
        post("/reset-password") {
            try {
                val request = call.receive<ResetPassword>()
                val success = UserService.resetPassword(request.email, request.otpCode, request.newPassword, request.userType)

                if (success) {
                    call.respond(HttpStatusCode.OK, mapOf("message" to "Password reset successfully."))
                } else {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid or expired verification code (OTP) or incorrect email."))
                }
            } catch (e: Exception) {
                application.log.error("Error in /reset-password: ${e.localizedMessage}", e)
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to (e.message ?: "Internal server error")))
            }
        }
    }
}