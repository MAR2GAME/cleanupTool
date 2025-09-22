package com.mycleaner.phonecleantool.push

import android.util.Log
import cn.thinkingdata.analytics.TDAnalytics
import com.mycleaner.phonecleantool.adv.AdvCheckManager
import com.mycleaner.phonecleantool.command.AppConfig
import com.mycleaner.phonecleantool.utils.AppPrefsUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.intellij.lang.annotations.Language
import java.util.Locale
import java.util.concurrent.TimeUnit

@Serializable
data class PushData(
    val PackageName: String,
    val Tag: Int,
    val Token: String,
    val Language: String,
    val DistinctId: String,
)

object PushManager {
    private const val TAG = "PushManager"
    private const val CONNECT_TIMEOUT = 5L
    private const val READ_TIMEOUT = 5L
    private const val WRITE_TIMEOUT = 5L
    private const val JSON_MEDIA_TYPE = "application/json; charset=utf-8"

    // 使用懒加载初始化HTTP客户端
    private val client by lazy {
        OkHttpClient.Builder()
            .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
            .build()
    }

    // 使用懒加载初始化JSON序列化器
    private val json by lazy { Json { ignoreUnknownKeys = true } }

    // 推送令牌的偏好设置委托
    var pushToken: String by AppPrefsUtils.PreferenceDelegate("pushToken", "")

    /**
     * 通知服务器应用退出
     */
    fun notifyServerAppExit() {

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = sendExitNotification()
                logResponse(response)
            } catch (e: Exception) {
                Log.e(TAG, "notifyServerAppExit: Failed to send exit notification", e)
            }
        }
    }

    /**
     * 发送退出通知到服务器
     */
    private suspend fun sendExitNotification(): String {
        return withContext(Dispatchers.IO) {
            val mediaType = JSON_MEDIA_TYPE.toMediaTypeOrNull()
            val pushData = createPushData()
            Log.i(TAG, "Sending exit notification: $pushData")
            val jsonBody = json.encodeToString(pushData).toRequestBody(mediaType)
            val request = Request.Builder()
                .url(AppConfig.PushUrl)
                .post(jsonBody)
                .build()

            client.newCall(request).execute().use { response ->
                response.body?.string() ?: ""
            }
        }
    }

    /**
     * 创建推送数据对象
     */
    private fun createPushData(): PushData {
        return PushData(
            PackageName = AppConfig.packageName,
            Tag = AdvCheckManager.params.tag,
            Token = pushToken,
            Language = Locale.getDefault().language,
            TDAnalytics.getDistinctId(),
        )
    }

    /**
     * 记录服务器响应
     */
    private fun logResponse(responseBody: String) {
        if (responseBody.isNotBlank()) {
            Log.i(TAG, "Server response: $responseBody")
        } else {
            Log.w(TAG, "Server returned empty response")
        }
    }

    /**
     * 强制发送退出通知（忽略错误）
     * 适用于应用退出时确保通知发送
     */
    fun forceNotifyServerAppExit() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                sendExitNotification()
            } catch (e: Exception) {
                // 强制发送时忽略错误，只记录日志
                Log.w(TAG, "Force notify failed: ${e.message}")
            }
        }
    }

    /**
     * 检查推送令牌是否有效
     */
    fun hasValidToken(): Boolean {
        return pushToken.isNotBlank()
    }

    /**
     * 清除推送令牌
     */
    fun clearToken() {
        pushToken = ""
        Log.i(TAG, "Push token cleared")
    }
}