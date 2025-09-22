package com.mycleaner.phonecleantool.activity

import android.content.Intent
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat.startActivity
import com.mycleaner.phonecleantool.R
import com.mycleaner.phonecleantool.activity.AboutActivity
import com.mycleaner.phonecleantool.adv.Ads
import com.mycleaner.phonecleantool.base.activity.BaseActivity
import com.mycleaner.phonecleantool.command.rateUS
import com.mycleaner.phonecleantool.command.readyGo
import com.mycleaner.phonecleantool.command.readyGoThenKill
import com.mycleaner.phonecleantool.command.safeClick
import com.mycleaner.phonecleantool.databinding.ActivitySettingBinding
import com.mycleaner.phonecleantool.utils.LogUtil


class SettingActivity : BaseActivity<ActivitySettingBinding>() {
    override fun init() {

        binding.ivSettingBack.safeClick {
            onBack()
        }
        binding.tvShare.safeClick {
            shareApp()
        }
        binding.tvRate.safeClick {
            rateUS()
        }
        binding.tvAbout.safeClick{
            readyGo(AboutActivity::class.java)
        }
        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                onBack()
            }
        })
    }

    fun onBack(){
        Ads.showInterstitialAd(this@SettingActivity, "returnHomePageAdv"){
            readyGoThenKill(MainActivity::class.java)
        }
    }

    fun shareApp() {
        try {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"

            // 应用名称
            val appName = getString(R.string.app_name)

            // 应用商店链接（替换为你的应用实际链接）
            val appLink = "https://play.google.com/store/apps/details?id=${packageName}"

            // 分享文本
            val shareText = "Check out $appName at: $appLink"

            shareIntent.putExtra(Intent.EXTRA_SUBJECT, appName)
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareText)

            startActivity(Intent.createChooser(shareIntent, "Share $appName"))
        } catch (e: Exception) {
            e.printStackTrace()
            // 处理异常，例如显示错误消息
        }
    }



}