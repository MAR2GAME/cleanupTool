package com.mycleaner.phonecleantool.service

import android.Manifest
import android.R.attr.tag
import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.graphics.BitmapFactory
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.mycleaner.phonecleantool.R
import com.mycleaner.phonecleantool.activity.LoadingActivity
import com.mycleaner.phonecleantool.activity.MainActivity
import com.mycleaner.phonecleantool.activity.ScanFilesActivity
import com.mycleaner.phonecleantool.activity.SplashActivity
import com.mycleaner.phonecleantool.bean.MemoryInfo
import com.mycleaner.phonecleantool.command.hasStoragePermission
import com.mycleaner.phonecleantool.service.PersistentNotificationService.Companion.NOTIFICATION_ID
import com.mycleaner.phonecleantool.utils.MemoryUtils
import com.mycleaner.phonecleantool.utils.NotificationChannelManager
import com.mycleaner.phonecleantooll.base.BaseConstant
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable.isActive
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.jvm.java

class PersistentNotificationService : Service() {
    private lateinit var wakeLock: PowerManager.WakeLock

    private val serviceScope = CoroutineScope(Dispatchers.Main)
    private var updateJob: Job? = null
    private lateinit var notificationManager: NotificationManager

    private var batteryLevel = -1

    private var lastMemoryPercent = -1


    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            // 电池状态变化时触发通知更新
            batteryLevel = getBatteryLevel(intent)
            updateNotification()
        }
    }

    companion object {
        const val NOTIFICATION_ID = 1002
    }

    override fun onBind(intent: Intent?): IBinder? = null


    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        var registerReceiver =
            registerReceiver(batteryReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        batteryLevel = getBatteryLevel(registerReceiver)
        lastMemoryPercent = getMemoryInfo().memoryUsage
        // 创建通知渠道
        NotificationChannelManager.createPersistentChannel(this)
        // 获取唤醒锁，防止系统休眠时服务被终止
        acquireWakeLock()
        updateJob = serviceScope.launch {
            while (isActive) {
                val info = withContext(Dispatchers.IO) {
                    MemoryUtils.getMemoryInfo(application)
                }
                if (info.memoryUsage != lastMemoryPercent) {
                    lastMemoryPercent = info.memoryUsage
                    updateNotification()
                }
                delay(60000) // 每5秒更新一次
            }
        }


    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForegroundService()
        return START_STICKY // 关键：服务被杀死后自动重启
    }



    private fun startForegroundService() {
        // 构建通知
        val notification = buildDualActionNotification()

        // 启动前台服务
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    fun updateNotification() {
        val notification = buildDualActionNotification()
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    @SuppressLint("RemoteViewLayout")
    private fun buildDualActionNotification(): Notification {
        val remoteViews = RemoteViews(packageName, R.layout.custom_notification)
        remoteViews.setProgressBar(R.id.ram_progress, 100, lastMemoryPercent, false)
        remoteViews.setProgressBar(R.id.battery_progress, 100, batteryLevel, false)
        // 为每个图标设置点击事件
        setIconClickAction(remoteViews, R.id.icon1, 1,getString(R.string.junk_files))
        setIconClickAction(remoteViews, R.id.icon2, 2,getString(R.string.process_manager))
        setIconClickAction(remoteViews, R.id.icon3, 3,getString(R.string.app_manager))
        setIconClickAction(remoteViews, R.id.icon4, 4,getString(R.string.battery_info))

        val bigRemoteViews = RemoteViews(
            packageName,
            R.layout.big_custom_notification
        )
        bigRemoteViews.setProgressBar(R.id.big_ram_progress, 100, lastMemoryPercent, false)
        bigRemoteViews.setProgressBar(R.id.big_battery_progress, 100, batteryLevel, false)
        bigRemoteViews.setTextViewText(R.id.tv_ram_progress, "${lastMemoryPercent}%")
        bigRemoteViews.setTextViewText(R.id.tv_battery_progress, "${batteryLevel}%")
        setIconClickAction(bigRemoteViews, R.id.big_icon1, 5,getString(R.string.junk_files))
        setIconClickAction(bigRemoteViews, R.id.big_icon2, 6,getString(R.string.process_manager))
        setIconClickAction(bigRemoteViews, R.id.big_icon3, 7,getString(R.string.app_manager))
        setIconClickAction(bigRemoteViews, R.id.big_icon4, 8,getString(R.string.battery_info))

        return NotificationCompat.Builder(this, NotificationChannelManager.PERSISTENT_CHANNEL_ID)
            .setContentText(getString(R.string.app_name))
            .setSmallIcon(R.mipmap.ic_notify)
            .setCustomContentView(remoteViews) // 设置自定义视图
            .setCustomBigContentView(bigRemoteViews)
            .setOngoing(true) // 设置常驻通知
            .setLargeIcon(BitmapFactory.decodeResource(resources, R.mipmap.ic_logo))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setShowWhen(false)
            .setSilent(true)
            .build()
    }

    fun getIntent(tag: String): Intent{
       var  pageIntent = Intent(this, SplashActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        var bundle = Bundle()
         bundle.putString(BaseConstant.NEXT_TAG, tag)
        bundle.putString("referrer_name", "notification")
        bundle.putString("AppOpenFrom", "notification")
        pageIntent.putExtras(bundle)
        return pageIntent
    }

    private fun setIconClickAction(
        remoteViews: RemoteViews,
        viewId: Int,
        requestCode: Int,
        tag:String
    ) {
        when (tag) {
            getString(R.string.process_manager) -> {
                val pendingIntent = PendingIntent.getActivity(
                    this,
                    requestCode,
                    getIntent(getString(R.string.process_manager)),
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
                remoteViews.setOnClickPendingIntent(viewId, pendingIntent)

            }
            getString(R.string.app_manager) -> {

                val pendingIntent = PendingIntent.getActivity(
                    this,
                    requestCode,
                    getIntent(getString(R.string.app_manager)),
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
                remoteViews.setOnClickPendingIntent(viewId, pendingIntent)
            }

            getString(R.string.junk_files) -> {
                val pendingIntent = PendingIntent.getActivity(
                    this,
                    requestCode,
                    getIntent(getString(R.string.junk_files)),
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
                remoteViews.setOnClickPendingIntent(viewId, pendingIntent)
            }

            getString(R.string.battery_info) -> {
                val pendingIntent = PendingIntent.getActivity(
                    this,
                    requestCode,
                    getIntent(getString(R.string.battery_info)),
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
                remoteViews.setOnClickPendingIntent(viewId, pendingIntent)
            }
        }

    }


    fun getMemoryInfo(): MemoryInfo {
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        val totalMemory = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            memoryInfo.totalMem
        } else {
            // 低版本API需要通过反射或其他方式获取，这里简单返回0
            0L
        }
        val availableMemory = memoryInfo.availMem
        val usedMemory = totalMemory - availableMemory
        val memoryUsageRatio = if (totalMemory > 0) {
            usedMemory.toFloat() / totalMemory.toFloat()
        } else {
            0f
        }
        var info = MemoryInfo(
            (memoryUsageRatio * 100).toInt(),
            ""
        )
        return info
    }

    private fun getBatteryLevel(batteryStatus: Intent?): Int {
        val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        if (level != null) {
            if (scale != null) {
                return if (level >= 0 && scale > 0) {
                    (level * 100 / scale.toFloat()).toInt()
                } else {
                    -1
                }
            }
        }

        return 0
    }


    private fun acquireWakeLock() {
        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "DualActionNotificationService::WakeLock"
        )
        wakeLock.acquire(10 * 60 * 1000L /*10 minutes*/)
    }

    override fun onDestroy() {
        super.onDestroy()
        // 释放唤醒锁
        if (::wakeLock.isInitialized && wakeLock.isHeld) {
            wakeLock.release()
        }
        updateJob?.cancel()
        try {
            unregisterReceiver(batteryReceiver)

        } catch (e: IllegalArgumentException) {
            // 忽略未注册接收器的异常
        }
        notificationManager.cancel(NOTIFICATION_ID)
        stopForeground(true)
        stopSelf()
    }
}