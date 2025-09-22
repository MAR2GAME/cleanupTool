package com.mycleaner.phonecleantool.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.applovin.mediation.ads.MaxAdView
import com.bumptech.glide.Glide
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder

import com.cloud.cleanjunksdk.task.CheckSdkCallback
import com.cloud.cleanjunksdk.task.Clean
import com.cloud.cleanjunksdk.task.CleanSDK
import com.cloud.cleanjunksdk.tools.Region
import com.google.android.gms.ads.AdView
import com.google.android.play.core.install.model.AppUpdateType
import com.mycleaner.phonecleantool.R
import com.mycleaner.phonecleantool.adv.AdvCheckManager
import com.mycleaner.phonecleantool.adv.AreaKey
import com.mycleaner.phonecleantool.base.BaseApplication
import com.mycleaner.phonecleantool.base.activity.BaseMvvmActivity
import com.mycleaner.phonecleantool.bean.BannerEvent
import com.mycleaner.phonecleantool.bean.CleanFilesBean
import com.mycleaner.phonecleantool.bean.CleanSdkEvent
import com.mycleaner.phonecleantool.bean.FunctionItemBean
import com.mycleaner.phonecleantool.command.AppConfig
import com.mycleaner.phonecleantool.command.REQUEST_CODE_MANAGE_STORAGE
import com.mycleaner.phonecleantool.command.REQUEST_CODE_NOTIFICATION
import com.mycleaner.phonecleantool.command.REQUEST_CODE_STORAGE_PERMISSION
import com.mycleaner.phonecleantool.command.checkAndRequestPermissions
import com.mycleaner.phonecleantool.command.checkNotificationPermission
import com.mycleaner.phonecleantool.command.hasStoragePermission
import com.mycleaner.phonecleantool.command.openAppInfoSettings
import com.mycleaner.phonecleantool.command.readyGo
import com.mycleaner.phonecleantool.command.safeClick
import com.mycleaner.phonecleantool.databinding.ActivityMainBinding
import com.mycleaner.phonecleantool.service.PersistentNotificationService
import com.mycleaner.phonecleantool.utils.ActivityManagerUtils

import com.mycleaner.phonecleantool.utils.AppPrefsUtils
import com.mycleaner.phonecleantool.utils.AppUpdateHelper
import com.mycleaner.phonecleantool.utils.LogUtil
import com.mycleaner.phonecleantool.utils.NotificationChannelManager
import com.mycleaner.phonecleantool.utils.SizeUtil
import com.mycleaner.phonecleantool.view.NotificationDialog
import com.mycleaner.phonecleantool.view.RequestPermissionDialog
import com.mycleaner.phonecleantool.viewmodel.MainViewModel
import com.mycleaner.phonecleantooll.base.BaseConstant

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.concurrent.TimeUnit
import kotlin.jvm.java


class MainActivity : BaseMvvmActivity<ActivityMainBinding, MainViewModel>() {
    var requestPermissionDialog: RequestPermissionDialog? = null
    var notificationDialog: NotificationDialog? = null

    private lateinit var appUpdateHelper: AppUpdateHelper

    //var isToScan=false

    var toTag = ""


    var isAnimation = false
    var advView:ViewGroup?=null

    private lateinit var functionAdapter: BaseQuickAdapter<FunctionItemBean, BaseViewHolder>


    override fun init() {
        EventBus.getDefault().register(this)
        initNotification()
        if(AdvCheckManager.params.limitTime < System.currentTimeMillis()){
            advView = getBannerAd(this, AreaKey.openMainBottomAdv)
            binding.flAd.visibility= View.VISIBLE
            binding.flAd.addView(advView)
        }
        appUpdateHelper = AppUpdateHelper(this)
        binding.ivScanBg.setImageAssetsFolder("images/")
        initAdapter()
        viewModel.functionItemData.observe(this) {
            functionAdapter.setList(it)
        }
        viewModel.getFunctionBeans(
            arrayOf(
                getString(R.string.process_manager), getString(R.string.app_manager),
                getString(R.string.speaker_cleaner), getString(R.string.battery_info),
                getString(R.string.large_file_cleaner), getString(R.string.screenshot_manager)
            )
        )
        initCleanSDK()
        binding.progressBar.setProgressColor(this@MainActivity.getColor(R.color.btn_nor))
        binding.btClean.safeClick {
            LogUtil.log("click_btn_clean", mapOf())
            LogUtil.log("enter_clean", mapOf("referrer_name" to "homepage"))
            toScanActivity()
        }
        binding.ivSetting.safeClick {
            readyGo(SettingActivity::class.java)
        }
        // 应用启动时检查更新
        checkUpdateOnStart()
        viewModel.infoData.observe(this) {
            if (it.usagePercentage > 0) {
                binding.tvMemory.text =
                    SizeUtil.formatSize3(it.usedSpace) + "/" + SizeUtil.formatSize3(it.totalSpace)
                if (!isAnimation) {
                    isAnimation = true
                    lifecycleScope.launch {
                        animateProgressBar(it.usagePercentage)
                    }
                } else {
                    binding.progressBar.setProgress(it.usagePercentage)
                    binding.tvUsagePercentage.text = "${it.usagePercentage.toInt()}%"
                }

            }
        }
        viewModel.getStorageInfo()
        LogUtil.setUser(mapOf("total_open_num" to 1) ,"user_add")
    }

