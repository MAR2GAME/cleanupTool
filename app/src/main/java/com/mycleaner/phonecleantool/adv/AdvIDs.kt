package com.mycleaner.phonecleantool.adv

import com.mycleaner.phonecleantool.command.AppConfig


object AdvIDs {

	private var ADMOB_BANNER_ID = "ca-app-pub-3615322193850391/1308677116"
	private var ADMOB_INTERSTITIAL_ID = "ca-app-pub-3615322193850391/4004027172"
	private var ADMOB_NATIVE_ID = "ca-app-pub-3615322193850391/6567090262"
	private var ADMOB_OPEN_ID = "ca-app-pub-3615322193850391/4241962210"

	private const val TEST_ADMOB_BANNER_ID = "ca-app-pub-3940256099942544/9214589741"
	private const val TEST_ADMOB_INTERSTITIAL_ID = "ca-app-pub-3940256099942544/1033173712"
	private const val TEST_ADMOB_NATIVE_ID = "ca-app-pub-3940256099942544/2247696110"
	private const val TEST_ADMOB_OPEN_ID = "ca-app-pub-3940256099942544/9257395921"

	const val MAX_SDK_KEY = "eitvS9P6OFat9gTtupcVe3qoDdAfksOVZfgZK7WHozH6kOsIcUnT1oOUESIGTxeTlBnTEd7X2ifkeumC_qJqob"
	var MAX_INTERSTITIAL_ID = "b3de1f8e51812b2c"
	var MAX_BANNER_ID = "62a72bc5d84097e7"
	var MAX_OPEN_ID = "aa3a1ad33c7aad6a"

	fun setMaxIDs(interstitialAdId: String, bannerAdId: String, openID: String) {
		MAX_INTERSTITIAL_ID = interstitialAdId
		MAX_BANNER_ID = bannerAdId
		MAX_OPEN_ID = openID
	}

	fun setAdmobIDs(
		bannerId: String,
		interstitialAdId: String,
		nativeAdId: String,
		openAdId: String,
	) {
		ADMOB_BANNER_ID = bannerId
		ADMOB_INTERSTITIAL_ID = interstitialAdId
		ADMOB_NATIVE_ID = nativeAdId
		ADMOB_OPEN_ID = openAdId
	}
	private fun selectId(testId: String, prodId: String): String {
		return if (AppConfig.isDebug) testId else prodId
	}
	fun getAdmobBannerId() = selectId(TEST_ADMOB_BANNER_ID, ADMOB_BANNER_ID)

	fun getAdmobInterstitialId() = selectId(TEST_ADMOB_INTERSTITIAL_ID, ADMOB_INTERSTITIAL_ID)

	fun getAdmobNativeId() = selectId(TEST_ADMOB_NATIVE_ID, ADMOB_NATIVE_ID)

	fun getAdmobOpenId() = selectId(TEST_ADMOB_OPEN_ID, ADMOB_OPEN_ID)
}