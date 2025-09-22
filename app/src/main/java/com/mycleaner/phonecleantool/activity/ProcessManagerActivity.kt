package com.mycleaner.phonecleantool.activity

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.mycleaner.phonecleantool.R
import com.mycleaner.phonecleantool.adv.Ads
import com.mycleaner.phonecleantool.adv.AdvCheckManager
import com.mycleaner.phonecleantool.adv.AreaKey
import com.mycleaner.phonecleantool.base.activity.BaseMvvmActivity
import com.mycleaner.phonecleantool.bean.BannerEvent
import com.mycleaner.phonecleantool.bean.ProcessEvent
import com.mycleaner.phonecleantool.bean.RunningAppInfo
import com.mycleaner.phonecleantool.command.openAppSettings
import com.mycleaner.phonecleantool.command.readyGo
import com.mycleaner.phonecleantool.command.readyGoThenKill
import com.mycleaner.phonecleantool.command.safeClick
import com.mycleaner.phonecleantool.databinding.ActivityProcessManagerBinding
import com.mycleaner.phonecleantool.utils.AppPrefsUtils
import com.mycleaner.phonecleantool.utils.LogUtil
import com.mycleaner.phonecleantool.view.ProgressDialog
import com.mycleaner.phonecleantool.viewmodel.ProcessManagerViewModel
import com.mycleaner.phonecleantooll.base.BaseConstant


import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.concurrent.TimeUnit

class ProcessManagerActivity :
    BaseMvvmActivity<ActivityProcessManagerBinding, ProcessManagerViewModel>() {

    private var isToload = false

    var advView : ViewGroup?=null
    private var progressDialog: ProgressDialog? = null
    private lateinit var appAdapter: BaseQuickAdapter<RunningAppInfo, BaseViewHolder>
    override fun init() {
        EventBus.getDefault().register(this)
        LogUtil.setUser(mapOf("total_processman_num" to 1),"user_add")
        AppPrefsUtils.putLong(
            BaseConstant.PROCESS_TIME,
            System.currentTimeMillis()
        )
        viewModel.memoryInfo.observe(this) {
            binding.tvMemoryUsage.text = "${it.memoryUsage}%"
        }
        viewModel.isShowLoading.observe(this) {
            if (progressDialog == null) {
                progressDialog = ProgressDialog(this@ProcessManagerActivity)
            }
            if (it) {
                progressDialog!!.toShow()
            } else {
                progressDialog!!.dismiss()
            }
        }
        viewModel._apps.observe(this) {
            binding.tvSize.text = "${it.size}"
            if (it.size == 0) {
                binding.rlNodata.visibility = View.VISIBLE
            } else {
                binding.rlNodata.visibility = View.GONE
                appAdapter.setList(it)
            }
        }
        binding.ivProcessBack.safeClick {
            onBack()
        }
        binding.btStart.safeClick {
            var bundle = Bundle()
            bundle.putString(BaseConstant.NEXT_TAG, getString(R.string.process_manager))
            readyGoThenKill(ToNextActivity::class.java, bundle)
        }
        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                onBack()
            }
        })
        if(AdvCheckManager.params.limitTime < System.currentTimeMillis()){
            advView = getBannerAd(this, AreaKey.processPageBottomAdv)
            binding.flAd.visibility= View.VISIBLE
            binding.flAd.addView(advView)
        }
    }
    fun onBack(){
        Ads.showInterstitialAd(this@ProcessManagerActivity, "returnHomePageAdv"){
            LogUtil.log("enter_homepage", mapOf("referrer_name" to "processman"))
            readyGoThenKill(MainActivity::class.java)
        }

    }

    override fun onResume() {
        super.onResume()
        viewModel.getMemoryInfo()
        if (!isToload) {
            isToload = true
        } else {
            viewModel.loadRunningApps()
        }

    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: ProcessEvent) {
        initAdapter()
        binding.tvSize.text = "${event.data.size}"
        if (event.data.size == 0) {
            binding.rlNodata.visibility = View.VISIBLE
        } else {
            binding.rlNodata.visibility = View.GONE
            appAdapter.setList(event.data)
        }
        EventBus.getDefault().removeStickyEvent(event)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: BannerEvent) {
        if (binding.flAd.visibility == View.VISIBLE) {
            binding.flAd.removeView(advView)
            binding.flAd.visibility = View.GONE
            adViewSet.clear()
        }
    }





    fun initAdapter() {
        appAdapter =
            object : BaseQuickAdapter<RunningAppInfo, BaseViewHolder>(R.layout.item_process) {
                override fun convert(
                    holder: BaseViewHolder,
                    item: RunningAppInfo
                ) {
                    item.let {
                        holder.setText(R.id.tv_name, it.appName)
                        Glide.with(context).load(it.icon)
                            .into(holder.getView<ImageView>(R.id.iv_app))
                    }

                    holder.getView<Button>(R.id.bt_stop).safeClick {

                        openAppSettings(
                            this@ProcessManagerActivity, item.packageName
                        )
                        lifecycleScope.launch {
                            delay(200)
                            var bundle = Bundle()
                            bundle.putString(
                                BaseConstant.NEXT_TAG,
                                getString(R.string.process_manager)
                            )
                            readyGo(HintActivity::class.java, bundle)
                        }

                    }


                }

            }

        binding.rcProcess.adapter = appAdapter
        binding.rcProcess.layoutManager =
            LinearLayoutManager(this@ProcessManagerActivity, RecyclerView.VERTICAL, false)


    }

    override fun onDestroy() {
        super.onDestroy()
        if (progressDialog != null) {
            progressDialog!!.relese()
            progressDialog = null
        }
        EventBus.getDefault().unregister(this)
    }

}