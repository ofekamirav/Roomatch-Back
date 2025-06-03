package com

import com.database.DatabaseManager
import kotlinx.coroutines.runBlocking

object TestDatabaseCleaner {

    fun cleanAllCollections() = runBlocking {
        if (System.getenv("KTOR_TEST") == "true") {
            DatabaseManager.getRoommatesCollection()?.deleteMany()
            DatabaseManager.getOwnersCollection()?.deleteMany()
            DatabaseManager.getPropertiesCollection()?.deleteMany()
            println("✅ Test database cleaned.")
        }
    }
}

