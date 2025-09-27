package com.mycleaner.phonecleantool.adv

import android.Manifest
import android.content.Context
import android.content.res.Resources
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.RequiresPermission
import com.applovin.mediation.MaxAd
import com.applovin.mediation.MaxAdViewAdListener
import com.applovin.mediation.MaxError
import com.applovin.mediation.ads.MaxAdView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.OnPaidEventListener
import com.google.firebase.analytics.FirebaseAnalytics
import com.mycleaner.phonecleantool.command.AppConfig
import com.mycleaner.phonecleantool.utils.LogUtil

import com.singular.sdk.Singular
import com.singular.sdk.SingularAdData
import org.json.JSONException
import org.json.JSONObject

object ShowBannerAd {

	private const val TAG = "ShowBannerAd"

	var logParams: MutableMap<String, Any> = mutableMapOf()

	@RequiresPermission(Manifest.permission.INTERNET)
	fun getBannerAd(context: Context, areaKey: String): ViewGroup {
		return when (AppConfig.showAdPlatform) {
			LogAdParam.ad_platform_admob -> {
				getAdmobBannerAd(context, areaKey)
			}
			LogAdParam.ad_platform_max -> {
				getMaxBannerAd(context, areaKey)
			}
			else -> getAdmobBannerAd(context, areaKey)
		}
	}

