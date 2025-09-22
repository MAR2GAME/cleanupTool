package com.mycleaner.phonecleantool.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.Gravity
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
import com.mycleaner.phonecleantool.bean.CleanFilesBean
import com.mycleaner.phonecleantool.bean.CleanLargeFileBean
import com.mycleaner.phonecleantool.bean.LargeFile
import com.mycleaner.phonecleantool.bean.LargeFileEvent
import com.mycleaner.phonecleantool.bean.TagBean
import com.mycleaner.phonecleantool.command.readyGoThenKill
import com.mycleaner.phonecleantool.command.safeClick
import com.mycleaner.phonecleantool.databinding.ActivityLargeFileBinding
import com.mycleaner.phonecleantool.utils.AppPrefsUtils
import com.mycleaner.phonecleantool.utils.LogUtil
import com.mycleaner.phonecleantool.utils.SizeUtil
import com.mycleaner.phonecleantool.view.CleanFileDialog
import com.mycleaner.phonecleantool.view.LargeFileTagPop
import com.mycleaner.phonecleantool.view.ProgressDialog
import com.mycleaner.phonecleantooll.base.BaseConstant
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.collections.filter


class LargeFileActivity : BaseActivity<ActivityLargeFileBinding>() {
    lateinit var appAdapter: BaseQuickAdapter<LargeFile, BaseViewHolder>

    var cleanFileDialog: CleanFileDialog?=null

    var largeFileTagPop: LargeFileTagPop? = null

    var allLargeFiles: MutableList<LargeFile> = mutableListOf()

    var isSelectAll = false
    var tagBeans1 = mutableListOf<TagBean>()
    var tagBeans2 = mutableListOf<TagBean>()

