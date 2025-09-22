package com.mycleaner.phonecleantool.adv

import android.app.Application
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.applovin.mediation.MaxAd
import com.applovin.mediation.MaxAdListener
import com.applovin.mediation.MaxError
import com.applovin.mediation.ads.MaxAppOpenAd
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.OnPaidEventListener
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.firebase.analytics.FirebaseAnalytics
import com.mycleaner.phonecleantool.base.BaseApplication
import com.mycleaner.phonecleantool.command.AppConfig
import com.mycleaner.phonecleantool.utils.AppPrefsUtils
import com.mycleaner.phonecleantool.utils.LogUtil
import com.singular.sdk.Singular
import com.singular.sdk.SingularAdData
import org.json.JSONException
import org.json.JSONObject

class AppOpenHelper(
    private val context: BaseApplication,
    private val areaKey: String,
    private val platForm: String
) : DefaultLifecycleObserver {

    companion object {
        private const val TAG = "AppOpenManager"
        private const val BACKGROUND_DURATION_MULTIPLIER = 1000L
    }

    private var isAppInBackground = true

    private var maxAppOpenAd: MaxAppOpenAd? = null

    private var showOpenAdvTime: Long by AppPrefsUtils.PreferenceDelegate("showOpenAdvTime", 0L)
    private var enterBackgroundTime = 0L

    init {
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        initializeMaxAdIfNeeded()
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        if (isAppInBackground) {
            isAppInBackground = false
            Log.d(TAG, "onStart: $showOpenAdvTime ${AdvCheckManager.params.backgroundDuration} ${System.currentTimeMillis()}")
            if (shouldShowAd()) {
                showAdIfReady()
            }
        }
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        isAppInBackground = true
        enterBackgroundTime = System.currentTimeMillis()
    }

    private fun shouldShowAd(): Boolean {
        return enterBackgroundTime + AdvCheckManager.params.backgroundDuration * BACKGROUND_DURATION_MULTIPLIER < System.currentTimeMillis()
                &&AdvCheckManager.params.limitTime <System.currentTimeMillis()
    }

    fun showAdIfReady() {
        when {
            platForm == LogAdParam.ad_platform_admob && AppConfig.showAdPlatform == LogAdParam.ad_platform_admob -> {
                showAdmob()
            }
            platForm == LogAdParam.ad_platform_max && AppConfig.showAdPlatform == LogAdParam.ad_platform_max -> {
                showMax()
            }
        }
    }

    private fun showAdmob() {


        Log.d(TAG, "showAdmob: ")
        LogUtil.log(
            LogAdData.ad_start_loading,
            mapOf(
                LogAdParam.ad_platform to LogAdParam.ad_platform_admob,
                LogAdParam.ad_areakey to areaKey,
                LogAdParam.ad_format to LogAdParam.ad_format_open,
                LogAdParam.ad_unit_name to AdvIDs.getAdmobOpenId(),
            )
        )


        val startLoadingTime = System.currentTimeMillis()

        AppOpenAd.load(
            context,
            AdvIDs.getAdmobOpenId(),
            AdRequest.Builder().build(),
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {

                    handleAdmobAdLoaded(ad, startLoadingTime)
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {

                    Log.e(TAG, "onAdFailedToLoad: ${loadAdError.message}")
                }
            }
        )
    }

    private fun handleAdmobAdLoaded(ad: AppOpenAd, startLoadingTime: Long) {
        val adSource = ad.responseInfo.loadedAdapterResponseInfo?.adSourceName ?: LogAdParam.unknown

        LogUtil.log(
            LogAdData.ad_finish_loading,
            mapOf(
                LogAdParam.ad_platform to LogAdParam.ad_platform_admob,
                LogAdParam.duration to (System.currentTimeMillis() - startLoadingTime),
                LogAdParam.ad_areakey to areaKey,
                LogAdParam.ad_format to LogAdParam.ad_format_open,
                LogAdParam.ad_source to (ad.responseInfo.loadedAdapterResponseInfo?.adSourceName ?: "unknow"),
                LogAdParam.ad_unit_name to AdvIDs.getAdmobOpenId(),
            )
        )

        ad.fullScreenContentCallback = createAdmobFullScreenCallback(ad,adSource, startLoadingTime)
        ad.onPaidEventListener = createAdmobPaidEventListener(ad, adSource)

        context.currentActivity?.let {
            ad.show(it)
        } ?: run {
            Log.e(TAG, "onAdLoaded: currentActivity is null")
        }
    }

    private fun createAdmobFullScreenCallback(
        ad: AppOpenAd,
        adSource: String,
        startLoadingTime: Long
    ): FullScreenContentCallback {
        return object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                LogUtil.log(
                    LogAdData.ad_close,
                    mapOf(
                        LogAdParam.ad_platform to LogAdParam.ad_platform_admob,
                        LogAdParam.duration to (System.currentTimeMillis() - startLoadingTime),
                        LogAdParam.ad_areakey to areaKey,
                        LogAdParam.ad_format to LogAdParam.ad_format_open,
                        LogAdParam.ad_source to (ad.responseInfo.loadedAdapterResponseInfo?.adSourceName ?: LogAdParam.unknown),
                        LogAdParam.ad_unit_name to AdvIDs.getAdmobOpenId(),
                    )
                )

            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                Log.e(TAG, adError.message)

            }

            override fun onAdShowedFullScreenContent() {
                Log.d(TAG, "Ad showed fullscreen content.")
            }

            override fun onAdImpression() {
                AdvCheckManager.params.openTimes++
            }

            override fun onAdClicked() {
                LogUtil.log(
                    LogAdData.ad_click,
                    mapOf(
                        LogAdParam.ad_platform to LogAdParam.ad_platform_admob,
                        LogAdParam.duration to (System.currentTimeMillis() - startLoadingTime),
                        LogAdParam.ad_areakey to areaKey,
                        LogAdParam.ad_format to LogAdParam.ad_format_open,
                        LogAdParam.ad_source to (ad.responseInfo.loadedAdapterResponseInfo?.adSourceName ?: LogAdParam.unknown),
                        LogAdParam.ad_unit_name to AdvIDs.getAdmobOpenId(),
                    )
                )
            }
        }
    }

    private fun createAdmobPaidEventListener(ad: AppOpenAd, adSource: String): OnPaidEventListener {
        return OnPaidEventListener { adValue ->
            val micros = adValue.valueMicros
            val currency = adValue.currencyCode
            val revenue = micros / 1_000_000.0

            logRevenueEvent(LogAdParam.ad_platform_admob, adSource, revenue, currency)
            LogUtil.logTaiChiAdmob(adValue)
        }
    }

    private fun initializeMaxAdIfNeeded() {
        if (platForm == LogAdParam.ad_platform_max && maxAppOpenAd == null) {
            maxAppOpenAd = MaxAppOpenAd(AdvIDs.MAX_OPEN_ID).apply {
                setListener(createMaxAdListener())
                setRevenueListener(createMaxRevenueListener())
            }
        }
    }

    private fun showMax() {
        maxAppOpenAd?.let {
            LogUtil.log(
                LogAdData.ad_start_loading,
                mapOf(
                    LogAdParam.ad_platform to LogAdParam.ad_platform_max,
                    LogAdParam.ad_areakey to areaKey,
                    LogAdParam.ad_format to LogAdParam.ad_format_open,
                    LogAdParam.ad_unit_name to AdvIDs.MAX_OPEN_ID,
                )
            )
            it.loadAd()
        }
    }

    private fun createMaxAdListener(): MaxAdListener {
        val startLoadingTime = System.currentTimeMillis()

        return object : MaxAdListener {
            override fun onAdLoaded(maxAd: MaxAd) {
                LogUtil.log(
                    LogAdData.ad_finish_loading,
                    mapOf(
                        LogAdParam.ad_platform to LogAdParam.ad_platform_max,
                        LogAdParam.duration to (System.currentTimeMillis() - startLoadingTime),
                        LogAdParam.ad_areakey to areaKey,
                        LogAdParam.ad_format to LogAdParam.ad_format_open,
                        LogAdParam.ad_source to maxAd.networkName,
                        LogAdParam.ad_unit_name to AdvIDs.MAX_OPEN_ID,
                    )
                )
                maxAppOpenAd?.showAd()
            }

            override fun onAdDisplayed(maxAd: MaxAd) {
                AdvCheckManager.params.openTimes++
                logImpressionEvent(maxAd)
            }

            override fun onAdHidden(maxAd: MaxAd) {
                LogUtil.log(
                    LogAdData.ad_close,
                    mapOf(
                        LogAdParam.ad_platform to LogAdParam.ad_platform_max,
                        LogAdParam.duration to (System.currentTimeMillis() - startLoadingTime),
                        LogAdParam.ad_areakey to areaKey,
                        LogAdParam.ad_format to LogAdParam.ad_format_open,
                        LogAdParam.ad_source to maxAd.networkName,
                        LogAdParam.ad_unit_name to AdvIDs.MAX_OPEN_ID,
                    )
                )
            }

            override fun onAdClicked(maxAd: MaxAd) {
                LogUtil.log(
                    LogAdData.ad_click,
                    mapOf(
                        LogAdParam.ad_platform to LogAdParam.ad_platform_max,
                        LogAdParam.duration to (System.currentTimeMillis() - startLoadingTime),
                        LogAdParam.ad_areakey to areaKey,
                        LogAdParam.ad_format to LogAdParam.ad_format_open,
                        LogAdParam.ad_source to maxAd.networkName,
                        LogAdParam.ad_unit_name to AdvIDs.MAX_OPEN_ID,
                    )
                )
            }

            override fun onAdLoadFailed(adUnitId: String, error: MaxError) {
                // Handle ad load failure
            }

            override fun onAdDisplayFailed(ad: MaxAd, error: MaxError) {
                // Handle ad display failure
            }
        }
    }

    private fun createMaxRevenueListener(): (MaxAd?) -> Unit {
        return { maxAd ->
            maxAd?.let {
                logRevenueEvent(LogAdParam.ad_platform_max, it.networkName, it.revenue, LogAdParam.USD)
                LogUtil.logTaiChiMax(it)
            }
        }
    }



    private fun logImpressionEvent(maxAd: MaxAd) {
        LogUtil.log(
            LogAdData.ad_impression,
            mapOf(
                LogAdParam.ad_areakey to areaKey,
                FirebaseAnalytics.Param.AD_PLATFORM to LogAdParam.ad_platform_max,
                FirebaseAnalytics.Param.AD_UNIT_NAME to AdvIDs.MAX_OPEN_ID,
                FirebaseAnalytics.Param.AD_FORMAT to LogAdParam.ad_format_open,
                LogAdParam.ad_source to maxAd.networkName,
                FirebaseAnalytics.Param.CURRENCY to LogAdParam.USD,
                FirebaseAnalytics.Param.VALUE to maxAd.revenue,
            )
        )
    }

    private fun logRevenueEvent(platform: String, adSource: String, revenue: Double, currency: String) {
        // Singular tracking
        val att = JSONObject().apply {
            try {
                put(LogAdParam.revenue, revenue)
                put(LogAdParam.adType, LogAdParam.OpenAd)
            } catch (e: JSONException) {
                Log.e(TAG, "JSON error in logRevenueEvent: ", e)
            }
        }
        Singular.eventJSON(LogAdData.ad_revenue, att)

        if (revenue > 0) {
            val data = SingularAdData(LogAdParam.adMob, currency, revenue)
            Singular.adRevenue(data)
        }

        // Firebase logging
        val params = mapOf(
            LogAdParam.ad_areakey to areaKey,
            FirebaseAnalytics.Param.AD_PLATFORM to platform,
            FirebaseAnalytics.Param.AD_UNIT_NAME to getAdUnitId(platform),
            FirebaseAnalytics.Param.AD_FORMAT to LogAdParam.ad_format_open,
            FirebaseAnalytics.Param.AD_SOURCE to adSource,
            FirebaseAnalytics.Param.CURRENCY to currency,
            FirebaseAnalytics.Param.VALUE to revenue,
        )

        LogUtil.log(LogAdData.ad_impression, params)
        LogUtil.log(LogAdData.ad_revenue, params)
    }

    private fun getAdUnitId(platform: String): String {
        return when (platform) {
            LogAdParam.ad_platform_admob -> AdvIDs.getAdmobOpenId()
            LogAdParam.ad_platform_max -> AdvIDs.MAX_OPEN_ID
            else -> "unknown"
        }
    }
}