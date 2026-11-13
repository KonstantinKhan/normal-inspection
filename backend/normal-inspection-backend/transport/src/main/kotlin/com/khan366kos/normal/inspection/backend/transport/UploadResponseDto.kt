package com.khan366kos.normal.inspection.backend.transport

import kotlinx.serialization.Serializable

@Serializable
data class UploadResponseDto(
    val accepted: List<String> = emptyList(),
    val rejected: List<RejectionDto> = emptyList(),
    val invalid: List<InvalidPartDto> = emptyList(),
)
