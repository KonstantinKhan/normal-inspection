package com.khan366kos.normal.inspection.backend.odt.processor

data class Chapter(
    val level: Int?,
    val title: String,
    var paragraphs: List<Paragraph>,
)
