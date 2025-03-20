package com.routes

import com.models.PropertyOwnerUser
import com.models.RoommateUser
import com.services.UserService
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable



@Serializable
data class UserResponse(val token: String?, val userId: String?)

fun Routing.configureUserRoutes() {

    route("/roommates") {
        //Register a roommate user
        post("/register") {
            try {
                val user = call.receive<RoommateUser>()

                val result = UserService.registerRoommate(user)

                if (result["error"] != null) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to result["error"]))
                } else {
                    val response = UserResponse(result["token"].toString(), result["userId"].toString())
                    call.respond(HttpStatusCode.Created, response)
                }
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("error" to (e.message ?: "Internal server error"))
                )
            }
        }
    }
    route("/owners"){
        //Register a property owner user
        post("/register"){
            try {
                val user = call.receive<PropertyOwnerUser>()

                val result = UserService.registerPropertyOwner(user)

                if (result["error"] != null) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to result["error"]))
                } else {
                    val response = UserResponse(result["token"].toString(), result["userId"].toString())
                    call.respond(HttpStatusCode.Created, response)
                }
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("error" to (e.message ?: "Internal server error"))
                )
            }

        }
    }
}
