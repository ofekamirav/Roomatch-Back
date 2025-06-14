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
        get("/owner/{ownerId}") {
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
        get("/id/{propertyId}"){
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
        //Update Property availability
        put("/{propertyId}/availability") {
            try {
                val propertyId = call.parameters["propertyId"]
                if (propertyId == null) {
                    call.respondText("Property Id is required", status = HttpStatusCode.BadRequest)
                    return@put
                }
                val isAvailable = call.receive<Boolean>()
                val result = PropertyController.updateAvailability(propertyId, isAvailable)

                if (result == true) {
                    call.respond(HttpStatusCode.OK, mapOf("message" to "Property availability updated successfully"))
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
        //Delete Property
        delete("/{propertyId}") {
            try {
                val propertyId = call.parameters["propertyId"]
                if (propertyId == null) {
                    call.respondText("Property Id is required", status = HttpStatusCode.BadRequest)
                    return@delete
                }
                val result = PropertyController.deleteProperty(propertyId)

                if (result) {
                    call.respond(HttpStatusCode.OK, mapOf("message" to "Property deleted successfully"))
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
        //Update Property
        put("/{propertyId}") {
            try {
                val propertyId = call.parameters["propertyId"]
                if (propertyId == null) {
                    call.respondText("Property Id is required", status = HttpStatusCode.BadRequest)
                    return@put
                }
                val property = call.receive<Property>()
                val result = PropertyController.updateProperty(propertyId, property)

                if (result != null) {
                    call.respond(HttpStatusCode.OK, result)
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