package com.routes

import com.models.Match
import com.services.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
fun Route.configureDislikeRoutes() {
    post("/dislike/swipe-left") {
        val match = call.receive<Match>()
        val success = DislikeController.swipeLeft(match)

        if (success) {
            call.respond(HttpStatusCode.Created, mapOf("message" to "Match disliked successfully."))
        } else {
            call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Failed to save dislike."))
        }
    }
}
