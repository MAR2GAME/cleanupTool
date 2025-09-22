package com.mycleaner.phonecleantool.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.mycleaner.phonecleantool.R


class RingProgressBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 40f
        color = ContextCompat.getColor(context, R.color.progress_bg)
    }

    private val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 40f
        strokeCap = Paint.Cap.ROUND
    }

//    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
//        textAlign = Paint.Align.CENTER
//        textSize = 40f
//        color = ContextCompat.getColor(context, android.R.color.black)
//    }

    private val rectF = RectF()
    private var progress = 0f
    private var max = 100f

    fun setProgress(progress: Float) {
        this.progress = progress
        invalidate()
    }

    fun setMax(max: Float) {
        this.max = max
    }

    fun setProgressColor(color: Int) {
        progressPaint.color = color
        invalidate()
    }

    fun setStrokeWidth(width: Float){
        progressPaint.strokeWidth=width
        backgroundPaint.strokeWidth=width
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val width = width.toFloat()
        val height = height.toFloat()
        val centerX = width / 2
        val centerY = height / 2
        val radius = (minOf(width, height) - backgroundPaint.strokeWidth) / 2

        rectF.set(
            centerX - radius,
            centerY - radius,
            centerX + radius,
            centerY + radius
        )

        // 绘制背景圆环
        canvas.drawArc(rectF, 0f, 360f, false, backgroundPaint)

        // 绘制进度圆环
        val sweepAngle = 360f * progress / max
        canvas.drawArc(rectF, -90f, sweepAngle, false, progressPaint)

        // 绘制进度文本
//        val progressText = "${progress.toInt()}%"
//        val textY = centerY - (textPaint.descent() + textPaint.ascent()) / 2
//        canvas.drawText(progressText, centerX, textY, textPaint)
    }
}