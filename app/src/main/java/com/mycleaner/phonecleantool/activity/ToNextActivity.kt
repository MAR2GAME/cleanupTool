package com.mycleaner.phonecleantool.activity

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.util.Log
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.lifecycleScope
import com.mycleaner.phonecleantool.R
import com.mycleaner.phonecleantool.adv.Ads
import com.mycleaner.phonecleantool.adv.AreaKey
import com.mycleaner.phonecleantool.base.activity.BaseActivity
import com.mycleaner.phonecleantool.command.readyGoThenKill
import com.mycleaner.phonecleantool.databinding.ActivityToNextBinding
import com.mycleaner.phonecleantooll.base.BaseConstant

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.jvm.java

class ToNextActivity : BaseActivity<ActivityToNextBinding>() {
    override fun init() {
        val bundle = intent.extras
        val tag = bundle?.getString(BaseConstant.NEXT_TAG)
        var areaKey=""
        binding.ivNext.setImageAssetsFolder("images/")
        when (tag) {
            getString(R.string.process_manager) -> {
                areaKey="processBeforeFinishAdv"
                binding.content.setBackgroundColor(getColor(R.color.process_bg))
                binding.ivNext.setAnimation(R.raw.rocket)
                startCounterAnimation()
            }
            getString(R.string.app_manager) -> {
                areaKey="appBeforeFinishAdv"
                binding.content.setBackgroundColor(getColor(R.color.app_bg))
                binding.ivNext.setAnimation(R.raw.app)
            }
            getString(R.string.battery_info) -> {
                areaKey="batteryBeforeFinishAdv"
                binding.content.setBackgroundColor(getColor(R.color.battery_bg))
                binding.ivNext.setAnimation(R.raw.battery)
            }
            getString(R.string.large_file_cleaner) -> {
                areaKey="largefileBeforeFinishAdv"
                binding.content.setBackgroundColor(getColor(R.color.large_bg))
                binding.ivNext.setAnimation(R.raw.document)
            }
            getString(R.string.screenshot_manager) -> {
                areaKey="screenshotBeforeFinishAdv"
                binding.content.setBackgroundColor(getColor(R.color.screenshot_bg))
                binding.ivNext.setAnimation(R.raw.pic)
            }
        }
        binding.ivNext.addAnimatorListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                if(!isToFinished){
                    isToFinished=true
                    Ads.showInterstitialAd(this@ToNextActivity, areaKey){
                        readyGoThenKill(FinishedActivity::class.java, intent.extras)
                    }
                }
            }
        })
        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
            }
        })
    }
    var isToFinished=false

    private fun startCounterAnimation() {
        lifecycleScope.launch {
            // 从 0 到 100 的动画
            for (i in 100 downTo 0) {
                delay(30) // 控制变化速度，30毫秒更新一次
                binding.tvHint.text = "$i%"
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.ivNext.cancelAnimation()
        binding.ivNext.clearAnimation()
    }

}