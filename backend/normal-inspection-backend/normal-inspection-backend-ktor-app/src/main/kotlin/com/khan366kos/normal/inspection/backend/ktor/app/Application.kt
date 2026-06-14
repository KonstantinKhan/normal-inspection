package com.khan366kos.normal.inspection.backend.ktor.app

import io.ktor.server.application.Application
import io.ktor.server.netty.EngineMain

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
    configureStatusPages()
    configureSerialization()
    configureRouting()
    configureHttp()
}
