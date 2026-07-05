package com.khan366kos.normal.inspection.backend.odt.processor.common

data class Heading(
    val level: Int,
    var text: String = "",
    val paragraphs: List<Paragraph> = emptyList()
)
