package com.example.util

import java.util.Locale

object ByteFormatter {
    fun formatBytes(bytes: Long): String {
        if (bytes <= 0) return "0 MB"
        val kb = bytes / 1024.0
        val mb = kb / 1024.0
        val gb = mb / 1024.0

        return when {
            gb >= 1.0 -> String.format(Locale.US, "%.2f GB", gb)
            mb >= 1.0 -> String.format(Locale.US, "%.1f MB", mb)
            kb >= 1.0 -> String.format(Locale.US, "%.0f KB", kb)
            else -> "$bytes B"
        }
    }

    fun formatBytesPair(bytes: Long): Pair<String, String> {
        if (bytes <= 0) return Pair("0", "MB")
        val kb = bytes / 1024.0
        val mb = kb / 1024.0
        val gb = mb / 1024.0

        return when {
            gb >= 1.0 -> Pair(String.format(Locale.US, "%.2f", gb), "GB")
            mb >= 1.0 -> Pair(String.format(Locale.US, "%.1f", mb), "MB")
            kb >= 1.0 -> Pair(String.format(Locale.US, "%.0f", kb), "KB")
            else -> Pair("$bytes", "B")
        }
    }
}
