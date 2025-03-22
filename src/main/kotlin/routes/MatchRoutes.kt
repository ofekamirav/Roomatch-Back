package com.routes

import com.services.MatchService
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Routing.configureMatchRoutes() {
    route("/match") {

        get("/feed/{seekerId}") {
            val seekerId = call.parameters["seekerId"]
            if (seekerId == null) {
                call.respondText("Seeker Id is required", status = HttpStatusCode.BadRequest)
                return@get
            }

            val match = MatchService.getNextMatchFromFeed(seekerId)

            if (match != null) {
                call.respond(HttpStatusCode.OK,match)
            } else {
                call.respond(HttpStatusCode.NoContent,mapOf("message" to "No more matches"))
            }
        }



        post("/like/{seekerId}/{MatchId}") {
            val seekerId = call.parameters["seekerId"] ?: return@post call.respond(HttpStatusCode.BadRequest)
            val matchId = call.parameters["MatchId"] ?: return@post call.respond(HttpStatusCode.BadRequest)

            val result = MatchService.LikeMatch(seekerId, matchId)

            if (result) {
                call.respond(HttpStatusCode.OK)
            } else {
                call.respond(HttpStatusCode.BadRequest)
            }
        }








    }
}