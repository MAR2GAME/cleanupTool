package com.mycleaner.phonecleantool.viewmodel

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.mycleaner.phonecleantool.base.viewmodel.BaseViewModel
import com.mycleaner.phonecleantool.bean.BatteryInfo
import com.mycleaner.phonecleantool.utils.BatteryInfoHelper

import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BatteryInfoViewModel(application: Application) : BaseViewModel(application)  {
    private val _batteryInfo = MutableStateFlow<BatteryInfo?>(null)
    val batteryInfo: StateFlow<BatteryInfo?> = _batteryInfo.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private var updateJob: Job? = null

    /**
     * 开始定时获取电池信息
     * @param intervalMillis 更新间隔（毫秒），
     */
    fun startPeriodicUpdate(intervalMillis: Long = 10000,batteryInfoHelper: BatteryInfoHelper) {
        // 停止现有的更新任务
        stopPeriodicUpdate()
        updateJob = viewModelScope.launch {
            while (true) {
                try {
                    _isLoading.value = true
                    _errorMessage.value = null

                    // 获取电池信息
                    val info = batteryInfoHelper.getBatteryInfo()
                    _batteryInfo.value = info

                    _isLoading.value = false
                } catch (e: Exception) {
                    _errorMessage.value = "${e.message}"
                    _isLoading.value = false
                }
                // 等待指定的时间间隔
                delay(intervalMillis)
            }
        }
    }

    /**
     * 停止定时获取电池信息
     */
    fun stopPeriodicUpdate() {
        updateJob?.cancel()
        updateJob = null
    }

    /**
     * 手动获取一次电池信息
     */
    fun refreshBatteryInfo(batteryInfoHelper: BatteryInfoHelper) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null

                // 获取电池信息
                val info = batteryInfoHelper.getBatteryInfo()
                _batteryInfo.value = info

                _isLoading.value = false
            } catch (e: Exception) {
                _errorMessage.value = "${e.message}"
                _isLoading.value = false
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        // 清理资源
        stopPeriodicUpdate()
    }
}