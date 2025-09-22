package com.mycleaner.phonecleantool.bean

import com.cloud.cleanjunksdk.task.Clean

data class CleanSdkEvent(var isInit: Boolean,var sdkClean: Clean?) {
}