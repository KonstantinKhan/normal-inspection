package com.khan366kos.normal.inspection.backend.odt.processor

import com.ctc.wstx.stax.WstxInputFactory
import com.khan366kos.normal.inspection.backend.odt.processor.common.OdtProcessor
import java.nio.file.Files
import java.nio.file.Paths
import java.util.zip.ZipInputStream
import javax.xml.stream.XMLStreamConstants

fun main() {
    val factory = WstxInputFactory()
    val processor = OdtProcessor()

    val odtPath =
        Paths.get("/Users/khan/Projects/normal-inspection/backend/normal-inspection-backend/normal-inspection-backend-odt-processor/src/main/resources/test.odt")
    if (!Files.exists(odtPath)) return
    ZipInputStream(Files.newInputStream(odtPath)).use { zip ->
        var entry = zip.nextEntry
        while (entry != null) {
            if (entry.name == "content.xml") {
                val reader = factory.createXMLStreamReader(zip)
            }
            entry = zip.nextEntry
        }
    }
}