package com.mycleaner.phonecleantool.utils

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import com.mycleaner.phonecleantool.bean.MemoryInfo


object MemoryUtils {
    suspend fun getMemoryInfo(context: Context): MemoryInfo {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        val totalMemory = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            memoryInfo.totalMem
        } else {
            // 低版本API需要通过反射或其他方式获取，这里简单返回0
            0L
        }
        val availableMemory = memoryInfo.availMem
        val usedMemory = totalMemory - availableMemory
        val memoryUsageRatio = if (totalMemory > 0) {
            usedMemory.toFloat() / totalMemory.toFloat()
        } else {
            0f
        }
        var info = MemoryInfo(
            (memoryUsageRatio * 100).toInt(),
            ""
        )
        return info
    }

}