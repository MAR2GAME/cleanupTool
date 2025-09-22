package com.mycleaner.phonecleantool.view

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.core.graphics.drawable.toDrawable
import com.mycleaner.phonecleantool.R
import com.mycleaner.phonecleantool.command.AppConfig
import com.mycleaner.phonecleantool.command.safeClick
import com.mycleaner.phonecleantool.utils.DisplayUtil
import com.mycleaner.phonecleantool.utils.LogUtil


class NotificationDialog(context: Context) : Dialog(context) {
    private var v: View? = null

    init {
        var inflater = LayoutInflater.from(context)
        v = inflater.inflate(R.layout.dialog_notlification, null)
        setContentView(v!!)
        setCancelable(true)
        setCanceledOnTouchOutside(false)
        val params = this.window!!.attributes
        params.width = DisplayUtil.dp2px(context, 320)
        params.height = DisplayUtil.dp2px(context, 270)
        window!!.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        this.window!!.attributes = params
        this.window!!.setGravity(Gravity.CENTER)
        v!!.findViewById<Button>(R.id.bt_setting).safeClick {
            LogUtil.log("notification_popup", mapOf("if_ok" to true))
            dismiss()
            if(mOnClickListener!=null){
                mOnClickListener!!.onAllow()
            }
        }
        v!!.findViewById<TextView>(R.id.tv_not_allow).safeClick {
            LogUtil.log("notification_popup", mapOf("if_ok" to false))
            dismiss()
        }
    }
    fun toShow() {
        if (v == null) {
            return
        }
        if (!isShowing) {
            show()
        }
    }

    fun relese() {
        if (isShowing) {
            dismiss()
        }
        mOnClickListener=null
        v = null
    }

    override fun dismiss() {
        LogUtil.log("notification_popup", mapOf("noticeflag" to AppConfig.NOTICE_FLAG))
        super.dismiss()
    }


    private var mOnClickListener: onClickListener? = null

    interface onClickListener {
        fun onAllow() //void
    }
    fun setmOnClickListener(mOnClickListener: onClickListener) {
        this.mOnClickListener = mOnClickListener
    }
}