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
        post("/{ownerId}") {
            try {
                val ownerId = call.parameters["ownerId"]
                if (ownerId == null) {
                    call.respondText("Owner Id is required", status = HttpStatusCode.BadRequest)
                    return@post
                }
                val property = call.receive<Property>()
                val result = PropertyController.uploadProperty(ownerId, property)

                if (result != null) {
                    // Success! Respond with 201 Created and the UserResponse object
                    call.respond(HttpStatusCode.Created, result)
                } else {
                    println("Error: Failed to insert property into database.")
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Failed to create property."))
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

        //Get Property by ID
        get("/{propertyId}"){
            try {
                val propertyId = call.parameters["propertyId"]
                if (propertyId == null) {
                    call.respondText("Property Id is required", status = HttpStatusCode.BadRequest)
                    return@get
                }
                val property = PropertyController.getPropertyById(propertyId)

                if (property != null) {
                    call.respond(HttpStatusCode.OK, property)
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "Property not found"))
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