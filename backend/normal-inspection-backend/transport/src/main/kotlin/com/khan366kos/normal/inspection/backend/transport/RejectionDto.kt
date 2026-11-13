package com.khan366kos.normal.inspection.backend.transport

import kotlinx.serialization.Serializable

@Serializable
data class RejectionDto(
    val reason: String,
    val detail: String,
)