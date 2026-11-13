package com.khan366kos.normal.inspection.backend.odt.processor.common

import javax.xml.stream.XMLStreamReader

class OdtProcessor {

    fun parse(reader: XMLStreamReader): List<StyleElement.Heading> {
        val headings = mutableListOf<StyleElement.Heading>()
        var currentHeading: StyleElement.Heading = StyleElement.Heading.Empty
        var currentElement: StyleElement.Paragraph? = null
        val textBuffer = StringBuilder()

        fun onStartElement() {
            textBuffer.clear()
            when (val element = StyleElement.from(reader)) {
                is StyleElement.Paragraph -> {
                    currentElement = element
                }

                is StyleElement.Heading.WithoutContent ->
                    currentHeading = element

                else -> {}
            }
        }

        fun onCharacters() {
            if (!reader.isWhiteSpace) {
                textBuffer.append(reader.text)
            }
        }

        fun onEndElement() {
            val heading = currentHeading
            when (reader.namespaceURI) {
                OdfNamespaces.TEXT_NS
                    if (reader.localName == OdfElement.HEADING.localName
                            && heading is StyleElement.Heading.WithoutContent) -> {
                    currentHeading = with(heading) {
                        StyleElement.Heading.WithContent(
                            level = level,
                            localName = localName,
                            namespaceURI = namespaceURI,
                            text = textBuffer.toString().trim(),
                            paragraphs = paragraphs
                        )
                    }
                    headings.add(currentHeading)
                }

                OdfNamespaces.TEXT_NS
                    if (reader.localName == OdfElement.PARAGRAPH.localName
                            && heading is StyleElement.Heading.WithContent) -> {
                    currentElement = currentElement?.copy(text = textBuffer.toString().trim())
                    heading.paragraphs.add(currentElement!!)
                }

                else -> {}
            }
        }

        while (reader.hasNext()) {
            when (reader.next()) {
                XMLStreamReader.START_ELEMENT -> onStartElement()
                XMLStreamReader.CHARACTERS -> onCharacters()
                XMLStreamReader.END_ELEMENT -> onEndElement()
            }
        }
        return headings
    }
}
