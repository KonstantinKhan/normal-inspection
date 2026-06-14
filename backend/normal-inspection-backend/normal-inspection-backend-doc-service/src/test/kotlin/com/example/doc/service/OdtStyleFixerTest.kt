package com.example.doc.service

import org.odftoolkit.odfdom.doc.OdfTextDocument
import org.odftoolkit.odfdom.dom.OdfDocumentNamespace
import org.w3c.dom.Element
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class OdtStyleFixerTest {

    private val fixer = OdtStyleFixer()

    @Test
    fun `should replace paragraph automatic style with target`() {
        val testFile = createOdtWithAutoParagraphStyle()
        try {
            val result = fixer.fixParagraphStyles(testFile, "Text_20_body")

            assertEquals(1, result.fixedParagraphs)
            assertEquals(1, result.removedStyles)
            assertTrue(result.outputFile.exists())
            assertTrue(result.outputFile.name.endsWith("_fixed.odt"))

            val parser = OdtStyleParser()
            val doc = parser.parseAll(result.outputFile)
            assertTrue(doc.automaticStyles.isEmpty())

            val usages = parser.findStyleUsages(result.outputFile, setOf("Text_20_body"))
            assertNotNull(usages["Text_20_body"])
            assertTrue(usages["Text_20_body"]!!.any { it.textContent == "Custom paragraph" })

            result.outputFile.delete()
        } finally {
            testFile.delete()
        }
    }

    @Test
    fun `should not modify paragraphs without automatic styles`() {
        val testFile = createOdtWithoutAutoStyles()
        try {
            val result = fixer.fixParagraphStyles(testFile, "Text_20_body")

            assertEquals(0, result.fixedParagraphs)
            assertEquals(0, result.removedStyles)

            result.outputFile.delete()
        } finally {
            testFile.delete()
        }
    }

    @Test
    fun `should keep span styles untouched when fixing paragraphs`() {
        val testFile = createOdtWithSpanStyle()
        try {
            val result = fixer.fixParagraphStyles(testFile, "Text_20_body")

            assertEquals(1, result.fixedParagraphs)

            val parser = OdtStyleParser()
            val fixedDoc = parser.parseAll(result.outputFile)

            val spanAutoStyle = fixedDoc.automaticStyles.find { it.family == "text" }
            assertNotNull(spanAutoStyle)

            result.outputFile.delete()
        } finally {
            testFile.delete()
        }
    }

    @Test
    fun `should generate output file next to original`() {
        val testFile = createOdtWithAutoParagraphStyle()
        try {
            val result = fixer.fixParagraphStyles(testFile, "Text_20_body")

            assertEquals(testFile.parentFile, result.outputFile.parentFile)
            assertTrue(result.outputFile.name.startsWith(testFile.nameWithoutExtension))
            assertTrue(result.outputFile.name.endsWith("_fixed.odt"))

            result.outputFile.delete()
        } finally {
            testFile.delete()
        }
    }

    private fun createOdtWithAutoParagraphStyle(): File {
        val doc = OdfTextDocument.newTextDocument()
        val contentDom = doc.contentDom
        val root = contentDom.rootElement

        val styleNs = OdfDocumentNamespace.STYLE.uri
        val officeNs = OdfDocumentNamespace.OFFICE.uri
        val textNs = OdfDocumentNamespace.TEXT.uri

        val autoStyles = root.getElementsByTagNameNS(officeNs, "automatic-styles")
        val autoStylesEl = if (autoStyles.length > 0) autoStyles.item(0) as Element else {
            val el = contentDom.createElementNS(officeNs, "office:automatic-styles") as Element
            root.appendChild(el)
            el
        }

        val style = contentDom.createElementNS(styleNs, "style:style") as Element
        style.setAttributeNS(styleNs, "style:name", "P1")
        style.setAttributeNS(styleNs, "style:family", "paragraph")
        style.setAttributeNS(styleNs, "style:parent-style-name", "Text_20_body")
        autoStylesEl.appendChild(style)

        val bodyNodes = root.getElementsByTagNameNS(officeNs, "text")
        val textRoot = bodyNodes.item(0) as Element

        val p = contentDom.createElementNS(textNs, "text:p") as Element
        p.setAttributeNS(textNs, "text:style-name", "P1")
        p.textContent = "Custom paragraph"
        textRoot.appendChild(p)

        val file = File.createTempFile("test_auto_para", ".odt")
        file.deleteOnExit()
        doc.save(file)
        doc.close()
        return file
    }

    private fun createOdtWithoutAutoStyles(): File {
        val doc = OdfTextDocument.newTextDocument()
        val contentDom = doc.contentDom
        val root = contentDom.rootElement

        val textNs = OdfDocumentNamespace.TEXT.uri
        val officeNs = OdfDocumentNamespace.OFFICE.uri

        val bodyNodes = root.getElementsByTagNameNS(officeNs, "text")
        val textRoot = bodyNodes.item(0) as Element

        val p = contentDom.createElementNS(textNs, "text:p") as Element
        p.setAttributeNS(textNs, "text:style-name", "Text_20_body")
        p.textContent = "Normal paragraph"
        textRoot.appendChild(p)

        val file = File.createTempFile("test_no_auto", ".odt")
        file.deleteOnExit()
        doc.save(file)
        doc.close()
        return file
    }

    private fun createOdtWithSpanStyle(): File {
        val doc = OdfTextDocument.newTextDocument()
        val contentDom = doc.contentDom
        val root = contentDom.rootElement

        val styleNs = OdfDocumentNamespace.STYLE.uri
        val officeNs = OdfDocumentNamespace.OFFICE.uri
        val textNs = OdfDocumentNamespace.TEXT.uri
        val foNs = OdfDocumentNamespace.FO.uri

        val autoStyles = root.getElementsByTagNameNS(officeNs, "automatic-styles")
        val autoStylesEl = autoStyles.item(0) as Element

        val pStyle = contentDom.createElementNS(styleNs, "style:style") as Element
        pStyle.setAttributeNS(styleNs, "style:name", "P1")
        pStyle.setAttributeNS(styleNs, "style:family", "paragraph")
        autoStylesEl.appendChild(pStyle)

        val tStyle = contentDom.createElementNS(styleNs, "style:style") as Element
        tStyle.setAttributeNS(styleNs, "style:name", "T1")
        tStyle.setAttributeNS(styleNs, "style:family", "text")
        val textProps = contentDom.createElementNS(styleNs, "style:text-properties") as Element
        textProps.setAttributeNS(foNs, "fo:font-style", "italic")
        tStyle.appendChild(textProps)
        autoStylesEl.appendChild(tStyle)

        val bodyNodes = root.getElementsByTagNameNS(officeNs, "text")
        val textRoot = bodyNodes.item(0) as Element

        val p = contentDom.createElementNS(textNs, "text:p") as Element
        p.setAttributeNS(textNs, "text:style-name", "P1")
        val span = contentDom.createElementNS(textNs, "text:span") as Element
        span.setAttributeNS(textNs, "text:style-name", "T1")
        span.textContent = "italic"
        p.appendChild(contentDom.createTextNode("Normal "))
        p.appendChild(span)
        textRoot.appendChild(p)

        val file = File.createTempFile("test_span_style", ".odt")
        file.deleteOnExit()
        doc.save(file)
        doc.close()
        return file
    }
}
