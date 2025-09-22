package com.mycleaner.phonecleantool.viewmodel

import android.app.Application
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import com.cloud.cleanjunksdk.tools.Utils.isSystemPackage
import com.mycleaner.phonecleantool.base.viewmodel.BaseViewModel
import com.mycleaner.phonecleantool.bean.AppInfo
import com.mycleaner.phonecleantool.utils.AdvancedAppInfoHelper

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ScanFilesViewModel(application: Application) : BaseViewModel(application)  {
    val hintList =  MutableLiveData<List<AppInfo>>()

    fun loadInstalledApps() {
        viewModelScope.launch {
            try {
                val apps = withContext(Dispatchers.IO) {
                   // getInstalledApps()
                    AdvancedAppInfoHelper(application).getNonSystemApps()
                }

                hintList.value = apps
            } catch (e: Exception) {
            }
        }
    }




}