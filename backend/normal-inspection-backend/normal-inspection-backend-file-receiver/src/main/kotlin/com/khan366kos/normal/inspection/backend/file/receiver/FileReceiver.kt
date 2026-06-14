package com.khan366kos.normal.inspection.backend.file.receiver

import com.khan366kos.normal.inspection.backend.common.IFileReceiver
import com.khan366kos.normal.inspection.backend.common.IFileStorage
import com.khan366kos.normal.inspection.backend.common.domain.ReceptionResult
import com.khan366kos.normal.inspection.backend.common.domain.RejectionReason
import com.khan366kos.normal.inspection.backend.common.domain.StorageOutcome
import com.khan366kos.normal.inspection.backend.file.storage.FileStorage
import java.io.InputStream

class FileReceiver(
    private val storage: IFileStorage = FileStorage(),
    private val allowedExtensions: Set<String> = DEFAULT_ALLOWED_EXTENSIONS,
    private val maxBytes: Long = DEFAULT_MAX_BYTES,
) : IFileReceiver {

    override suspend fun receiveFile(inputStream: InputStream, fileName: String): ReceptionResult {
        val safeName = sanitizeName(fileName)
        val ext = safeName.substringAfterLast('.', "").lowercase()
        if (ext.isEmpty() || ext !in allowedExtensions)
            return ReceptionResult.Rejected(RejectionReason.FORBIDDEN_EXTENSION, fileName)

        val bounded = LimitedInputStream(inputStream, maxBytes)
        return try {
            when (val outcome = storage.store(safeName, bounded)) {
                is StorageOutcome.Written ->
                    ReceptionResult.Accepted(outcome.name, outcome.size)
                is StorageOutcome.Failed ->
                    ReceptionResult.Rejected(
                        RejectionReason.STORAGE_FAILURE,
                        outcome.cause.message ?: "unknown"
                    )
            }
        } catch (e: SizeLimitExceededException) {
            ReceptionResult.Rejected(RejectionReason.TOO_LARGE, e.message ?: "size limit exceeded")
        }
    }

    private fun sanitizeName(raw: String): String =
        raw.substringAfterLast('/')
            .substringAfterLast('\\')
            .trim()

    companion object {
        private val DEFAULT_ALLOWED_EXTENSIONS: Set<String> = setOf("odt")
        private const val DEFAULT_MAX_BYTES: Long = 100L * 1024 * 1024 // 100 MiB
    }
}
