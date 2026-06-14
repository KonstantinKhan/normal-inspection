package com.khan366kos.normal.inspection.backend.ktor.app.dto

import kotlinx.serialization.Serializable

@Serializable
data class UploadResponse(
    val accepted: List<String> = emptyList(),
    val rejected: List<RejectionDto> = emptyList(),
    val invalid: List<InvalidPartDto> = emptyList(),
)

@Serializable
data class RejectionDto(
    val reason: String,
    val detail: String,
)

@Serializable
data class InvalidPartDto(
    val reason: String,
    val detail: String,
)

@Serializable
data class MessageResponse(val message: String)

enum class InvalidPartReason {
    MISSING_FILENAME,
    INVALID_CHARACTERS,
}
