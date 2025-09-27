package com.mycleaner.phonecleantool.adv

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.applovin.mediation.MaxAd
import com.applovin.mediation.MaxAdListener
import com.applovin.mediation.MaxError
import com.applovin.mediation.ads.MaxInterstitialAd
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.OnPaidEventListener
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.firebase.analytics.FirebaseAnalytics
import com.mycleaner.phonecleantool.command.AppConfig
import com.mycleaner.phonecleantool.databinding.ActivityShowinterstitialadBinding
import com.mycleaner.phonecleantool.utils.ActivityManagerUtils
import com.mycleaner.phonecleantool.utils.LogUtil

import com.singular.sdk.Singular
import com.singular.sdk.SingularAdData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import java.util.Timer
import java.util.TimerTask

const val ADV_RESULT_CODE = 8888

class ShowInterstitialAdActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "ShowInterstitialAdActivity"
        var job: Job? = null

        fun getIntent(context: AdvActivity, areaKey: String): Intent {
            return Intent(context, ShowInterstitialAdActivity::class.java).apply {
                putExtra(AREA_KEY, areaKey)
            }
        }

        fun openPage(context: AdvActivity, areaKey: String, onClosed: () -> Unit) {
            if (AdvCheckManager.params.limitTime > System.currentTimeMillis()) {
                onClosed()
            } else {
                context.onClosedCallback = onClosed
                context.interstitialLauncher.launch(getIntent(context, areaKey))
            }
        }
    }

    val binding by lazy {
        ActivityShowinterstitialadBinding.inflate(layoutInflater)
    }

    val areaKey: String by lazy {
        intent.getStringExtra(AREA_KEY) ?: ""
    }

    var isShowing = false

    var logParams: MutableMap<String, Any> = mutableMapOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        initData()
        initUi()
    }

    override fun onStop() {
        super.onStop()
        isShowing = false
        timeoutTimer?.cancel()
    }

    fun initData() {
        setResult(ADV_RESULT_CODE)
    }

    private fun showLoading() {
        binding.rlProgress.visibility = View.VISIBLE
        isLoading = true
    }

    private fun hideLoading() {
        binding.rlProgress.visibility = View.GONE
        isLoading = false
    }

    private var isLoading = false
    private var timeoutTimer: CountDownTimer? = null
    fun initUi() {
        lifecycleScope.launch {
            showLoading()
            var canPlay = withContext(Dispatchers.IO) {
                // 这里执行实际的耗时操作
                AdvCheckManager.checkAdv(areaKey)
            }
            if (canPlay) {
                if (!isShowing) {
                    startLoadingTimeout()
                    isShowing = true
                    if (AppConfig.showAdPlatform == LogAdParam.ad_platform_admob) {
                        showAdmobAdv()
                    } else if (AppConfig.showAdPlatform == LogAdParam.ad_platform_max) {
                        showMaxAdv()
                    }
                }
            } else {
                finish()
            }
        }
    }

    val LOADING_TIMEOUT = 5000L //
    private fun startLoadingTimeout() {
        timeoutTimer = object : CountDownTimer(LOADING_TIMEOUT, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                // 可以在这里更新倒计时UI（如果需要）
            }

            override fun onFinish() {
                if (isLoading) {
                    finish()
                }
            }
        }.start()
    }

    private fun cancelLoadingTimeout() {
        timeoutTimer?.cancel()
        timeoutTimer = null
    }


    fun showAdmobAdv() {
        if (logParams.isNotEmpty()) {
            logParams.clear()
        }
        logParams.put(LogAdParam.ad_platform, LogAdParam.ad_platform_admob)
        logParams.put(LogAdParam.ad_areakey, areaKey)
        logParams.put(LogAdParam.ad_format, LogAdParam.ad_format_interstitial)
        logParams.put(LogAdParam.ad_unit_name, AdvIDs.getAdmobInterstitialId())
        LogUtil.log(
            LogAdData.ad_start_loading,
            logParams
        )
        val startLoadingTime = System.currentTimeMillis()
        InterstitialAd.load(
            this,
            AdvIDs.getAdmobInterstitialId(),
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    Log.e(TAG, "Ad was loaded.")
                    hideLoading()
                    cancelLoadingTimeout()
                    if (logParams.isNotEmpty()) {
                        logParams.clear()
                    }
                    logParams.put(LogAdParam.ad_platform, LogAdParam.ad_platform_admob)
                    logParams.put(
                        LogAdParam.duration,
                        (System.currentTimeMillis() - startLoadingTime)
                    )
                    logParams.put(LogAdParam.ad_areakey, areaKey)
                    logParams.put(LogAdParam.ad_format, LogAdParam.ad_format_interstitial)
                    logParams.put(
                        LogAdParam.ad_source,
                        (ad.responseInfo.loadedAdapterResponseInfo?.adSourceName ?: "unknow")
                    )
                    logParams.put(LogAdParam.ad_unit_name, AdvIDs.getAdmobInterstitialId())
                    LogUtil.log(
                        LogAdData.ad_finish_loading,
                        logParams
                    )
                    ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdClicked() {
                            super.onAdClicked()
                            if (logParams.isNotEmpty()) {
                                logParams.clear()
                            }
                            logParams.put(LogAdParam.ad_platform, LogAdParam.ad_platform_admob)
                            logParams.put(
                                LogAdParam.duration,
                                (System.currentTimeMillis() - startLoadingTime)
                            )
                            logParams.put(LogAdParam.ad_areakey, areaKey)
                            logParams.put(LogAdParam.ad_format, LogAdParam.ad_format_interstitial)
                            logParams.put(
                                LogAdParam.ad_source,
                                (ad.responseInfo.loadedAdapterResponseInfo?.adSourceName
                                    ?: LogAdParam.unknown)
                            )
                            logParams.put(LogAdParam.ad_unit_name, AdvIDs.getAdmobInterstitialId())
                            LogUtil.log(
                                LogAdData.ad_click,
                                logParams
                            )
                        }

                        override fun onAdDismissedFullScreenContent() {
                            super.onAdDismissedFullScreenContent()
                            if (logParams.isNotEmpty()) {
                                logParams.clear()
                            }
                            logParams.put(LogAdParam.ad_platform, LogAdParam.ad_platform_admob)
                            logParams.put(
                                LogAdParam.duration,
                                (System.currentTimeMillis() - startLoadingTime)
                            )
                            logParams.put(LogAdParam.ad_areakey, areaKey)
                            logParams.put(LogAdParam.ad_format, LogAdParam.ad_format_interstitial)
                            logParams.put(
                                LogAdParam.ad_source,
                                (ad.responseInfo.loadedAdapterResponseInfo?.adSourceName
                                    ?: LogAdParam.unknown)
                            )
                            logParams.put(LogAdParam.ad_unit_name, AdvIDs.getAdmobInterstitialId())
                            LogUtil.log(
                                LogAdData.ad_close,
                                logParams
                            )
                            finish()
                        }

                        override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                            super.onAdFailedToShowFullScreenContent(p0)
                            //     showAdmobAdv()
                        }

                        override fun onAdImpression() {
                            super.onAdImpression()
                            AdvCheckManager.params.interTimes++


//							Singular.event("${LogAdData.ad_display}_${Ads.advDisplayCount}")
//							Ads.advDisplayCount++
                        }

                        override fun onAdShowedFullScreenContent() {
                            super.onAdShowedFullScreenContent()
                        }
                    }

                    ad.onPaidEventListener = OnPaidEventListener { adValue ->
                        val micros = adValue.valueMicros         // 广告价值（微元单位，需除以1,000,000得到实际金额）
                        val currency = adValue.currencyCode     // ISO 4217货币代码（如："USD"）
                        val precision = adValue.precisionType    // 金额精度类型（0=估算，1=发布商定义，2=精确计算）
                        // 收入跟踪（示例：转换为美元）
                        val revenue = micros.toDouble() / 1_000_000.0
                        val att = JSONObject()
                        try {
                            att.put(LogAdParam.revenue, revenue)
                            att.put(LogAdParam.adType, LogAdParam.InterAd)
                        } catch (e: JSONException) {
                            e.printStackTrace()
                            Log.e(TAG, "loadAdmobInterstitialAd: ", e)
                        }
                        Singular.eventJSON(LogAdData.ad_revenue, att)
                        if (revenue > 0) {
                            val data = SingularAdData(
                                LogAdParam.adMob,
                                LogAdParam.USD,
                                revenue
                            )
                            Singular.adRevenue(data)
                        }

                        if (logParams.isNotEmpty()) {
                            logParams.clear()
                        }
                        logParams.put(LogAdParam.ad_areakey, areaKey)
                        logParams.put(
                            FirebaseAnalytics.Param.AD_PLATFORM,
                            LogAdParam.ad_platform_admob
                        )
                        logParams.put(
                            FirebaseAnalytics.Param.AD_UNIT_NAME,
                            AdvIDs.getAdmobInterstitialId()
                        )
                        logParams.put(
                            FirebaseAnalytics.Param.AD_FORMAT,
                            LogAdParam.ad_format_interstitial
                        )
                        logParams.put(
                            FirebaseAnalytics.Param.AD_SOURCE,
                            (ad.responseInfo.loadedAdapterResponseInfo?.adSourceName
                                ?: LogAdParam.unknown)
                        )
                        logParams.put(FirebaseAnalytics.Param.CURRENCY, currency)
                        logParams.put(FirebaseAnalytics.Param.VALUE, revenue)
                        LogUtil.log(
                            LogAdData.ad_impression,
                            logParams
                        )
                        LogUtil.log(
                            LogAdData.ad_revenue,
                            logParams
                        )
                        LogUtil.logTaiChiAdmob(adValue)
                    }

                    ad.show(this@ShowInterstitialAdActivity)

                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.e(TAG, adError.message)
                    //showAdmobAdv()
                }
            },
        )
    }

    fun showMaxAdv() {
        if (logParams.isNotEmpty()) {
            logParams.clear()
        }
        logParams.put(LogAdParam.ad_platform, LogAdParam.ad_platform_max)
        logParams.put(LogAdParam.ad_areakey, areaKey)
        logParams.put(LogAdParam.ad_format, LogAdParam.ad_format_interstitial)
        logParams.put(LogAdParam.ad_unit_name, AdvIDs.MAX_INTERSTITIAL_ID)


        LogUtil.log(
            LogAdData.ad_start_loading,
            logParams
        )
        val startLoadingTime = System.currentTimeMillis()
        val interstitialAd = MaxInterstitialAd(AdvIDs.MAX_INTERSTITIAL_ID)
        interstitialAd.setListener(object : MaxAdListener {
            override fun onAdLoaded(maxAd: MaxAd) {
                hideLoading()
                cancelLoadingTimeout()
                if(logParams.isNotEmpty()){
                    logParams.clear()
                }
                logParams.put(LogAdParam.ad_platform,LogAdParam.ad_platform_max)
                logParams.put(LogAdParam.duration,(System.currentTimeMillis() - startLoadingTime))
                logParams.put(LogAdParam.ad_areakey,areaKey)
                logParams.put(LogAdParam.ad_format,LogAdParam.ad_format_interstitial)
                logParams.put(LogAdParam.ad_source,maxAd.networkName)
                logParams.put(LogAdParam.ad_unit_name,AdvIDs.MAX_INTERSTITIAL_ID)

                LogUtil.log(
                    LogAdData.ad_finish_loading,
                    logParams
                )
                if (interstitialAd.isReady) {
                    interstitialAd.showAd(this@ShowInterstitialAdActivity)
                } else {
                    showMaxAdv()
                }
            }

            override fun onAdDisplayed(maxAd: MaxAd) {
                AdvCheckManager.params.interTimes++
                if(logParams.isNotEmpty()){
                    logParams.clear()
                }
                logParams.put(LogAdParam.ad_platform,LogAdParam.ad_platform_max)
                logParams.put( FirebaseAnalytics.Param.CURRENCY,LogAdParam.USD)
                logParams.put(FirebaseAnalytics.Param.VALUE,maxAd.revenue)
                logParams.put(LogAdParam.ad_areakey,areaKey)
                logParams.put(LogAdParam.ad_format,LogAdParam.ad_format_interstitial)
                logParams.put(LogAdParam.ad_source,maxAd.networkName)
                logParams.put(LogAdParam.ad_unit_name,AdvIDs.MAX_INTERSTITIAL_ID)

//				Singular.event("${LogAdData.ad_display}_${Ads.advDisplayCount}")
//				Ads.advDisplayCount++
                LogUtil.log(
                    LogAdData.ad_impression,
                    logParams
                )
            }

            override fun onAdHidden(maxAd: MaxAd) {
                if(logParams.isNotEmpty()){
                    logParams.clear()
                }
                logParams.put(LogAdParam.ad_platform,LogAdParam.ad_platform_max)
                logParams.put(LogAdParam.duration,(System.currentTimeMillis() - startLoadingTime))
                logParams.put(LogAdParam.ad_areakey,areaKey)
                logParams.put(LogAdParam.ad_format,LogAdParam.ad_format_interstitial)
                logParams.put(LogAdParam.ad_source,maxAd.networkName)
                logParams.put(LogAdParam.ad_unit_name,AdvIDs.MAX_INTERSTITIAL_ID)
                LogUtil.log(
                    LogAdData.ad_close,
                    logParams
                )
                finish()
            }

            override fun onAdClicked(maxAd: MaxAd) {


                if(logParams.isNotEmpty()){
                    logParams.clear()
                }
                logParams.put(LogAdParam.ad_platform,LogAdParam.ad_platform_max)
                logParams.put(LogAdParam.duration,(System.currentTimeMillis() - startLoadingTime))
                logParams.put(LogAdParam.ad_areakey,areaKey)
                logParams.put(LogAdParam.ad_format,LogAdParam.ad_format_interstitial)
                logParams.put(LogAdParam.ad_source,maxAd.networkName)
                logParams.put(LogAdParam.ad_unit_name,AdvIDs.MAX_INTERSTITIAL_ID)
                LogUtil.log(
                    LogAdData.ad_click,
                    logParams
                )
            }

            override fun onAdLoadFailed(s: String, maxError: MaxError) {


                //  showMaxAdv()
            }

            override fun onAdDisplayFailed(maxAd: MaxAd, maxError: MaxError) {
                //	showMaxAdv()
            }
        })
        interstitialAd.setRevenueListener { maxAd: MaxAd? ->
            val revenue = maxAd!!.revenue
            val att = JSONObject()
            try {
                att.put(LogAdParam.revenue, revenue)
                att.put(LogAdParam.adType, LogAdParam.InterAd)
            } catch (e: JSONException) {
                e.printStackTrace()
                Log.e(TAG, "createInterstitialAd: ", e)
            }
            Singular.eventJSON(LogAdData.ad_revenue, att)
            if (revenue > 0) {
                val data = SingularAdData(
                    LogAdParam.ad_platform_max,
                    LogAdParam.USD,
                    revenue
                )
                Singular.adRevenue(data)
            }

            if(logParams.isNotEmpty()){
                logParams.clear()
            }
            logParams.put(LogAdParam.ad_areakey,areaKey)
            logParams.put(FirebaseAnalytics.Param.AD_PLATFORM,LogAdParam.ad_platform_max)
            logParams.put(FirebaseAnalytics.Param.AD_UNIT_NAME ,  AdvIDs.MAX_INTERSTITIAL_ID)
            logParams.put(FirebaseAnalytics.Param.AD_FORMAT,LogAdParam.ad_format_interstitial)
            logParams.put(LogAdParam.ad_source, maxAd.networkName)
            logParams.put(FirebaseAnalytics.Param.CURRENCY, LogAdParam.USD)
            logParams.put(FirebaseAnalytics.Param.VALUE,maxAd.revenue)
            LogUtil.log(
                LogAdData.ad_revenue,
                logParams
            )
            LogUtil.logTaiChiMax(maxAd)
        }
        interstitialAd.loadAd()
    }

    override fun onDestroy() {
        super.onDestroy()
        timeoutTimer?.cancel()
        binding.rotatingRingView.visibility = View.GONE
        if (job != null && !job!!.isCancelled) {
            job!!.cancel()
            job = null
        }
        ActivityManagerUtils.removeActivity(this) //销毁Activity移出栈
    }

}