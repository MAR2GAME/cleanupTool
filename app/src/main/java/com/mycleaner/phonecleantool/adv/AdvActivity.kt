package com.mycleaner.phonecleantool.adv

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import com.applovin.mediation.ads.MaxAdView
import com.google.android.gms.ads.AdView



open class AdvActivity: AppCompatActivity() {

	val adViewSet = mutableSetOf<ViewGroup>()
	lateinit var interstitialLauncher: ActivityResultLauncher<Intent>
	var onClosedCallback: (() -> Unit)? = null
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		interstitialLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
			if (result.resultCode == ADV_RESULT_CODE) {
				onClosedCallback?.invoke()
				onClosedCallback = null
			}
		}
	}

	@RequiresPermission(Manifest.permission.INTERNET)
	fun getBannerAd(context: Context, areaKey: String): ViewGroup {
		val adView = Ads.getBannerAd(context, areaKey)
		adViewSet.add(adView)
		return adView
	}

	fun removeBannerAd(adView: ViewGroup) {
		adViewSet.remove(adView)
	}

	override fun onResume(){
		super.onResume()
		for (adView in adViewSet) {
			when(adView){
				is MaxAdView->{
					adView.startAutoRefresh()
				}
				is AdView->{
					adView.resume()
				}
			}
		}
	}
	override fun onStop() {
		super.onStop()
		for (adView in adViewSet) {
			when(adView){
				is MaxAdView->{
					adView.stopAutoRefresh()
				}
				is AdView->{
					adView.pause()
				}
			}
		}

	}

	override fun onDestroy() {
		super.onDestroy()
		adViewSet.clear()
	}

}