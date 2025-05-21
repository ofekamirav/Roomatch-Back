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

        // POST /likes - Save a new Match (like)
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

        // POST /likes/property - Like a property and dislike roommates
        post("/property") {
            try {
                val match = call.receive<Match>()
                val dislikeResult = LikeController.likeProperty(match)
                if (dislikeResult) {
                    call.respond(HttpStatusCode.OK)
                } else {
                    call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Property liked, no dislikes updated"))
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to (e.message ?: "Internal server error")))
            }
        }

        // POST /likes/roommates - Like a roommate and dislike properties
        post("/roommates") {
            try {
                val match = call.receive<Match>()
                val disLikeResult = LikeController.likeRoommate(match)
                if (disLikeResult) {
                    call.respond(HttpStatusCode.OK)
                } else {
                    call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Roommate liked, no dislikes updated"))
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to (e.message ?: "Internal server error")))
            }
        }
    }
}
