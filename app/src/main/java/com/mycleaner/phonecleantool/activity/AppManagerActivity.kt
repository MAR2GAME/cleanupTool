package com.mycleaner.phonecleantool.activity

import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat.registerReceiver
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
import com.mycleaner.phonecleantool.base.activity.BaseActivity
import com.mycleaner.phonecleantool.bean.AppInfo
import com.mycleaner.phonecleantool.bean.BannerEvent
import com.mycleaner.phonecleantool.bean.InstallAppEvent
import com.mycleaner.phonecleantool.bean.SortRule
import com.mycleaner.phonecleantool.command.openAppSettings
import com.mycleaner.phonecleantool.command.readyGo
import com.mycleaner.phonecleantool.command.readyGoThenKill
import com.mycleaner.phonecleantool.command.safeClick
import com.mycleaner.phonecleantool.databinding.ActivityAppManagerActivtyBinding
import com.mycleaner.phonecleantool.receiver.AppUninstallReceiver
import com.mycleaner.phonecleantool.utils.AppPrefsUtils
import com.mycleaner.phonecleantool.utils.LogUtil
import com.mycleaner.phonecleantool.utils.SizeUtil
import com.mycleaner.phonecleantooll.base.BaseConstant



import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.collections.filterNot
import kotlin.collections.sortedBy
import kotlin.collections.sortedByDescending


class AppManagerActivity : BaseActivity<ActivityAppManagerActivtyBinding>() {
    var  advView: ViewGroup?=null

    private lateinit var appAdapter: BaseQuickAdapter<AppInfo, BaseViewHolder>
    private val uninstallReceiver = AppUninstallReceiver()
    var rule: SortRule = SortRule.SIZE_DESC
    override fun init() {
        AppPrefsUtils.putLong(
            BaseConstant.APP_TIME,
            System.currentTimeMillis()
        )
        LogUtil.setUser(mapOf("total_appman_num" to 1),"user_add")
        EventBus.getDefault().register(this)
        if(AdvCheckManager.params.limitTime < System.currentTimeMillis()){
            advView = getBannerAd(this, AreaKey.appPageBottomAdv)
            binding.flAd.visibility= View.VISIBLE
            binding.flAd.addView(advView)
        }

        binding.ivAppmanagerBack.safeClick {
            onBack()
        }
        binding.btOk.safeClick {
            var bundle = Bundle()
            bundle.putString(BaseConstant.NEXT_TAG, getString(R.string.app_manager))
            readyGoThenKill(ToNextActivity::class.java, bundle)
        }
        binding.rlSize.setOnClickListener {
            if(appAdapter.data.isEmpty()){
                return@setOnClickListener
            }
            when (rule) {
                SortRule.SIZE_DESC -> {
                    rule = SortRule.SIZE_ASC
                    binding.ivSizeSort.setImageResource(R.mipmap.ic_sort_esc)
                    binding.ivTimeSort.setImageResource(R.mipmap.ic_sort_nor)
                    appAdapter.setList(appAdapter.data.sortedBy { it.size })
                }
                SortRule.SIZE_ASC, SortRule.INSTALL_TIME_DESC, SortRule.INSTALL_TIME_ASC -> {
                    rule = SortRule.SIZE_DESC
                    binding.ivSizeSort.setImageResource(R.mipmap.ic_sort_desc)
                    binding.ivTimeSort.setImageResource(R.mipmap.ic_sort_nor)
                    appAdapter.setList(appAdapter.data.sortedByDescending { it.size })
                }
            }
        }
        binding.rlTime.setOnClickListener {
            if(appAdapter.data.isEmpty()){
                return@setOnClickListener
            }
            when (rule) {
                SortRule.SIZE_DESC, SortRule.SIZE_ASC, SortRule.INSTALL_TIME_ASC -> {
                    rule = SortRule.INSTALL_TIME_DESC
                    binding.ivSizeSort.setImageResource(R.mipmap.ic_sort_nor)
                    binding.ivTimeSort.setImageResource(R.mipmap.ic_sort_desc)
                    appAdapter.setList(appAdapter.data.sortedByDescending { it.firstInstallTime })
                }

                SortRule.INSTALL_TIME_DESC -> {
                    rule = SortRule.INSTALL_TIME_ASC
                    appAdapter.setList(appAdapter.data.sortedBy { it.firstInstallTime })
                    binding.ivSizeSort.setImageResource(R.mipmap.ic_sort_nor)
                    binding.ivTimeSort.setImageResource(R.mipmap.ic_sort_esc)
                }
            }
        }


        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                onBack()
            }
        })

        // 注册卸载监听
        uninstallReceiver.setOnUninstallListener {packageName ->
            // 处理应用卸载事件
            appAdapter.setList(appAdapter.data.filterNot { it.packageName==packageName })
        }
        registerReceiver(
            uninstallReceiver,
            IntentFilter(Intent.ACTION_PACKAGE_REMOVED).apply {
                addDataScheme("package")
            }
        )
    }

    fun onBack(){
        Ads.showInterstitialAd(this@AppManagerActivity, "returnHomePageAdv"){
            LogUtil.log("enter_homepage", mapOf("referrer_name" to "appman"))
            readyGoThenKill(MainActivity::class.java)
        }
    }


    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: InstallAppEvent) {
        if (event.data.isEmpty()) {
            binding.rlNodata.visibility = View.VISIBLE
        } else {
            binding.rlNodata.visibility = View.GONE
        }
        initAdapter()
        appAdapter.setList(event.data)
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
        appAdapter = object : BaseQuickAdapter<AppInfo, BaseViewHolder>(R.layout.item_installapp) {
            override fun convert(
                holder: BaseViewHolder,
                item: AppInfo
            ) {

                item.let {
                    holder.setText(R.id.tv_name, it.name)
                    Glide.with(context).load(it.icon).into(holder.getView<ImageView>(R.id.iv_app))
                    holder.setText(
                        R.id.tv_information, "" + SizeUtil.formatSize3(it.size) + " " + sdf.format(
                            Date(it.firstInstallTime)
                        )
                    )
                }
                holder.getView<Button>(R.id.bt_uninstall).safeClick {
                    openAppSettings(
                        this@AppManagerActivity, item.packageName
                    )
                    lifecycleScope.launch {
                        delay(300)
                        var bundle = Bundle()
                        bundle.putString(
                            BaseConstant.NEXT_TAG,
                            getString(R.string.app_manager)
                        )
                        readyGo(HintActivity::class.java, bundle)
                    }
                }
            }
        }
        binding.rcApp.adapter = appAdapter
        binding.rcApp.layoutManager =
            LinearLayoutManager(this@AppManagerActivity, RecyclerView.VERTICAL, false)

    }


    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
        unregisterReceiver(uninstallReceiver)
    }
}