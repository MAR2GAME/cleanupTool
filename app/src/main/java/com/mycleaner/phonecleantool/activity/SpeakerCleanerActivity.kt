package com.mycleaner.phonecleantool.activity

import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import com.mycleaner.phonecleantool.adv.Ads
import com.mycleaner.phonecleantool.adv.AdvCheckManager
import com.mycleaner.phonecleantool.adv.AreaKey
import com.mycleaner.phonecleantool.base.activity.BaseActivity
import com.mycleaner.phonecleantool.bean.BannerEvent
import com.mycleaner.phonecleantool.command.readyGoThenKill
import com.mycleaner.phonecleantool.command.safeClick
import com.mycleaner.phonecleantool.databinding.ActivitySpeakerCleanerBinding
import com.mycleaner.phonecleantool.utils.AppPrefsUtils
import com.mycleaner.phonecleantool.utils.LogUtil
import com.mycleaner.phonecleantooll.base.BaseConstant
import com.singular.sdk.Events
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


class SpeakerCleanerActivity : BaseActivity<ActivitySpeakerCleanerBinding>() {

    var advView: ViewGroup? = null
    override fun init() {
        EventBus.getDefault().register(this)
        AppPrefsUtils.putLong(
            BaseConstant.SPEAKER_TIME,
            System.currentTimeMillis()
        )
        LogUtil.setUser(mapOf("total_speaker_num" to 1), "user_add")
        binding.btCleanDust.safeClick {
            readyGoThenKill(PlayAudioActivity::class.java)
        }
        binding.ivSpeakerBack.safeClick {
            onBack()
        }
        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                onBack()
            }
        })
        if (AdvCheckManager.params.limitTime < System.currentTimeMillis()) {
            advView = getBannerAd(this, AreaKey.speakerPageBottomAdv)
            binding.flAd.visibility = View.VISIBLE
            binding.flAd.addView(advView)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: BannerEvent) {
        if (binding.flAd.visibility == View.VISIBLE) {
            binding.flAd.removeView(advView)
            binding.flAd.visibility = View.GONE
            adViewSet.clear()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    fun onBack() {
        Ads.showInterstitialAd(this@SpeakerCleanerActivity, "returnHomePageAdv"){
            LogUtil.log("enter_homepage", mapOf("referrer_name" to "speaker"))
            readyGoThenKill(MainActivity::class.java)
        }
    }


}