package com.mycleaner.phonecleantool.view

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import androidx.core.graphics.drawable.toDrawable
import com.mycleaner.phonecleantool.R
import com.mycleaner.phonecleantool.utils.DisplayUtil


class ProgressDialog(context: Context) : Dialog(context)  {
    private var v: View? = null

    init {
        var inflater = LayoutInflater.from(context)
        v = inflater.inflate(R.layout.dialog_progress, null)
        setContentView(v!!)
        setCancelable(false)
        setCanceledOnTouchOutside(false)
        val params = this.window!!.attributes
        params.width = DisplayUtil.dp2px(context, 140)
        params.height = DisplayUtil.dp2px(context, 140)
        window!!.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        this.window!!.attributes = params
        this.window!!.setGravity(Gravity.CENTER)
    }
    fun toShow() {
        if (v == null) {
            return
        }
        v!!.findViewById<RotatingRingView>(R.id.rotatingRingView).visibility= View.VISIBLE
        if (!isShowing) {
            show()
        }
    }

    fun relese() {
        v!!.findViewById<RotatingRingView>(R.id.rotatingRingView).visibility= View.GONE
        if (isShowing) {
            dismiss()
        }
        v = null
    }

}