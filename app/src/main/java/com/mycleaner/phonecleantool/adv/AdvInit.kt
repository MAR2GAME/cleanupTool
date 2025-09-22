package com.mycleaner.phonecleantool.adv

import android.app.Application
import androidx.core.net.toUri
import com.applovin.sdk.AppLovinMediationProvider
import com.applovin.sdk.AppLovinSdk
import com.applovin.sdk.AppLovinSdkConfiguration
import com.applovin.sdk.AppLovinSdkInitializationConfiguration
import com.facebook.ads.AdSettings
import com.google.android.gms.ads.MobileAds
import com.mycleaner.phonecleantool.base.BaseApplication
import com.mycleaner.phonecleantool.command.AppConfig
import com.mycleaner.phonecleantool.utils.LogUtil
import com.mycleaner.phonecleantooll.base.BaseConstant
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object AdvInit {
	private var isAdmobInitialized = false
	private var isMaxInitialized = false

	/**
	 * 初始化所有广告SDK
	 */
	fun initAdv(context: BaseApplication) {
		initAdmob(context)
		initMAX(context)
	}

	/**
	 * 初始化AdMob SDK
	 */
	private fun initAdmob(context: BaseApplication) {
		if (isAdmobInitialized) return
		CoroutineScope(Dispatchers.IO).launch {
			try {
				LogUtil.log(
					LogAdData.adv_sdk_init,
					mapOf(LogAdParam.ad_platform to LogAdParam.ad_platform_admob)
				)

				val advInitTime = System.currentTimeMillis()

				if (!AppConfig.openAdmobMediation) {
					MobileAds.disableMediationAdapterInitialization(context)
				}

				MobileAds.initialize(context) { initializationStatus ->
					val duration = System.currentTimeMillis() - advInitTime

					LogUtil.log(
						LogAdData.adv_sdk_initcomplete,
						mapOf(
							LogAdParam.ad_platform to LogAdParam.ad_platform_admob,
							LogAdParam.duration to duration
						)
					)

					// 只在主线程创建AppOpenHelper
					CoroutineScope(Dispatchers.Main).launch {
						AppOpenHelper(
							context,
							LogAdParam.foregroundKey,
							LogAdParam.ad_platform_admob
						)
					}

					isAdmobInitialized = true
				}
			} catch (e: Exception) {
			}
		}
	}

	/**
	 * 初始化AppLovin MAX SDK
	 */
	private fun initMAX(context: BaseApplication) {
		if (isMaxInitialized) return

		CoroutineScope(Dispatchers.IO).launch {
			try {
				LogUtil.log(
					LogAdData.adv_sdk_init,
					mapOf(LogAdParam.ad_platform to LogAdParam.ad_platform_max)
				)

				val advInitTime = System.currentTimeMillis()

				// 配置Facebook广告设置
				AdSettings.setDataProcessingOptions(arrayOf<String?>())

				// 配置AppLovin SDK
				val initConfig = AppLovinSdkInitializationConfiguration.builder(AdvIDs.MAX_SDK_KEY)
					.setMediationProvider(AppLovinMediationProvider.MAX)
					.build()

				val settings = AppLovinSdk.getInstance(context).settings
				withContext(Dispatchers.Main) {
					settings.termsAndPrivacyPolicyFlowSettings.isEnabled = false
					settings.termsAndPrivacyPolicyFlowSettings.privacyPolicyUri =
						BaseConstant.PRIVACY_URL.toUri()
					settings.termsAndPrivacyPolicyFlowSettings.termsOfServiceUri =
						BaseConstant.TERMS_URL.toUri()
				}

				// 初始化SDK
				AppLovinSdk.getInstance(context).initialize(initConfig) { sdkConfig ->
					val duration = System.currentTimeMillis() - advInitTime

					LogUtil.log(
						LogAdData.adv_sdk_initcomplete,
						mapOf(
							LogAdParam.ad_platform to LogAdParam.ad_platform_max,
							LogAdParam.duration to duration
						)
					)

					// 只在主线程创建AppOpenHelper
					CoroutineScope(Dispatchers.Main).launch {
						AppOpenHelper(
							context,
							LogAdParam.foregroundKey,
							LogAdParam.ad_platform_max
						)
					}

					isMaxInitialized = true
				}
			} catch (e: Exception) {

			}
		}
	}
}