package com.mycleaner.phonecleantool.adv

import android.Manifest
import android.app.Activity
import android.app.Application
import android.content.Context
import android.util.Log
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresPermission



object Ads {
	private const val TAG = "Ads"


	fun showInterstitialAd(activity: AdvActivity, areaKey: String, onClosed: () -> Unit) {
		return ShowInterstitialAdActivity.openPage(activity, areaKey, onClosed)
	}

	@RequiresPermission(Manifest.permission.INTERNET)
	fun getBannerAd(context: Context, areaKey: String): ViewGroup  {
		return ShowBannerAd.getBannerAd(context, areaKey)
	}

	fun showOpenAd(activity: Activity, areaKey: String, listener: ShowOpenAd.OpenAdListener?) {
		ShowOpenAd.showOpenAd(activity, areaKey, listener)
	}


}
