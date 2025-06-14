package com

import com.config.AppConfig
import com.services.EmailService
import com.utils.configureMiddleware
import io.ktor.server.application.*
import io.github.cdimascio.dotenv.dotenv
import io.ktor.server.application.ApplicationStarted
import io.ktor.server.application.ApplicationStopping


fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}


fun Application.module() {
    try {
        println("Gemini API Key Loaded: ${AppConfig.geminiApiKey.take(4)}***")
        val config = environment.config
        println("▶▶ Host: ${config.propertyOrNull("ktor.deployment.host")?.getString()}")
        println("▶▶ Port: ${config.propertyOrNull("ktor.deployment.port")?.getString()}")

        environment.monitor.subscribe(ApplicationStarted) {
            println("🚀 Application started.")
        }
        if (AppConfig.isTestEnv) {
            println("Running in TEST environment")
        } else {
            println("Running in PROD environment")
        }

        environment.monitor.subscribe(ApplicationStopping) {
            println("🛑 Application is stopping.")
        }

        configureSerialization()
        configureMiddleware()
        configureHTTP()
        configureRouting()
    } catch (e: Exception) {
        println("❌ Exception in Application.module: ${e.message}")
        e.printStackTrace()
    }
}
