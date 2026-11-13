package com.khan366kos.normal.inspection.backend.ktor.app

import com.khan366kos.normal.inspection.backend.common.IFileReceiver
import com.khan366kos.normal.inspection.backend.file.receiver.FileReceiver

class AppConfig(
    val fileReceiver: IFileReceiver = FileReceiver()
) {
}