package com.mycleaner.phonecleantool.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.lifecycleScope
import com.cleanmaster.junk.bean.SDcardRubbishResult
import com.cloud.cleanjunksdk.ad.AdvBean
import com.cloud.cleanjunksdk.cache.CacheBean
import com.cloud.cleanjunksdk.filescan.ApkBean
import com.cloud.cleanjunksdk.filescan.LogBean
import com.cloud.cleanjunksdk.filescan.TmpBean
import com.cloud.cleanjunksdk.residual.ResidualBean
import com.cloud.cleanjunksdk.task.CheckSdkCallback
import com.cloud.cleanjunksdk.task.Clean
import com.cloud.cleanjunksdk.task.CleanSDK
import com.cloud.cleanjunksdk.task.JunkScanCallback
import com.cloud.cleanjunksdk.tools.Region
import com.cm.plugincluster.junkengine.junk.bean.MediaFile
import com.cm.plugincluster.junkengine.junk.engine.MEDIA_TYPE
import com.mycleaner.phonecleantool.R
import com.mycleaner.phonecleantool.adapter.ExpandableListAdapter
import com.mycleaner.phonecleantool.adv.Ads
import com.mycleaner.phonecleantool.adv.AdvCheckManager
import com.mycleaner.phonecleantool.adv.AreaKey
import com.mycleaner.phonecleantool.base.BaseApplication
import com.mycleaner.phonecleantool.base.activity.BaseMvvmActivity
import com.mycleaner.phonecleantool.bean.AppInfo
import com.mycleaner.phonecleantool.bean.BannerEvent
import com.mycleaner.phonecleantool.bean.CleanFilesBean
import com.mycleaner.phonecleantool.bean.CleanSdkEvent
import com.mycleaner.phonecleantool.bean.JunkChildBean
import com.mycleaner.phonecleantool.bean.JunkMotherBean
import com.mycleaner.phonecleantool.command.rateUS
import com.mycleaner.phonecleantool.command.readyGoThenKill
import com.mycleaner.phonecleantool.command.safeClick
import com.mycleaner.phonecleantool.databinding.ActivityScanFilesBinding
import com.mycleaner.phonecleantool.utils.AppPrefsUtils
import com.mycleaner.phonecleantool.utils.LogUtil
import com.mycleaner.phonecleantool.utils.SizeUtil
import com.mycleaner.phonecleantool.view.CleanHintDialog
import com.mycleaner.phonecleantool.viewmodel.ScanFilesViewModel
import com.mycleaner.phonecleantooll.base.BaseConstant

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


class ScanFilesActivity : BaseMvvmActivity<ActivityScanFilesBinding, ScanFilesViewModel>() {

    var junkMotherBeans = mutableListOf<JunkMotherBean>()
    var hintList: MutableList<AppInfo> = mutableListOf()
    var isScan: Boolean = false

    //   lateinit var adFileDetector: AdFileDetector
    var job: Job? = null

    lateinit var expandableListAdapter: ExpandableListAdapter

    var cleanHintDialog: CleanHintDialog? = null

    var    advView: ViewGroup?=null


