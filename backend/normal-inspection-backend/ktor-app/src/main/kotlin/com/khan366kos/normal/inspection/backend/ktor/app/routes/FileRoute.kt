package com.khan366kos.normal.inspection.backend.ktor.app.routes

import com.example.doc.service.DocService
import com.khan366kos.normal.inspection.backend.common.FilenameValidation
import com.khan366kos.normal.inspection.backend.common.IFileReceiver
import com.khan366kos.normal.inspection.backend.common.InvalidPartReason
import com.khan366kos.normal.inspection.backend.common.domain.ReceptionResult
import com.khan366kos.normal.inspection.backend.transport.InvalidPartDto
import com.khan366kos.normal.inspection.backend.transport.MessageResponseDto
import com.khan366kos.normal.inspection.backend.transport.RejectionDto
import com.khan366kos.normal.inspection.backend.transport.UploadResponseDto
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.server.request.receiveMultipart
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.utils.io.jvm.javaio.toInputStream
import java.io.File

fun Route.files(fileReceiver: IFileReceiver) = route("files") {
    val docService = DocService()

    post("/upload") {
        val multipartData = call.receiveMultipart()
        val results = mutableListOf<ReceptionResult>()
        val invalidParts = mutableListOf<InvalidPartDto>()

        multipartData.forEachPart { part ->
            when (part) {
                is PartData.FormItem -> Unit
                is PartData.FileItem ->
                    when (val filenameValidation = fileReceiver.validateFilename(part.originalFileName)) {
                        is FilenameValidation.Valid ->
                            part.provider().toInputStream()
                                .use { stream ->
                                    results.add(fileReceiver.receiveFile(stream, filenameValidation.name))
                                }

                        is FilenameValidation.Invalid -> {
                            invalidParts.add(
                                InvalidPartDto(
                                    filenameValidation.reason.name,
                                    invalidDetail(filenameValidation.reason)
                                )
                            )
                        }
                    }

                else -> Unit
            }
            part.release()
        }

        if (results.isEmpty() && invalidParts.isEmpty()) {
            call.respond(HttpStatusCode.BadRequest, MessageResponseDto("No file provided"))
            return@post
        }

        val accepted = results.filterIsInstance<ReceptionResult.Accepted>().map { it.name }
        val rejections = results.filterIsInstance<ReceptionResult.Rejected>()
        val rejected = rejections.map { RejectionDto(it.reason.name, it.detail) }

        val response = UploadResponseDto(accepted, rejected, invalidParts)
        val status = when {
            accepted.isNotEmpty() -> HttpStatusCode.OK
            rejections.isNotEmpty() -> HttpStatusCode.UnprocessableEntity
            else -> HttpStatusCode.BadRequest
        }

        if (accepted.isNotEmpty()) {
            accepted.forEach { path ->
                docService.processor(File(path))
            }
        }

        call.respond(status, response)
    }

    get("/") {
        call.respond(
            HttpStatusCode.OK,
        )
    }
}

private fun invalidDetail(reason: InvalidPartReason): String = when (reason) {
    InvalidPartReason.MISSING_FILENAME -> "filename was null or blank"
    InvalidPartReason.INVALID_CHARACTERS -> "filename contains control characters"
}