    fun toScanActivity() {
        if (!hasStoragePermission()) {
            if (requestPermissionDialog == null) {
                requestPermissionDialog = RequestPermissionDialog(this)
                requestPermissionDialog!!.setmOnClickListener(object :
                    RequestPermissionDialog.onClickListener {
                    override fun onAllow() {
                        toTag = getString(R.string.junk_files)
                        checkAndRequestPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, {
                            lifecycleScope.launch {
                                delay(200)
                                readyGo(TransparentActivity::class.java)
                            }
                        })
                    }
                })
            }
            requestPermissionDialog!!.toShow()
        } else {
            EventBus.getDefault().postSticky(CleanSdkEvent(isInitSuccess, sdkClean))
            readyGo(ScanFilesActivity::class.java)
        }
    }

    override fun onResume() {
        super.onResume()
        getInfo()
        appUpdateHelper.onResume(this)


    }

    override fun onPause() {
        super.onPause()
        // 暂停时停止更新以节省资源
        if (binding.ivScanBg.isAnimating) {
            binding.ivScanBg.pauseAnimation()
        }

    }

    private fun checkUpdateOnStart() {
        // 可以根据需要决定是否在启动时检查更新
        // 例如，可以每24小时检查一次，避免频繁请求
        var lastCheckTime = AppPrefsUtils.getLong(BaseConstant.LAST_CHECK_UPDATE_TIME)
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastCheckTime > 24 * 60 * 60 * 1000) { // 24小时
            appUpdateHelper.checkForUpdate(this, AppUpdateType.FLEXIBLE)
            AppPrefsUtils.putLong(BaseConstant.LAST_CHECK_UPDATE_TIME, currentTime)
        }
    }

    fun getInfo() {
        var junkTime = AppPrefsUtils.getLong(BaseConstant.JUKE_FILE_TIME)
        var isShowBlue = !(junkTime == 0L || System.currentTimeMillis() - junkTime > 86400000)
        var processTime = AppPrefsUtils.getLong(BaseConstant.PROCESS_TIME)
        var appTime = AppPrefsUtils.getLong(BaseConstant.APP_TIME)
        var speakerTime = AppPrefsUtils.getLong(BaseConstant.SPEAKER_TIME)
        var batteryTime = AppPrefsUtils.getLong(BaseConstant.BATTERY_TIME)
        var large_file_time = AppPrefsUtils.getLong(BaseConstant.LARGE_FILE_TIME)
        var screenshot_time = AppPrefsUtils.getLong(BaseConstant.SCREENSHOT_TIME)
        var currentTime = System.currentTimeMillis()
        functionAdapter.data[0].isCheck =
            (processTime == 0L || currentTime - processTime > 86400000)
        functionAdapter.data[1].isCheck =
            (appTime == 0L || currentTime - appTime > 86400000)
        functionAdapter.data[2].isCheck =
            (speakerTime == 0L || currentTime - speakerTime > 86400000)
        functionAdapter.data[3].isCheck =
            (batteryTime == 0L || currentTime - batteryTime > 86400000)
        functionAdapter.data[4].isCheck =
            (large_file_time == 0L || currentTime - large_file_time > 86400000)
        functionAdapter.data[5].isCheck =
            (screenshot_time == 0L || currentTime - screenshot_time > 86400000)
        functionAdapter.notifyDataSetChanged()
        if (isShowBlue) {
            binding.btClean.background = getDrawable(R.drawable.btn_selector)
            binding.progressBar.setProgressColor(getColor(R.color.btn_nor))
            binding.ivScanBg.setAnimation(R.raw.bubble_blue)
            binding.ivScanBg.playAnimation()
        } else {
            binding.btClean.background = getDrawable(R.drawable.btn_stop_selector)
            binding.progressBar.setProgressColor(getColor(R.color.to_do))
            binding.ivScanBg.setAnimation(R.raw.bubblie_red)
            binding.ivScanBg.playAnimation()
        }
    }

    fun initNotification() {
        if (checkNotificationPermission()) {
            startPersistentService()
        } else {
            if (AppConfig.NOTICE_FLAG != 1L) {
                var time = AppPrefsUtils.getLong(BaseConstant.SHOW_NOTIFICATION_DIALOG_TIME)
                if (time == 0L || System.currentTimeMillis() - time > 86400000) {
                    when (AppConfig.NOTICE_FLAG) {
                        2L -> {
                            AppPrefsUtils.putLong(
                                BaseConstant.SHOW_NOTIFICATION_DIALOG_TIME,
                                System.currentTimeMillis()
                            )
                            requestPermissions(
                                arrayOf<String>(
                                    Manifest.permission.POST_NOTIFICATIONS
                                ),
                                REQUEST_CODE_NOTIFICATION
                            )
                        }

                        3L -> {
                            if (AppPrefsUtils.getLong(BaseConstant.JUKE_FILE_TIME) != 0L) {
                                AppPrefsUtils.putLong(
                                    BaseConstant.SHOW_NOTIFICATION_DIALOG_TIME,
                                    System.currentTimeMillis()
                                )
                                requestPermissions(
                                    arrayOf<String>(
                                        Manifest.permission.POST_NOTIFICATIONS
                                    ),
                                    REQUEST_CODE_NOTIFICATION
                                )
                            }
                        }
                    }
                }
            }
        }

    }

    fun initAdapter() {
        functionAdapter =
            object : BaseQuickAdapter<FunctionItemBean, BaseViewHolder>(R.layout.item_function) {
                override fun convert(
                    holder: BaseViewHolder,
                    item: FunctionItemBean
                ) {
                    item.let {
                        holder.setText(R.id.tv_function, it.itemName)
                        Glide.with(context).load(it.res)
                            .into(holder.getView<ImageView>(R.id.iv_function))

                        if (item.isCheck) {
                            holder.getView<ImageView>(R.id.iv_check).visibility = View.VISIBLE
                        } else {
                            holder.getView<ImageView>(R.id.iv_check).visibility = View.GONE
                        }
                        holder.getView<RelativeLayout>(R.id.rl_function).safeClick {
                            val bundle = Bundle()
                            when (item.itemName) {
                                getString(R.string.process_manager) -> {
                                    toProcess()
                                    LogUtil.log("click_btn_processman", mapOf())
                                }
                                getString(R.string.app_manager) -> {
                                    bundle.putString(
                                        BaseConstant.NEXT_TAG,
                                        getString(R.string.app_manager)
                                    )
                                    readyGo(LoadingActivity::class.java, bundle)
                                    LogUtil.log("click_btn_appman", mapOf())
                                }

                                getString(R.string.speaker_cleaner) -> {
                                    bundle.putString(
                                        BaseConstant.NEXT_TAG,
                                        getString(R.string.speaker_cleaner)
                                    )
                                    readyGo(LoadingActivity::class.java, bundle)
                                    LogUtil.log("click_btn_speaker", mapOf())
                                }

                                getString(R.string.battery_info) -> {
                                    bundle.putString(
                                        BaseConstant.NEXT_TAG,
                                        getString(R.string.battery_info)
                                    )
                                    readyGo(LoadingActivity::class.java, bundle)
                                    LogUtil.log("click_btn_battery", mapOf())
                                }
                                getString(R.string.large_file_cleaner) -> {
                                    toLargeFile(bundle)
                                    LogUtil.log("click_btn_largefile", mapOf())
                                }

                                getString(R.string.screenshot_manager) -> {
                                    toScreenshotManager(bundle)
                                    LogUtil.log("click_btn_screenshotman", mapOf())
                                }


                            }
                        }
                    }
                }
            }
        binding.rcItem.adapter = functionAdapter
        binding.rcItem.layoutManager = GridLayoutManager(this@MainActivity, 2)
    }


    fun toLargeFile(bundle: Bundle) {
        if (!hasStoragePermission()) {
            toTag = getString(R.string.large_file_cleaner)
            checkAndRequestPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, {
                lifecycleScope.launch {
                    delay(200)
                    readyGo(TransparentActivity::class.java)
                }
            })
        } else {
            bundle.putString(
                BaseConstant.NEXT_TAG,
                getString(R.string.large_file_cleaner)
            )
            readyGo(LoadingActivity::class.java, bundle)
        }
    }

    fun toScreenshotManager(bundle: Bundle) {
        if (!hasStoragePermission()) {
            toTag = getString(R.string.screenshot_manager)
            checkAndRequestPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, {
                lifecycleScope.launch {
                    delay(200)
                    readyGo(TransparentActivity::class.java)
                }
            })
        } else {
            bundle.putString(
                BaseConstant.NEXT_TAG,
                getString(R.string.screenshot_manager)
            )
            readyGo(LoadingActivity::class.java, bundle)
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            REQUEST_CODE_MANAGE_STORAGE -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && Environment.isExternalStorageManager()) {
                    onGetPermission()
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_CODE_STORAGE_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    onGetPermission()
                }
            }

            REQUEST_CODE_NOTIFICATION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    LogUtil.log("agree_push_permission", mapOf())
                    startPersistentService()
                } else {
                    if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                        // 用户拒绝但未选择"不再询问"
                        Toast.makeText(
                            this,
                            getString(R.string.openNotify_hint),
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    } else {
                        // 用户选择"不再询问"或系统策略禁止该权限
                        if (notificationDialog == null) {
                            notificationDialog = NotificationDialog(this@MainActivity)
                            notificationDialog!!.setmOnClickListener(object :
                                NotificationDialog.onClickListener {
                                override fun onAllow() {
                                    openAppInfoSettings(packageName)
                                }
                            })
                        }
                        notificationDialog!!.toShow()
                    }
                }
            }

        }
    }

    fun onGetPermission() {
        LogUtil.log("agree_filemanage_permission", mapOf())
        when (toTag) {
            getString(R.string.junk_files) -> {
                EventBus.getDefault().postSticky(CleanSdkEvent(isInitSuccess, sdkClean))
                lifecycleScope.launch {
                    delay(200)
                    readyGo(ScanFilesActivity::class.java)
                }
            }
            else -> {
                lifecycleScope.launch {
                    delay(200)
                    val bundle = Bundle()
                    bundle.putString(
                        BaseConstant.NEXT_TAG,
                        toTag
                    )
                    readyGo(LoadingActivity::class.java, bundle)
                }
            }
        }
    }

    var isInitSuccess = false
    var sdkClean: Clean? = null
    fun initCleanSDK() {
        CleanSDK.init(
            BaseApplication.instance,
            this@MainActivity,
            Region.INTL,
            object : CheckSdkCallback {
                override fun onSuccess(clean: Clean?) {
                    isInitSuccess = true
                    sdkClean = clean
                    sdkClean!!.timeout(100000)
                }

                override fun onError(errCode: Int) {
                    isInitSuccess = false
                    sdkClean = null
                }
            }) //initialize SDK
    }


    private suspend fun animateProgressBar(targetPercentage: Float) {
        val animationDuration = 1500L // 动画时长1.5秒
        val steps = 50 // 动画步骤数
        val delayPerStep = animationDuration / steps

        // 计算每一步的增量
        val increment = targetPercentage / steps
        // 从0开始逐步增加进度
        for (i in 1..steps) {
            val progress = i * increment
            binding.progressBar.setProgress(progress)
            binding.tvUsagePercentage.text = "${progress.toInt()}%"
            // 延迟一段时间
            delay(delayPerStep)
        }
        // 确保最终显示正确的百分比
        binding.progressBar.setProgress(targetPercentage)
        binding.tvUsagePercentage.text = "${targetPercentage.toInt()}%"
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: String) {
        when (event) {
            getString(R.string.junk_files) -> {
                toScanActivity()
            }

            getString(R.string.process_manager) -> {
                toProcess()
            }

            getString(R.string.large_file_cleaner) -> {
                toLargeFile(Bundle())
            }
            getString(R.string.screenshot_manager) -> {
                toScreenshotManager(Bundle())
            }
        }
    }

    fun toProcess() {
        var bundle = Bundle()
        bundle.putString(
            BaseConstant.NEXT_TAG,
            getString(R.string.process_manager)
        )
        readyGo(LoadingActivity::class.java, bundle)
    }

    private fun startPersistentService() {
        val serviceIntent = Intent(this, PersistentNotificationService::class.java)
        ContextCompat.startForegroundService(this, serviceIntent)
    }

    @Subscribe( threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: BannerEvent) {
        if(binding.flAd.visibility==View.VISIBLE){
            binding.flAd.removeView(advView)
            binding.flAd.visibility=View.GONE
            adViewSet.clear()
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
        var time=System.currentTimeMillis()
        LogUtil.setUser(mapOf("latest_open_time" to time),"user_set")
        LogUtil.setUser(mapOf("has_notification_permission" to checkNotificationPermission()),"user_set")
        LogUtil.setUser(mapOf("has_allfile_permission" to hasStoragePermission()),"user_set")
        LogUtil.setUser(mapOf("total_use_time" to time-AppPrefsUtils.getLong(BaseConstant.START_TIME)),"user_add")

        ActivityManagerUtils.finishAll()
        EventBus.getDefault().unregister(this)
        binding.ivScanBg.cancelAnimation()
        binding.ivScanBg.clearAnimation()
        appUpdateHelper.unregisterListener()
        if (sdkClean != null) {
            sdkClean!!.cancel() //Call this to release resources when destroy the activity
        }
        if (requestPermissionDialog != null) {
            requestPermissionDialog!!.relese()
            requestPermissionDialog = null
        }

        if (notificationDialog != null) {
            notificationDialog!!.relese()
            notificationDialog = null
        }
    }
}