package com.mycleaner.phonecleantool.view

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.text.SpannableString
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.core.graphics.drawable.toDrawable
import com.mycleaner.phonecleantool.R
import com.mycleaner.phonecleantool.command.safeClick
import com.mycleaner.phonecleantool.utils.DisplayUtil


class CleanHintDialog(context: Context) : Dialog(context) {
    private var v: View? = null

    private var isTodelete: Boolean=false

    init {
        var inflater = LayoutInflater.from(context)
        v = inflater.inflate(R.layout.dialog_cleanhint, null)
        setContentView(v!!)
        setCancelable(false)
        setCanceledOnTouchOutside(false)
        val params = this.window!!.attributes
        params.width = DisplayUtil.dp2px(context, 320)
        params.height = DisplayUtil.dp2px(context, 340)
        window!!.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        this.window!!.attributes = params
        this.window!!.setGravity(Gravity.CENTER)
        v!!.findViewById<Button>(R.id.bt_continue).safeClick {
            dismiss()
            if(mOnClickListener!=null){
                mOnClickListener!!.onNext(isTodelete)
            }
        }
        v!!.findViewById<TextView>(R.id.tv_stop).safeClick {
            dismiss()
            if(mOnClickListener!=null){
                mOnClickListener!!.onStop()
            }

        }
    }
    fun toShow(hint: SpannableString,isDelete: Boolean) {
        if (v == null) {
            return
        }
        this.isTodelete=isDelete
        v!!.findViewById<TextView>(R.id.tv_hint).text=hint
        if (!isShowing) {
            show()
        }
    }
    fun toShow(hint: String,isDelete: Boolean) {
        if (v == null) {
            return
        }
        this.isTodelete=isDelete
        v!!.findViewById<TextView>(R.id.tv_hint).text=hint
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


    private var mOnClickListener: onClickListener? = null

    interface onClickListener {
        fun onNext(isDelete: Boolean) //void
        fun onStop()
    }
    fun setmOnClickListener(mOnClickListener: onClickListener) {
        this.mOnClickListener = mOnClickListener

    }
}