package com.mycleaner.phonecleantool.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.os.Build
import android.os.StatFs
import androidx.lifecycle.MutableLiveData
import com.mycleaner.phonecleantool.R
import com.mycleaner.phonecleantool.base.BaseApplication
import com.mycleaner.phonecleantool.base.viewmodel.BaseViewModel
import com.mycleaner.phonecleantool.bean.FunctionItemBean
import com.mycleaner.phonecleantool.bean.StorageInfo

import java.io.File


class MainViewModel(application: Application) : BaseViewModel(application) {
    var functionItemData = MutableLiveData<MutableList<FunctionItemBean>>()
    var infoData = MutableLiveData<StorageInfo>()
    fun getFunctionBeans(names: Array<String>) {
        var functionItemBean = mutableListOf<FunctionItemBean>()
        functionItemBean.add(FunctionItemBean(names[0], R.mipmap.ic_processmanager, false))
        functionItemBean.add(FunctionItemBean(names[1], R.mipmap.ic_appmanager, false))
        functionItemBean.add(FunctionItemBean(names[2], R.mipmap.ic_speaker, false))
        functionItemBean.add(FunctionItemBean(names[3], R.mipmap.ic_battery, false))
        functionItemBean.add(FunctionItemBean(names[4], R.mipmap.ic_file, false))
        functionItemBean.add(FunctionItemBean(names[5], R.mipmap.ic_image, false))
        functionItemData.value = functionItemBean
    }


    @SuppressLint("DefaultLocale", "SetTextI18n")
    fun getStorageInfo() {
        var storageInfo: StorageInfo?
        val externalDir =
            File(
                BaseApplication.instance!!.getExternalFilesDir(null)?.absolutePath?.split("/Android")
                    ?.get(0) ?: ""
            )

        if (externalDir.exists()) {
            val statFs = StatFs(externalDir.absolutePath)

            // 获取存储块大小和数量
            val blockSize = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                statFs.blockSizeLong
            } else {
                statFs.blockSize.toLong()
            }

            val totalBlocks = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                statFs.blockCountLong
            } else {
                statFs.blockCount.toLong()
            }

            val availableBlocks = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                statFs.availableBlocksLong
            } else {
                statFs.availableBlocks.toLong()
            }
            // 计算存储空间
            val totalSize = totalBlocks * blockSize
            val availableSize = availableBlocks * blockSize
            val usedSize = totalSize - availableSize
            // 计算使用百分比
            val usedPercentage = if (totalSize > 0) {
                (usedSize * 100 / totalSize)
            } else {
                0
            }
            storageInfo = StorageInfo(totalSize, usedSize, usedPercentage.toFloat())
        } else {
            storageInfo = StorageInfo(0, 0, 0f)
        }
        infoData.value = storageInfo
    }


}