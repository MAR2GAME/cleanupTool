package com.mycleaner.phonecleantool.bean

import android.graphics.drawable.Drawable


data class AppInfo(
    val packageName: String,
    val name: String,
    val icon: Drawable?,
    val size: Long, // 单位：字节
    val firstInstallTime: Long,
    var isInstalled: Boolean = true
)

// 排序规则枚举
enum class SortRule {
    SIZE_DESC, // 大小降序
    SIZE_ASC,  // 大小升序
    INSTALL_TIME_DESC, // 安装时间降序（最新安装的在前）
    INSTALL_TIME_ASC,   // 安装时间升序（最早安装的在前）
}