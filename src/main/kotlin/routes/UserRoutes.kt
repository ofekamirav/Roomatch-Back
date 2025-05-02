package com.routes

import com.models.*
import com.services.GeminiService
import com.services.UserService
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable


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

                if (userResponse != null) {
                    // Success! Respond with 201 Created and the UserResponse object
                    call.respond(HttpStatusCode.Created, userResponse)
                } else {
                    println("Error: Failed to insert roommate ${user.email} into database.")
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Failed to create user account."))
                }

            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
            } catch (e: io.ktor.serialization.JsonConvertException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid request format: ${e.message}"))
            }
            catch (e: Exception) {
                println("Unexpected error during roommate registration: ${e.message}")
                e.printStackTrace() // Log stack trace for debugging
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to (e.message ?: "An internal server error occurred")))
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

        //Get all roommates
        get(""){
            try {
                val roommates = UserService.getAllRoommates()
                if (roommates.isNotEmpty()) {
                    call.respond(HttpStatusCode.OK, roommates)
                } else {
                    call.respond(HttpStatusCode.NoContent, mapOf("message" to "No roommates found"))
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to (e.message ?: "Failed to fetch roommates")))
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

                if (userResponse != null) {
                    // Success! Respond with 201 Created and the UserResponse object
                    call.respond(HttpStatusCode.Created, userResponse)
                } else {
                    println("Error: Failed to insert roommate ${user.email} into database.")
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Failed to create user account."))
                }

            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
            } catch (e: io.ktor.serialization.JsonConvertException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid request format: ${e.message}"))
            }
            catch (e: Exception) {
                println("Unexpected error during roommate registration: ${e.message}")
                e.printStackTrace() // Log stack trace for debugging
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to (e.message ?: "An internal server error occurred")))
            }
        }

        // Get property owner by ID
        get("/{id}") {
            val id = call.parameters["id"]
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "ID is required"))
                return@get
            }

            val user = UserService.getOwnerById(id)
            if (user != null) {
                call.respond(HttpStatusCode.OK, user)
            } else {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to "User not found"))
            }
        }
    }

    route("/login") {
        post {
            try {
                val request = call.receive<LoginRequest>()
                val email = request.email
                val password = request.password

                val userResponse = UserService.login(email, password)
                call.respond(HttpStatusCode.OK, userResponse)

            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to (e.message ?: "Bad Request")))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to (e.message ?: "Internal server error")))
            }
        }
    }
}

