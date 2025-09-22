package com.mycleaner.phonecleantool.activity

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.lifecycleScope
import com.mycleaner.phonecleantool.R
import com.mycleaner.phonecleantool.base.activity.BaseActivity
import com.mycleaner.phonecleantool.command.readyGoThenKill
import com.mycleaner.phonecleantool.databinding.ActivityFinishedBinding
import com.mycleaner.phonecleantool.utils.LogUtil
import com.mycleaner.phonecleantooll.base.BaseConstant


import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class FinishedActivity : BaseActivity<ActivityFinishedBinding>() {
    override fun init() {
        val bundle = intent.extras
        val tag = bundle?.getString(BaseConstant.NEXT_TAG)
        when (tag) {
            getString(R.string.junk_files) -> {
                binding.content.setBackgroundColor(getColor(R.color.btn_nor))
                LogUtil.log("complete_clean", mapOf())
            }
            getString(R.string.process_manager) -> {
                binding.content.setBackgroundColor(getColor(R.color.process_bg))
                LogUtil.log("complete_processman", mapOf())
            }
            getString(R.string.app_manager) -> {
                binding.content.setBackgroundColor(getColor(R.color.app_bg))
                LogUtil.log("complete_appman", mapOf())
            }
            getString(R.string.speaker_cleaner) -> {
                binding.content.setBackgroundColor(getColor(R.color.speaker_bg))
                LogUtil.log("complete_speaker", mapOf())
            }
            getString(R.string.battery_info) -> {
                binding.content.setBackgroundColor(getColor(R.color.battery_bg))
                LogUtil.log("complete_battery", mapOf())
            }
            getString(R.string.large_file_cleaner) -> {
                binding.content.setBackgroundColor(getColor(R.color.large_bg))
                LogUtil.log("complete_largefile", mapOf())
            }
            getString(R.string.screenshot_manager)->{
                binding.content.setBackgroundColor(getColor(R.color.screenshot_bg))
                LogUtil.log("complete_screenshotman", mapOf())
            }
        }
        binding.ivSuccess.setImageAssetsFolder("images/")
        binding.ivSuccess.addAnimatorListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                if(!isToTask){
                    isToTask=true
                    readyGoThenKill(TaskListActivity::class.java, bundle)
                }
            }
        })

        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
            }
        })


    }
    var isToTask=false

    override fun onDestroy() {
        super.onDestroy()
        binding.ivSuccess.cancelAnimation()
        binding.ivSuccess.clearAnimation()
    }
}