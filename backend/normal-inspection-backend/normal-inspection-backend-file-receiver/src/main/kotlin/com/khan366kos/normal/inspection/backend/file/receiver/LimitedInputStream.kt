package com.khan366kos.normal.inspection.backend.file.receiver

import java.io.InputStream

class LimitedInputStream(
    private val delegate: InputStream,
    private val maxBytes: Long,
) : InputStream() {

    private var read: Long = 0L

    override fun read(): Int {
        val b = delegate.read()
        if (b >= 0) checkLimit(1L)
        return b
    }

    override fun read(b: ByteArray, off: Int, len: Int): Int {
        val n = delegate.read(b, off, len)
        if (n > 0) checkLimit(n.toLong())
        return n
    }

    private fun checkLimit(delta: Long) {
        read += delta
        if (read > maxBytes) throw SizeLimitExceededException(maxBytes)
    }
}

class SizeLimitExceededException(val limit: Long) : RuntimeException(
    "File size exceeds limit of $limit bytes"
)
