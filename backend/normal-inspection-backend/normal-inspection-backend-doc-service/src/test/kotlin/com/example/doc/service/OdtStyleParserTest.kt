package com.example.doc.service

import org.odftoolkit.odfdom.doc.OdfTextDocument
import org.odftoolkit.odfdom.dom.OdfDocumentNamespace
import org.w3c.dom.Element
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class OdtStyleParserTest {

    private val parser = OdtStyleParser()

    @Test
    fun `should extract automatic styles from odt file`() {
        val testFile = createTestOdt()
        try {
            val styles = parser.parseAutomaticStyles(testFile)

            assertEquals(2, styles.size)

            val paragraphStyle = styles.find { it.name == "P1" }
            assertNotNull(paragraphStyle)
            assertEquals("paragraph", paragraphStyle.family)
            assertEquals("Standard", paragraphStyle.parentStyleName)
            assertEquals("bold", paragraphStyle.textProperties["font-weight"])

            val textStyle = styles.find { it.name == "T1" }
            assertNotNull(textStyle)
            assertEquals("text", textStyle.family)
            assertEquals("italic", textStyle.textProperties["font-style"])
            assertEquals("#FF0000", textStyle.textProperties["color"])
        } finally {
            testFile.delete()
        }
    }

    @Test
    fun `should extract automatic styles from input stream`() {
        val testFile = createTestOdt()
        try {
            val styles = testFile.inputStream().use { inputStream ->
                parser.parseAutomaticStyles(inputStream)
            }

            assertEquals(2, styles.size)
        } finally {
            testFile.delete()
        }
    }

    @Test
    fun `should return empty list when no automatic styles`() {
        val testFile = createEmptyTestOdt()
        try {
            val styles = parser.parseAutomaticStyles(testFile)

            assertTrue(styles.isEmpty())
        } finally {
            testFile.delete()
        }
    }

    @Test
    fun `should extract paragraph properties`() {
        val testFile = createTestOdtWithParagraphProperties()
        try {
            val styles = parser.parseAutomaticStyles(testFile)

            val style = styles.find { it.name == "P2" }
            assertNotNull(style)
            assertEquals("10pt", style.paragraphProperties["margin-bottom"])
            assertEquals("0pt", style.paragraphProperties["margin-left"])
        } finally {
            testFile.delete()
        }
    }

    @Test
    fun `should extract common styles from styles xml`() {
        val testFile = createTestOdtWithCommonStyles()
        try {
            val doc = parser.parseAll(testFile)

            assertNotNull(doc.commonStyles["TextBody"])
            assertEquals("paragraph", doc.commonStyles["TextBody"]!!.family)
            assertEquals("Text body", doc.commonStyles["TextBody"]!!.displayName)
            assertEquals("Standard", doc.commonStyles["TextBody"]!!.parentStyleName)
            assertEquals("115%", doc.commonStyles["TextBody"]!!.paragraphProperties["line-height"])
        } finally {
            testFile.delete()
        }
    }

    @Test
    fun `should extract default styles from styles xml`() {
        val testFile = createTestOdtWithCommonStyles()
        try {
            val doc = parser.parseAll(testFile)

            assertNotNull(doc.defaultStyles["paragraph"])
            assertEquals("12pt", doc.defaultStyles["paragraph"]!!.textProperties["font-size"])
        } finally {
            testFile.delete()
        }
    }

    @Test
    fun `should compute diff showing only changes and additions`() {
        val parentProps = mapOf("font-size" to "12pt", "font-name" to "Serif", "color" to "black")
        val childProps = mapOf("font-size" to "14pt", "font-name" to "Serif", "font-weight" to "bold")

        val diff = diffProperties(parentProps, childProps)
        val changesOnly = diff.filter { it.type != DiffType.INHERITED }

        val changed = changesOnly.filter { it.type == DiffType.CHANGED }
        val added = changesOnly.filter { it.type == DiffType.ADDED }

        assertEquals(1, changed.size)
        assertEquals("font-size", changed[0].property)
        assertEquals("12pt", changed[0].parentValue)
        assertEquals("14pt", changed[0].childValue)

        assertEquals(1, added.size)
        assertEquals("font-weight", added[0].property)
        assertEquals(null, added[0].parentValue)
        assertEquals("bold", added[0].childValue)
    }

    @Test
    fun `should find style usage in paragraphs`() {
        val testFile = createTestOdtWithBody()
        try {
            val usages = parser.findStyleUsages(testFile, setOf("P1", "T1"))

            assertEquals(1, usages["P1"]?.size)
            assertEquals(3, usages["P1"]!![0].paragraphIndex)
            assertEquals("Second paragraph", usages["P1"]!![0].textContent)
            assertEquals("p", usages["P1"]!![0].elementName)

            assertEquals(1, usages["T1"]?.size)
            assertEquals(2, usages["T1"]!![0].paragraphIndex)
            assertEquals("italic text", usages["T1"]!![0].textContent)
            assertEquals("span", usages["T1"]!![0].elementName)
        } finally {
            testFile.delete()
        }
    }

    @Test
    fun `should return empty usages for unknown style`() {
        val testFile = createTestOdtWithBody()
        try {
            val usages = parser.findStyleUsages(testFile, setOf("UNKNOWN"))

            assertTrue(usages.isEmpty())
        } finally {
            testFile.delete()
        }
    }

    private fun createTestOdt(): File {
        val doc = OdfTextDocument.newTextDocument()
        val contentDom = doc.contentDom
        val root = contentDom.rootElement

        val styleNs = OdfDocumentNamespace.STYLE.uri
        val officeNs = OdfDocumentNamespace.OFFICE.uri
        val foNs = OdfDocumentNamespace.FO.uri

        val autoStyles = findOrCreateAutomaticStyles(root, contentDom, officeNs)

        val style1 = contentDom.createElementNS(styleNs, "style:style") as Element
        style1.setAttributeNS(styleNs, "style:name", "P1")
        style1.setAttributeNS(styleNs, "style:family", "paragraph")
        style1.setAttributeNS(styleNs, "style:parent-style-name", "Standard")
        val textProps1 = contentDom.createElementNS(styleNs, "style:text-properties") as Element
        textProps1.setAttributeNS(foNs, "fo:font-weight", "bold")
        style1.appendChild(textProps1)
        autoStyles.appendChild(style1)

        val style2 = contentDom.createElementNS(styleNs, "style:style") as Element
        style2.setAttributeNS(styleNs, "style:name", "T1")
        style2.setAttributeNS(styleNs, "style:family", "text")
        val textProps2 = contentDom.createElementNS(styleNs, "style:text-properties") as Element
        textProps2.setAttributeNS(foNs, "fo:font-style", "italic")
        textProps2.setAttributeNS(foNs, "fo:color", "#FF0000")
        style2.appendChild(textProps2)
        autoStyles.appendChild(style2)

        val file = File.createTempFile("test-odt", ".odt")
        file.deleteOnExit()
        doc.save(file)
        doc.close()
        return file
    }

    private fun createEmptyTestOdt(): File {
        val doc = OdfTextDocument.newTextDocument()
        val file = File.createTempFile("test-empty-odt", ".odt")
        file.deleteOnExit()
        doc.save(file)
        doc.close()
        return file
    }

    private fun createTestOdtWithParagraphProperties(): File {
        val doc = OdfTextDocument.newTextDocument()
        val contentDom = doc.contentDom
        val root = contentDom.rootElement

        val styleNs = OdfDocumentNamespace.STYLE.uri
        val officeNs = OdfDocumentNamespace.OFFICE.uri
        val foNs = OdfDocumentNamespace.FO.uri

        val autoStyles = findOrCreateAutomaticStyles(root, contentDom, officeNs)

        val style = contentDom.createElementNS(styleNs, "style:style") as Element
        style.setAttributeNS(styleNs, "style:name", "P2")
        style.setAttributeNS(styleNs, "style:family", "paragraph")
        val paraProps = contentDom.createElementNS(styleNs, "style:paragraph-properties") as Element
        paraProps.setAttributeNS(foNs, "fo:margin-bottom", "10pt")
        paraProps.setAttributeNS(foNs, "fo:margin-left", "0pt")
        style.appendChild(paraProps)
        autoStyles.appendChild(style)

        val file = File.createTempFile("test-para-props-odt", ".odt")
        file.deleteOnExit()
        doc.save(file)
        doc.close()
        return file
    }

    private fun createTestOdtWithCommonStyles(): File {
        val doc = OdfTextDocument.newTextDocument()

        val stylesDom = doc.stylesDom
        val stylesRoot = stylesDom.rootElement
        val styleNs = OdfDocumentNamespace.STYLE.uri
        val officeNs = OdfDocumentNamespace.OFFICE.uri
        val foNs = OdfDocumentNamespace.FO.uri

        val officeStylesNodes = stylesRoot.getElementsByTagNameNS(officeNs, "styles")
        val officeStyles = if (officeStylesNodes.length > 0) {
            officeStylesNodes.item(0) as Element
        } else {
            val el = stylesDom.createElementNS(officeNs, "office:styles") as Element
            stylesRoot.appendChild(el)
            el
        }

        val defaultStyle = stylesDom.createElementNS(styleNs, "style:default-style") as Element
        defaultStyle.setAttributeNS(styleNs, "style:family", "paragraph")
        val defaultTextProps = stylesDom.createElementNS(styleNs, "style:text-properties") as Element
        defaultTextProps.setAttributeNS(foNs, "fo:font-size", "12pt")
        defaultStyle.appendChild(defaultTextProps)
        officeStyles.appendChild(defaultStyle)

        val standardStyle = stylesDom.createElementNS(styleNs, "style:style") as Element
        standardStyle.setAttributeNS(styleNs, "style:name", "Standard")
        standardStyle.setAttributeNS(styleNs, "style:family", "paragraph")
        officeStyles.appendChild(standardStyle)

        val textBodyStyle = stylesDom.createElementNS(styleNs, "style:style") as Element
        textBodyStyle.setAttributeNS(styleNs, "style:name", "TextBody")
        textBodyStyle.setAttributeNS(styleNs, "style:display-name", "Text body")
        textBodyStyle.setAttributeNS(styleNs, "style:family", "paragraph")
        textBodyStyle.setAttributeNS(styleNs, "style:parent-style-name", "Standard")
        val paraProps = stylesDom.createElementNS(styleNs, "style:paragraph-properties") as Element
        paraProps.setAttributeNS(foNs, "fo:line-height", "115%")
        textBodyStyle.appendChild(paraProps)
        officeStyles.appendChild(textBodyStyle)

        val file = File.createTempFile("test-common-styles-odt", ".odt")
        file.deleteOnExit()
        doc.save(file)
        doc.close()
        return file
    }

    private fun createTestOdtWithBody(): File {
        val doc = OdfTextDocument.newTextDocument()
        val contentDom = doc.contentDom
        val root = contentDom.rootElement

        val styleNs = OdfDocumentNamespace.STYLE.uri
        val officeNs = OdfDocumentNamespace.OFFICE.uri
        val textNs = OdfDocumentNamespace.TEXT.uri

        findOrCreateAutomaticStyles(root, contentDom, officeNs).apply {
            val p1 = contentDom.createElementNS(styleNs, "style:style") as Element
            p1.setAttributeNS(styleNs, "style:name", "P1")
            p1.setAttributeNS(styleNs, "style:family", "paragraph")
            appendChild(p1)

            val t1 = contentDom.createElementNS(styleNs, "style:style") as Element
            t1.setAttributeNS(styleNs, "style:name", "T1")
            t1.setAttributeNS(styleNs, "style:family", "text")
            appendChild(t1)
        }

        val bodyNodes = root.getElementsByTagNameNS(officeNs, "text")
        val textRoot = bodyNodes.item(0) as Element

        val p1 = contentDom.createElementNS(textNs, "text:p") as Element
        p1.setAttributeNS(textNs, "text:style-name", "Standard")
        val span = contentDom.createElementNS(textNs, "text:span") as Element
        span.setAttributeNS(textNs, "text:style-name", "T1")
        span.textContent = "italic text"
        p1.appendChild(contentDom.createTextNode("Normal text "))
        p1.appendChild(span)
        textRoot.appendChild(p1)

        val p2 = contentDom.createElementNS(textNs, "text:p") as Element
        p2.setAttributeNS(textNs, "text:style-name", "P1")
        p2.textContent = "Second paragraph"
        textRoot.appendChild(p2)

        val file = File.createTempFile("test-body-odt", ".odt")
        file.deleteOnExit()
        doc.save(file)
        doc.close()
        return file
    }

    private fun findOrCreateAutomaticStyles(
        root: Element,
        contentDom: org.odftoolkit.odfdom.pkg.OdfFileDom,
        officeNs: String,
    ): Element {
        val existing = root.getElementsByTagNameNS(officeNs, "automatic-styles")
        if (existing.length > 0) {
            return existing.item(0) as Element
        }

        val autoStyles = contentDom.createElementNS(officeNs, "office:automatic-styles") as Element
        val body = root.getElementsByTagNameNS(officeNs, "body")
        if (body.length > 0) {
            root.insertBefore(autoStyles, body.item(0))
        } else {
            root.appendChild(autoStyles)
        }
        return autoStyles
    }
}
