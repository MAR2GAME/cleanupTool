package com.mycleaner.phonecleantool.activity

import android.R.attr.text
import android.annotation.SuppressLint
import android.text.TextUtils
import android.util.Log
import androidx.activity.OnBackPressedCallback
import androidx.compose.ui.unit.TextUnit
import androidx.lifecycle.lifecycleScope
import com.mycleaner.phonecleantool.R
import com.mycleaner.phonecleantool.base.activity.BaseMvvmActivity
import com.mycleaner.phonecleantool.bean.InstallAppEvent
import com.mycleaner.phonecleantool.bean.LargeFileEvent
import com.mycleaner.phonecleantool.bean.ProcessEvent

import com.mycleaner.phonecleantool.bean.SortRule
import com.mycleaner.phonecleantool.command.readyGoThenKill
import com.mycleaner.phonecleantool.databinding.ActivityLoadingBinding
import com.mycleaner.phonecleantool.utils.LogUtil
import com.mycleaner.phonecleantool.utils.SizeUtil
import com.mycleaner.phonecleantool.viewmodel.LoadingViewModel
import com.mycleaner.phonecleantooll.base.BaseConstant


import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import kotlin.jvm.java

class LoadingActivity : BaseMvvmActivity<ActivityLoadingBinding, LoadingViewModel>() {
    var firstTime: Long = 0
    var lastTime: Long = 0
    override fun init() {
        binding.ivLoading.setImageAssetsFolder("images/")
        initViewmodel()
        val bundle = intent.extras
        val tag = bundle?.getString(BaseConstant.NEXT_TAG)
        var  referrer_name =intent.getStringExtra("referrer_name")
        when (tag) {
            getString(R.string.process_manager) -> {
                binding.clContent.setBackgroundColor(getColor(R.color.process_bg))
                binding.ivLoading.setAnimation(R.raw.process_management)
                if(!TextUtils.isEmpty(referrer_name)){
                    LogUtil.log("enter_processman", mapOf("referrer_name" to "notification"))
                }
                firstTime = System.currentTimeMillis()
                viewModel.loadRunningApps()
                startCounterAnimation(
                    50,
                    null,
                    getString(R.string.scanning_for_running_process)
                )
            }
            getString(R.string.app_manager) -> {
                if(!TextUtils.isEmpty(referrer_name)){
                    LogUtil.log("enter_appman", mapOf("referrer_name" to "notification"))
                }
                binding.ivLoading.setAnimation(R.raw.appmg)
                firstTime = System.currentTimeMillis()
                binding.clContent.setBackgroundColor(getColor(R.color.app_bg))
                startCounterAnimation(
                    50,
                    null,
                    getString(R.string.scanning_apps)
                )
                viewModel.loadInstalledApps(SortRule.SIZE_DESC)
            }

            getString(R.string.speaker_cleaner) -> {
                binding.ivLoading.setAnimation(R.raw.sound_cleanning)
                startCounterAnimation(
                    25,
                    SpeakerCleanerActivity::class.java,
                    getString(R.string.loading_the_components_of_speaker_cleaner)
                )
                binding.clContent.setBackgroundColor(getColor(R.color.speaker_bg))
            }
            getString(R.string.battery_info) -> {
                binding.ivLoading.setAnimation(R.raw.battery_management)
                if(!TextUtils.isEmpty(referrer_name)){
                    LogUtil.log("enter_battery", mapOf("referrer_name" to "notification"))
                }
                startCounterAnimation(
                    25,
                    BatteryInfoActivity::class.java,
                    getString(R.string.scanning_for_battery_info)
                )
                binding.clContent.setBackgroundColor(getColor(R.color.battery_bg))
            }
            getString(R.string.large_file_cleaner) -> {
                binding.ivLoading.setAnimation(R.raw.largefiles)
                firstTime = System.currentTimeMillis()
                viewModel.startScanLargeFile(contentResolver = contentResolver)
                startCounterAnimation(
                    30,
                    null,
                    getString(R.string.scanning_large_file)
                )
                binding.clContent.setBackgroundColor(getColor(R.color.large_bg))
            }
            getString(R.string.screenshot_manager) -> {
                binding.ivLoading.setAnimation(R.raw.screenshot)
                firstTime = System.currentTimeMillis()
                viewModel.getScreenshotData()
                startCounterAnimation(
                    30,
                    null,
                    getString(R.string.scanning_screenshots)
                )
                binding.clContent.setBackgroundColor(getColor(R.color.screenshot_bg))
            }
        }
        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {

            }
        })

    }

    fun initViewmodel() {
        viewModel.runningAppInfo.observe(this) {
            lastTime = System.currentTimeMillis()
            if (lastTime - firstTime < 3000) {
                lifecycleScope.launch {
                    delay(1500)
                    EventBus.getDefault().postSticky(ProcessEvent(it))
                    readyGoThenKill(ProcessManagerActivity::class.java)
                }
            } else {
                EventBus.getDefault().postSticky(ProcessEvent(it))
                readyGoThenKill(ProcessManagerActivity::class.java)
            }

        }

        viewModel._appList.observe(this) {
            lastTime = System.currentTimeMillis()
            if (lastTime - firstTime < 3000) {
                lifecycleScope.launch {
                    delay(1500)
                    EventBus.getDefault().postSticky(InstallAppEvent(it))
                    readyGoThenKill(AppManagerActivity::class.java)
                }
            } else {
                EventBus.getDefault().postSticky(InstallAppEvent(it))
                readyGoThenKill(AppManagerActivity::class.java)
            }
        }

        viewModel.largeFilesData.observe(this){
            lastTime = System.currentTimeMillis()
            if (lastTime - firstTime < 3000) {
                lifecycleScope.launch {
                    delay(1500)
                    EventBus.getDefault().postSticky(LargeFileEvent(it))
                    readyGoThenKill(LargeFileActivity::class.java)
                }
            } else {
                EventBus.getDefault().postSticky(LargeFileEvent(it))
                readyGoThenKill(LargeFileActivity::class.java)
            }
        }
        viewModel.screenshotData.observe(this){
            lastTime = System.currentTimeMillis()
            if (lastTime - firstTime < 3000) {
                lifecycleScope.launch {
                    delay(1500)
                    EventBus.getDefault().postSticky(it)
                    readyGoThenKill(ScreenshotManagerActivity::class.java)
                }
            } else {
                EventBus.getDefault().postSticky(it)
                readyGoThenKill(ScreenshotManagerActivity::class.java)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun startCounterAnimation(
        time: Long,
        clazz: Class<*>?,
        hint: String,
        total: Int = 100
    ) {
        binding.tvHint.text = hint
        lifecycleScope.launch {
            // 从 0 到 100 的动画
            for (i in 0..total) {
                delay(time) // 控制变化速度，30毫秒更新一次
                binding.tvProgress.text = "$i%"
            }
            if (clazz != null) {
                readyGoThenKill(clazz)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.ivLoading.cancelAnimation()
        binding.ivLoading.clearAnimation()
    }
}