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
		LogUtil.log(
			LogAdData.ad_start_loading,
			mapOf(
				LogAdParam.ad_platform to LogAdParam.ad_platform_admob,
				LogAdParam.ad_areakey to areaKey,
				LogAdParam.ad_format to LogAdParam.ad_format_banner,
				LogAdParam.ad_unit_name to AdvIDs.getAdmobBannerId(),
			)
		)
		val startLoadingTime = System.currentTimeMillis()
		val admobAdView = AdView(context)
		admobAdView.adUnitId = AdvIDs.getAdmobBannerId()
		admobAdView.setAdSize(AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, 360))
		admobAdView.adListener = object: AdListener(){
			override fun onAdLoaded() {
				super.onAdLoaded()
				LogUtil.log(
					LogAdData.ad_finish_loading,
					mapOf(
						LogAdParam.ad_platform to LogAdParam.ad_platform_admob,
						LogAdParam.duration to (System.currentTimeMillis() - startLoadingTime),
						LogAdParam.ad_areakey to areaKey,
						LogAdParam.ad_format to LogAdParam.ad_format_banner,
						LogAdParam.ad_source to (admobAdView.responseInfo?.loadedAdapterResponseInfo?.adSourceName ?: "unknow"),
						LogAdParam.ad_unit_name to AdvIDs.getAdmobBannerId(),
					)
				)
			}

			override fun onAdImpression() {
				super.onAdImpression()
				AdvCheckManager.params.bannerTimes++
			}

			override fun onAdClicked() {
				super.onAdClicked()
				LogUtil.log(
					LogAdData.ad_click,
					mapOf(
						LogAdParam.ad_platform to LogAdParam.ad_platform_admob,
						LogAdParam.duration to (System.currentTimeMillis() - startLoadingTime),
						LogAdParam.ad_areakey to areaKey,
						LogAdParam.ad_format to LogAdParam.ad_format_banner,
						LogAdParam.ad_source to (admobAdView.responseInfo?.loadedAdapterResponseInfo?.adSourceName ?: LogAdParam.unknown),
						LogAdParam.ad_unit_name to AdvIDs.getAdmobBannerId(),
					)
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
			LogUtil.log(
				LogAdData.ad_impression,
				mapOf(
					LogAdParam.ad_areakey to areaKey,
					FirebaseAnalytics.Param.AD_PLATFORM to LogAdParam.ad_platform_admob,
					FirebaseAnalytics.Param.AD_UNIT_NAME to AdvIDs.getAdmobBannerId(),
					FirebaseAnalytics.Param.AD_FORMAT to LogAdParam.ad_format_banner,
					FirebaseAnalytics.Param.AD_SOURCE to (admobAdView.responseInfo?.loadedAdapterResponseInfo?.adSourceName ?: LogAdParam.unknown),
					FirebaseAnalytics.Param.CURRENCY to currency,
					FirebaseAnalytics.Param.VALUE to revenue,
				)
			)
			LogUtil.log(
				LogAdData.ad_revenue,
				mapOf(
					LogAdParam.ad_areakey to areaKey,
					FirebaseAnalytics.Param.AD_PLATFORM to LogAdParam.ad_platform_admob,
					FirebaseAnalytics.Param.AD_UNIT_NAME to AdvIDs.getAdmobBannerId(),
					FirebaseAnalytics.Param.AD_FORMAT to LogAdParam.ad_format_banner,
					FirebaseAnalytics.Param.AD_SOURCE to (admobAdView.responseInfo?.loadedAdapterResponseInfo?.adSourceName ?: LogAdParam.unknown),
					FirebaseAnalytics.Param.CURRENCY to currency,
					FirebaseAnalytics.Param.VALUE to revenue,
				)
			)
			LogUtil.logTaiChiAdmob(adValue)
		}

		val adRequest = AdRequest.Builder().build()
		admobAdView.loadAd(adRequest)
		return admobAdView
	}

	fun getMaxBannerAd(context: Context, areaKey: String): ViewGroup {
		LogUtil.log(
			LogAdData.ad_start_loading,
			mapOf(
				LogAdParam.ad_platform to LogAdParam.ad_platform_max,
				LogAdParam.ad_areakey to areaKey,
				LogAdParam.ad_format to LogAdParam.ad_format_banner,
				LogAdParam.ad_unit_name to AdvIDs.MAX_BANNER_ID,
			)
		)
		val maxAdView = MaxAdView(AdvIDs.MAX_BANNER_ID)
		val startLoadTime = System.currentTimeMillis()
		maxAdView.setListener(object : MaxAdViewAdListener {
			override fun onAdExpanded(maxAd: MaxAd) {}
			override fun onAdCollapsed(maxAd: MaxAd) {}

			override fun onAdLoaded(maxAd: MaxAd) {
				LogUtil.log(
					LogAdData.ad_finish_loading,
					mapOf(
						LogAdParam.ad_platform to LogAdParam.ad_platform_max,
						LogAdParam.duration to (System.currentTimeMillis() - startLoadTime),
						LogAdParam.ad_areakey to areaKey,
						LogAdParam.ad_format to LogAdParam.ad_format_banner,
						LogAdParam.ad_source to maxAd.networkName,
						LogAdParam.ad_unit_name to AdvIDs.MAX_BANNER_ID,
					)
				)
			}

			override fun onAdDisplayed(maxAd: MaxAd) {
				AdvCheckManager.params.bannerTimes++
				LogUtil.log(
					LogAdData.ad_impression,
					mapOf(
						LogAdParam.ad_areakey to areaKey,
						LogAdParam.duration to (System.currentTimeMillis() - startLoadTime),
						FirebaseAnalytics.Param.AD_PLATFORM to LogAdParam.ad_platform_max,
						FirebaseAnalytics.Param.AD_UNIT_NAME to AdvIDs.MAX_BANNER_ID,
						FirebaseAnalytics.Param.AD_FORMAT to LogAdParam.ad_format_banner,
						LogAdParam.ad_source to maxAd.networkName,
						FirebaseAnalytics.Param.CURRENCY to LogAdParam.USD,
						FirebaseAnalytics.Param.VALUE to maxAd.revenue,
					)
				)
			}

			override fun onAdHidden(maxAd: MaxAd) {
				LogUtil.log(
					LogAdData.ad_close,
					mapOf(
						LogAdParam.ad_platform to LogAdParam.ad_platform_max,
						LogAdParam.duration to (System.currentTimeMillis() - startLoadTime),
						LogAdParam.ad_areakey to areaKey,
						LogAdParam.ad_format to LogAdParam.ad_format_banner,
						LogAdParam.ad_source to maxAd.networkName,
						LogAdParam.ad_unit_name to AdvIDs.MAX_BANNER_ID,
					)
				)
			}

			override fun onAdClicked(maxAd: MaxAd) {
				LogUtil.log(
					LogAdData.ad_click,
					mapOf(
						LogAdParam.ad_platform to LogAdParam.ad_platform_max,
						LogAdParam.duration to (System.currentTimeMillis() - startLoadTime),
						LogAdParam.ad_areakey to areaKey,
						LogAdParam.ad_format to LogAdParam.ad_format_banner,
						LogAdParam.ad_source to maxAd.networkName,
						LogAdParam.ad_unit_name to AdvIDs.MAX_BANNER_ID,
					)
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
			LogUtil.log(
				LogAdData.ad_revenue,
				mapOf(
					LogAdParam.ad_areakey to areaKey,
					LogAdParam.duration to (System.currentTimeMillis() - startLoadTime),
					FirebaseAnalytics.Param.AD_PLATFORM to LogAdParam.ad_platform_max,
					FirebaseAnalytics.Param.AD_UNIT_NAME to AdvIDs.MAX_BANNER_ID,
					FirebaseAnalytics.Param.AD_FORMAT to LogAdParam.ad_format_banner,
					LogAdParam.ad_source to maxAd.networkName,
					FirebaseAnalytics.Param.CURRENCY to LogAdParam.USD,
					FirebaseAnalytics.Param.VALUE to maxAd.revenue,
				)
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