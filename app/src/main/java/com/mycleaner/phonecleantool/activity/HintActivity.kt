package com.mycleaner.phonecleantool.activity

import com.mycleaner.phonecleantool.R
import com.mycleaner.phonecleantool.base.activity.BaseActivity
import com.mycleaner.phonecleantool.command.safeClick
import com.mycleaner.phonecleantool.databinding.ActivityHintBinding
import com.mycleaner.phonecleantooll.base.BaseConstant


class HintActivity  : BaseActivity<ActivityHintBinding>() {
    override fun init() {
        val bundle = intent.extras
        val tag = bundle?.getString(BaseConstant.NEXT_TAG)
        if(tag==getString(R.string.process_manager)){
            binding.tvHint.text=getString(R.string.force_stop)
        }else{
            binding.tvHint.text=getString(R.string.uninstall_app)
        }
        binding.btGotIt.safeClick {
            finish()
        }
    }
}