	@RequiresPermission(Manifest.permission.INTERNET)
	fun getAdmobBannerAd(context: Context, areaKey: String): ViewGroup {
		if (logParams.isNotEmpty()) {
			logParams.clear()
		}
		logParams.put(LogAdParam.ad_platform, LogAdParam.ad_platform_admob)
		logParams.put(LogAdParam.ad_areakey, areaKey)
		logParams.put(LogAdParam.ad_format, LogAdParam.ad_format_banner)
		logParams.put(LogAdParam.ad_unit_name, AdvIDs.getAdmobBannerId())
		LogUtil.log(
			LogAdData.ad_start_loading,
			logParams
		)
		val startLoadingTime = System.currentTimeMillis()
		val admobAdView = AdView(context)
		admobAdView.adUnitId = AdvIDs.getAdmobBannerId()
		admobAdView.setAdSize(AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, 360))
		admobAdView.adListener = object: AdListener(){
			override fun onAdLoaded() {
				super.onAdLoaded()
				if (logParams.isNotEmpty()) {
					logParams.clear()
				}
				logParams.put(LogAdParam.ad_platform, LogAdParam.ad_platform_admob)
				logParams.put(
					LogAdParam.duration,
					(System.currentTimeMillis() - startLoadingTime)
				)
				logParams.put(LogAdParam.ad_areakey, areaKey)
				logParams.put(LogAdParam.ad_format, LogAdParam.ad_format_banner)
				logParams.put(
					LogAdParam.ad_source,
					(admobAdView.responseInfo?.loadedAdapterResponseInfo?.adSourceName ?: "unknow")
				)
				logParams.put(LogAdParam.ad_unit_name, AdvIDs.getAdmobBannerId())
				LogUtil.log(
					LogAdData.ad_finish_loading,
					logParams
				)
			}
			override fun onAdImpression() {
				super.onAdImpression()
				AdvCheckManager.params.bannerTimes++
			}

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
				logParams.put(LogAdParam.ad_format, LogAdParam.ad_format_banner)
				logParams.put(
					LogAdParam.ad_source,
					(admobAdView.responseInfo?.loadedAdapterResponseInfo?.adSourceName ?: LogAdParam.unknown),
				)
				logParams.put(LogAdParam.ad_unit_name, AdvIDs.getAdmobBannerId())
				LogUtil.log(
					LogAdData.ad_click,
					logParams
				)
			}

		}
		admobAdView.onPaidEventListener = OnPaidEventListener { adValue ->
			val micros = adValue.valueMicros         // 广告价值（微元单位，需除以1,000,000得到实际金额）
			val currency = adValue.currencyCode     // ISO 4217货币代码（如："USD"）
			val precision = adValue.precisionType    // 金额精度类型（0=估算，1=发布商定义，2=精确计算）
			// 收入跟踪（示例：转换为美元）
			val revenue = micros / 1_000_000.0
			val att = JSONObject()
			try {
				att.put(LogAdParam.revenue, revenue)
				att.put(LogAdParam.adType, LogAdParam.BannerAd)
			} catch (e: JSONException) {
				e.printStackTrace()
				Log.e(TAG, "loadAdmobBannerstitialAd: ", e)
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
				AdvIDs.getAdmobBannerId()
			)
			logParams.put(
				FirebaseAnalytics.Param.AD_FORMAT,
				LogAdParam.ad_format_banner
			)
			logParams.put(
				FirebaseAnalytics.Param.AD_SOURCE,
				(admobAdView.responseInfo?.loadedAdapterResponseInfo?.adSourceName ?: LogAdParam.unknown)
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

		val adRequest = AdRequest.Builder().build()
		admobAdView.loadAd(adRequest)
		return admobAdView
	}

	fun getMaxBannerAd(context: Context, areaKey: String): ViewGroup {
		if (logParams.isNotEmpty()) {
			logParams.clear()
		}
		logParams.put(LogAdParam.ad_platform, LogAdParam.ad_platform_max)
		logParams.put(LogAdParam.ad_areakey, areaKey)
		logParams.put(LogAdParam.ad_format, LogAdParam.ad_format_banner)
		logParams.put(LogAdParam.ad_unit_name, AdvIDs.MAX_BANNER_ID)
		LogUtil.log(
			LogAdData.ad_start_loading,
			logParams
		)
		val maxAdView = MaxAdView(AdvIDs.MAX_BANNER_ID)
		val startLoadTime = System.currentTimeMillis()
		maxAdView.setListener(object : MaxAdViewAdListener {
			override fun onAdExpanded(maxAd: MaxAd) {}
			override fun onAdCollapsed(maxAd: MaxAd) {}

			override fun onAdLoaded(maxAd: MaxAd) {

				if(logParams.isNotEmpty()){
					logParams.clear()
				}
				logParams.put(LogAdParam.ad_platform,LogAdParam.ad_platform_max)
				logParams.put(LogAdParam.duration,(System.currentTimeMillis() - startLoadTime))
				logParams.put(LogAdParam.ad_areakey,areaKey)
				logParams.put(LogAdParam.ad_format,LogAdParam.ad_format_banner)
				logParams.put(LogAdParam.ad_source,maxAd.networkName)
				logParams.put(LogAdParam.ad_unit_name,AdvIDs.MAX_BANNER_ID)
				LogUtil.log(
					LogAdData.ad_finish_loading,
					logParams
				)
			}
			override fun onAdDisplayed(maxAd: MaxAd) {
				AdvCheckManager.params.bannerTimes++
				if(logParams.isNotEmpty()){
					logParams.clear()
				}
				logParams.put(LogAdParam.ad_platform,LogAdParam.ad_platform_max)
				logParams.put(LogAdParam.duration,(System.currentTimeMillis() - startLoadTime))
				logParams.put( FirebaseAnalytics.Param.CURRENCY,LogAdParam.USD)
				logParams.put(FirebaseAnalytics.Param.VALUE,maxAd.revenue)
				logParams.put(LogAdParam.ad_areakey,areaKey)
				logParams.put(LogAdParam.ad_format,LogAdParam.ad_format_banner)
				logParams.put(LogAdParam.ad_source,maxAd.networkName)
				logParams.put(LogAdParam.ad_unit_name,AdvIDs.MAX_BANNER_ID)
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
				logParams.put(LogAdParam.duration,(System.currentTimeMillis() - startLoadTime))
				logParams.put(LogAdParam.ad_areakey,areaKey)
				logParams.put(LogAdParam.ad_format,LogAdParam.ad_format_banner)
				logParams.put(LogAdParam.ad_source,maxAd.networkName)
				logParams.put(LogAdParam.ad_unit_name,AdvIDs.MAX_BANNER_ID)
				LogUtil.log(
					LogAdData.ad_close,
					logParams
				)
			}
			override fun onAdClicked(maxAd: MaxAd) {


				if(logParams.isNotEmpty()){
					logParams.clear()
				}
				logParams.put(LogAdParam.ad_platform,LogAdParam.ad_platform_max)
				logParams.put(LogAdParam.duration,(System.currentTimeMillis() - startLoadTime))
				logParams.put(LogAdParam.ad_areakey,areaKey)
				logParams.put(LogAdParam.ad_format,LogAdParam.ad_format_banner)
				logParams.put(LogAdParam.ad_source,maxAd.networkName)
				logParams.put(LogAdParam.ad_unit_name,AdvIDs.MAX_BANNER_ID)
				LogUtil.log(
					LogAdData.ad_click,
					logParams
				)
			}
			override fun onAdLoadFailed(s: String, maxError: MaxError) {
				maxAdView.loadAd()
			}

			override fun onAdDisplayFailed(maxAd: MaxAd, maxError: MaxError) {
				maxAdView.visibility = View.GONE
			}
		})
		maxAdView.setRevenueListener { maxAd: MaxAd? ->
			val revenue = maxAd!!.revenue
			val att = JSONObject()
			try {
				att.put(LogAdParam.revenue, revenue)
				att.put(LogAdParam.adType, LogAdParam.BannerAd)
			} catch (e: JSONException) {
				e.printStackTrace()
				Log.e(TAG, "createBannerAd: ", e)
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
			logParams.put(LogAdParam.duration,(System.currentTimeMillis() - startLoadTime))
			logParams.put(FirebaseAnalytics.Param.AD_PLATFORM,LogAdParam.ad_platform_max)
			logParams.put(FirebaseAnalytics.Param.AD_UNIT_NAME ,  AdvIDs.MAX_BANNER_ID)
			logParams.put(FirebaseAnalytics.Param.AD_FORMAT,LogAdParam.ad_format_banner)
			logParams.put(LogAdParam.ad_source, maxAd.networkName)
			logParams.put(FirebaseAnalytics.Param.CURRENCY, LogAdParam.USD)
			logParams.put(FirebaseAnalytics.Param.VALUE,maxAd.revenue)
			LogUtil.log(
				LogAdData.ad_revenue,
				logParams
			)
			LogUtil.logTaiChiMax(maxAd)
		}
		val width = ViewGroup.LayoutParams.MATCH_PARENT
		val heightPx = dip2px(60f)
		maxAdView.setLayoutParams(FrameLayout.LayoutParams(width, heightPx))
		maxAdView.loadAd()
		return maxAdView
	}

	fun dip2px(dpValue: Float): Int {
		val scale = Resources.getSystem().displayMetrics.density
		return (dpValue * scale + 0.5f).toInt()
	}
}