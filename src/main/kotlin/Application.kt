package com

import io.ktor.server.application.*
import io.github.cdimascio.dotenv.dotenv


fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}


fun Application.module() {
    val dotenv = dotenv()
    println("Gemini API Key Loaded: ${dotenv["GEMINI_API_KEY"]?.take(4)}***")
    configureSerialization()
    configureHTTP()
    configureRouting()
}
