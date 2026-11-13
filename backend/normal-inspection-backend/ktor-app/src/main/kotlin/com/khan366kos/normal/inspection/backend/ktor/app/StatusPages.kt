package com.khan366kos.normal.inspection.backend.ktor.app

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.UnsupportedMediaTypeException
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*

fun Application.configureStatusPages() {
    install(StatusPages) {
        exception<UnsupportedMediaTypeException> { call, _ ->
            call.respond(
                HttpStatusCode.UnsupportedMediaType,
                mapOf("message" to "Content-Type multipart/form-data is required")
            )
        }
        exception<Throwable> { call, cause ->
            call.respondText(text = "500: $cause", status = HttpStatusCode.InternalServerError)
        }
    }
}
