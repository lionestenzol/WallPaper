package com.focusblack.wallos.data.cache

import java.io.File
import java.security.MessageDigest
import java.util.Locale

class DiskLruCache(
    private val directory: File,
    private val maxSizeBytes: Long
) {

    init {
        if (!directory.exists()) {
            directory.mkdirs()
        }
    }

    fun get(key: String): File? {
        val file = fileForKey(key)
        if (file.exists()) {
            file.setLastModified(System.currentTimeMillis())
            return file
        }
        return null
    }

    fun put(key: String, writeToFile: (File) -> Unit): File {
        val file = fileForKey(key)
        if (!file.exists()) {
            val tempFile = File(directory, "${file.name}.tmp")
            if (tempFile.exists()) {
                tempFile.delete()
            }
            writeToFile(tempFile)
            if (tempFile.exists()) {
                tempFile.renameTo(file)
            }
        }
        file.setLastModified(System.currentTimeMillis())
        trimToSize()
        return file
    }

    private fun trimToSize() {
        val files = directory.listFiles()?.toList().orEmpty()
        var totalSize = files.sumOf { it.length() }
        if (totalSize <= maxSizeBytes) {
            return
        }
        val sorted = files.sortedBy { it.lastModified() }
        for (file in sorted) {
            if (totalSize <= maxSizeBytes) {
                break
            }
            val length = file.length()
            if (file.delete()) {
                totalSize -= length
            }
        }
    }

    private fun fileForKey(key: String): File {
        val hash = sha256(key)
        return File(directory, hash)
    }

    private fun sha256(value: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest(value.toByteArray())
        return bytes.joinToString("") { "%02x".format(Locale.US, it) }
    }
}
