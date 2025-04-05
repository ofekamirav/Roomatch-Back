package com

import com.routes.*
import io.ktor.server.application.*
import io.ktor.server.plugins.swagger.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        configureUserRoutes()
        configurePropertyRoutes()
        configureAuthRoutes()
        configureMatchRoutes()
        configureLikeRoutes()
    }
}
