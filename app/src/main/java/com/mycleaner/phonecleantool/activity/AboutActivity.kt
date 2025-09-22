package com.mycleaner.phonecleantool.activity

import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.google.android.gms.common.wrappers.Wrappers.packageManager
import com.mycleaner.phonecleantool.base.activity.BaseActivity
import com.mycleaner.phonecleantool.command.safeClick
import com.mycleaner.phonecleantool.command.setupMultiLinkTextView
import com.mycleaner.phonecleantooll.base.BaseConstant
import com.mycleaner.phonecleantool.R
import com.mycleaner.phonecleantool.databinding.ActivityAboutBinding


class AboutActivity : BaseActivity<ActivityAboutBinding>() {
    override fun init() {
        binding.ivAboutBack.safeClick {
            finish()
        }
        setupMultiLinkTextView(
            binding.tvAgreement,
            arrayOf(
                getString(R.string.terms_of_use),
                getString(R.string.privacy_policies)
            ),
            arrayOf(
                BaseConstant.TERMS_URL,
                BaseConstant.PRIVACY_URL,
            ),
            ContextCompat.getColor(this, R.color.btn_nor),
            false,"about"
        )
        binding.tvCode.text=getString(R.string.version)+getAppVersionName()

    }
    fun getAppVersionName(): String {
        return try {
            val packageInfo = packageManager.getPackageInfo(packageName, 0)
            packageInfo.versionName ?: ""
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            ""
        }
    }


}