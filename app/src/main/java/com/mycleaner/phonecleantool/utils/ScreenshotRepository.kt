package com.mycleaner.phonecleantool.utils

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.mycleaner.phonecleantool.bean.Screenshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ScreenshotRepository(private val context: Context) {

    var totalSize:Long=0

    suspend fun loadScreenshots(): List<Screenshot> = withContext(Dispatchers.IO) {
        val screenshots = mutableListOf<Screenshot>()
        val contentResolver: ContentResolver = context.contentResolver

        // 定义查询的列
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATE_TAKEN,
            MediaStore.Images.Media.SIZE,
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.RELATIVE_PATH
        )

        // 查询条件 - 查找截图目录中的文件
        val selection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            "${MediaStore.Images.Media.RELATIVE_PATH} LIKE ?"
        } else {
            "${MediaStore.Images.Media.DATA} LIKE ?"
        }
        val selectionArgs = arrayOf("%Screenshots%")
        // 排序方式 - 按时间降序
        val sortOrder = "${MediaStore.Images.Media.DATE_TAKEN} DESC"

        contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
            val dateColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)
            val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val name = cursor.getString(nameColumn)
                val dateTaken = cursor.getLong(dateColumn)
                val size = cursor.getLong(sizeColumn)
                val path = cursor.getString(dataColumn)
                totalSize+=size
                val contentUri = Uri.withAppendedPath(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    id.toString()
                )
                screenshots.add(Screenshot(id, name, dateTaken, size, contentUri,path))
            }
        }

        return@withContext screenshots
    }

    suspend fun deleteScreenshot(screenshot: Screenshot): Boolean = withContext(Dispatchers.IO) {
        try {
            val contentResolver = context.contentResolver
            val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                .buildUpon()
                .appendPath(screenshot.id.toString())
                .build()

            val rowsDeleted = contentResolver.delete(uri, null, null)
            rowsDeleted > 0
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}