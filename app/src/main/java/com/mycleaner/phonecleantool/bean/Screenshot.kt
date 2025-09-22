package com.mycleaner.phonecleantool.bean

import android.net.Uri
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class Screenshot(
    val id: Long,
    val name: String,
    val dateTaken: Long, // 时间戳
    val size: Long,      // 文件大小(字节)
    val contentUri: Uri,
    val path: String,    // 文件路径
    var isSelected: Boolean=false
) {
    // 格式化日期
    fun formattedDate(): String {
        return SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            .format(Date(dateTaken))
    }

    // 格式化文件大小
    fun formattedSize(): String {
        return when {
            size >= 1024 * 1024 -> String.format("%.1f MB", size / (1024.0 * 1024.0))
            size >= 1024 -> String.format("%.1f KB", size / 1024.0)
            else -> "$size B"
        }
    }
}