    @SuppressLint("SetTextI18n")
    override fun init() {
        binding.ivScan.setImageAssetsFolder("images/")
        AppPrefsUtils.putLong(
            BaseConstant.JUKE_FILE_TIME,
            System.currentTimeMillis()
        )


        initAdapter()
        initJunkData()
        //   adFileDetector=AdFileDetector()
        EventBus.getDefault().register(this)
        binding.ivBack.safeClick {
            onBack()
        }
        binding.tvRate.setOnClickListener {
            rateUS()
        }
        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                onBack()
            }
        })
        binding.btClean.safeClick {
            EventBus.getDefault().postSticky(CleanFilesBean(size, junkMotherBeans))
            val bundle = Bundle()
            bundle.putString(BaseConstant.NEXT_TAG, getString(R.string.junk_files))
            readyGoThenKill(CleanFilesActivity::class.java,bundle)
        }
        if(AdvCheckManager.params.limitTime < System.currentTimeMillis()){
            advView= getBannerAd(this, AreaKey.cleanPageBottomAdv)
            binding.flAd.visibility= View.VISIBLE
            binding.flAd.addView(advView)
        }

    }



    var size: Long = 0
    var postion = 0

    @SuppressLint("SetTextI18n", "UseCompatLoadingForDrawables")
    fun onScanFinished() {
        junkMotherBeans[1].isShowCheck = true
        expandableListAdapter.notifyDataSetChanged()
        isScan = false
        binding.tvSize.text=getString(R.string.junk_selected)
     //   binding.tvState.text = getString(R.string.scanning_completed)
        binding.tvBg.visibility = View.GONE
        size = 0
        for (junkMotherBean in junkMotherBeans) {
            size += junkMotherBean.size
        }
        binding.tvState.textSize=24f
        binding.tvHint.visibility=View.VISIBLE
        binding.ivScan.visibility = View.GONE
        if (size > 0) {
            binding.tvState.text = "" + SizeUtil.formatSize3(size)
            binding.tvHint.text = getString(R.string.clean_hint)
            binding.tvHint.background=getDrawable(R.drawable.tv_scan_shape)
            binding.btClean.background = getDrawable(R.drawable.btn_selector)
            binding.main.background=getDrawable(R.mipmap.ic_san_bg)
            binding.btClean.isClickable = true
            binding.btClean.isEnabled = true
            binding.tvRate.visibility=View.GONE
        } else {
            binding.tvState.text = "0KB"
            binding.tvHint.text = getString(R.string.your_phone_is_very_clean)
            binding.tvRate.visibility=View.VISIBLE
            AppPrefsUtils.putLong(BaseConstant.CLEAN_SIZE,AppPrefsUtils.getLong(BaseConstant.CLEAN_SIZE)+1)
            LogUtil.setUser(mapOf("total_cleancpl_num" to 1),"user_add")
        }
    }
    fun onBack() {
        if (cleanHintDialog == null) {
            cleanHintDialog = CleanHintDialog(this@ScanFilesActivity)
            cleanHintDialog!!.setmOnClickListener(object : CleanHintDialog.onClickListener {
                override fun onNext(isTodelete: Boolean) {
                    if (isTodelete) {
                        EventBus.getDefault().postSticky(CleanFilesBean(size, junkMotherBeans))
                        var bundle = Bundle()
                        bundle.putString(BaseConstant.NEXT_TAG, getString(R.string.junk_files))
                        readyGoThenKill(CleanFilesActivity::class.java,bundle)
                    }
                }
                override fun onStop() {
                    Ads.showInterstitialAd(this@ScanFilesActivity, "returnHomePageAdv"){
                        readyGoThenKill(MainActivity::class.java)
                    }

                }
            })
        }
        if (isScan) {
            cleanHintDialog!!.toShow(getString(R.string.scanning_in_progress),false)
        } else {
            if (size > 0) {
                var hint = SpannableString(
                    SizeUtil.formatSize3(size) + " " + getString(R.string.cleaning_in_progress)
                )
                hint.setSpan(
                    ForegroundColorSpan(getColor(R.color.btn_nor)),
                    0,
                    SizeUtil.formatSize3(size).length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                cleanHintDialog!!.toShow(hint,true)
            } else {
                Ads.showInterstitialAd(this@ScanFilesActivity, "returnHomePageAdv"){
                    readyGoThenKill(MainActivity::class.java)
                }

            }

        }
    }

    fun initAdapter() {
        expandableListAdapter = ExpandableListAdapter(this@ScanFilesActivity, junkMotherBeans)
        expandableListAdapter.setmonCheckChangeLister(object :
            ExpandableListAdapter.OnCheckChangeLister {
            @SuppressLint("UseCompatLoadingForDrawables")
            override fun onCheckChange() {
               // var total = 0
                size = 0
                for (junkMotherBean in junkMotherBeans) {
                    if (junkMotherBean.isChecked) {
                        size += junkMotherBean.size
                    }
                }
                if (size > 0) {
                    binding.tvState.text = "" + SizeUtil.formatSize3(size)
                    binding.btClean.isClickable = true
                    binding.btClean.isEnabled = true
                    binding.btClean.background = getDrawable(R.drawable.btn_selector)
                } else {
                    binding.tvState.text = "0KB"
                    binding.btClean.isClickable = false
                    binding.btClean.isEnabled = false
                    binding.btClean.background = getDrawable(R.drawable.btn_unclick)
                }
            }

        })
        binding.exJunk.setAdapter(expandableListAdapter)
    }

    fun initJunkData() {
        junkMotherBeans.clear()
        junkMotherBeans.add(
            JunkMotherBean(
                getString(R.string.obsolete_files), R.mipmap.ic_obsolete_files,
                0, mutableListOf()
            )
        )
        junkMotherBeans.add(
            JunkMotherBean(
                getString(R.string.residuals), R.mipmap.ic_residuals,
                0, mutableListOf()
            )
        )
        junkMotherBeans.add(
            JunkMotherBean(
                getString(R.string.ad_junk), R.mipmap.ic_adjunk,
                0, mutableListOf()
            )
        )
        junkMotherBeans.add(
            JunkMotherBean(
                getString(R.string.trash_items), R.mipmap.ic_trash,
                0, mutableListOf()
            )
        )
        viewModel.hintList.observe(this) {
            hintList.addAll(it)
            if (hintList.size > 0) {
                job = CoroutineScope(Dispatchers.Main).launch {
                    while (isActive) { // 检查协程是否活跃
                        if (adFinish&&apkFinish&&temFinish&& System.currentTimeMillis()-startTime>4500) {
                            cancel() // 自动停止协程
                            onScanFinished()
                            return@launch
                        }
                        if(postion<hintList.size-1){
                            binding.tvSize.text = hintList[postion].packageName
                            postion++
                        }
                        delay(200)
                    }
                }
            } else {
                onScanFinished()
            }
        }
        viewModel.loadInstalledApps()
    }
    var startTime:Long=0

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: CleanSdkEvent) {
        // 处理事件（在主线程执行）
        if (event.isInit) {
            sdkClean = event.sdkClean
            startTime= System.currentTimeMillis()
            startScan(sdkClean!!)
        } else {
            initCleanSDK()
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

    var isInitSuccess = false
    var sdkClean: Clean? = null
    fun initCleanSDK() {
        CleanSDK.init(
            BaseApplication.instance,
            this@ScanFilesActivity,
            Region.INTL,
            object : CheckSdkCallback {
                override fun onSuccess(clean: Clean?) {
                    isInitSuccess = true
                    sdkClean = clean
                    sdkClean!!.timeout(60000)
                    startScan(sdkClean!!)
                }

                override fun onError(errCode: Int) {
                    isInitSuccess = false
                    sdkClean = null
//                    Toast.makeText(
//                        this@ScanFilesActivity,
//                        getString(R.string.sdk_init_error),
//                        Toast.LENGTH_SHORT
//                    )
//                        .show()
                }
            }) //initialize SDK

    }

    var adFinish: Boolean = false
    var residualFinish: Boolean = false
    var apkFinish: Boolean = false
    var temFinish: Boolean = false


    fun startScan(sdkClean: Clean) {
        sdkClean.cancel()
        lifecycleScope.launch { // 自动绑定生命周期
            // 在后台线程执行耗时操作
            withContext(Dispatchers.IO) {
                sdkClean.startScan(object : JunkScanCallback {
                    override fun onStart() {
                        isScan = true
                    }

                    override fun error(p0: Int, p1: Throwable?) {

                    }
                    override fun onAdJunkEmitOne(advBean: AdvBean?) {
                        junkMotherBeans[2].size += advBean!!.size
                        junkMotherBeans[2].junkChildrenItems.add(
                            JunkChildBean(
                                advBean!!.name,
                                advBean.path, advBean.size
                            )
                        )

                        runOnUiThread {
                            expandableListAdapter.notifyDataSetChanged()
                        }

                    }

                    override fun onAdJunkSucceed() {
                        runOnUiThread {
                            adFinish = true
                            junkMotherBeans[2].isShowCheck = true
                            expandableListAdapter.notifyDataSetChanged()
                        }


                    }

                    override fun onApkJunkEmitOne(p0: ApkBean?) {
                        junkMotherBeans[0].size += p0!!.size
                        junkMotherBeans[0].junkChildrenItems.add(
                            JunkChildBean(
                                p0!!.name,
                                p0.path, p0.size
                            )
                        )
                        runOnUiThread {
                            expandableListAdapter.notifyDataSetChanged()
                        }

                    }

                    override fun onApkJunkScanSucceed() {
                        runOnUiThread {
                            apkFinish = true
                            junkMotherBeans[0].isShowCheck = true
                            expandableListAdapter.notifyDataSetChanged()
                        }
                    }

                    override fun onTmpJunkEmitOne(p0: TmpBean?) {
                        junkMotherBeans[3].size += p0!!.size
                        junkMotherBeans[3].junkChildrenItems.add(
                            JunkChildBean(
                                p0!!.name,
                                p0.path, p0.size
                            )
                        )
                        runOnUiThread {
                            expandableListAdapter.notifyDataSetChanged()
                        }
                    }

                    override fun onTmpJunkScanSucceed() {
                        runOnUiThread {
                            temFinish = true
                            junkMotherBeans[3].isShowCheck = true
                            expandableListAdapter.notifyDataSetChanged()
                        }
                    }

                    override fun onLogJunkEmitOne(p0: LogBean?) {

                    }

                    override fun onLogJunkScanSucceed() {

                    }

                    override fun onCacheJunkEmitOne(p0: CacheBean?) {


                    }

                    override fun onCacheJunkSucceed() {

                    }

                    override fun onResidualEmitOne(residualBean: ResidualBean?) {

                    }

                    override fun onResidualJunkSucceed() {

                    }

                    override fun onThumbnailJunkEmitOne(p0: SDcardRubbishResult?) {

                    }

                    override fun onThumbnailJunkScanSucceed() {

                    }

                    override fun onMediaFileJunkEmitOne(
                        p0: MEDIA_TYPE?,
                        p1: MediaFile?
                    ) {

                    }

                    override fun onMediaFileJunkScanSucceed(p0: MEDIA_TYPE?) {

                    }

                    override fun onTimeOut() {
                        if (isScan) {
                            runOnUiThread {
                                onScanFinished()
                            }
                        }
                    }

                }, true)

            }
            // 自动切换回主线程更新 UI

        }
    }


    override fun onDestroy() {
        super.onDestroy()
        if (job != null && !job!!.isCancelled) {
            job!!.cancel()
            job=null
        }
        if (cleanHintDialog != null) {
            cleanHintDialog!!.relese()
            cleanHintDialog = null
        }
        binding.ivScan.cancelAnimation()
        binding.ivScan.clearAnimation()
        EventBus.getDefault().unregister(this)
    }


}