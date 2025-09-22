package com.mycleaner.phonecleantool.utils

import android.R.attr.path
import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import com.mycleaner.phonecleantool.bean.LargeFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class FileScanner(private val contentResolver: ContentResolver) {

    // 最小文件大小：10MB
    private val minFileSize = 10 * 1024 * 1024

    suspend fun scanLargeFiles(): List<LargeFile> = withContext(Dispatchers.IO) {
        val largeFiles = mutableListOf<LargeFile>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11+ 使用MediaStore API
            scanWithMediaStore(largeFiles)
        } else {
            // 旧版本Android使用直接文件访问
            scanWithFileApi(largeFiles)
        }

        return@withContext largeFiles
    }

    private suspend fun scanWithMediaStore(largeFiles: MutableList<LargeFile>) {
        val projection = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.DISPLAY_NAME,
            MediaStore.Files.FileColumns.SIZE,
            MediaStore.Files.FileColumns.MIME_TYPE,
            MediaStore.Files.FileColumns.DATE_MODIFIED,
            MediaStore.Files.FileColumns.DATA
        )

        val selection = "${MediaStore.Files.FileColumns.SIZE} >= ?"
        val selectionArgs = arrayOf(minFileSize.toString())

        val sortOrder = "${MediaStore.Files.FileColumns.SIZE} DESC"

        contentResolver.query(
            MediaStore.Files.getContentUri("external"),
            projection,
            selection,
            selectionArgs,
            sortOrder
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
            val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE)
            val typeColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE)
            val dateColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_MODIFIED)
            val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA)

            while (cursor.moveToNext()) {
                val path = cursor.getString(dataColumn)
                if (path != null && isSystemDirectory(path)) {
                    continue
                }
                val id = cursor.getLong(idColumn)
                val name = cursor.getString(nameColumn)
                val size = cursor.getLong(sizeColumn)
                val type = cursor.getString(typeColumn) ?: "unknown"
                val date = cursor.getLong(dateColumn) * 1000
                val largeFile = LargeFile(id, name, path, size, type, date)
                if (type.startsWith("video/") ||
                    type.startsWith("image/") ||
                    type.startsWith("audio/")) {
                    largeFile.thumbnailUri = getMediaThumbnail(id, type)
                }
                largeFiles.add(largeFile)
            }
        }
    }


    // 获取媒体文件缩略图
    private suspend fun getMediaThumbnail( mediaId: Long, mimeType: String): Uri? {
        return withContext(Dispatchers.IO) {
            try {
                if (mimeType.startsWith("image/")) {
                    // 对于图片，直接使用ContentUris生成URI
                    ContentUris.withAppendedId(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        mediaId
                    )
                } else if (mimeType.startsWith("video/")) {
                    // 对于视频，使用MediaStore获取缩略图
                    val thumbUri = ContentUris.withAppendedId(
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                        mediaId
                    )
                    // 或者可以使用MediaMetadataRetriever获取视频第一帧
                    thumbUri
                } else {
                    null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
    private fun isSystemDirectory(path: String): Boolean {
        val systemDirs = listOf(
            Environment.getDownloadCacheDirectory().absolutePath,
            "/system",
            "/cache",
            "/proc",
            "/acct",
            "/dev",
            "/config",
            "/mnt",
            "/sys"
        )
        return systemDirs.any { path.contains(it) }
    }

    private fun scanWithFileApi(largeFiles: MutableList<LargeFile>) {
        val storageDir = Environment.getExternalStorageDirectory()
        scanDirectory(File(storageDir.absolutePath), largeFiles)
    }

    private fun scanDirectory(dir: File, largeFiles: MutableList<LargeFile>) {
        try {
            dir.listFiles()?.forEach { file ->
                if (file.isDirectory) {
                    // 递归扫描子目录
                    scanDirectory(file, largeFiles)
                } else if (file.isFile && file.length() >= minFileSize) {
                    val extension = getFileType(file)
                    // 获取文件扩展名作为类型

                    if (file.absolutePath != null && !isSystemDirectory(file.absolutePath)) {
                        largeFiles.add(
                            LargeFile(
                                id = file.hashCode().toLong(),
                                name = file.name,
                                path = file.absolutePath,
                                size = file.length(),
                                type = extension,
                                date=file.lastModified()* 1000
                            )
                        )
                    }
                }
            }
        } catch (e: SecurityException) {
            // 处理权限问题
            e.printStackTrace()
        }
    }
    private fun getFileType(file: File): String {
        val name = file.name
        val extension = if (name.contains(".")) {
            name.substringAfterLast(".", "").lowercase()
        } else {
            ""
        }
        return when (extension) {
            "jpg", "jpeg", "png", "gif", "bmp", "webp" -> "image/$extension"
            "mp4", "avi", "mov", "wmv", "flv", "mkv" -> "video/$extension"
            "mp3", "wav", "ogg", "flac", "m4a" -> "audio/$extension"
            else -> extension
        }
    }

    suspend fun deleteFiles(files: List<LargeFile>): Boolean = withContext(Dispatchers.IO) {
        var allDeleted = true
        files.forEach { file ->
            try {
                val fileObj = File(file.path)
                if (fileObj.exists()) {
                    fileObj.delete()
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    // 使用MediaStore删除
                    val uri = ContentUris.withAppendedId(
                        MediaStore.Files.getContentUri("external"),
                        file.id
                    )
                    contentResolver.delete(uri, null, null)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                allDeleted = false
            }
        }
        return@withContext allDeleted
    }
}