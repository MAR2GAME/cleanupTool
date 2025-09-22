package com.mycleaner.phonecleantool.utils

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.drawable.Drawable
import androidx.lifecycle.application
import com.mycleaner.phonecleantool.bean.AppInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class AdvancedAppInfoHelper(private val context: Context) {

    // 获取非系统应用列表（多种方法结合）
    suspend fun getNonSystemApps(): List<AppInfo> = withContext(Dispatchers.IO) {
        val packageManager = context.packageManager
        val currentAppPackage = context.packageName

        // 方法1: 查询可启动应用
        val launcherApps = getLauncherApps(packageManager, currentAppPackage)

        // 方法2: 查询特定类型的应用（如浏览器、地图等）
        val specificTypeApps = getSpecificTypeApps(packageManager, currentAppPackage)

        // 合并结果并去重
        (launcherApps + specificTypeApps)
            .distinctBy { it.packageName }
    }

    // 获取启动器中的应用
    private fun getLauncherApps(packageManager: PackageManager, currentAppPackage: String): List<AppInfo> {
        return try {
            val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }
            val resolveInfos = packageManager.queryIntentActivities(mainIntent, 0)
            resolveInfos.mapNotNull { resolveInfo ->
                try {
                    val packageName = resolveInfo.activityInfo.packageName
                    createAppInfo(packageManager, packageName, currentAppPackage)
                } catch (e: Exception) {
                    null
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // 获取特定类型的应用
    private fun getSpecificTypeApps(packageManager: PackageManager, currentAppPackage: String): List<AppInfo> {
        return try {
            // 查询浏览器应用
            val browserIntent = Intent(Intent.ACTION_VIEW).apply {
                addCategory(Intent.CATEGORY_BROWSABLE)
                data = android.net.Uri.parse("http://www.example.com")
            }

            val resolveInfos = packageManager.queryIntentActivities(browserIntent, 0)

            resolveInfos.mapNotNull { resolveInfo ->
                try {
                    val packageName = resolveInfo.activityInfo.packageName
                    createAppInfo(packageManager, packageName, currentAppPackage)
                } catch (e: Exception) {
                    null
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // 创建应用信息对象
    private fun createAppInfo(packageManager: PackageManager, packageName: String, currentAppPackage: String): AppInfo? {
        if (packageName == currentAppPackage) return null

        try {
            val applicationInfo = packageManager.getApplicationInfo(packageName, 0)

            // 排除系统应用
            if ((applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0 ||
                (applicationInfo.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0||applicationInfo.packageName == context.packageName) {
                return null
            }
            // 获取包信息以获取安装时间
            val packageInfo = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                packageManager.getPackageInfo(packageName, 0)
            }

            val appName = applicationInfo.loadLabel(packageManager).toString()
            val icon = applicationInfo.loadIcon(packageManager)

            // 计算应用大小
            val size = calculateAppSize(applicationInfo)

            // 获取安装时间
            val installTime = getInstallTime(packageInfo)

            return AppInfo(
                packageName = packageName,
                name = appName,
                icon = icon,
                size = size,
                firstInstallTime = installTime
            )
        } catch (e: Exception) {
            return null
        }
    }

    // 计算应用大小
    private fun calculateAppSize(applicationInfo: ApplicationInfo): Long {
        return try {
            val appFile = File(applicationInfo.publicSourceDir)
            appFile.length()
        } catch (e: Exception) {

            0L
        }
    }

    // 获取安装时间
    private fun getInstallTime(packageInfo: PackageInfo): Long {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            packageInfo.firstInstallTime
        } else {
            @Suppress("DEPRECATION")
            packageInfo.firstInstallTime
        }
    }
}