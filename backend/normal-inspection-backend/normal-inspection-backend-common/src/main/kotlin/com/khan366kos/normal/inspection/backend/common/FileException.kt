package com.khan366kos.normal.inspection.backend.common

class FileSaveStorageOperationException(cause: Throwable) :
    RuntimeException("File storage save operation failed", cause)

class FileOperationFailedException(cause: Throwable): RuntimeException("File operation failed", cause)