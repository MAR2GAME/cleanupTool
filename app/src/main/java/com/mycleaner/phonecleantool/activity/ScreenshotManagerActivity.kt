package com.mycleaner.phonecleantool.activity

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.activity.OnBackPressedCallback
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
import com.mycleaner.phonecleantool.bean.BannerEvent

import com.mycleaner.phonecleantool.bean.Screenshot
import com.mycleaner.phonecleantool.bean.ScreenshotEvent
import com.mycleaner.phonecleantool.bean.ScreenshotsData
import com.mycleaner.phonecleantool.command.readyGo
import com.mycleaner.phonecleantool.command.readyGoThenKill
import com.mycleaner.phonecleantool.command.safeClick
import com.mycleaner.phonecleantool.databinding.ActivityScreeshotManagerBinding
import com.mycleaner.phonecleantool.utils.AppPrefsUtils
import com.mycleaner.phonecleantool.utils.LogUtil
import com.mycleaner.phonecleantool.utils.SizeUtil
import com.mycleaner.phonecleantool.view.CleanFileDialog
import com.mycleaner.phonecleantooll.base.BaseConstant
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.text.SimpleDateFormat

class ScreenshotManagerActivity : BaseActivity<ActivityScreeshotManagerBinding>() {

  lateinit var appAdapter: BaseQuickAdapter<Screenshot, BaseViewHolder>

  var selectData: MutableList<Screenshot> =mutableListOf()

    var cleanFileDialog: CleanFileDialog?=null
    var deleteSize:Long=0

    var isSelectAll: Boolean=false
    var   advView:ViewGroup?=null

