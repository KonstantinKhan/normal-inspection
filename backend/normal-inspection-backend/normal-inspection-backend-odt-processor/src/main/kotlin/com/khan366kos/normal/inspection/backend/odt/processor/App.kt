package com.khan366kos.normal.inspection.backend.odt.processor

fun main() {
    val doc = DocGenerator()
    val chapter = doc.generateChapter()
    println(chapter)
}