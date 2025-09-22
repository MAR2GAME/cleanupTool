package com.mycleaner.phonecleantool.view

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.compose.ui.graphics.Color
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.mycleaner.phonecleantool.R
import com.mycleaner.phonecleantool.bean.TagBean

class LargeFileTagPop(var context: Context) : PopupWindow(context) {
    private  var view: View?=null
    private lateinit var popitemAdapter: BaseQuickAdapter<TagBean, BaseViewHolder>


    fun initPop() {
        var inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        view = inflater.inflate(R.layout.pop_tag, null)
        this.setContentView(view)
//        // 设置弹出窗体的宽
        this.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT)
        // 设置弹出窗体的高
        this.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT)
        // 设置弹出窗体可点击()
        this.setFocusable(true)
        this.setOutsideTouchable(true)
        // 实例化一个ColorDrawable颜色为半透明
        var dw = ColorDrawable(0x00FFFFFF)
        //    设置弹出窗体的背景
        this.setBackgroundDrawable(dw)
        var recyclerView = view!!.findViewById<RecyclerView>(R.id.rc_tag)
        popitemAdapter =
            object : BaseQuickAdapter<TagBean, BaseViewHolder>(R.layout.item_pop_tag) {
                override fun convert(
                    holder: BaseViewHolder,
                    item: TagBean
                ) {
                    item.let {
                        holder.setText(R.id.tv_tag_name,it.tagName)
                        if(it.isSelected){
                            holder.setTextColor(R.id.tv_tag_name, context.getColor(R.color.btn_nor))
                        }else{
                            holder.setTextColor(R.id.tv_tag_name, context.getColor(R.color.white))
                        }

                    }
                    holder.getView<RelativeLayout>(R.id.rl_tag).setOnClickListener {
                        dismiss()
                        if(mOnClickListener!=null){
                            mOnClickListener!!.onClick(isSize,popitemAdapter.getItemPosition(item))
                        }
                    }

                }
            }
        recyclerView.adapter = popitemAdapter
        recyclerView.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
    }

    var isSize: Boolean=false

    fun toUpdate(data: MutableList<TagBean>,isSize: Boolean) {
        this.isSize=isSize
        popitemAdapter.setList(data)

    }
    fun relese() {
        if (isShowing) {
            dismiss()
        }
        mOnClickListener=null
        view = null
    }
    private var mOnClickListener: onClickListener? = null

    interface onClickListener {
        fun onClick(isSize: Boolean,pos:Int) //void
    }
    fun setmOnClickListener(mOnClickListener: onClickListener) {
        this.mOnClickListener = mOnClickListener
    }

}