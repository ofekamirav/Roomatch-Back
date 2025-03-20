package com.routes

import com.models.Property
import com.services.PropertyController
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Routing.configurePropertyRoutes() {

    route("/properties") {
        //Add property to specific owner
        post("/upload/{ownerId}") {
            try {
                val ownerId = call.parameters["ownerId"]
                if (ownerId == null) {
                    call.respondText("Owner Id is required", status = HttpStatusCode.BadRequest)
                    return@post
                }
                val property = call.receive<Property>()
                val result = PropertyController.uploadProperty(ownerId, property)

                if (result["error"] != null) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to result["error"]))
                } else {
                    call.respond(HttpStatusCode.Created, result)
                }
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("error" to (e.message ?: "Internal server error"))
                )
            }
        }

        //Get all properties of specific owner
        get("/{ownerId}") {
            try {
                val ownerId = call.parameters["ownerId"]
                if (ownerId == null) {
                    call.respondText("Owner Id is required", status = HttpStatusCode.BadRequest)
                    return@get
                }
                val properties = PropertyController.getPropertiesByOwnerId(ownerId)

                call.respond(HttpStatusCode.OK, properties)
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("error" to (e.message ?: "Internal server error"))
                )
            }
        }
    }

}