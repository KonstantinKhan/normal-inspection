package com.khan366kos.normal.inspection.backend.odt.processor.dsl

import com.khan366kos.normal.inspection.backend.odt.processor.Chapter
import com.khan366kos.normal.inspection.backend.odt.processor.Paragraph

@DocDsl
class ChapterBuilder {
    var level: Int = -1
    var title: String = ""
    var paragraphs: MutableList<Paragraph> = mutableListOf()

    fun paragraphs(block: ParagraphBlockContainer.() -> Unit) {
        val paragraphBlockContainer = ParagraphBlockContainer().apply(block)
        paragraphs.addAll(paragraphBlockContainer.content)
    }

    fun build(): Chapter = Chapter(
        level = level,
        title = title,
        paragraphs = paragraphs
    )
}

@DocDsl
class ParagraphBlockContainer {
    val content: MutableList<Paragraph> = mutableListOf()

    inline fun paragraph(block: ParagraphBuilder.() -> Unit) {
        ParagraphBuilder().apply(block).build().also { content.add(it) }
    }
}