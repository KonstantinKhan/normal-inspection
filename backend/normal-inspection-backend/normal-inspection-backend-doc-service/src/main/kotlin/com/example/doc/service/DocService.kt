package com.example.doc.service

import org.odftoolkit.odfdom.doc.OdfTextDocument
import org.odftoolkit.odfdom.dom.OdfDocumentNamespace
import org.odftoolkit.odfdom.dom.element.office.OfficeDocumentContentElement
import org.w3c.dom.Element
import org.w3c.dom.NodeList
import java.io.File
import kotlin.collections.emptyList

class DocService {

    private fun rootElement(doc: OdfTextDocument) = doc.contentDom.rootElement

    fun processor(file: File) {
        OdfTextDocument.loadDocument(file).use { document ->
            val rootElement = rootElement(document)
            val userStyles = userStyles(rootElement)
            if (userStyles.isNotEmpty()) {
                println("В документе есть пользовательские стили:")
                userStyles.forEach { styles ->
                    println(styles)
                }
            } else println("В документе пользовательские стили отсутствуют")

            content(rootElement, userStyles)
        }
    }

    fun userStyles(rootElement: OfficeDocumentContentElement): List<String> {

        val automaticStyleNodeList = rootElement.getElementsByTagNameNS(
            OdfDocumentNamespace.OFFICE.uri,
            "automatic-styles"
        )

        if (automaticStyleNodeList.length == 0) return emptyList<String>()

        val automaticStyles = (automaticStyleNodeList.item(0) as? Element) ?: return emptyList<String>()

        val styles = mutableListOf<String>()
        val styleNodes = automaticStyles.getElementsByTagNameNS(
            OdfDocumentNamespace.STYLE.uri,
            "style"
        )
        for (i in 0 until styleNodes.length) {
            val styleElement = styleNodes.item(i) as? Element ?: continue
            styles.add(styleElement.getAttributeNS(
                OdfDocumentNamespace.STYLE.uri,
                "name"
            ))
        }
        return styles
    }

    fun content(rootElement: OfficeDocumentContentElement, styles: List<String>) {
        val paragraph = rootElement.getElementsByTagNameNS(
            OdfDocumentNamespace.TEXT.uri,
            "p"
        )
        val heading = rootElement.getElementsByTagNameNS(
            OdfDocumentNamespace.TEXT.uri,
            "h"
        )
        val span = rootElement.getElementsByTagNameNS(
            OdfDocumentNamespace.TEXT.uri,
            "span"
        )
        processTag(paragraph, styles)
        processTag(span, styles)
        processTag(heading, styles)
    }

    private fun processTag(elements: NodeList, styles: List<String>) {
        for (i in 0 until elements.length) {
            val element = elements.item(i)
            val styleName = element.attributes?.getNamedItemNS(
                OdfDocumentNamespace.TEXT.uri,
                "style-name"
            )?.nodeValue

            if (styles.contains(styleName)) {
                val text = element.firstChild?.nodeValue?.trim() ?: ""
                if (text.isNotEmpty())
                    println(
                        """
                    У этого текста -> ${element.firstChild.nodeValue} <- оформление отличается от установленного
                    """.trimIndent()
                    )
            }
        }
    }
}