package com.mycleaner.phonecleantool.view

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import androidx.core.graphics.drawable.toDrawable
import com.mycleaner.phonecleantool.R
import com.mycleaner.phonecleantool.command.safeClick
import com.mycleaner.phonecleantool.utils.DisplayUtil
import com.mycleaner.phonecleantool.utils.LogUtil


class RateUsDialog(context: Context) : Dialog(context) {
    private var v: View? = null

    private var isToRate=false

    init {
        var inflater = LayoutInflater.from(context)
        v = inflater.inflate(R.layout.dailog_rate, null)
        setContentView(v!!)
        setCancelable(true)  // 允许返回键关闭
        setCanceledOnTouchOutside(false)  // 禁用外部点击关闭
        val params = this.window!!.attributes
        params.width = DisplayUtil.dp2px(context, 360)
        params.height = DisplayUtil.dp2px(context, 400)
        window!!.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        this.window!!.attributes = params
        this.window!!.setGravity(Gravity.CENTER)
        v!!.findViewById<Button>(R.id.bt_rate).safeClick {
            isToRate=true
            if(mOnClickListener!=null){
                mOnClickListener!!.onAllow()
            }
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
        mOnClickListener = null
        v = null

    }

    override fun dismiss() {
        LogUtil.log("rate_popup", mapOf("if_ok" to isToRate))
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