    var advView:ViewGroup?=null
    @SuppressLint("NotifyDataSetChanged")
    override fun init() {
        getTageBeans()
        EventBus.getDefault().register(this)
        LogUtil.setUser(mapOf("total_largefile_num" to 1),"user_add")
        AppPrefsUtils.putLong(
            BaseConstant.LARGE_FILE_TIME,
            System.currentTimeMillis()
        )
        if(AdvCheckManager.params.limitTime < System.currentTimeMillis()){
            advView = getBannerAd(this, AreaKey.languagePageBottomAdv)
            binding.flAd.visibility= View.VISIBLE
            binding.flAd.addView(advView)
        }
        binding.ivLargeFileBack.safeClick {
            onBack()
        }
        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                onBack()
            }
        })


        binding.rlSelectAll.setOnClickListener {
            if (appAdapter.data.isNotEmpty()){
                isSelectAll = !isSelectAll
                for (latgeFile in appAdapter.data) {
                    latgeFile.isSelected = isSelectAll
                    if(isSelectAll){
                        if(!selectLargeFile.contains(latgeFile)){
                            selectLargeFile.add(latgeFile)
                            deleteSize += latgeFile.size
                        }
                    }else{
                        selectLargeFile.remove(latgeFile)
                        deleteSize -= latgeFile.size
                    }
                }
                appAdapter.notifyDataSetChanged()
                setBtnState()
            }
        }
        binding.rlType.setOnClickListener {
            if (allLargeFiles.isNotEmpty()) {
                showPop(false)
            }
        }
        binding.rlSize.setOnClickListener {
            if (allLargeFiles.isNotEmpty()) {
                showPop(true)
            }
        }
        binding.btDelete.setOnClickListener {
            if( binding.btDelete.text.toString().equals(getString(R.string.ok))){
                var bundle = Bundle()
                bundle.putString(BaseConstant.NEXT_TAG, getString(R.string.large_file_cleaner))
                readyGoThenKill(ToNextActivity::class.java, bundle)
            }else{
                if(cleanFileDialog==null){
                    cleanFileDialog=CleanFileDialog(this@LargeFileActivity)
                    cleanFileDialog!!.setHint(getString(R.string.delete_large_file_hint))
                    cleanFileDialog!!.setmOnClickListener(object : CleanFileDialog.onClickListener{
                        override fun onNext() {

                            EventBus.getDefault().postSticky(CleanLargeFileBean(deleteSize,selectLargeFile))
                            val bundle = Bundle()
                            bundle.putString(BaseConstant.NEXT_TAG, getString(R.string.large_file_cleaner))
                            readyGoThenKill(CleanFilesActivity::class.java,bundle)
                        }
                    })
                }
                cleanFileDialog!!.toShow()
            }
        }
    }


    fun onBack(){
        Ads.showInterstitialAd(this@LargeFileActivity, "returnHomePageAdv"){
            LogUtil.log("enter_homepage", mapOf("referrer_name" to "largefile"))
            readyGoThenKill(MainActivity::class.java)
        }

    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: LargeFileEvent) {
        allLargeFiles.addAll(event.data)
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
            initAdapter()
            appAdapter.setList(event.data)
        }
        EventBus.getDefault().removeStickyEvent(event)
    }

    fun showPop(isSize: Boolean) {
        if (largeFileTagPop == null) {
            largeFileTagPop = LargeFileTagPop(this@LargeFileActivity)
            largeFileTagPop!!.initPop()
            largeFileTagPop!!.setmOnClickListener(object : LargeFileTagPop.onClickListener {
                override fun onClick(isSize: Boolean, pos: Int) {
                    if (isSize) {
                        if (binding.tvSize.text.toString() != tagBeans2.get(pos).tagName) {
                            for (tagBean in tagBeans2) {
                                tagBean.isSelected = false
                            }
                            tagBeans2.get(pos).isSelected = true
                            binding.tvSize.text = tagBeans2.get(pos).tagName
                            toFilterData()
                        }
                    } else {
                        if (binding.tvType.text.toString() != tagBeans1.get(pos).tagName) {
                            for (tagBean in tagBeans1) {
                                tagBean.isSelected = false
                            }
                            tagBeans1.get(pos).isSelected = true
                            binding.tvType.text = tagBeans1.get(pos).tagName
                            toFilterData()
                        }
                    }
                }
            })
        }
        // 获取目标View的位置
        var location = IntArray(2)
        if (isSize) {
            binding.rlSize.getLocationOnScreen(location)
            var x = location[0]
            var y: Int = location[1] + binding.rlSize.getHeight()
            largeFileTagPop!!.toUpdate(tagBeans2, true)
            // 显示PopupWindow，使用showAtLocation
            largeFileTagPop!!.showAtLocation(binding.rlSize, Gravity.NO_GRAVITY, x + 20, y + 10)
        } else {
            binding.rlType.getLocationOnScreen(location)
            var x = location[0]
            var y: Int = location[1] + binding.rlType.getHeight()
            largeFileTagPop!!.toUpdate(tagBeans1, false)
            // 显示PopupWindow，使用showAtLocation
            largeFileTagPop!!.showAtLocation(binding.rlType, Gravity.NO_GRAVITY, x + 40, y + 10)
        }

    }


    fun toFilterData() {
        var typeFilterData = when (binding.tvType.text.toString()) {
            getString(R.string.all_types) -> {
                allLargeFiles
            }

            getString(R.string.video) -> {
                allLargeFiles.filter { largeFile -> largeFile.type.startsWith("video/") }
            }

            getString(R.string.audio) -> {
                allLargeFiles.filter { largeFile -> largeFile.type.startsWith("audio/") }
            }

            getString(R.string.image) -> {
                allLargeFiles.filter { largeFile -> largeFile.type.startsWith("image/") }
            }

            getString(R.string.apk) -> {
                allLargeFiles.filter { largeFile -> largeFile.name.endsWith(".apk") }
            }

            else -> {
                allLargeFiles.filter { largeFile ->
                    !largeFile.type.startsWith("image/") &&
                            !largeFile.type.startsWith("audio/") && !largeFile.type.startsWith("video/")
                            && !largeFile.name.endsWith(".apk")
                }
            }
        }
        var sizeTypeData = when (binding.tvSize.text.toString()) {
            getString(R.string.all_size) -> {
                typeFilterData
            }

            "10MB" -> {
                typeFilterData.filter { largeFile -> largeFile.size < 30 * 1024 * 1024 }
            }

            "30MB" -> {
                typeFilterData.filter { largeFile -> 30 * 1024 * 1024 <= largeFile.size && largeFile.size < 100 * 1024 * 1024 }
            }

            "100MB" -> {
                typeFilterData.filter { largeFile -> 100 * 1024 * 1024 <= largeFile.size && largeFile.size < 500 * 1024 * 1024 }
            }

            "500MB" -> {
                typeFilterData.filter { largeFile -> 500 * 1024 * 1024 <= largeFile.size && largeFile.size < 1024 * 1024 * 1024 }
            }

            else -> {
                typeFilterData.filter { largeFile -> 1024 * 1024 * 1024 <= largeFile.size }
            }
        }
        if (sizeTypeData.isNotEmpty()) {
            binding.rlNodata.visibility = View.GONE
            if (selectLargeFile.isNotEmpty()) {
                for (largeFile in sizeTypeData) {
                    largeFile.isSelected = selectLargeFile.contains(largeFile)
                }
            }
            if (selectLargeFile.containsAll(sizeTypeData)) {
                isSelectAll = true
            } else {
                isSelectAll = false
            }
        } else {
            binding.rlNodata.visibility = View.VISIBLE
            isSelectAll = false
        }
        appAdapter.setList(sizeTypeData)
        if (isSelectAll) {
            binding.ivCheck.setImageResource(R.mipmap.ic_check)
        } else {
            binding.ivCheck.setImageResource(R.mipmap.ic_check_nor)
        }
    }

    fun getTageBeans() {
        tagBeans1.add(TagBean(getString(R.string.all_types), true))
        tagBeans1.add(TagBean(getString(R.string.image), false))
        tagBeans1.add(TagBean(getString(R.string.video), false))
        tagBeans1.add(TagBean(getString(R.string.audio), false))
        tagBeans1.add(TagBean(getString(R.string.apk), false))
        tagBeans1.add(TagBean(getString(R.string.other), false))
        tagBeans2.add(TagBean(getString(R.string.all_size), true))
        tagBeans2.add(TagBean("10MB", false))
        tagBeans2.add(TagBean("30MB", false))
        tagBeans2.add(TagBean("100MB", false))
        tagBeans2.add(TagBean("500MB", false))
        tagBeans2.add(TagBean("1GB", false))
    }

    var selectLargeFile: MutableList<LargeFile> = mutableListOf()
    var deleteSize: Long = 0
    fun initAdapter() {
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        appAdapter = object : BaseQuickAdapter<LargeFile, BaseViewHolder>(R.layout.item_largefile) {
            override fun convert(
                holder: BaseViewHolder,
                item: LargeFile
            ) {

                item.let {
                    holder.setText(R.id.tv_name, it.name)
                    var time = format.format(it.date)
                    holder.setText(
                        R.id.tv_info, SizeUtil.formatSize3(it.size) + "  " + time
                    )
                    if (it.thumbnailUri == null) {
                        holder.setImageResource(R.id.iv_largefile, R.mipmap.ic_large_file)
                    } else {
                        Glide.with(this@LargeFileActivity)
                            .load(it.thumbnailUri)
                            .placeholder(R.mipmap.ic_large_file) // 这个占位图最好也是圆角的
                            .error(R.mipmap.ic_large_file)
                            .into(holder.getView(R.id.iv_largefile))
                    }
                    if (it.type.startsWith("video/")) {
                        holder.getView<ImageView>(R.id.iv_play).visibility = View.VISIBLE
                    } else {
                        holder.getView<ImageView>(R.id.iv_play).visibility = View.GONE
                    }
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
                        selectLargeFile.add(item)
                        deleteSize += item.size
                    } else {
                        selectLargeFile.remove(item)
                        deleteSize -= item.size
                    }
                    if (selectLargeFile.isNotEmpty() && selectLargeFile.containsAll(appAdapter.data)) {
                        isSelectAll = true
                    } else {
                        isSelectAll = false
                    }
                    setBtnState()
                }
            }
        }

        binding.rcFile.adapter = appAdapter
        binding.rcFile.layoutManager =
            LinearLayoutManager(this@LargeFileActivity, RecyclerView.VERTICAL, false)


    }


    fun setBtnState() {
        if (selectLargeFile.isEmpty()) {
            binding.btDelete.isClickable = false
            binding.btDelete.isEnabled = false
            binding.btDelete.text = getString(R.string.delete)
            binding.btDelete.background = getDrawable(R.drawable.btn_unclick)
        } else {
            binding.btDelete.isClickable = true
            binding.btDelete.isEnabled = true
            binding.btDelete.text =
                getString(R.string.delete) + "(${SizeUtil.formatSize3(deleteSize)})"
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
        if (largeFileTagPop != null) {
            largeFileTagPop!!.relese()
            largeFileTagPop = null
        }
        if (cleanFileDialog != null) {
            cleanFileDialog!!.relese()
            cleanFileDialog = null
        }

    }


}