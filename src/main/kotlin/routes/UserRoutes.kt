package com.routes

import com.models.PropertyOwnerUser
import com.models.RoommateUser
import com.models.BioRequest
import com.models.BioResponse
import com.services.GeminiService
import com.services.UserService
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

@Serializable
data class UserResponse(val token: String?, val refreshToken: String?, val userId: String?, val userType: String?)

fun Routing.configureUserRoutes() {

    get("/api/test") {
        call.respondText("It works!")
    }
    route("/roommates") {
        // Register a roommate user
        post("/register") {
            try {
                val user = call.receive<RoommateUser>()
                println("Received user from front side: $user")

                val userResponse = UserService.registerRoommate(user)

                call.respond(userResponse)

            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to (e.message ?: "Internal server error")))
            }
        }


        // Get roommate by ID
        get("/{id}") {
            val id = call.parameters["id"]
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "ID is required"))
                return@get
            }

            val user = UserService.getRoommateById(id)
            if (user != null) {
                call.respond(HttpStatusCode.OK, user)
            } else {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to "User not found"))
            }
        }

        // Generate bio with Gemini
        post("/generate-bio") {
            try {
                val request = call.receive<BioRequest>()
                val generatedBio = GeminiService.generateBio(request)
                call.respond(BioResponse(generatedBio))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to (e.message ?: "Failed to generate bio")))
            }
        }
    }

    route("/owners") {
        // Register a property owner user
        post("/register") {
            try {
                val user = call.receive<PropertyOwnerUser>()
                println("Received user from front side: $user")

                val userResponse = UserService.registerPropertyOwner(user)

                call.respond(userResponse)

            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to (e.message ?: "Internal server error")))
            }
        }
    }

    route("/login"){
        post {
            try {
                val request = call.receive<Map<String, String>>()
                val email = request["email"] ?: return@post call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Email is required"))
                val password = request["password"] ?: return@post call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Password is required"))

                val result = UserService.login(email, password)
                if (result["error"] != null) {
                    call.respond(HttpStatusCode.Unauthorized, mapOf("error" to result["error"]))
                } else {
                    val response = UserResponse(result["token"].toString(),result["refreshToken"].toString() ,result["userId"].toString(), result["userType"].toString())
                    call.respond(HttpStatusCode.OK, response)
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to (e.message ?: "Internal server error")))
            }
        }
    }

    route("/auth") {

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

@Serializable
data class PasswordResetRequest(val email: String, val userType: String)

@Serializable
data class ResetPassword(val token: String, val newPassword: String, val userType: String)
