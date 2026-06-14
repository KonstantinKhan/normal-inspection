package com.example.doc.service

import java.nio.file.Files
import java.nio.file.Paths

fun main() {
    println("Введите путь к файлу:")
    val filePath = readlnOrNull() ?: return
    val path = Paths.get(filePath)

    when {
        Files.exists(path) -> {
            if (Files.isRegularFile(path)) {
                println("Файл найден: ${path.fileName}")
                val file = path.toFile()
                if (file.extension == "odt") {
                    println("Это ODT файл")
                    val docService = DocService()
                    val userStyles = docService.processor(file)

                } else {
                    println("Путь не указывает на обычный файл: $filePath")
                }
            }
        }
    }
}