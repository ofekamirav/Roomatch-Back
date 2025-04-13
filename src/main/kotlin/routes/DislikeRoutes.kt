package com.routes
import com.services.DislikeController
import com.models.Match
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Routing.configureDislikeRoutes() {

    route("/dislikes") {

        post {
            try {
                val match = call.receive<Match>()
                val result = DislikeController.dislikeMatch(match)

                result.onSuccess {
                    call.respond(HttpStatusCode.Created, it)
                }.onFailure {
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to it.message))
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to (e.message ?: "Internal server error")))
            }
        }

        get("/{seekerId}") {
            val seekerId = call.parameters["seekerId"]
            if (seekerId.isNullOrBlank()) {
                call.respond(HttpStatusCode.BadRequest, "SeekerId is required")
                return@get
            }

            try {
                val dislikes = DislikeController.getDislikedMatches(seekerId)
                call.respond(HttpStatusCode.OK, dislikes)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to (e.message ?: "Internal server error")))
            }
        }
    }
}
