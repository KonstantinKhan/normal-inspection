package com.khan366kos.normal.inspection.backend.common.domain

import java.io.IOException

/**
 * Инфра-уровневый исход операции записи на диск.
 * Не знает бизнес-причин — только «записано» либо «провал I/O».
 * Перевод в доменный [ReceptionResult] — ответственность FileReceiver.
 */
sealed class StorageOutcome {
    data class Written(val name: String, val size: Long) : StorageOutcome()
    data class Failed(val cause: IOException) : StorageOutcome()
}
