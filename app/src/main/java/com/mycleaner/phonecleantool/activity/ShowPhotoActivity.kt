package com.mycleaner.phonecleantool.activity

import android.net.Uri
import android.os.Bundle

import com.bumptech.glide.Glide
import com.mycleaner.phonecleantool.R
import com.mycleaner.phonecleantool.base.activity.BaseActivity
import com.mycleaner.phonecleantool.command.safeClick
import com.mycleaner.phonecleantool.databinding.ActivityShowPhotoBinding

class ShowPhotoActivity : BaseActivity<ActivityShowPhotoBinding>() {
    override fun init() {
        binding.ivPhotoBack.safeClick {
            finish()
        }
        var uri= intent.extras?.getString("Uri")
        Glide.with(this)
            .load(Uri.parse(uri))
            .into(binding.photoView);
    }
}