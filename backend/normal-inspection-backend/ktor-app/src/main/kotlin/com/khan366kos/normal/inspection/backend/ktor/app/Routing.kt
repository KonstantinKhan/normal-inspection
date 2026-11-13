package com.khan366kos.normal.inspection.backend.ktor.app

import com.khan366kos.normal.inspection.backend.ktor.app.routes.files
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting(config: AppConfig) {
    routing {
        get("/") {
            call.respondText("Hello, World!")
        }
        get("/json/kotlinx-serialization") {
            call.respond(mapOf("hello" to "world"))
        }
        files(config.fileReceiver)
    }
}