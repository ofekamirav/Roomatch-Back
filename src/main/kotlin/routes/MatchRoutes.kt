package com.routes

import com.services.MatchService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.litote.kmongo.deleteMany
import org.slf4j.LoggerFactory

fun Route.configureMatchRoutes() {
    val logger = LoggerFactory.getLogger(MatchService::class.java)
    route("/match") {
        get("/{seekerId}") {
            val seekerId = call.parameters["seekerId"]
            val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 1

            if (seekerId == null) {
                logger.error("Seeker Id is required")
                call.respond(HttpStatusCode.BadRequest, "Seeker Id is required")
                return@get
            }

            val matches = MatchService.getNextMatchesForSwipe(seekerId, limit)

            if (matches.isNotEmpty()) {
                logger.info("Matches found for seekerId: $seekerId, limit: $limit")
                call.respond(HttpStatusCode.OK, matches)
            } else {
                logger.info("No more matches for seekerId: $seekerId")
                call.respond(HttpStatusCode.NoContent, mapOf("message" to "No more matches"))
            }
        }

        delete("/{matchId}"){
            val matchId = call.parameters["matchId"]
            if (matchId == null) {
                call.respond(HttpStatusCode.BadRequest, "Match Id is required")
                return@delete
            }

            val result = MatchService.deleteMatch(matchId)

            if (result) {
                call.respond(HttpStatusCode.OK, mapOf("message" to "Match deleted successfully"))
            } else {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Failed to delete match"))
            }
        }
    }
}