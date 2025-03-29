package com.routes

import com.services.MatchService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.configureMatchRoutes() {
    route("/match") {
        get("/{seekerId}") {
            val seekerId = call.parameters["seekerId"]

            if (seekerId == null) {
                call.respondText("Seeker Id is required", status = HttpStatusCode.BadRequest)
                return@get
            }

            val match = MatchService.getNextMatchForSwipe(seekerId)

            if (match != null) {
                call.respond(HttpStatusCode.OK, match)
            } else {
                call.respond(HttpStatusCode.NoContent, mapOf("message" to "No more matches"))
            }
        }
    }
}