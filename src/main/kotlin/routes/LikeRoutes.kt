package com.routes

import com.services.LikeController
import com.models.Match
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Routing.configureLikeRoutes() {

    route("/likes") {

        // POST /likes - Save a new Match (like) with custom dislike logic
        post {
            try {
                val match = call.receive<Match>()
                val result = LikeController.saveMatch(match)

                result.onSuccess {
                    call.respond(HttpStatusCode.Created, it)
                }.onFailure {
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to it.message))
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to (e.message ?: "Internal server error")))
            }
        }

        // GET /likes/{seekerId} - Get all Matches for a seeker
        get("/{seekerId}") {
            val seekerId = call.parameters["seekerId"]
            if (seekerId.isNullOrBlank()) {
                call.respond(HttpStatusCode.BadRequest, "SeekerId is required")
                return@get
            }

            try {
                val matches = LikeController.getMatchesBySeekerId(seekerId)
                call.respond(HttpStatusCode.OK, matches)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to (e.message ?: "Internal server error")))
            }
        }
    }
}
