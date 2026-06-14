package com.khan366kos.normal.inspection.backend.ktor.app

import com.khan366kos.normal.inspection.backend.common.domain.ReceptionResult
import com.khan366kos.normal.inspection.backend.file.receiver.FileReceiver
import com.khan366kos.normal.inspection.backend.ktor.app.dto.InvalidPartDto
import com.khan366kos.normal.inspection.backend.ktor.app.dto.InvalidPartReason
import com.khan366kos.normal.inspection.backend.ktor.app.dto.MessageResponse
import com.khan366kos.normal.inspection.backend.ktor.app.dto.RejectionDto
import com.khan366kos.normal.inspection.backend.ktor.app.dto.UploadResponse
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.server.request.receiveMultipart
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.utils.io.jvm.javaio.toInputStream
import org.slf4j.LoggerFactory

fun Routing.files() = route("files") {
    val receiver = FileReceiver()
    val logger = LoggerFactory.getLogger("FileRouter")

    post("/upload") {
        val multipartData = call.receiveMultipart()
        val results = mutableListOf<ReceptionResult>()
        val invalidParts = mutableListOf<InvalidPartDto>()

        multipartData.forEachPart { part ->
            when (part) {
                is PartData.FormItem ->
                    logger.debug("file description: {}", part.value)

                is PartData.FileItem -> when (val v = validateFilename(part.originalFileName)) {
                    is FilenameValidation.Valid ->
                        part.provider().toInputStream().use { stream ->
                            results.add(receiver.receiveFile(stream, v.name))
                        }

                    is FilenameValidation.Invalid -> {
                        logger.info("Invalid file part: reason={}", v.reason)
                        invalidParts.add(InvalidPartDto(v.reason.name, invalidDetail(v.reason)))
                    }
                }

                else -> Unit
            }
            part.release()
        }

        if (results.isEmpty() && invalidParts.isEmpty()) {
            call.respond(HttpStatusCode.BadRequest, MessageResponse("No file provided"))
            return@post
        }

        val accepted = results.filterIsInstance<ReceptionResult.Accepted>().map { it.name }
        val rejections = results.filterIsInstance<ReceptionResult.Rejected>()
        rejections.forEach { r ->
            logger.info("File rejected: reason={}, detail={}", r.reason, r.detail)
        }
        val rejected = rejections.map { RejectionDto(it.reason.name, it.detail) }

        val response = UploadResponse(accepted, rejected, invalidParts)
        val status = when {
            accepted.isNotEmpty() -> HttpStatusCode.OK
            rejections.isNotEmpty() -> HttpStatusCode.UnprocessableEntity
            else -> HttpStatusCode.BadRequest
        }
        call.respond(status, response)
    }
}

sealed interface FilenameValidation {
    data class Valid(val name: String) : FilenameValidation
    data class Invalid(val reason: InvalidPartReason) : FilenameValidation
}

private fun validateFilename(raw: String?): FilenameValidation = when {
    raw.isNullOrBlank() -> FilenameValidation.Invalid(InvalidPartReason.MISSING_FILENAME)
    raw.any { it.isISOControl() } -> FilenameValidation.Invalid(InvalidPartReason.INVALID_CHARACTERS)
    else -> FilenameValidation.Valid(raw)
}

private fun invalidDetail(reason: InvalidPartReason): String = when (reason) {
    InvalidPartReason.MISSING_FILENAME -> "filename was null or blank"
    InvalidPartReason.INVALID_CHARACTERS -> "filename contains control characters"
}
