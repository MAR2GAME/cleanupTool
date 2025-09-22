package com.mycleaner.phonecleantool.activity


import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import com.mycleaner.phonecleantool.R
import com.mycleaner.phonecleantool.adv.Ads
import com.mycleaner.phonecleantool.adv.AreaKey
import com.mycleaner.phonecleantool.adv.ShowOpenAd
import com.mycleaner.phonecleantool.base.activity.BaseActivity
import com.mycleaner.phonecleantool.command.AppConfig
import com.mycleaner.phonecleantool.command.checkNotificationPermission
import com.mycleaner.phonecleantool.command.getDetailCurrentTime
import com.mycleaner.phonecleantool.command.hasStoragePermission
import com.mycleaner.phonecleantool.command.readyGo
import com.mycleaner.phonecleantool.command.readyGoThenKill
import com.mycleaner.phonecleantool.command.safeClick
import com.mycleaner.phonecleantool.command.setupMultiLinkTextView
import com.mycleaner.phonecleantool.databinding.ActivitySplashBinding
import com.mycleaner.phonecleantool.utils.AppPrefsUtils
import com.mycleaner.phonecleantool.utils.LogUtil
import com.mycleaner.phonecleantool.view.ProgressDialog
import com.mycleaner.phonecleantooll.base.BaseConstant


import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.log


@SuppressLint("CustomSplashScreen")
class SplashActivity : BaseActivity<ActivitySplashBinding>() {

    var startTime: Long = 0

    private var progressDialog: ProgressDialog? = null


