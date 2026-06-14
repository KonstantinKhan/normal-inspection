package com.khan366kos.normal.inspection.backend.file.storage

import com.khan366kos.normal.inspection.backend.common.IFileStorage
import com.khan366kos.normal.inspection.backend.common.domain.StorageOutcome
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.StandardCopyOption

class FileStorage : IFileStorage {
    private val logger = LoggerFactory.getLogger(javaClass)

    override suspend fun store(path: String, inputStream: InputStream): StorageOutcome =
        withContext(Dispatchers.IO) {
            try {
                val file = File(path)
                file.parentFile?.mkdirs()
                Files.copy(inputStream, file.toPath(), StandardCopyOption.REPLACE_EXISTING)
                StorageOutcome.Written(file.name, file.length())
            } catch (e: CancellationException) {
                throw e
            } catch (e: IOException) {
                logger.error("Failed to store file: {}", path, e)
                StorageOutcome.Failed(e)
            }
        }
}
