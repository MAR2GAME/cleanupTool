package com.mycleaner.phonecleantool.bean

data class AdFile(
    val name: String,
    val path: String,
    val size: Long,
    val packageName: String? = null,
    var isSelected: Boolean = false
)