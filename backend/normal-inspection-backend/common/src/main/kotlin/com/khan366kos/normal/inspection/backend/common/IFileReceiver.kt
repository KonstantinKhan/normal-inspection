package com.khan366kos.normal.inspection.backend.common

import com.khan366kos.normal.inspection.backend.common.domain.ReceptionResult
import java.io.InputStream

interface IFileReceiver {
    suspend fun receiveFile(inputStream: InputStream, fileName: String): ReceptionResult
}
