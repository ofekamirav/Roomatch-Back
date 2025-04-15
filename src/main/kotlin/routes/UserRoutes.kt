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
                val result = UserService.registerRoommate(user)
                if (result["error"] != null) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to result["error"]))
                } else {
                    val response = UserResponse(result["token"].toString(),result["refreshToken"].toString() ,result["userId"].toString(), result["userType"].toString())
                    call.respond(HttpStatusCode.Created, response)
                }
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
                val result = UserService.registerPropertyOwner(user)
                if (result["error"] != null) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to result["error"]))
                } else {
                    val response = UserResponse(result["token"].toString(),result["refreshToken"].toString() ,result["userId"].toString(), result["userType"].toString())
                    call.respond(HttpStatusCode.Created, response)
                }
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
}