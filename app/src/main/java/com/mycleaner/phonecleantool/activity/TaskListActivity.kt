package com.mycleaner.phonecleantool.activity

import android.R.attr.tag
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import androidx.activity.OnBackPressedCallback
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.mycleaner.phonecleantool.R
import com.mycleaner.phonecleantool.adv.Ads
import com.mycleaner.phonecleantool.base.activity.BaseActivity
import com.mycleaner.phonecleantool.bean.TaskBean
import com.mycleaner.phonecleantool.command.AppConfig
import com.mycleaner.phonecleantool.command.rateUS
import com.mycleaner.phonecleantool.command.readyGoThenKill
import com.mycleaner.phonecleantool.command.safeClick
import com.mycleaner.phonecleantool.databinding.ActivityTaskListBinding
import com.mycleaner.phonecleantool.utils.AppPrefsUtils
import com.mycleaner.phonecleantool.utils.LogUtil
import com.mycleaner.phonecleantool.view.RateUsDialog
import com.mycleaner.phonecleantooll.base.BaseConstant

import org.greenrobot.eventbus.EventBus
import kotlin.jvm.java

class TaskListActivity : BaseActivity<ActivityTaskListBinding>() {
    private lateinit var taskAdapter: BaseQuickAdapter<TaskBean, BaseViewHolder>


