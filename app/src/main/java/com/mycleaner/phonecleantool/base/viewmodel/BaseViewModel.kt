package com.mycleaner.phonecleantool.base.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel

import com.mycleaner.phonecleantool.base.BaseApplication
import com.mycleaner.phonecleantool.utils.NetWorkUtils




open class BaseViewModel(application: Application): AndroidViewModel(application) {
    /**
     * 管理RxJava请求
     */








    // 检查网络
    fun checkNetWork(): Boolean {
        if (NetWorkUtils.isNetWorkAvailable(BaseApplication.instance!!)) {
            return true
        }
        return false
    }



    /**
     * ViewModel销毁同时也取消请求
     */
    override fun onCleared() {
        super.onCleared()

    }


}