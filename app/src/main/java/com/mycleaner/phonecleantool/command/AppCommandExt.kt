package com.mycleaner.phonecleantool.command


import android.Manifest
import android.annotation.SuppressLint
import android.app.*
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.core.app.NotificationManagerCompat
import androidx.core.net.toUri
import com.mycleaner.phonecleantool.utils.LogUtil
import com.mycleaner.phonecleantooll.base.BaseConstant

import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone


/**
 * startActivity
 *
 * @param clazz
 */
fun Activity.readyGo(clazz: Class<*>?) {
    val intent = Intent(this, clazz)
    startActivity(intent)
}

fun getDetailCurrentTime(time:Long): String {
    val date = Date(time)
    val strDateFormat = "yyyy-MM-dd HH:mm:ss"
    val format = SimpleDateFormat(strDateFormat)
    format.timeZone = TimeZone.getDefault()
    return format.format(date)
}


/**
 * 设置多链接TextView
 *
 * @param linkTexts 可点击文本数组
 * @param urls 对应的URL数组
 * @param linkColor 链接颜色
 * @param showUnderline 是否显示下划线
 */
fun Activity.setupMultiLinkTextView(
    textView: TextView,
    linkTexts: Array<String>,
    urls: Array<String>,
    linkColor: Int,
    showUnderline: Boolean,
    tag: String
) {

    val fullText = textView.text.toString()
    val spannableString = SpannableString(fullText)
    // 为每个链接文本添加点击事件
    linkTexts.forEachIndexed { index, linkText ->
        val url = urls[index]
        var startIndex = fullText.indexOf(linkText)

        if (startIndex == -1) {
            // 如果找不到文本，尝试小写匹配
            startIndex = fullText.indexOf(linkText, ignoreCase = true)
            if (startIndex == -1) return@forEachIndexed
        }

        val endIndex = startIndex + linkText.length

        // 创建可点击区域
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                if(url== BaseConstant.PRIVACY_URL){
                    LogUtil.log("check_privacy", mapOf("referrer_name" to tag ))
                }else{
                    LogUtil.log("check_terms", mapOf("referrer_name" to tag ))
                }
                val intent = Intent(Intent.ACTION_VIEW, url.toUri())
                startActivity(intent)
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                // 自定义链接样式
                ds.color = linkColor
                ds.isUnderlineText = showUnderline
            }
        }

        // 设置可点击区域
        spannableString.setSpan(
            clickableSpan,
            startIndex,
            endIndex,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        // 设置链接颜色
        val colorSpan = ForegroundColorSpan(linkColor)
        spannableString.setSpan(
            colorSpan,
            startIndex,
            endIndex,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        val boldSpan: StyleSpan = StyleSpan(Typeface.BOLD)
        spannableString.setSpan(
            boldSpan,
            startIndex,
            endIndex,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }

    textView.text = spannableString
    textView.movementMethod = LinkMovementMethod.getInstance()

    // 移除点击后的背景高亮
    textView.highlightColor = Color.TRANSPARENT
}


/**
 * startActivity with bundle
 *
 * @param clazz
 * @param bundle
 */
fun Activity.readyGo(clazz: Class<*>?, bundle: Bundle?) {
    val intent = Intent(this, clazz)
    if (null != bundle) {
        intent.putExtras(bundle)
    }
    startActivity(intent)
}



/**
 * startActivity then finish
 *
 * @param clazz
 */
fun Activity.readyGoThenKill(clazz: Class<*>?) {
    val intent = Intent(this, clazz)
    startActivity(intent)
    finish()
}


 fun Activity.checkUsageStatsPermission(): Boolean {
    val appOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
    val mode = appOps.checkOpNoThrow(
        AppOpsManager.OPSTR_GET_USAGE_STATS,
        android.os.Process.myUid(), packageName
    )
    return mode == AppOpsManager.MODE_ALLOWED
}


/**
 * startActivity with bundle then finish
 *
 * @param clazz
 * @param bundle
 */
fun Activity.readyGoThenKill(clazz: Class<*>?, bundle: Bundle?) {
    val intent = Intent(this, clazz)
    if (null != bundle) {
        intent.putExtras(bundle)
    }
    startActivity(intent)
    finish()
}



fun Activity.openAppSettings(context: Context, packageName: String? = null,code: Int=0) {
    try {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val targetPackageName = packageName ?: context.packageName
        val uri = Uri.fromParts("package", targetPackageName, null)
        intent.data = uri

        if(code!=0){
            startActivityForResult(intent,code)
        }else{
            startActivity(intent)
        }

    } catch (e: Exception) {
        e.printStackTrace()
    }
}

 fun Activity.openAppInfoSettings(pkgName: String?) {
    try {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        intent.data = ("package:$pkgName").toUri()
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    } catch (e: java.lang.Exception) {
        // 最终回退方案
        try {
            val intent = Intent(Settings.ACTION_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        } catch (e: java.lang.Exception) {
        }
    }
}



const val REQUEST_CODE_STORAGE_PERMISSION = 1001

const val REQUEST_CODE_MANAGE_STORAGE = 1002

const val REQUEST_CODE_NOTIFICATION = 1003



const val REQUEST_CODE_PROCESS= 1004

const val REQUEST_CODE_FORCE_STOP=1005




//const val REQUEST_CODE_STORAGE_PERMISSION = 1001
//
//const val REQUEST_CODE_MANAGE_STORAGE = 1002


@SuppressLint("CheckResult")
fun Activity.checkAndRequestPermissions(
    permission: String,
    toTransparent: () -> Unit
) {
    when (permission) {
        Manifest.permission.READ_EXTERNAL_STORAGE -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
                try {
                    // 方法1：使用专属 Intent
                    val intent =
                        Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                    intent.data = ("package:$packageName").toUri()
                    startActivityForResult(intent, REQUEST_CODE_MANAGE_STORAGE)
                } catch (e: ActivityNotFoundException) {
                    try {
                        // 方法2：备用专属 Intent（某些设备）
                        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
                        intent.data = ("package:$packageName").toUri()
                        startActivityForResult(intent, REQUEST_CODE_MANAGE_STORAGE)
                    } catch (ex: ActivityNotFoundException) {
                        // 方法3：跳转到全局设置页面
                        val fallbackIntent =
                            Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                        startActivityForResult(fallbackIntent, REQUEST_CODE_MANAGE_STORAGE)
                    }
                }
                toTransparent()
            } else {
                requestPermissions(
                    arrayOf<String>(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ),
                    REQUEST_CODE_STORAGE_PERMISSION
                )
            }
        }
    }

}

