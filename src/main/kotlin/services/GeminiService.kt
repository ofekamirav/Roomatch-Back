package com.services

import com.models.BioRequest
import com.utils.buildPrompt
import io.github.cdimascio.dotenv.dotenv
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

private val dotenv = dotenv {
    ignoreIfMalformed = true
    ignoreIfMissing = true
}

private val json = Json {
    ignoreUnknownKeys = true
}

object GeminiService {
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json()
        }
    }

    suspend fun generateBio(user: BioRequest): String {
        val apiKey = dotenv["GEMINI_API_KEY"] ?: return "API key not set"
        val prompt = buildPrompt(user)

        val httpResponse = client.post("https://generativelanguage.googleapis.com/v1/models/gemini-1.5-flash:generateContent?key=$apiKey") {
            contentType(ContentType.Application.Json)
            setBody(
                mapOf(
                    "contents" to listOf(
                        mapOf("parts" to listOf(mapOf("text" to prompt)))
                    )
                )
            )
        }

        val responseBody = httpResponse.bodyAsText()
        val jsonObject = json.parseToJsonElement(responseBody).jsonObject

        if ("error" in jsonObject) {
            val message = jsonObject["error"]?.jsonObject?.get("message")?.jsonPrimitive?.content
            return "Gemini API Error: $message"
        }

        val response: GeminiResponse = json.decodeFromString(responseBody)

        return response.candidates
            .firstOrNull()?.content?.parts?.firstOrNull()?.text ?: "Could not generate bio"
    }
}

@Serializable
data class GeminiResponse(
    val candidates: List<GeminiCandidate>
)

@Serializable
data class GeminiCandidate(
    val content: GeminiContent
)

@Serializable
data class GeminiContent(
    val parts: List<GeminiPart>
)

@Serializable
data class GeminiPart(
    val text: String
)
