package com.mycleaner.phonecleantool.remoteconfig

import android.util.Log
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.mycleaner.phonecleantool.adv.AdvIDs
import com.mycleaner.phonecleantool.command.AppConfig
import com.mycleaner.phonecleantool.command.AppConfig.openAdmobMediation
import com.mycleaner.phonecleantool.command.AppConfig.showAdPlatform


object RemoteConfig {
    private const val TAG = "RemoteConfig"
    fun update(remoteConfig: FirebaseRemoteConfig) {
        AppConfig.ACCESS_FLAG = remoteConfig.getLong("accessflag")
        AppConfig.RATE_FLAG = remoteConfig.getLong("rateflag")
        AppConfig.NOTICE_FLAG = remoteConfig.getLong("noticeflag")
        // 广告设置
        AppConfig.openAdmobMediation = remoteConfig.getBoolean("OpenAdmobMediation")
        AppConfig.showAdPlatform = remoteConfig.getString("ShowAdPlatform")
        // admob相关配置
        val admobBanner = remoteConfig.getString("Admob_Banner")
        val admobInterset = remoteConfig.getString("Admob_Interset")
        val admobNative = remoteConfig.getString("Admob_Native")
        val admobOpen = remoteConfig.getString("Admob_Open")
        AdvIDs.setAdmobIDs(admobBanner, admobInterset, admobNative, admobOpen)
        // max相关配置
        val maxInterset = remoteConfig.getString("Max_Interset")
        val maxBanner = remoteConfig.getString("Max_Banner")
        val maxOpen = remoteConfig.getString("Max_Open")

        AdvIDs.setMaxIDs(maxInterset, maxBanner, maxOpen)

    }


}