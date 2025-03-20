package com

import com.routes.configurePropertyRoutes
import com.routes.configureUserRoutes
import io.ktor.server.application.*
import io.ktor.server.plugins.swagger.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        configureUserRoutes()
        configurePropertyRoutes()
    }
}
