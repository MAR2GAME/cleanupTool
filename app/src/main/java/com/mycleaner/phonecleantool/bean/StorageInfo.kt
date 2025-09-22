package com.mycleaner.phonecleantool.bean

data class StorageInfo(
    var totalSpace: Long,
    var usedSpace: Long,
    var usagePercentage: Float
)