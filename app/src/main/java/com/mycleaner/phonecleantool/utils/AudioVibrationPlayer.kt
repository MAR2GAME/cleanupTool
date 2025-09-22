package com.mycleaner.phonecleantool.utils

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.annotation.RawRes
class AudioVibrationPlayer(private val context: Context) {

    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private var isPlay= false
    private var vibrationThread: Thread? = null
    private var shouldVibrate = false

    // 播放完成回调
    var onPlaybackComplete: (() -> Unit)? = null

    init {
        // 初始化震动器
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibrator = vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }

    /**
     * 播放 raw 资源中的音频文件并持续震动
     * @param audioResourceId raw 资源ID
     * @param vibrationIntensity 震动强度 (0-255)，默认 255（最大强度）
     */
    fun playAudioWithContinuousVibration(@RawRes audioResourceId: Int, vibrationIntensity: Int = 255) {
        if(isPlay){
            return
        }
        try {
            // 释放之前的媒体播放器
            release()
            // 创建并配置媒体播放器
            mediaPlayer = MediaPlayer.create(context, audioResourceId).apply {
                setOnCompletionListener {
                    stopVibration()
                    isPlay = false
                    onPlaybackComplete?.invoke()
                }

                setOnErrorListener { mp, what, extra ->
                    stopVibration()
                    isPlay = false
                    false
                }

                // 开始播放
                start()
                isPlay = true

                // 开始持续震动
                startContinuousVibration(vibrationIntensity)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            onPlaybackComplete?.invoke()
        }
    }


    /**
     * 开始持续震动
     * @param intensity 震动强度 (0-255)
     */
    private fun startContinuousVibration(intensity: Int = 255) {
        if (vibrator?.hasVibrator() != true) return

        shouldVibrate = true

        // 创建一个线程来处理持续震动
        vibrationThread = Thread {
            while (shouldVibrate && isPlay) {
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        // Android 8.0+ 使用新的震动API
                        val effect = VibrationEffect.createOneShot(500, intensity)
                        vibrator?.vibrate(effect)
                    } else {
                        // 旧版本API
                        @Suppress("DEPRECATION")
                        vibrator?.vibrate(500)
                    }

                    // 等待震动完成
                    Thread.sleep(500)
                } catch (e: InterruptedException) {
                    // 线程被中断，退出循环
                    break
                } catch (e: Exception) {
                    e.printStackTrace()
                    break
                }
            }
        }

        vibrationThread?.start()
    }

    /**
     * 停止震动
     */
    private fun stopVibration() {
        shouldVibrate = false
        vibrationThread?.interrupt()
        vibrator?.cancel()
    }

    /**
     * 暂停播放和震动
     */
    fun pause() {
        mediaPlayer?.pause()
        stopVibration()
        isPlay = false
    }

    /**
     * 恢复播放和震动
     */
    fun resume() {
        mediaPlayer?.start()
        isPlay = true
        startContinuousVibration()
    }

    /**
     * 停止播放和震动
     */
    fun stop() {
        mediaPlayer?.stop()
        stopVibration()
        isPlay = false
    }

    /**
     * 释放资源
     */
    fun release() {
        mediaPlayer?.release()
        mediaPlayer = null
        stopVibration()
        isPlay = false
    }

    /**
     * 检查是否正在播放
     */
    fun isPlaying(): Boolean {
        return isPlay
    }

    private val audioManager: AudioManager by lazy {
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }

    /**
     * 将媒体音量设置为最大
     * @param showUi 是否显示音量调整UI
     */
    fun setMediaVolumeToMax(showUi: Boolean = false) {
        try {
            val maxVolume = getMaxMediaVolume()

            // 设置媒体音量到最大
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                // Android 9.0+ 使用新API
                audioManager.setStreamVolume(
                    AudioManager.STREAM_MUSIC,
                    maxVolume,
                    if (showUi) AudioManager.FLAG_SHOW_UI else 0
                )
            } else {
                // 旧版本API
                @Suppress("DEPRECATION")
                audioManager.setStreamVolume(
                    AudioManager.STREAM_MUSIC,
                    maxVolume,
                    if (showUi) AudioManager.FLAG_SHOW_UI else 0
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }



    /**
     * 获取媒体音量最大值
     */
    fun getMaxMediaVolume(): Int {
        return audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
    }

}