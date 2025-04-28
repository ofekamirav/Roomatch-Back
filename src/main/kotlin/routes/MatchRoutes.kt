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
            val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 1

            if (seekerId == null) {
                call.respond(HttpStatusCode.BadRequest, "Seeker Id is required")
                return@get
            }

            val matches = MatchService.getNextMatchesForSwipe(seekerId, limit)

            if (matches.isNotEmpty()) {
                call.respond(HttpStatusCode.OK, matches)
            } else {
                call.respond(HttpStatusCode.NoContent, mapOf("message" to "No more matches"))
            }
        }

    }
}