    var tag: String=""
    private var rateUsDialog: RateUsDialog?=null
    override fun init() {
        val bundle = intent.extras
        tag = bundle?.getString(BaseConstant.NEXT_TAG).toString()
        if(tag==getString(R.string.junk_files)){
            AppPrefsUtils.putLong(BaseConstant.CLEAN_SIZE,AppPrefsUtils.getLong(BaseConstant.CLEAN_SIZE)+1)
            LogUtil.setUser(mapOf("total_cleancpl_num" to 1),"user_add")
        }
        binding.ivTaskBack.safeClick {
            onBack()
        }
        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                onBack()
            }
        })
        initAdapter(tag)
        toRateUs()
    }

    fun onBack(){
        var enter_name=when(tag){
            getString(R.string.junk_files) -> {
                "clean"
            }
            getString(R.string.process_manager) -> {
                "processman"
            }
            getString(R.string.app_manager) -> {
                "appman"
            }

            getString(R.string.speaker_cleaner) -> {
                "speaker"
            }

            getString(R.string.battery_info) -> {
                "battery"
            }
            getString(R.string.large_file_cleaner) -> {
                "largefile"
            }
            else -> {
                "screenshot"
            }
        }

        Ads.showInterstitialAd(this@TaskListActivity, "returnHomePageAdv"){
            LogUtil.log("enter_homepage", mapOf("referrer_name" to enter_name))
            readyGoThenKill(MainActivity::class.java)
        }

    }


    fun toRateUs(){
        if(!AppPrefsUtils.getBoolean(BaseConstant.IS_RATE_US)&&AppPrefsUtils.getLong(BaseConstant.CLEAN_SIZE)>= AppConfig.RATE_FLAG){
            var time= AppPrefsUtils.getLong(BaseConstant.SHOW_RATE_US_TIME)
            if(time == 0L || System.currentTimeMillis() - time > 86400000){
                rateUsDialog= RateUsDialog(this)
                rateUsDialog!!.setmOnClickListener(object : RateUsDialog.onClickListener{
                    override fun onAllow() {
                        AppPrefsUtils.putBoolean(BaseConstant.IS_RATE_US,true)
                        rateUS()
                    }

                })
                AppPrefsUtils.putLong(BaseConstant.SHOW_RATE_US_TIME,System.currentTimeMillis())
                rateUsDialog!!.toShow()
            }
        }
    }
    override fun onResume() {
        super.onResume()
        var junkTime = AppPrefsUtils.getLong(BaseConstant.JUKE_FILE_TIME)
        var processTime = AppPrefsUtils.getLong(BaseConstant.PROCESS_TIME)
        var appTime = AppPrefsUtils.getLong(BaseConstant.APP_TIME)
        var speakerTime = AppPrefsUtils.getLong(BaseConstant.SPEAKER_TIME)
        var batteryTime = AppPrefsUtils.getLong(BaseConstant.BATTERY_TIME)
        var largeTime = AppPrefsUtils.getLong(BaseConstant.LARGE_FILE_TIME)
        var screenshotTime = AppPrefsUtils.getLong(BaseConstant.SCREENSHOT_TIME)
        for (taskBean in taskAdapter.data) {
            when (taskBean.name) {
                getString(R.string.junk_files) -> {
                    taskBean.isCheck =
                        (junkTime == 0L || System.currentTimeMillis() - junkTime > 86400000)
                }

                getString(R.string.process_manager) -> {
                    taskBean.isCheck =
                        (processTime == 0L || System.currentTimeMillis() - processTime > 86400000)
                }

                getString(R.string.app_manager) -> {
                    taskBean.isCheck =
                        (appTime == 0L || System.currentTimeMillis() - appTime > 86400000)
                }

                getString(R.string.speaker_cleaner) -> {
                    taskBean.isCheck =
                        (speakerTime == 0L || System.currentTimeMillis() - speakerTime > 86400000)
                }

                getString(R.string.battery_info) -> {
                    taskBean.isCheck =
                        (batteryTime == 0L || System.currentTimeMillis() - batteryTime > 86400000)
                }
                getString(R.string.large_file_cleaner) -> {
                    taskBean.isCheck =
                        (largeTime == 0L || System.currentTimeMillis() - largeTime > 86400000)
                }
                getString(R.string.screenshot_manager) -> {
                    taskBean.isCheck =
                        (screenshotTime == 0L || System.currentTimeMillis() - screenshotTime > 86400000)
                }
            }
        }
        taskAdapter.notifyDataSetChanged()
    }

    fun initAdapter(tag: String) {
        var taskList = mutableListOf<TaskBean>()
        taskList.add(
            TaskBean(
                getString(R.string.junk_files),
                getString(R.string.free_up_memory_by_cleaning_junk_files),
                getString(R.string.clean)
            )
        )
        taskList.add(
            TaskBean(
                getString(R.string.process_manager),
                getString(R.string.check_ram_status),
                getString(R.string.go)
            )
        )
        taskList.add(
            TaskBean(
                getString(R.string.app_manager),
                getString(R.string.uninstall_infrequently_used_apps_and_free_up_storage_space),
                getString(R.string.check)
            )
        )
        taskList.add(
            TaskBean(
                getString(R.string.speaker_cleaner),
                getString(R.string.clean_the_phone_earpiece),
                getString(R.string.try_it)
            )
        )
        taskList.add(
            TaskBean(
                getString(R.string.battery_info),
                getString(R.string.check_the_detail_information_of_phone_battery),
                getString(R.string.check)
            )
        )
        taskList.add(
            TaskBean(
                getString(R.string.large_file_cleaner),
                getString(R.string.clean_up_large_files_on_your_phone),
                getString(R.string.delete)
            )
        )
        taskList.add(
            TaskBean(
                getString(R.string.screenshot_manager),
                getString(R.string.clean_up_screenshots_on_your_phone),
                getString(R.string.delete)
            )
        )
        var data = taskList.filterNot { it.name == tag }

        taskAdapter = object : BaseQuickAdapter<TaskBean, BaseViewHolder>(R.layout.item_task) {
            override fun convert(
                holder: BaseViewHolder,
                item: TaskBean
            ) {
                item.let {
                    holder.setText(R.id.tv_name, it.name)
                    holder.setText(R.id.tv_hint, it.hint)
                    holder.setText(R.id.bt_next, it.next)
                    if (item.isCheck) {
                        holder.getView<ImageView>(R.id.iv_check).visibility = View.VISIBLE
                    } else {
                        holder.getView<ImageView>(R.id.iv_check).visibility = View.GONE
                    }
                    holder.getView<Button>(R.id.bt_next).safeClick {
                        val bundle = Bundle()
                        when (item.name) {
                            getString(R.string.junk_files) -> {
                                LogUtil.log("enter_clean", mapOf("referrer_name" to getReferrerName()))
                                EventBus.getDefault().post(item.name)
                                finish()
                            }
                            getString(R.string.process_manager) -> {
                                LogUtil.log("enter_processman", mapOf("referrer_name" to getReferrerName()))
                                EventBus.getDefault().post(item.name)
                                finish()
                            }
                            getString(R.string.screenshot_manager) -> {
                                LogUtil.log("enter_screenshotman", mapOf("referrer_name" to getReferrerName()))
                                EventBus.getDefault().post(item.name)
                                finish()
                            }
                            getString(R.string.app_manager) -> {
                                LogUtil.log("enter_appman", mapOf("referrer_name" to getReferrerName()))
                                bundle.putString(
                                    BaseConstant.NEXT_TAG,
                                    getString(R.string.app_manager)
                                )
                                readyGoThenKill(LoadingActivity::class.java, bundle)
                            }

                            getString(R.string.speaker_cleaner) -> {
                                LogUtil.log("enter_speaker", mapOf("referrer_name" to getReferrerName()))
                                bundle.putString(
                                    BaseConstant.NEXT_TAG,
                                    getString(R.string.speaker_cleaner)
                                )
                                readyGoThenKill(LoadingActivity::class.java, bundle)
                            }

                            getString(R.string.battery_info) -> {
                                LogUtil.log("enter_battery", mapOf("referrer_name" to getReferrerName()))
                                bundle.putString(
                                    BaseConstant.NEXT_TAG,
                                    getString(R.string.battery_info)
                                )
                                readyGoThenKill(LoadingActivity::class.java, bundle)
                            }
                            getString(R.string.large_file_cleaner) -> {
                                LogUtil.log("enter_largefile", mapOf("referrer_name" to getReferrerName()))
                                EventBus.getDefault().post(item.name)
                                finish()
                            }
                        }
                    }
                }

            }
        }
        binding.rcTask.adapter = taskAdapter
        binding.rcTask.layoutManager =
            LinearLayoutManager(this@TaskListActivity, RecyclerView.VERTICAL, false)
        taskAdapter.setList(data)
    }


    fun getReferrerName(): String{
        return when(tag){
            getString(R.string.junk_files) -> {
                "cleancpl"
            }
            getString(R.string.process_manager) -> {
                "processmancpl"
            }
            getString(R.string.app_manager) -> {
                "appmancpl"
            }

            getString(R.string.speaker_cleaner) -> {
                "speakercpl"
            }

            getString(R.string.battery_info) -> {
                "batterycpl"
            }
            getString(R.string.large_file_cleaner) -> {
                "largefilecpl"
            }
            else -> {
                "screenshotcpl"
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (rateUsDialog != null) {
            rateUsDialog!!.relese()
            rateUsDialog = null
        }
    }
}

