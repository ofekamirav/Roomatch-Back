package com

import io.ktor.client.request.*
import io.ktor.client.statement.bodyAsText
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlin.test.Test
import kotlin.test.assertEquals

class ApplicationTest {

    @Test
    fun testTestRoute() = testApplication {
        application {
            module()
        }
        client.get("/api/test").apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals("It works!", bodyAsText())
        }
    }
}

