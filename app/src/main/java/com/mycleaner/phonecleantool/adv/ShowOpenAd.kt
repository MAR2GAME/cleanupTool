package com.mycleaner.phonecleantool.adv

import android.app.Activity
import android.icu.number.Precision.currency
import android.util.Log
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
import com.mycleaner.phonecleantool.command.AppConfig
import com.mycleaner.phonecleantool.utils.LogUtil

import com.singular.sdk.Singular
import com.singular.sdk.SingularAdData
import org.json.JSONException
import org.json.JSONObject
import java.lang.ref.WeakReference
import java.util.concurrent.atomic.AtomicBoolean

object ShowOpenAd {

	interface OpenAdListener {
		fun onClose()

		fun onLoad()
	}

	private const val TAG = "ShowOpenAd"
	private var maxAppOpenAd: MaxAppOpenAd? = null
	private var isMaxAdLoading = AtomicBoolean(false)

	var logParams: MutableMap<String, Any> = mutableMapOf()


	fun showOpenAd(activity: Activity, areaKey: String, listener: OpenAdListener?) {
		if(AdvCheckManager.params.limitTime < System.currentTimeMillis()){
			listener?.onClose()
		}else{
			when (AppConfig.showAdPlatform) {
				LogAdParam.ad_platform_admob -> showAdmobOpenAd(activity, areaKey, listener)
				LogAdParam.ad_platform_max -> showMAXOpenAd(activity, areaKey, listener)
			}
		}

	}
	fun showAdmobOpenAd(activity: Activity, areaKey: String, listener: OpenAdListener?) {
		val adUnitId = AdvIDs.getAdmobOpenId()
		val startLoadingTime = System.currentTimeMillis()
		val weakActivity = WeakReference(activity)
		if(logParams.isNotEmpty()){
			logParams.clear()
		}
		logParams.put(LogAdParam.ad_platform,LogAdParam.ad_platform_admob)
		logParams.put(LogAdParam.ad_areakey,areaKey)
		logParams.put(LogAdParam.ad_format,LogAdParam.ad_format_open)
		logParams.put(LogAdParam.ad_unit_name,AdvIDs.getAdmobOpenId())
		LogUtil.log(
			LogAdData.ad_start_loading,
			logParams
		)
		AppOpenAd.load(
			activity,
			AdvIDs.getAdmobOpenId(),
			AdRequest.Builder().build(),
			object : AppOpenAd.AppOpenAdLoadCallback() {
				override fun onAdLoaded(admobAppOpenAd: AppOpenAd) {

					val activityRef = weakActivity.get()
					if(activityRef==null){
						listener?.onClose()
						return
					}
					listener?.onLoad()
					if(logParams.isNotEmpty()){
						logParams.clear()
					}
					logParams.put(LogAdParam.ad_platform,LogAdParam.ad_platform_admob)
					logParams.put(LogAdParam.duration,(System.currentTimeMillis() - startLoadingTime))
					logParams.put(LogAdParam.ad_areakey,areaKey)
					logParams.put(LogAdParam.ad_format,LogAdParam.ad_format_open)
					logParams.put(LogAdParam.ad_source, (admobAppOpenAd.responseInfo.loadedAdapterResponseInfo?.adSourceName ?: "unknow"))
					logParams.put(LogAdParam.ad_unit_name,AdvIDs.getAdmobOpenId())
					LogUtil.log(
						LogAdData.ad_finish_loading,
						logParams
					)
					admobAppOpenAd.fullScreenContentCallback =
						object : FullScreenContentCallback() {
							override fun onAdDismissedFullScreenContent() {
								LogUtil.log(
									LogAdData.ad_close,
									mapOf(
										LogAdParam.ad_platform to LogAdParam.ad_platform_admob,
										LogAdParam.duration to (System.currentTimeMillis() - startLoadingTime),
										LogAdParam.ad_areakey to areaKey,
										LogAdParam.ad_format to LogAdParam.ad_format_open,
										LogAdParam.ad_source to (admobAppOpenAd.responseInfo.loadedAdapterResponseInfo?.adSourceName ?: LogAdParam.unknown),
										LogAdParam.ad_unit_name to AdvIDs.getAdmobOpenId(),
									)
								)
								listener?.onClose()
							}
							override fun onAdFailedToShowFullScreenContent(adError: AdError) {
								Log.e(TAG, adError.message)
								listener?.onClose()
							}
							override fun onAdShowedFullScreenContent() {
								Log.e(TAG, "Ad showed fullscreen content.")
							}
							override fun onAdImpression() {
								AdvCheckManager.params.openTimes++
							}
							override fun onAdClicked() {
								if(logParams.isNotEmpty()){
									logParams.clear()
								}
								logParams.put(LogAdParam.ad_platform,LogAdParam.ad_platform_admob)
								logParams.put(LogAdParam.duration,(System.currentTimeMillis() - startLoadingTime))
								logParams.put(LogAdParam.ad_areakey,areaKey)
								logParams.put(LogAdParam.ad_format,LogAdParam.ad_format_open)
								logParams.put(LogAdParam.ad_source, (admobAppOpenAd.responseInfo.loadedAdapterResponseInfo?.adSourceName ?: LogAdParam.unknown))
								logParams.put(LogAdParam.ad_unit_name,AdvIDs.getAdmobOpenId())
								LogUtil.log(
									LogAdData.ad_click,
									logParams
								)
							}
						}
					admobAppOpenAd.onPaidEventListener = OnPaidEventListener { adValue -> // 可获取的核心参数：
						val micros = adValue.valueMicros         // 广告价值（微元单位，需除以1,000,000得到实际金额）
						val currency = adValue.currencyCode     // ISO 4217货币代码（如："USD"）
						val precision = adValue.precisionType    // 金额精度类型（0=估算，1=发布商定义，2=精确计算）
						// 收入跟踪（示例：转换为美元）
						val revenue = micros / 1_000_000.0
						val att = JSONObject()
						try {
							att.put(LogAdParam.revenue, revenue)
							att.put(LogAdParam.adType, LogAdParam.OpenAd)
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
						if(logParams.isNotEmpty()){
							logParams.clear()
						}
						logParams.put(LogAdParam.ad_areakey,areaKey)
						logParams.put(FirebaseAnalytics.Param.AD_PLATFORM,LogAdParam.ad_platform_admob)
						logParams.put(FirebaseAnalytics.Param.AD_UNIT_NAME , AdvIDs.getAdmobOpenId())
						logParams.put(FirebaseAnalytics.Param.AD_FORMAT,LogAdParam.ad_format_open)
						logParams.put(FirebaseAnalytics.Param.AD_SOURCE,(admobAppOpenAd.responseInfo.loadedAdapterResponseInfo?.adSourceName ?: LogAdParam.unknown))
						logParams.put(FirebaseAnalytics.Param.CURRENCY, currency)
						logParams.put(FirebaseAnalytics.Param.VALUE,revenue)
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
					admobAppOpenAd.show(activityRef)
				}

				override fun onAdFailedToLoad(loadAdError: LoadAdError) {
					listener?.onClose()
				}
			},
		)
	}


	fun showMAXOpenAd(activity: Activity, areaKey: String, listener: OpenAdListener?) {
		if (isMaxAdLoading.getAndSet(true)) {
			listener?.onClose()
			return
		}
		val weakActivity = WeakReference(activity)

		if(logParams.isNotEmpty()){
			logParams.clear()
		}
		logParams.put(LogAdParam.ad_platform,LogAdParam.ad_platform_max)
		logParams.put(LogAdParam.ad_areakey,areaKey)
		logParams.put(LogAdParam.ad_format,LogAdParam.ad_format_open)
		logParams.put(LogAdParam.ad_unit_name,AdvIDs.MAX_OPEN_ID)
		LogUtil.log(
			LogAdData.ad_start_loading,
			logParams
		)
		val startLoadingTime = System.currentTimeMillis()
		maxAppOpenAd = MaxAppOpenAd(AdvIDs.MAX_OPEN_ID).apply {
			this.setListener(object : MaxAdListener {
				override fun onAdLoaded(maxAd: MaxAd) {
					isMaxAdLoading.set(false)
					listener?.onLoad()
					if(logParams.isNotEmpty()){
						logParams.clear()
					}
					logParams.put(LogAdParam.ad_platform,LogAdParam.ad_platform_max)
					logParams.put(LogAdParam.duration,(System.currentTimeMillis() - startLoadingTime))
					logParams.put(LogAdParam.ad_areakey,areaKey)
					logParams.put(LogAdParam.ad_format,LogAdParam.ad_format_open)
					logParams.put(LogAdParam.ad_source,maxAd.networkName)
					logParams.put(LogAdParam.ad_unit_name,AdvIDs.MAX_OPEN_ID)


					LogUtil.log(
						LogAdData.ad_finish_loading,
						logParams
					)
					weakActivity.get()?.let { maxAppOpenAd?.showAd() }
				}

				override fun onAdDisplayed(maxAd: MaxAd) {
					AdvCheckManager.params.openTimes++
					if(logParams.isNotEmpty()){
						logParams.clear()
					}
					logParams.put(LogAdParam.ad_areakey,areaKey)
					logParams.put(FirebaseAnalytics.Param.AD_PLATFORM,LogAdParam.ad_platform_max)
					logParams.put(FirebaseAnalytics.Param.AD_UNIT_NAME ,  AdvIDs.MAX_OPEN_ID)
					logParams.put(FirebaseAnalytics.Param.AD_FORMAT,LogAdParam.ad_format_open)
					logParams.put(LogAdParam.ad_source, maxAd.networkName)
					logParams.put(FirebaseAnalytics.Param.CURRENCY, LogAdParam.USD)
					logParams.put(FirebaseAnalytics.Param.VALUE,maxAd.revenue)
					LogUtil.log(
						LogAdData.ad_impression,
						logParams
					)
					//maxAppOpenAd?.loadAd()
				}

				override fun onAdHidden(maxAd: MaxAd) {
					if(logParams.isNotEmpty()){
						logParams.clear()
					}
					logParams.put(LogAdParam.ad_platform,LogAdParam.ad_platform_max)
					logParams.put(LogAdParam.duration,(System.currentTimeMillis() - startLoadingTime))
					logParams.put(LogAdParam.ad_areakey,areaKey)
					logParams.put(LogAdParam.ad_format,LogAdParam.ad_format_open)
					logParams.put(LogAdParam.ad_source,maxAd.networkName)
					logParams.put(LogAdParam.ad_unit_name,AdvIDs.MAX_OPEN_ID)
					LogUtil.log(
						LogAdData.ad_close,
						logParams
					)
					maxAppOpenAd?.loadAd()
					listener?.onClose()
				}

				override fun onAdClicked(maxAd: MaxAd) {
					if(logParams.isNotEmpty()){
						logParams.clear()
					}
					logParams.put(LogAdParam.ad_platform,LogAdParam.ad_platform_max)
					logParams.put(LogAdParam.duration,(System.currentTimeMillis() - startLoadingTime))
					logParams.put(LogAdParam.ad_areakey,areaKey)
					logParams.put(LogAdParam.ad_format,LogAdParam.ad_format_open)
					logParams.put(LogAdParam.ad_source,maxAd.networkName)
					logParams.put(LogAdParam.ad_unit_name,AdvIDs.MAX_OPEN_ID)
					LogUtil.log(
						LogAdData.ad_click,
						logParams
					)
				}

				override fun onAdLoadFailed(p0: String, p1: MaxError) {
					maxAppOpenAd?.loadAd()
					listener?.onClose()
				}

				override fun onAdDisplayFailed(p0: MaxAd, p1: MaxError) {
					maxAppOpenAd?.loadAd()
					listener?.onClose()
				}
			})
			this.setRevenueListener { maxAd: MaxAd? ->
				val revenue = maxAd!!.revenue
				val att = JSONObject()
				try {
					att.put(LogAdParam.revenue, revenue)
					att.put(LogAdParam.adType, LogAdParam.OpenAd)
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
				logParams.put(FirebaseAnalytics.Param.AD_UNIT_NAME ,  AdvIDs.MAX_OPEN_ID)
				logParams.put(FirebaseAnalytics.Param.AD_FORMAT,LogAdParam.ad_format_open)
				logParams.put(LogAdParam.ad_source, maxAd.networkName)
				logParams.put(FirebaseAnalytics.Param.CURRENCY, LogAdParam.USD)
				logParams.put(FirebaseAnalytics.Param.VALUE,maxAd.revenue)
				LogUtil.log(
					LogAdData.ad_revenue,
					logParams
				)
				LogUtil.logTaiChiMax(maxAd)
			}
			this.loadAd()
		}
	}
}