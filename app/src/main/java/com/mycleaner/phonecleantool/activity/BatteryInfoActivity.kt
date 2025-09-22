package com.mycleaner.phonecleantool.activity

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.lifecycleScope
import com.mycleaner.phonecleantool.R
import com.mycleaner.phonecleantool.adv.Ads
import com.mycleaner.phonecleantool.adv.AdvCheckManager
import com.mycleaner.phonecleantool.adv.AreaKey
import com.mycleaner.phonecleantool.base.activity.BaseMvvmActivity
import com.mycleaner.phonecleantool.bean.BannerEvent
import com.mycleaner.phonecleantool.command.readyGoThenKill
import com.mycleaner.phonecleantool.command.safeClick
import com.mycleaner.phonecleantool.databinding.ActivityBatteryInfoBinding
import com.mycleaner.phonecleantool.utils.AppPrefsUtils
import com.mycleaner.phonecleantool.utils.BatteryInfoHelper
import com.mycleaner.phonecleantool.utils.LogUtil
import com.mycleaner.phonecleantool.viewmodel.BatteryInfoViewModel
import com.mycleaner.phonecleantooll.base.BaseConstant


import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class BatteryInfoActivity : BaseMvvmActivity<ActivityBatteryInfoBinding, BatteryInfoViewModel>() {
    var advView: ViewGroup?=null
    lateinit var batteryInfoHelper: BatteryInfoHelper
    override fun init() {
        EventBus.getDefault().register(this)
        AppPrefsUtils.putLong(
            BaseConstant.BATTERY_TIME,
            System.currentTimeMillis()
        )
        LogUtil.setUser(mapOf("total_battery_num" to 1),"user_add")
        batteryInfoHelper = BatteryInfoHelper(this@BatteryInfoActivity)
        observeData()
        binding.ivBatteryBack.safeClick {
            onBack()
        }
        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                onBack()
            }
        })
        binding.btGotIt.safeClick {
            var bundle = Bundle()
            bundle.putString(BaseConstant.NEXT_TAG, getString(R.string.battery_info))
            readyGoThenKill(ToNextActivity::class.java, bundle)
        }
        if(AdvCheckManager.params.limitTime < System.currentTimeMillis()){
            advView = getBannerAd(this, AreaKey.batteryPageBottomAdv)
            binding.flAd.visibility= View.VISIBLE
            binding.flAd.addView(advView)
        }

    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: BannerEvent) {
        if (binding.flAd.visibility == View.VISIBLE) {
            binding.flAd.removeView(advView)
            binding.flAd.visibility = View.GONE
            adViewSet.clear()
        }
    }

    fun onBack(){
        Ads.showInterstitialAd(this@BatteryInfoActivity, "returnHomePageAdv"){
            LogUtil.log("enter_homepage", mapOf("referrer_name" to "battery"))
            readyGoThenKill(MainActivity::class.java)
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    override fun onPause() {
        super.onPause()
        // 暂停时停止更新以节省资源
        viewModel.stopPeriodicUpdate()
    }

    override fun onResume() {
        super.onResume()
        // 恢复时重新开始更新（如果之前是开启状态）
        viewModel.startPeriodicUpdate(60000L, batteryInfoHelper)
    }

    private fun observeData() {
        lifecycleScope.launch {
            viewModel.batteryInfo.collectLatest { batteryInfo ->
                batteryInfo?.let {
                    binding.tvTemperature.text = "${it.temperature}℃"
                    binding.tvPercentage.text = "${it.level}%"
                    binding.tvProgress.text = "${it.level}%"
                    binding.tvTime.text = "${it.availableTime}"
                    binding.progressBar.setProgress(it.level)
                    binding.tvVoltage.text="${it.voltage}mV"
                    binding.tvCapacity.text="${(it.capacity * (it.level/100.0)).toInt()}/${it.capacity}MAh"
                    binding.tvHealth.text="${it.health}"
                    binding.tvBattreyType.text="${it.technology}"

                }
            }
        }


        lifecycleScope.launch {
            viewModel.errorMessage.collectLatest { errorMessage ->
                errorMessage?.let {
                    Toast.makeText(
                        this@BatteryInfoActivity,
                        getString(R.string.failed_to_obtain_battery_information),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }


}