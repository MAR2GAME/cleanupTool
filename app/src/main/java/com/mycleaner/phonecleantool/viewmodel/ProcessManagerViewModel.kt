package com.mycleaner.phonecleantool.viewmodel

import android.app.Application
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import com.mycleaner.phonecleantool.base.viewmodel.BaseViewModel
import com.mycleaner.phonecleantool.bean.MemoryInfo
import com.mycleaner.phonecleantool.bean.RunningAppInfo
import com.mycleaner.phonecleantool.utils.BackgroundAppsProvider
import com.mycleaner.phonecleantool.utils.MemoryUtils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProcessManagerViewModel(application: Application) : BaseViewModel(application) {
    var memoryInfo=MutableLiveData<MemoryInfo>()

    var isShowLoading = MutableLiveData<Boolean>()
     val _apps = MutableLiveData<List<RunningAppInfo>>()


    fun loadRunningApps() {
        isShowLoading.value=true
        viewModelScope.launch {
            try {
                val runningApps = withContext(Dispatchers.IO) {
                    BackgroundAppsProvider(application).getRunningApps()
                }
                isShowLoading.value=false
                // 自动切换到主线程更新数据
                _apps.value = runningApps

            } catch (e: Exception) {
                _apps.value=mutableListOf<RunningAppInfo>()
            }
        }
    }

    fun getMemoryInfo(){
        viewModelScope.launch {
            val info = withContext(Dispatchers.IO){
                MemoryUtils.getMemoryInfo(application)
            }
            memoryInfo.value=info
        }
    }
}