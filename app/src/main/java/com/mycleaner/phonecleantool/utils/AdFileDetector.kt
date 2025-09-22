package com.mycleaner.phonecleantool.utils

import android.content.Context
import android.os.Build
import android.os.Environment
import java.io.File
import java.util.regex.Pattern

/**
 * 广告文件检测器
 */
class AdFileDetector(private val context: Context)  {

    // 常见的广告文件扩展名
    private val adFileExtensions = setOf(
        ".adcache", ".adc", ".ads", ".ad", ".advert",
        ".adtmp", ".adtemp", ".adcachetmp",
        ".addata", ".addb", ".adjson", ".adxml",
        ".adbanner", ".adimg", ".adpic", ".adthumbnail",
        ".advideo", ".admedia", ".admp4", ".adbin"
    )

    // 广告文件常见名称模式（正则表达式）
    private val adFileNamePatterns = listOf(
        Pattern.compile("advertisement", Pattern.CASE_INSENSITIVE),
        Pattern.compile("ad_", Pattern.CASE_INSENSITIVE),
        Pattern.compile("ads?", Pattern.CASE_INSENSITIVE),
        Pattern.compile("advert", Pattern.CASE_INSENSITIVE),
        Pattern.compile("banner", Pattern.CASE_INSENSITIVE),
        Pattern.compile("promotion", Pattern.CASE_INSENSITIVE),
        Pattern.compile("sponsor", Pattern.CASE_INSENSITIVE),
        Pattern.compile("adcache", Pattern.CASE_INSENSITIVE),
        Pattern.compile("ad_data", Pattern.CASE_INSENSITIVE),
        Pattern.compile("adservice", Pattern.CASE_INSENSITIVE)
    )

    // 常见的广告目录路径模式
    private val adDirectoryPatterns = listOf(
        Pattern.compile("/ad/", Pattern.CASE_INSENSITIVE),
        Pattern.compile("/ads/", Pattern.CASE_INSENSITIVE),
        Pattern.compile("/adcache/", Pattern.CASE_INSENSITIVE),
        Pattern.compile("/advertisement/", Pattern.CASE_INSENSITIVE),
        Pattern.compile("/ad_data/", Pattern.CASE_INSENSITIVE),
        Pattern.compile("/adservice/", Pattern.CASE_INSENSITIVE)
    )

    // 已知的广告SDK包名（用于判断目录）
    private val adSdkPackageNames = setOf(
        "com.google.ads",
        "com.facebook.ads",
        "com.unity3d.ads",
        "com.vungle.publisher",
        "com.mopub.mobileads",
        "com.chartboost.sdk",
        "com.adcolony.sdk",
        "com.applovin",
        "com.ironsource",
        "com.tapjoy",
        "com.inmobi",
        "com.admob"
    )

    // 百度相关缓存目录
    private val baiduPatterns = listOf(
        Pattern.compile("/baidu/", Pattern.CASE_INSENSITIVE),
        Pattern.compile("/Baidu/", Pattern.CASE_INSENSITIVE)
    )

    /**
     * 判断文件是否为广告垃圾文件
     */
    fun isAdFile(file: File): Boolean {
        // 检查文件扩展名
        if (hasAdExtension(file)) {
            return true
        }

        // 检查文件名
        if (hasAdFileName(file)) {
            return true
        }

        // 检查文件路径是否包含广告目录
        if (isInAdDirectory(file)) {
            return true
        }

        // 检查文件是否属于已知广告SDK
        if (isFromAdSdk(file)) {
            return true
        }

        // 检查是否是百度相关缓存
        if (isBaiduCache(file)) {
            return true
        }

        return false
    }

    /**
     * 检查文件扩展名是否为广告文件扩展名
     */
    private fun hasAdExtension(file: File): Boolean {
        val fileName = file.name.lowercase()
        return adFileExtensions.any { fileName.endsWith(it) }
    }

    /**
     * 检查文件名是否匹配广告文件模式
     */
    private fun hasAdFileName(file: File): Boolean {
        val fileName = file.name
        return adFileNamePatterns.any { pattern ->
            pattern.matcher(fileName).find()
        }
    }

    /**
     * 检查文件是否位于广告目录中
     */
    private fun isInAdDirectory(file: File): Boolean {
        val path = file.absolutePath.lowercase()
        return adDirectoryPatterns.any { pattern ->
            pattern.matcher(path).find()
        }
    }

    /**
     * 检查文件是否来自已知的广告SDK
     */
    private fun isFromAdSdk(file: File): Boolean {
        val path = file.absolutePath

        // 检查路径是否包含广告SDK包名
        return adSdkPackageNames.any { packageName ->
            path.contains("/$packageName/") ||
                    path.contains("/${packageName.replace('.', '/')}/")
        }
    }

    /**
     * 检查是否是百度相关缓存
     */
    private fun isBaiduCache(file: File): Boolean {
        val path = file.absolutePath.lowercase()
        return baiduPatterns.any { pattern ->
            pattern.matcher(path).find()
        } && (path.contains("/cache/") || path.contains("/temp/"))
    }

    /**
     * 获取所有可扫描的存储目录（适配所有Android版本）
     */
    fun getStorageDirectories(): List<File> {
        val directories = mutableListOf<File>()

        // 内部存储
        context.getExternalFilesDir(null)?.parentFile?.let {
            directories.add(it)
        }

        // 外部存储 (SD卡等)
        val externalDirs = context.getExternalFilesDirs(null)
        externalDirs.forEach { dir ->
            dir?.parentFile?.let {
                if (!directories.contains(it)) {
                    directories.add(it)
                }
            }
        }

        // 对于Android 10+，尝试获取更多目录
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                Environment.getExternalStorageDirectory()?.let {
                    if (!directories.contains(it)) {
                        directories.add(it)
                    }
                }

                // 公共目录
                listOf(
                    Environment.DIRECTORY_DOWNLOADS,
                    Environment.DIRECTORY_DCIM,
                    Environment.DIRECTORY_PICTURES,
                    Environment.DIRECTORY_MOVIES,
                    Environment.DIRECTORY_MUSIC
                ).forEach { dirType ->
                    Environment.getExternalStoragePublicDirectory(dirType)?.let {
                        if (!directories.contains(it)) {
                            directories.add(it)
                        }
                    }
                }
            } catch (e: Exception) {
                // 忽略权限错误
            }
        }

        return directories.distinct().filter { it.exists() && it.isDirectory }
    }

}