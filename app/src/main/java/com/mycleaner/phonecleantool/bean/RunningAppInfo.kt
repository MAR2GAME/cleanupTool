package com.mycleaner.phonecleantool.bean

import android.graphics.drawable.Drawable

data class RunningAppInfo(
    val packageName: String,
    val appName: String,
    val icon: Drawable?,
)