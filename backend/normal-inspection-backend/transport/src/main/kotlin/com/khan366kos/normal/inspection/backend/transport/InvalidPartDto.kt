package com.khan366kos.normal.inspection.backend.transport

import kotlinx.serialization.Serializable

@Serializable
data class InvalidPartDto(
    val reason: String,
    val detail: String,
)