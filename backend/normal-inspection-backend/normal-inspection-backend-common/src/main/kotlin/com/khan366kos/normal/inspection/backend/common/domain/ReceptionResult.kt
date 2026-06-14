package com.khan366kos.normal.inspection.backend.common.domain

sealed class ReceptionResult {
    data class Accepted(val name: String, val size: Long) : ReceptionResult()
    data class Rejected(val reason: RejectionReason, val detail: String) : ReceptionResult()
}

enum class RejectionReason {
    FORBIDDEN_EXTENSION,
    TOO_LARGE,
    STORAGE_FAILURE
}
