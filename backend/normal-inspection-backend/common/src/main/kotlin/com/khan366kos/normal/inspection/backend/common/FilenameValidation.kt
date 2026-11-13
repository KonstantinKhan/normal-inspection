package com.khan366kos.normal.inspection.backend.common

sealed interface FilenameValidation {
    data class Valid(val name: String) : FilenameValidation
    data class Invalid(val reason: InvalidPartReason) : FilenameValidation
}