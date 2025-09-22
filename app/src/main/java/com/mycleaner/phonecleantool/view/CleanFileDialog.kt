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

class CleanFileDialog(context: Context) : Dialog(context) {
    private var v: View? = null

    init {
        var inflater = LayoutInflater.from(context)
        v = inflater.inflate(R.layout.dialog_cleanfile, null)
        setContentView(v!!)
        setCancelable(true)
        setCanceledOnTouchOutside(false)
        val params = this.window!!.attributes
        params.width = DisplayUtil.dp2px(context, 320)
        params.height = DisplayUtil.dp2px(context, 340)
        window!!.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        this.window!!.attributes = params
        this.window!!.setGravity(Gravity.CENTER)
        v!!.findViewById<Button>(R.id.bt_delete).safeClick {
            dismiss()
            if(mOnClickListener!=null){
                mOnClickListener!!.onNext()
            }
        }
        v!!.findViewById<TextView>(R.id.tv_cancel).safeClick {
            dismiss()
        }
    }

    fun setHint(hint: String){
        v!!.findViewById<TextView>(R.id.tv_hint).text=hint
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


    private var mOnClickListener: onClickListener? = null

    interface onClickListener {
        fun onNext() //void
    }
    fun setmOnClickListener(mOnClickListener: onClickListener) {
        this.mOnClickListener = mOnClickListener
    }
}