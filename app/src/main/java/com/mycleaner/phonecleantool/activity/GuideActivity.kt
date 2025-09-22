package com.mycleaner.phonecleantool.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.cloud.cleanjunksdk.task.CheckSdkCallback
import com.cloud.cleanjunksdk.task.Clean
import com.cloud.cleanjunksdk.task.CleanSDK
import com.cloud.cleanjunksdk.tools.Region
import com.mycleaner.phonecleantool.R
import com.mycleaner.phonecleantool.base.BaseApplication
import com.mycleaner.phonecleantool.base.activity.BaseActivity
import com.mycleaner.phonecleantool.bean.CleanSdkEvent
import com.mycleaner.phonecleantool.command.AppConfig
import com.mycleaner.phonecleantool.command.REQUEST_CODE_MANAGE_STORAGE
import com.mycleaner.phonecleantool.command.REQUEST_CODE_STORAGE_PERMISSION
import com.mycleaner.phonecleantool.command.checkAndRequestPermissions
import com.mycleaner.phonecleantool.command.hasStoragePermission
import com.mycleaner.phonecleantool.command.readyGo
import com.mycleaner.phonecleantool.command.readyGoThenKill
import com.mycleaner.phonecleantool.command.safeClick
import com.mycleaner.phonecleantool.databinding.ActivityGuideBinding
import com.mycleaner.phonecleantool.utils.AppPrefsUtils
import com.mycleaner.phonecleantool.utils.LogUtil
import com.mycleaner.phonecleantool.view.RequestPermissionDialog
import com.mycleaner.phonecleantooll.base.BaseConstant


import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import java.util.concurrent.TimeUnit
import kotlin.jvm.java

class GuideActivity : BaseActivity<ActivityGuideBinding>() {
    var requestPermissionDialog: RequestPermissionDialog? = null

    override fun init() {
        initCleanSDK()
        lifecycleScope.launch {
            delay(1400)
            binding.tvSkip.visibility = View.VISIBLE
        }
        binding.tvSkip.safeClick {
            AppPrefsUtils.putBoolean(BaseConstant.IS_TOGUIDE, true)
            LogUtil.log("enter_homepage", mapOf("referrer_name" to "open"))
            readyGoThenKill(MainActivity::class.java)
        }
        binding.btContinue.safeClick {
            if (hasStoragePermission()) {
                AppPrefsUtils.putBoolean(BaseConstant.IS_TOGUIDE, true)
                if (isInitSuccess) {
                    EventBus.getDefault().postSticky(CleanSdkEvent(isInitSuccess, sdkClean))
                    readyGoThenKill(ScanFilesActivity::class.java)
                } else {
                    readyGoThenKill(MainActivity::class.java)
                }
            } else {
                if (AppConfig.ACCESS_FLAG == 1L) {
                    if (requestPermissionDialog == null) {
                        requestPermissionDialog = RequestPermissionDialog(this@GuideActivity)
                        requestPermissionDialog!!.setmOnClickListener(object :
                            RequestPermissionDialog.onClickListener {
                            override fun onAllow() {
                                checkAndRequestPermissions(
                                    Manifest.permission.READ_EXTERNAL_STORAGE,
                                    {
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
                    checkAndRequestPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, {
                        lifecycleScope.launch {
                            delay(200)
                            readyGo(TransparentActivity::class.java)
                        }
                    })
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_MANAGE_STORAGE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && Environment.isExternalStorageManager()) {
                onGetPermission()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_STORAGE_PERMISSION && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            onGetPermission()
        } else {
            Toast.makeText(this, getString(R.string.guide_permission_hint), Toast.LENGTH_SHORT)
                .show()
        }
    }

    fun onGetPermission() {
        LogUtil.log("agree_filemanage_permission", mapOf())
        lifecycleScope.launch {
            delay(300)
            AppPrefsUtils.putBoolean(BaseConstant.IS_TOGUIDE, true)
            if (isInitSuccess) {
                EventBus.getDefault().postSticky(CleanSdkEvent(isInitSuccess, sdkClean))
                readyGoThenKill(ScanFilesActivity::class.java)
            } else {
                readyGoThenKill(MainActivity::class.java)
            }
        }

    }


    override fun onDestroy() {
        super.onDestroy()
        if (requestPermissionDialog != null) {
            requestPermissionDialog!!.relese()
            requestPermissionDialog = null
        }
    }

    var isInitSuccess = false
    var sdkClean: Clean? = null
    fun initCleanSDK() {
        CleanSDK.init(
            BaseApplication.instance,
            this@GuideActivity,
            Region.INTL,
            object : CheckSdkCallback {
                override fun onSuccess(clean: Clean?) {
                    isInitSuccess = true
                    sdkClean = clean
                    sdkClean!!.timeout(60000)
                }

                override fun onError(errCode: Int) {
                    isInitSuccess = false
                    sdkClean = null
                }
            }) //initialize SDK
    }


}


