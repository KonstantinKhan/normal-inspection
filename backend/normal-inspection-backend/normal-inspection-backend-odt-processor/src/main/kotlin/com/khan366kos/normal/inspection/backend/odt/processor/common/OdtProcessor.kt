package com.khan366kos.normal.inspection.backend.odt.processor.common

import javax.xml.stream.XMLStreamReader

class OdtProcessor {
    private var headings = mutableListOf<Heading>()
    private var currentHeading: Heading? = null
    private var textBuffer = StringBuilder()

    fun parse(reader: XMLStreamReader): List<Heading> {
        while (reader.hasNext()) {
            when (reader.next()) {
                XMLStreamReader.START_ELEMENT -> onStartElement(reader)
                XMLStreamReader.CHARACTERS -> onCharacters(reader)
                XMLStreamReader.END_ELEMENT -> onEndElement(reader)
            }
        }
        return headings
    }

    private fun onStartElement(reader: XMLStreamReader) {
        textBuffer.clear()
        when (reader.localName) {
            "h" -> {
                currentHeading =
                    Heading(
                    level = reader.getAttributeValue(null, "outline-level")?.toIntOrNull() ?: 1,
                    text = ""
                )
                headings.add(currentHeading!!)
            }
        }
    }

    private fun onCharacters(reader: XMLStreamReader) {
        if (!reader.isWhiteSpace) {
            textBuffer.append(reader.text)
        }
    }

    private fun onEndElement(reader: XMLStreamReader) {
        when (reader.localName) {
            "h" -> {
                currentHeading?.text = textBuffer.toString().trim()
            }
            "p" -> {
                val text = textBuffer.toString().trim()
                if (text.isNotEmpty()) {
//                    currentHeading?.paragraphs?.add(Paragraph(text))
                }
            }
        }
    }
}