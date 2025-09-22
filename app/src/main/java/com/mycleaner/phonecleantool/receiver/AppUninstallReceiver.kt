package com.mycleaner.phonecleantool.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AppUninstallReceiver : BroadcastReceiver() {

    private var onUninstallListener: ((String) -> Unit)? = null

    fun setOnUninstallListener(listener: (String) -> Unit) {
        this.onUninstallListener = listener
    }

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_PACKAGE_REMOVED -> {
                if (!intent.getBooleanExtra(Intent.EXTRA_REPLACING, false)) {
                    val packageName = intent.data?.schemeSpecificPart
                    packageName?.let { onUninstallListener?.invoke(it) }
                }
            }
        }
    }
}
