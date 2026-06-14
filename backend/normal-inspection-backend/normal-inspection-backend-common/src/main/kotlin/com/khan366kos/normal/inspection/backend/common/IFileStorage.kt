package com.khan366kos.normal.inspection.backend.common

import com.khan366kos.normal.inspection.backend.common.domain.StorageOutcome
import java.io.InputStream

interface IFileStorage {
    suspend fun store(
        path: String,
        inputStream: InputStream
    ): StorageOutcome
}
