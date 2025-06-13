package com.config

import io.github.cdimascio.dotenv.dotenv

object AppConfig {
    val isTestEnv: Boolean = System.getenv("KTOR_TEST") == "true"

    private val dotenv = dotenv {
        ignoreIfMissing = true
        directory = "./"
        if (isTestEnv) {
            filename = "...env.test"
        }
    }

    private fun getEnv(key: String): String {
        return System.getenv(key) ?: dotenv[key] ?: throw IllegalArgumentException("Environment variable '$key' is not set")
    }

    val mongoConnectionString: String = getEnv("MONGO_CONNECTION_STRING")
    val googleWebClientId: String = getEnv("GOOGLE_WEB_CLIENT_ID")
    val googleAndroidClientId: String = getEnv("GOOGLE_ANDROID_CLIENT_ID")
    val geminiApiKey: String = getEnv("GEMINI_API_KEY")
    val mongoDatabase: String = getEnv("MONGODB_DB")

}