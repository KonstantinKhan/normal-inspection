package com.khan366kos.normal.inspection.backend.odt.processor.dsl

import com.khan366kos.normal.inspection.backend.odt.processor.Paragraph

@DocDsl
class ParagraphBuilder {
    var text: String = ""

    fun build(): Paragraph = Paragraph(text)
}