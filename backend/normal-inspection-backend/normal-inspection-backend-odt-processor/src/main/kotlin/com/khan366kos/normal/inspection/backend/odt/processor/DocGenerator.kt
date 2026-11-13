package com.khan366kos.normal.inspection.backend.odt.processor

import com.khan366kos.normal.inspection.backend.odt.processor.dsl.ChapterBuilder

fun chapter(block: ChapterBuilder.() -> Unit): Chapter = ChapterBuilder().apply(block).build()

class DocGenerator {
    fun generateChapter(): Chapter =
        chapter {
            level = 1
            title = "Introduction"
            paragraphs {
                paragraph {
                    text = "This is the introduction of the document."
                }
                paragraph {
                    text = "This is another paragraph in the chapter."
                }
                paragraph {
                    text = "This is a third paragraph in the chapter."
                }
            }
        }
}