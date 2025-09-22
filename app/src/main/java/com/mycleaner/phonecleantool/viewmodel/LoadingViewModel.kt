package com.mycleaner.phonecleantool.viewmodel

import android.app.Application
import android.content.ContentResolver
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import com.mycleaner.phonecleantool.base.viewmodel.BaseViewModel
import com.mycleaner.phonecleantool.bean.AppInfo
import com.mycleaner.phonecleantool.bean.LargeFile
import com.mycleaner.phonecleantool.bean.RunningAppInfo
import com.mycleaner.phonecleantool.bean.Screenshot
import com.mycleaner.phonecleantool.bean.ScreenshotsData
import com.mycleaner.phonecleantool.bean.SortRule
import com.mycleaner.phonecleantool.utils.AdvancedAppInfoHelper
import com.mycleaner.phonecleantool.utils.BackgroundAppsProvider
import com.mycleaner.phonecleantool.utils.FileScanner
import com.mycleaner.phonecleantool.utils.ScreenshotRepository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.collections.sortedBy
import kotlin.collections.sortedByDescending

class LoadingViewModel(application: Application) : BaseViewModel(application) {

    val runningAppInfo = MutableLiveData<List<RunningAppInfo>>()

    var largeFilesData=MutableLiveData<List<LargeFile>>()


    var screenshotData=MutableLiveData<ScreenshotsData>()

    fun loadRunningApps() {
        viewModelScope.launch {
            try {
                val runningApps = withContext(Dispatchers.IO) {
                    BackgroundAppsProvider(application).getRunningApps()
                }
                // 自动切换到主线程更新数据
                runningAppInfo.value = runningApps
            } catch (e: Exception) {
                runningAppInfo.value = mutableListOf<RunningAppInfo>()
            }
        }
    }

    val _appList = MutableLiveData<List<AppInfo>>()

    fun loadInstalledApps(rule: SortRule) {
        viewModelScope.launch {
            try {
                val apps = withContext(Dispatchers.IO) {
                    //getInstalledApps()
                    AdvancedAppInfoHelper(application).getNonSystemApps()

                }
                val sortedList = when (rule) {
                    SortRule.SIZE_DESC -> apps.sortedByDescending { it.size }
                    SortRule.SIZE_ASC -> apps.sortedBy { it.size }
                    SortRule.INSTALL_TIME_DESC -> apps.sortedByDescending { it.firstInstallTime }
                    SortRule.INSTALL_TIME_ASC -> apps.sortedBy { it.firstInstallTime }
                }
                _appList.value = sortedList
            } catch (e: Exception) {
            }
        }
    }


    fun startScanLargeFile(contentResolver: ContentResolver) {
        viewModelScope.launch {
            val fileScanner = FileScanner(contentResolver)
            val largeFiles = fileScanner.scanLargeFiles()
            largeFilesData.value=largeFiles
        }

    }
    fun getScreenshotData(){
        viewModelScope.launch {
            val screenshotRepository = ScreenshotRepository(application)
            val screenshots=screenshotRepository.loadScreenshots()
            var totalSize=screenshotRepository.totalSize
            screenshotData.value=ScreenshotsData(screenshots,totalSize)
        }

    }
}