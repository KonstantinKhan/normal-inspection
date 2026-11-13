package com.khan366kos.normal.inspection.backend.odt.processor.common

import javax.xml.stream.XMLStreamReader

sealed class StyleElement {
    abstract val localName: String
    abstract val namespaceURI: String

    data object Unknown : StyleElement() {
        override val localName = ""
        override val namespaceURI = ""
    }

    data class Paragraph(
        override val localName: String,
        override val namespaceURI: String,
        val text: String = "",
    ) : StyleElement()

    sealed class Heading : StyleElement() {
        abstract val level: Level

        data object Empty : Heading() {
            override val level: Level = Level.Empty
            override val localName = ""
            override val namespaceURI = ""
        }

        data class WithoutContent(
            override val level: Level,
            override val localName: String,
            override val namespaceURI: String,
            val text: String = "",
            val paragraphs: MutableList<Paragraph> = mutableListOf()
        ) : Heading()

        data class WithContent(
            override val level: Level,
            override val localName: String,
            override val namespaceURI: String,
            val text: String,
            val paragraphs: MutableList<Paragraph>
        ) : Heading()
    }

    companion object {
        fun from(xmlReader: XMLStreamReader): StyleElement {
            val localName = xmlReader.localName
            return when (val namespaceURI = xmlReader.namespaceURI) {
                OdfNamespaces.TEXT_NS -> when (localName) {
                    OdfElement.HEADING.localName -> {
                        Heading.WithoutContent(
                            Level.H1(
                                xmlReader.getAttributeValue(
                                    namespaceURI,
                                    XmlLocalName.OUTLINE_LEVEL.localName
                                )
                                    .toInt()
                            ),
                            localName,
                            namespaceURI
                        )
                    }

                    OdfElement.PARAGRAPH.localName -> {
                        StyleElement.Paragraph(
                            localName,
                            namespaceURI
                        )
                    }

                    else -> Unknown
                }

                else -> Unknown
            }
        }
    }
}