    override fun init() {
        EventBus.getDefault().register(this)
        LogUtil.setUser(mapOf("total_screenshot_num" to 1),"user_add")
        AppPrefsUtils.putLong(
            BaseConstant.SCREENSHOT_TIME,
            System.currentTimeMillis()
        )
        binding.ivScreenshotBack.safeClick {
            onBack()
        }

        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                onBack()
            }
        })
        if(AdvCheckManager.params.limitTime < System.currentTimeMillis()){
            advView = getBannerAd(this, AreaKey.screenshotPageBottomAdv)
            binding.flAd.visibility= View.VISIBLE
            binding.flAd.addView(advView)
        }
        binding.rlSelectAll.setOnClickListener {
            if (appAdapter.data.isNotEmpty()){
                isSelectAll = !isSelectAll
                selectData.clear()
                deleteSize=0
                for (latgeFile in appAdapter.data) {
                    latgeFile.isSelected = isSelectAll
                    if(isSelectAll){
                        selectData.add(latgeFile)
                        deleteSize += latgeFile.size
                    }
                }
                appAdapter.notifyDataSetChanged()
                setBtnState()
            }
        }
        binding.btDelete.setOnClickListener {
            if( binding.btDelete.text.toString().equals(getString(R.string.ok))){
                var bundle = Bundle()
                bundle.putString(BaseConstant.NEXT_TAG, getString(R.string.screenshot_manager))
                readyGoThenKill(ToNextActivity::class.java, bundle)
            }else{
                if(cleanFileDialog==null){
                    cleanFileDialog=CleanFileDialog(this@ScreenshotManagerActivity)
                    cleanFileDialog!!.setHint(getString(R.string.delete_screenshots_hint))
                    cleanFileDialog!!.setmOnClickListener(object : CleanFileDialog.onClickListener{
                        override fun onNext() {
                            EventBus.getDefault().postSticky(ScreenshotEvent(selectData,deleteSize))
                            val bundle = Bundle()
                            bundle.putString(BaseConstant.NEXT_TAG, getString(R.string.screenshot_manager))
                            readyGoThenKill(CleanFilesActivity::class.java,bundle)
                        }
                    })
                }
                cleanFileDialog!!.toShow()
            }
        }




    }



    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: ScreenshotsData) {
        if (event.data.isEmpty()) {
            binding.rlNodata.visibility = View.VISIBLE
            binding.rlSelectAll.visibility = View.GONE
            binding.btDelete.text = getString(R.string.ok)
        } else {
            binding.rlNodata.visibility = View.GONE
            binding.rlSelectAll.visibility = View.VISIBLE
            binding.btDelete.isClickable = false
            binding.btDelete.isEnabled = false
            binding.btDelete.background = getDrawable(R.drawable.btn_unclick)
            binding.tvSize.text= SizeUtil.formatSize3(event.totalSize)
            initAdapter()
            appAdapter.setList(event.data)
        }
        EventBus.getDefault().removeStickyEvent(event)

    }

    fun onBack(){
        Ads.showInterstitialAd(this@ScreenshotManagerActivity, "returnHomePageAdv"){
            LogUtil.log("enter_homepage", mapOf("referrer_name" to "screenshot"))
            readyGoThenKill(MainActivity::class.java)
        }

    }

    fun initAdapter(){
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        appAdapter=object : BaseQuickAdapter<Screenshot, BaseViewHolder>(R.layout.item_largefile){
            override fun convert(
                holder: BaseViewHolder,
                item: Screenshot
            ) {
                item.let {
                    holder.setText(R.id.tv_name, it.name)
                    var time = format.format(it.dateTaken)
                    holder.setText(
                        R.id.tv_info, SizeUtil.formatSize3(it.size) + "  " + time
                    )
                    Glide.with(this@ScreenshotManagerActivity)
                        .load(it.contentUri)
                        .placeholder(R.mipmap.ic_large_file) // 这个占位图最好也是圆角的
                        .error(R.mipmap.ic_large_file)
                        .into(holder.getView(R.id.iv_largefile))
                    if (it.isSelected) {
                        holder.setImageResource(R.id.iv_check, R.mipmap.ic_check)
                    } else {
                        holder.setImageResource(R.id.iv_check, R.mipmap.ic_check_nor)
                    }


                }

                holder.getView<RelativeLayout>(R.id.rl_function).setOnClickListener {
                    item.isSelected = !item.isSelected
                    setData(appAdapter.getItemPosition(item), item)
                    if (item.isSelected) {
                        selectData.add(item)
                        deleteSize += item.size
                    } else {
                        selectData.remove(item)
                        deleteSize -= item.size
                    }
                    if (selectData.isNotEmpty() && selectData.size==appAdapter.data.size) {
                        isSelectAll = true
                    } else {
                        isSelectAll = false
                    }
                    setBtnState()
                }
                holder.getView<ImageView>(R.id.iv_largefile).setOnClickListener {
                    var bundle= Bundle()
                    bundle.putString("Uri",item.contentUri.toString())
                    readyGo(ShowPhotoActivity::class.java,bundle)
                }
            }

        }

        binding.rcScreenshot.adapter = appAdapter
        binding.rcScreenshot.layoutManager =
            LinearLayoutManager(this@ScreenshotManagerActivity, RecyclerView.VERTICAL, false)
    }

    fun setBtnState() {
        if (selectData.isEmpty()) {
            binding.btDelete.isClickable = false
            binding.btDelete.isEnabled = false
            binding.btDelete.text = getString(R.string.delete)
            binding.btDelete.background = getDrawable(R.drawable.btn_unclick)
        } else {
            binding.btDelete.isClickable = true
            binding.btDelete.isEnabled = true
            binding.btDelete.text =
                getString(R.string.delete) + "(${SizeUtil.formatSize_1(deleteSize)})"
            binding.btDelete.background = getDrawable(R.drawable.btn_selector)
        }
        if (isSelectAll) {
            binding.ivCheck.setImageResource(R.mipmap.ic_check)
        } else {
            binding.ivCheck.setImageResource(R.mipmap.ic_check_nor)
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



    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
        if (cleanFileDialog != null) {
            cleanFileDialog!!.relese()
            cleanFileDialog = null
        }
    }

}