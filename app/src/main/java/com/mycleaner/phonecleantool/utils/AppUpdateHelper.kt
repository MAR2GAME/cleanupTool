package com.mycleaner.phonecleantool.utils

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.View
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.ktx.isFlexibleUpdateAllowed
import com.google.android.play.core.ktx.isImmediateUpdateAllowed
import com.mycleaner.phonecleantool.R


class AppUpdateHelper(private val context: Context) {

    private val appUpdateManager: AppUpdateManager by lazy {
        AppUpdateManagerFactory.create(context)
    }

    private var installStateUpdatedListener: InstallStateUpdatedListener? = null

    companion object {
        private const val TAG = "AppUpdateHelper"
        const val UPDATE_REQUEST_CODE = 1234
    }

    /**
     * 检查应用更新
     * @param activity 当前Activity
     * @param updateType 更新类型 (FLEXIBLE 或 IMMEDIATE)
     */
    fun checkForUpdate(activity: Activity, updateType: Int = AppUpdateType.FLEXIBLE) {
        // 获取应用更新信息
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo

        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            when {
                appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE -> {
                    when (updateType) {
                        AppUpdateType.FLEXIBLE -> {
                            if (appUpdateInfo.isFlexibleUpdateAllowed) {
                                startFlexibleUpdate(activity, appUpdateInfo)
                            } else {
                                Log.w(TAG, "Flexible update not allowed")
                            }
                        }
                        AppUpdateType.IMMEDIATE -> {
                            if (appUpdateInfo.isImmediateUpdateAllowed) {
                                startImmediateUpdate(activity, appUpdateInfo)
                            } else {
                                Log.w(TAG, "Immediate update not allowed")
                            }
                        }
                    }
                }
                appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS -> {
                    // 如果更新已经开始但未完成，继续更新流程
                    if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                        showCompletionSnackbar(activity)
                    }
                }
                else -> {
                    Log.d(TAG, "No update available or unknown status")
                }
            }
        }

        appUpdateInfoTask.addOnFailureListener { exception ->

        }
    }

    /**
     * 启动灵活更新
     */
    private fun startFlexibleUpdate(activity: Activity, appUpdateInfo: AppUpdateInfo) {
        // 注册安装状态监听器
        installStateUpdatedListener = InstallStateUpdatedListener { state ->
            when (state.installStatus()) {
                InstallStatus.DOWNLOADED -> {
                    // 更新已下载完成，显示提示
                    showCompletionSnackbar(activity)
                }
                InstallStatus.INSTALLED -> {
                    // 更新已安装，移除监听器
                    unregisterListener()
                }
                else -> {
                    Log.d(TAG, "Install state updated: ${state.installStatus()}")
                }
            }
        }

        appUpdateManager.registerListener(installStateUpdatedListener!!)

        // 启动灵活更新流程
        appUpdateManager.startUpdateFlowForResult(
            appUpdateInfo,
            AppUpdateType.FLEXIBLE,
            activity,
            UPDATE_REQUEST_CODE
        )
    }

    /**
     * 启动即时更新
     */
    private fun startImmediateUpdate(activity: Activity, appUpdateInfo: AppUpdateInfo) {
        appUpdateManager.startUpdateFlowForResult(
            appUpdateInfo,
            AppUpdateType.IMMEDIATE,
            activity,
            UPDATE_REQUEST_CODE
        )
    }

    /**
     * 显示完成更新的Snackbar提示
     */
    private fun showCompletionSnackbar(activity: Activity) {
        val rootView = activity.window.decorView.findViewById<View>(android.R.id.content)
        Snackbar.make(rootView, activity.getString(R.string.update_hint), Snackbar.LENGTH_INDEFINITE)
            .setAction(activity.getString(R.string.update)) {
                completeUpdate()
            }
            .show()
    }

    /**
     * 完成更新安装
     */
    fun completeUpdate() {
        appUpdateManager.completeUpdate()
    }

    /**
     * 在Activity的onResume中调用，检查更新状态
     */
    fun onResume(activity: Activity) {
        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                showCompletionSnackbar(activity)
            }

            // 对于即时更新，如果更新流程被中断，需要恢复
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                startImmediateUpdate(activity, appUpdateInfo)
            }
        }
    }

    /**
     * 取消注册监听器
     */
    fun unregisterListener() {
        installStateUpdatedListener?.let {
            appUpdateManager.unregisterListener(it)
        }
    }
}