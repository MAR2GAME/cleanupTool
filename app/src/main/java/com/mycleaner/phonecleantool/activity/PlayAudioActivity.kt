package com.mycleaner.phonecleantool.activity

import android.os.Bundle
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import com.mycleaner.phonecleantool.R
import com.mycleaner.phonecleantool.adv.Ads
import com.mycleaner.phonecleantool.base.activity.BaseActivity
import com.mycleaner.phonecleantool.command.readyGoThenKill
import com.mycleaner.phonecleantool.databinding.ActivityPlayAudioBinding
import com.mycleaner.phonecleantool.utils.AudioVibrationPlayer
import com.mycleaner.phonecleantooll.base.BaseConstant

import kotlin.jvm.java

class PlayAudioActivity : BaseActivity<ActivityPlayAudioBinding>() {
    private lateinit var audioVibrationPlayer: AudioVibrationPlayer
    override fun init() {
        binding.ivPlay.setImageAssetsFolder("images/")
        audioVibrationPlayer = AudioVibrationPlayer(this)
        audioVibrationPlayer.setMediaVolumeToMax()
        playAudioWithVibration()
        audioVibrationPlayer.onPlaybackComplete = {
            Ads.showInterstitialAd(this@PlayAudioActivity, "speakerBeforeFinishAdv"){
                val bundle = Bundle().apply {
                    putString(BaseConstant.NEXT_TAG, getString(R.string.speaker_cleaner))
                }
                readyGoThenKill(FinishedActivity::class.java, bundle)
            }
        }
        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (audioVibrationPlayer.isPlaying()) {
                    Toast.makeText(
                        this@PlayAudioActivity,
                        getString(R.string.speaker_back_hint),
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
            }
        })

    }

    private fun playAudioWithVibration() {
        audioVibrationPlayer.playAudioWithContinuousVibration(R.raw.speaker)
    }

    override fun onResume() {
        super.onResume()
        audioVibrationPlayer.resume()
    }

    override fun onPause() {
        super.onPause()
        // 当Activity暂停时，停止播放和震动
        audioVibrationPlayer.pause()
    }


    override fun onDestroy() {
        super.onDestroy()
        // 释放资源
        audioVibrationPlayer.release()
        binding.ivPlay.cancelAnimation()
        binding.ivPlay.clearAnimation()
    }


}