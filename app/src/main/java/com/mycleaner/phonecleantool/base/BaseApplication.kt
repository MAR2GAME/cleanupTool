package com.mycleaner.phonecleantool.base

import android.Manifest
import android.app.Activity
import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.os.Process
import android.util.Log
import androidx.annotation.RequiresPermission
import cn.thinkingdata.analytics.TDAnalytics
import cn.thinkingdata.analytics.TDConfig
import com.google.android.gms.ads.MobileAds
import com.google.firebase.analytics.FirebaseAnalytics
import com.mycleaner.phonecleantool.adv.AdvCheckManager
import com.mycleaner.phonecleantool.adv.AdvInit
import com.mycleaner.phonecleantool.command.AppConfig
import com.mycleaner.phonecleantool.push.PushManager
import com.mycleaner.phonecleantool.remoteconfig.RemoteConfigManager
import com.mycleaner.phonecleantool.utils.LogUtil

import com.mycleaner.phonecleantooll.base.BaseConstant
import com.singular.sdk.Singular
import com.singular.sdk.SingularConfig
import com.singular.sdk.SingularDeviceAttributionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.Date


class BaseApplication : Application() {
    var currentActivity: Activity? = null
    private var activityCount = 0
    companion object {
        var instance: Application? = null
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        addListener()
        initConfig()
        RemoteConfigManager.initRemoteConfig()
        AdvInit.initAdv(this)
        initThinkingAnalyticsSDK()
        initFireBase()
        initSingular()


    }

//    private  fun getProcessName(context: Context): String? {
//        val am = context.getSystemService(ACTIVITY_SERVICE) as ActivityManager
//        val runningApps = am.runningAppProcesses ?: return null
//        for (proInfo in runningApps) {
//            if (proInfo.pid == Process.myPid()) {
//                if (proInfo.processName != null) {
//                    return proInfo.processName
//                }
//            }
//        }
//        return null
//    }


    @RequiresPermission(allOf = [Manifest.permission.INTERNET, Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.WAKE_LOCK])
    private fun initFireBase() {
        val mFirebaseAnalytics = FirebaseAnalytics.getInstance(instance!!)
        mFirebaseAnalytics.setAnalyticsCollectionEnabled(true)
    }

    fun initConfig() {
        if (AdvCheckManager.params.isFirstOpen) {
            AdvCheckManager.params.installTime = System.currentTimeMillis()
            AdvCheckManager.params.isFirstOpen = false



        }
    }


    fun addListener() {
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityResumed(activity: Activity) {
                currentActivity = activity
            }

            override fun onActivityPaused(activity: Activity) {
                if (currentActivity == activity) {
                    currentActivity = null
                }
            }

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
            override fun onActivityStarted(activity: Activity) {
                currentActivity = activity
                activityCount++
            }
            override fun onActivityStopped(activity: Activity) {
                activityCount--
                if (activityCount == 0) {

                    PushManager.notifyServerAppExit()
                }
            }
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
            override fun onActivityDestroyed(activity: Activity) {}
        })
    }






    fun initThinkingAnalyticsSDK(){
        val config = TDConfig.getInstance(this, BaseConstant.THINKING_APPID,  BaseConstant.THINKING_URL)
        /*
       设置运行模式为 Debug 模式
       NORMAL模式:数据会存入缓存，并依据一定的缓存策略上报,默认为NORMAL模式；建议在线上环境使用
       Debug模式:数据逐条上报。当出现问题时会以日志和异常的方式提示用户；不建议在线上环境使用
       DebugOnly模式:只对数据做校验，不会入库；不建议在线上环境使用
        */
        config.setMode(if(AppConfig.isDebug) TDConfig.TDMode.DEBUG else TDConfig.TDMode.NORMAL)
        // 初始化 SDK
        TDAnalytics.init(config)

        //开启自动采集事件
        TDAnalytics.enableAutoTrack(
            TDAnalytics.TDAutoTrackEventType.APP_START or
                    TDAnalytics.TDAutoTrackEventType.APP_END or
                    TDAnalytics.TDAutoTrackEventType.APP_INSTALL or
                    TDAnalytics.TDAutoTrackEventType.APP_VIEW_SCREEN or
                    TDAnalytics.TDAutoTrackEventType.APP_CLICK or
                    TDAnalytics.TDAutoTrackEventType.APP_CRASH
        )
        //打印SDK日志
        TDAnalytics.enableLog(if(AppConfig.isDebug) true else false);
    }



    private fun initSingular() {
        val config = SingularConfig(AppConfig.Singular_Api_Key, AppConfig.Singular_Secret)
            .withLoggingEnabled()
            .withLogLevel(1)
            .withSingularDeviceAttribution(object : SingularDeviceAttributionHandler {
                override fun onDeviceAttributionInfoReceived(attributionData: Map<String, Any>) {
                    val promoteParams = JSONObject()
                    try {
                        val network = attributionData["network"]?.toString() ?: ""
                        promoteParams.put("network", network)
                        promoteParams.put("campaign_id", attributionData["campaign_id"].toString())
                        promoteParams.put("campaign_name", attributionData["campaign_name"].toString())
                        promoteParams.put("passthrough", attributionData["passthrough"].toString())
                        promoteParams.put("match_type", attributionData["match_type"].toString())
                        promoteParams.put("click_timestamp", attributionData["click_timestamp"].toString())
                        val isNatural = network.equals("organic", ignoreCase = true) || network.isEmpty()
                        AdvCheckManager.params.fromNature = isNatural
                        promoteParams.put("fromNature", isNatural)
                        TDAnalytics.setSuperProperties(promoteParams)
                    } catch (e: JSONException) {
                        // 处理异常

                    }
                }
            })
        Singular.init(instance, config)
    }
//    private fun initUncaughtExceptionHandler() {
//        Thread.setDefaultUncaughtExceptionHandler(Thread.UncaughtExceptionHandler { thread: Thread?, throwable: Throwable? ->
//            // 在这里处理异常，比如记录日志、发送崩溃报告等
//            // 然后可以选择结束当前线程，或者让应用继续运行
//            startActivity(Intent(this, MainActivity::class.java))
//        })
//    }

}