    @SuppressLint("CheckResult")
    override fun init() {
        startTime = System.currentTimeMillis()
        AppPrefsUtils.putLong(BaseConstant.START_TIME, startTime)
        if (!AppPrefsUtils.getBoolean(BaseConstant.IS_SP_FIRST_SET)) {
            AppPrefsUtils.putBoolean(BaseConstant.IS_SP_FIRST_SET, true)
            var device_id = Settings.Secure.getString(
                contentResolver,
                Settings.Secure.ANDROID_ID
            )
            LogUtil.setUser(
                mapOf(
                    "device_id" to device_id,
                    "first_open_time" to getDetailCurrentTime(startTime)
                ), "user_setOnce"
            )
        }
        if (!AppPrefsUtils.getBoolean(BaseConstant.IS_CAN_TO_GUIDE)) {
            binding.tvHint1.text = getString(R.string.agreement_prompt)
            setupMultiLinkTextView(
                binding.tvHint2,
                arrayOf(
                    getString(R.string.terms_of_use),
                    getString(R.string.privacy_policies)
                ),
                arrayOf(
                    BaseConstant.TERMS_URL,
                    BaseConstant.PRIVACY_URL,
                ),
                ContextCompat.getColor(this, R.color.btn_nor),
                false,
                "launchpage"
            )
            binding.btStart.safeClick {
                LogUtil.log("launch_display", mapOf())
                AppPrefsUtils.putBoolean(BaseConstant.IS_CAN_TO_GUIDE, true)
                readyGoThenKill(GuideActivity::class.java)
            }
        } else {
            if (progressDialog == null) {
                progressDialog = ProgressDialog(this@SplashActivity)
            }
            binding.rlProgress.visibility = View.VISIBLE
            binding.btStart.visibility = View.GONE
            binding.tvHint1.text = getString(R.string.please_wait_a_moment)
            binding.tvHint2.setTextColor("#999999".toColorInt())
            binding.tvHint2.text = getString(R.string.this_process_may_contain_ads)
            startProgress()
            Ads.showOpenAd(this, AreaKey.openPageAdv, object : ShowOpenAd.OpenAdListener {
                override fun onClose() {
                    if (System.currentTimeMillis() - startTime < 2000) {
                        lifecycleScope.launch {
                            delay(1500)
                            toNext()
                        }
                    } else {
                        toNext()
                    }
                }

                override fun onLoad() {
                    isLoading = false
                }
            })
        }
        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {}
        })
    }


    fun toNext() {
        val appOpenFrom = intent.getStringExtra("AppOpenFrom")
        Log.e("weiyu","appOpenFrom"+appOpenFrom)
        if (TextUtils.isEmpty(appOpenFrom)) {
            if (AppPrefsUtils.getBoolean(BaseConstant.IS_TOGUIDE)) {
                LogUtil.log("enter_homepage", mapOf("referrer_name" to "open"))
                readyGoThenKill(MainActivity::class.java)
            } else {
                readyGoThenKill(GuideActivity::class.java)
            }
        } else {
            if (appOpenFrom == "Push") {
                val paramsNoticeId =  intent.getStringExtra("NoticeId") ?: ""
                val paramsDistinctId =  intent.getStringExtra("DistinctId") ?: ""
                val route =  intent.getStringExtra("Route") ?: ""
                LogUtil.log(
                    "notification_clicked", mapOf(
                        "msg_id" to paramsNoticeId,
                        "target_user_id" to paramsDistinctId,
                    )
                )
                if (TextUtils.isEmpty(route)) {
                    if (AppPrefsUtils.getBoolean(BaseConstant.IS_TOGUIDE)) {
                        LogUtil.log("enter_homepage", mapOf("referrer_name" to "open"))
                        readyGoThenKill(MainActivity::class.java)
                    } else {
                        readyGoThenKill(GuideActivity::class.java)
                    }
                } else {
                    when (route) {
                        "/processManager" -> {
                            toLoadingActivity(getString(R.string.process_manager))
                        }

                        "/appManager" -> {
                            toLoadingActivity(getString(R.string.app_manager))
                        }

                        "/largeFileManager" -> {
                            if (!hasStoragePermission()) {
                                readyGoThenKill(MainActivity::class.java)
                            } else {
                                toLoadingActivity(getString(R.string.large_file_cleaner))
                            }
                        }

                        "/speakerCleaner" -> {
                            toLoadingActivity(getString(R.string.speaker_cleaner))
                        }

                        "/batteryInfo" -> {
                            toLoadingActivity(getString(R.string.battery_info))
                        }

                        "/screenshotManager" -> {
                            if (!hasStoragePermission()) {
                                readyGoThenKill(MainActivity::class.java)
                            } else {
                                toLoadingActivity(getString(R.string.screenshot_manager))
                            }
                        }

                        else -> {
                            readyGoThenKill(MainActivity::class.java)
                        }

                    }

                }
            } else {
                Log.e("weiyu","进来了"+intent.extras?.getString(BaseConstant.NEXT_TAG))
                val next = intent.extras?.getString(BaseConstant.NEXT_TAG) ?: ""
                if (next == getString(R.string.junk_files)) {
                    readyGoThenKill(MainActivity::class.java)
                } else {
                    readyGoThenKill(LoadingActivity::class.java, intent.extras)
                }
            }

        }

    }

    fun toLoadingActivity(tag: String) {
        var bundle = Bundle()
        bundle.putString(
            BaseConstant.NEXT_TAG,
            tag
        )
        bundle.putString("referrer_name", "notification")
        readyGoThenKill(LoadingActivity::class.java, bundle)
    }

    var isLoading = false

    @SuppressLint("SetTextI18n")
    private fun startProgress() {
        isLoading = true
        lifecycleScope.launch {
            try {
                for (i in 0..100) {
                    // 检查协程是否被取消
                    // 更新进度条
                    binding.progressBar.progress = i
                    binding.tvProgress.text = "${i}%"
                    // 模拟工作延迟
                    delay(50) // 延迟
                }
                if (isLoading) {
                    toNext()
                }
            } catch (e: Exception) {
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (progressDialog != null) {
            progressDialog!!.relese()
            progressDialog = null
        }
        LogUtil.log(
            "splash_display",
            mapOf("duration_time" to (System.currentTimeMillis() - startTime))
        )

    }


}