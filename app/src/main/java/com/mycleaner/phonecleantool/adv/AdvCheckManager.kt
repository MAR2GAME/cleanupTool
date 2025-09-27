package com.mycleaner.phonecleantool.adv

import android.util.Log
import com.mycleaner.phonecleantool.bean.BannerEvent
import com.mycleaner.phonecleantool.command.AppConfig
import com.mycleaner.phonecleantool.utils.AppPrefsUtils
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.greenrobot.eventbus.EventBus
import java.util.concurrent.TimeUnit

private const val TAG = "AdvCheck"
private const val CONNECT_TIMEOUT = 3L
private const val READ_TIMEOUT = 3L
private const val WRITE_TIMEOUT = 3L
private const val JSON_MEDIA_TYPE = "application/json; charset=utf-8"
private const val DEFAULT_BACKGROUND_DURATION = 180L

// 响应代码常量
private const val CODE_LIMIT_ADV_MAX = 401
private const val CODE_LIMIT_ADV_LIMIT = 402

@Serializable
data class AdvCheckParamsData(
    val FromNature: Boolean,
    val IsFirstOpen: Boolean,
    val InstallTime: Long,
    val InterTimes: Int,
    val BannerTimes: Int,
    val OpenTimes: Int,
    val PackageName: String,
    val AreaKey: String,
    val Times: Int
)

@Serializable
data class CheckAdvResponse(
    val Code: Int,
    val time: String,
    val Msg: String,
    val CanPlay: Boolean,
    val Tag: Int,
    val LimitTime: Long,
    val BackgroundDuration: Long
)

class AdvCheckParams {
    var packageName: String = AppConfig.packageName

    // 使用委托属性管理偏好设置
    var tag: Int by AppPrefsUtils.PreferenceDelegate("tag", 0)
    var fromNature: Boolean by AppPrefsUtils.PreferenceDelegate("fromNature", false)
    var isFirstOpen: Boolean by AppPrefsUtils.PreferenceDelegate("isFirstOpen", true)
    var installTime: Long by AppPrefsUtils.PreferenceDelegate("installTime", 0L)
    var limitTime: Long by AppPrefsUtils.PreferenceDelegate("limitTime", 0L)
    var backgroundDuration: Long by AppPrefsUtils.PreferenceDelegate("backgroundDuration", DEFAULT_BACKGROUND_DURATION)

    // 广告展示次数统计
    var times: Int by AppPrefsUtils.PreferenceDelegate("times", 0)
    var interTimes: Int by AppPrefsUtils.PreferenceDelegate("interTimes", 0)
    var bannerTimes: Int by AppPrefsUtils.PreferenceDelegate("bannerTimes", 0)
    var openTimes: Int by AppPrefsUtils.PreferenceDelegate("openTimes", 0)

    fun toData(areaKey: String): AdvCheckParamsData {
        return AdvCheckParamsData(
            FromNature = fromNature,
            IsFirstOpen = isFirstOpen,
            InstallTime = installTime,
            InterTimes = interTimes,
            BannerTimes = bannerTimes,
            OpenTimes = openTimes,
            PackageName = packageName,
            AreaKey = areaKey,
            Times = times
        )
    }

    fun resetCounters() {
        times = 0
        interTimes = 0
        bannerTimes = 0
        openTimes = 0
    }
}

object AdvCheckManager {
    private val json = Json { ignoreUnknownKeys = true }

    val params: AdvCheckParams = AdvCheckParams()

    // 使用懒加载初始化HTTP客户端
    private val client by lazy {
        OkHttpClient.Builder()
            .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
            .build()
    }

    /**
     * 获取IP信息
     */
    fun getIpInfo(): String? {
        return try {
            val mediaType = JSON_MEDIA_TYPE.toMediaTypeOrNull()
            val emptyBody = "".toRequestBody(mediaType)

            val request = Request.Builder()
                .url(AppConfig.IPInfoUrl)
                .post(emptyBody)
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    response.body?.string()
                } else {
                    Log.w(TAG, "getIpInfo: Unexpected response code ${response.code}")
                    null
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "getIpInfo: Failed to retrieve IP info", e)
            null
        }
    }

    /**
     * 检查广告状态
     * @param areaKey 区域标识
     * @return 是否可以播放广告
     */
    fun checkAdv(areaKey: String): Boolean {
        params.times++

        // 检查是否在限制时间内
        if (params.limitTime > System.currentTimeMillis()) {
            Log.d(TAG, "checkAdv: Currently in limit time period")
            return false
        }

        return try {
            val mediaType = JSON_MEDIA_TYPE.toMediaTypeOrNull()
            val dataParams = params.toData(areaKey)

            Log.d(TAG, "checkAdv: Request for $areaKey - $dataParams")

            val jsonBody = json.encodeToString(dataParams).toRequestBody(mediaType)
            val request = Request.Builder()
                .url(AppConfig.CheckUrl)
                .post(jsonBody)
                .build()

            client.newCall(request).execute().use { response ->
                val body = response.body?.string() ?: ""

                if (response.isSuccessful) {
                    processSuccessfulResponse(body, areaKey)
                } else {
                    Log.w(TAG, "checkAdv: Request failed with code ${response.code}")
                    false
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "checkAdv: Failed to check ad for $areaKey", e)
            false
        }
    }

    /**
     * 处理成功的响应
     */
    private fun processSuccessfulResponse
                (body: String, areaKey: String): Boolean {
        return try {
            val resp = json.decodeFromString<CheckAdvResponse>(body)
            Log.d(TAG, "checkAdv: $areaKey success, CanPlay: ${resp.CanPlay}")

            // 更新参数
            params.tag = resp.Tag

            if (resp.BackgroundDuration > 0) {
                params.backgroundDuration = resp.BackgroundDuration
            }

            when (resp.Code) {
                CODE_LIMIT_ADV_MAX, CODE_LIMIT_ADV_LIMIT -> {
                    params.resetCounters()
                    params.limitTime = System.currentTimeMillis() + resp.LimitTime * 1000
                    EventBus.getDefault().post(BannerEvent(false))
                }
            }

            resp.CanPlay
        } catch (e: Exception) {
            Log.e(TAG, "processSuccessfulResponse: Failed to parse response", e)
            false
        }
    }
}