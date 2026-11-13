package com.khan366kos.normal.inspection.backend.odt.processor.common

sealed interface Level {
    data object Empty : Level
    data class H1(val level: Int) : Level
}