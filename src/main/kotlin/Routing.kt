package com

import com.routes.*
import com.utils.configureMiddleware
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.swagger.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {

    routing {
        configureUserRoutes() // login, register..
        configureAuthRoutes() // generate tokens if expired

        authenticate("auth-jwt") {
            configurePropertyRoutes()
            configureMatchRoutes()
            configureLikeRoutes()
            configureDislikeRoutes()
        }

    }
}
