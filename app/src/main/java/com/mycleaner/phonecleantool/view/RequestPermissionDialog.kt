package com.mycleaner.phonecleantool.view

import android.annotation.SuppressLint
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


@SuppressLint("InflateParams")
class RequestPermissionDialog(context: Context) : Dialog(context) {
    private var v: View? = null

    init {
        var inflater = LayoutInflater.from(context)
        v = inflater.inflate(R.layout.dialog_requestpermission, null)
        setContentView(v!!)
        setCancelable(true)  // 允许返回键关闭
        setCanceledOnTouchOutside(false)  // 禁用外部点击关闭
        val params = this.window!!.attributes
        params.width = DisplayUtil.dp2px(context, 320)
        params.height = DisplayUtil.dp2px(context, 400)
        window!!.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        this.window!!.attributes = params
        this.window!!.setGravity(Gravity.CENTER)
        v!!.findViewById<Button>(R.id.bt_allow).safeClick {
            LogUtil.log("filemanager_popup", mapOf("if_ok" to true))
            dismiss()
            if(mOnClickListener!=null){
                mOnClickListener!!.onAllow()
            }
        }
        v!!.findViewById<TextView>(R.id.tv_cancel).safeClick {
            LogUtil.log("filemanager_popup", mapOf("if_ok" to false))
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
        super.dismiss()
        LogUtil.log("filemanager_popup", mapOf("accessflag" to AppConfig.ACCESS_FLAG))
    }


    private var mOnClickListener: onClickListener? = null

    interface onClickListener {
        fun onAllow() //void

    }
   fun setmOnClickListener(mOnClickListener: onClickListener) {
        this.mOnClickListener = mOnClickListener

    }





}