fun Activity.openUsageStatsSettings(toTransparent: () -> Unit) {
    val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
    // 尝试直接跳转到本应用的权限设置页面
    // 某些设备可能需要添加包名参数
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        intent.data = android.net.Uri.parse("package:${packageName}")
    }
    try {
        startActivityForResult(intent, REQUEST_CODE_PROCESS)
    } catch (e: Exception) {
        // 如果上述方法失败，尝试通用方法
        val fallbackIntent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
        startActivityForResult(fallbackIntent,REQUEST_CODE_PROCESS)
    } finally {
        toTransparent()
    }
}





fun getVersionName(context: Context): String {
    var manager = context.applicationContext.packageManager
    var code = ""
    try {
        var info = manager.getPackageInfo(context.applicationContext.packageName, 0)
        code = info.versionName!!
    } catch (e: PackageManager.NameNotFoundException) {
        e.printStackTrace()
    }
    return code
}


// 扩展函数实现防重复点击
fun View.safeClick(interval: Long = 1000, action: () -> Unit) {
    var lastClickTime = 0L
    setOnClickListener {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastClickTime > interval) {
            lastClickTime = currentTime
            action()
        }
    }
}





fun Activity.hasStoragePermission(): Boolean {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        return Environment.isExternalStorageManager()
    } else {
        return checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) === PackageManager.PERMISSION_GRANTED
    }

}
fun Activity.rateUS(){
    try {
        val uri = Uri.parse("https://play.google.com/store/apps/details?id=" +
                "" + getPackageName())
        val intent = Intent(Intent.ACTION_VIEW
            , uri)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    } catch (e: Exception) {
        e.printStackTrace()
        // 处理异常，例如显示错误消息
    }
}

fun Activity.checkNotificationPermission(): Boolean {
    val notificationManager = NotificationManagerCompat.from(this)
    val areNotificationsEnabled = notificationManager.areNotificationsEnabled()
    // 对于Android 13+，还需要检查是否明确授予了权限
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val hasNotificationPermission =
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) ==
                    android.content.pm.PackageManager.PERMISSION_GRANTED
        return areNotificationsEnabled && hasNotificationPermission
    }

    return areNotificationsEnabled
}


































