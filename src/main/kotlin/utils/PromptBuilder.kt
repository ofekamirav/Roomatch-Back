package com.utils

import com.models.BioRequest

fun buildPrompt(user: BioRequest): String {
    return """
        Write a short, friendly personal bio (2–3 sentences) for a roommate matching app.
        Name: ${user.fullName}
        Occupation: ${user.work}
        Hobbies: ${user.hobbies.joinToString()}
        Attributes: ${user.attributes.joinToString()}
    """.trimIndent()
}
