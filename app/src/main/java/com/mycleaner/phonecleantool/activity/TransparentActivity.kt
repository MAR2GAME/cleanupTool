package com.mycleaner.phonecleantool.activity

import com.mycleaner.phonecleantool.base.activity.BaseActivity
import com.mycleaner.phonecleantool.command.safeClick
import com.mycleaner.phonecleantool.databinding.ActivityTransparentBinding


class TransparentActivity : BaseActivity<ActivityTransparentBinding>() {

    override fun init() {
        binding.btOk.safeClick {
            finish()
        }
    }
}