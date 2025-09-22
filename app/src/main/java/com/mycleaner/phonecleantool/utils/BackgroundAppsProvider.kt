package com.mycleaner.phonecleantool.utils

import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import com.mycleaner.phonecleantool.bean.AppInfo
import com.mycleaner.phonecleantool.bean.RunningAppInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

import java.util.concurrent.TimeUnit

class BackgroundAppsProvider(private val context: Context) {


    // 获取非系统应用列表（多种方法结合）
    suspend fun getRunningApps(): List<RunningAppInfo> = withContext(Dispatchers.IO) {
        val packageManager = context.packageManager
        val currentAppPackage = context.packageName
        // 方法1: 查询可启动应用
        val launcherApps = getLauncherApps(packageManager, currentAppPackage)
        // 方法2: 查询特定类型的应用（如浏览器、地图等）
        val specificTypeApps = getSpecificTypeApps(packageManager, currentAppPackage)
        // 合并结果并去重
        (launcherApps + specificTypeApps)
            .distinctBy { it.packageName }
            .sortedBy { it.appName }

    }

    // 获取启动器中的应用
    private fun getLauncherApps(
        packageManager: PackageManager,
        currentAppPackage: String
    ): List<RunningAppInfo> {
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
    private fun getSpecificTypeApps(
        packageManager: PackageManager,
        currentAppPackage: String
    ): List<RunningAppInfo> {
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
    private fun createAppInfo(
        packageManager: PackageManager,
        packageName: String,
        currentAppPackage: String
    ): RunningAppInfo? {
        if (packageName == currentAppPackage) return null

        try {
            val applicationInfo = packageManager.getApplicationInfo(packageName, 0)
            // 排除系统应用
            if ((applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0 ||
                (applicationInfo.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0 || applicationInfo.packageName == context.packageName
                || (applicationInfo.flags and ApplicationInfo.FLAG_STOPPED) != 0
            ) {
                return null
            }
//            // 获取包信息以获取安装时间
//            val packageInfo = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
//                packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
//            } else {
//                @Suppress("DEPRECATION")
//                packageManager.getPackageInfo(packageName, 0)
//            }

            val appName = applicationInfo.loadLabel(packageManager).toString()
            val icon = applicationInfo.loadIcon(packageManager)
            return RunningAppInfo(
                packageName = packageName,
                appName = appName,
                icon = icon,
            )
        } catch (e: Exception) {
            return null
        }
    }


//
//    suspend fun getRunningBackgroundApps(): List<RunningAppInfo> {
//        val runningApps = mutableListOf<RunningAppInfo>()
//        val packageManager = context.packageManager
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            runningApps.addAll(getAppsFromUsageStats(packageManager, context))
//        }
//
//        // 按应用名称排序并返回
//        return runningApps.sortedBy { it.appName }
//    }
//
//    /**
//     * 使用UsageStatsManager获取运行中的应用
//     */
//    @android.annotation.TargetApi(Build.VERSION_CODES.LOLLIPOP)
//    private fun getAppsFromUsageStats(
//        packageManager: PackageManager,
//        context: Context
//    ): List<RunningAppInfo> {
//        val runningApps = mutableListOf<RunningAppInfo>()
//        val usageStatsManager =
//            context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
//        val currentTime = System.currentTimeMillis()
//        // 获取最近一段时间的使用统计
//        val stats = usageStatsManager.queryUsageStats(
//            UsageStatsManager.INTERVAL_BEST,
//            currentTime - TimeUnit.HOURS.toMillis(3),
//            System.currentTimeMillis()
//        )
//        stats.forEach { usageStat ->
//            try {
//                // 只关注最近使用过的应用
//                val appInfo = packageManager.getApplicationInfo(usageStat.packageName, 0)
//                val appName = packageManager.getApplicationLabel(appInfo).toString()
//                val icon = packageManager.getApplicationIcon(appInfo)
//                if (runningApps.none { it.packageName == usageStat.packageName } && usageStat.packageName != context.packageName
//                    && appInfo.flags and ApplicationInfo.FLAG_SYSTEM == 0&& (appInfo.flags and ApplicationInfo.FLAG_STOPPED) == 0) {
//                    //                // 检查应用是否处于停止状态（用户强制停止后）
//                    runningApps.add(RunningAppInfo(usageStat.packageName, appName, icon))
//                }
//            } catch (e: PackageManager.NameNotFoundException) {
//                // 忽略找不到包名的应用
//            }
//        }
//        return runningApps
//    }


}