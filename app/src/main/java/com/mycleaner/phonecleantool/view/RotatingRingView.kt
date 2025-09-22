package com.mycleaner.phonecleantool.view

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import com.mycleaner.phonecleantool.R


class RotatingRingView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 20f
        color = Color.parseColor("#337EFF")
        strokeCap = Paint.Cap.ROUND
    }

    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 20f
        color = Color.parseColor("#33337EFF") // 半透明背景
    }

    private val rectF = RectF()
    private var startAngle = 0f
    private var rotationAnimator: ValueAnimator? = null

    init {
        // 从XML属性读取自定义属性（如果有）
        attrs?.let {
            val typedArray = context.obtainStyledAttributes(it, R.styleable.RotatingRingView)
            ringPaint.strokeWidth = typedArray.getDimension(
                R.styleable.RotatingRingView_ringWidth,
                ringPaint.strokeWidth
            )
            ringPaint.color = typedArray.getColor(
                R.styleable.RotatingRingView_ringColor,
                ringPaint.color
            )
            backgroundPaint.strokeWidth = ringPaint.strokeWidth
            backgroundPaint.color = typedArray.getColor(
                R.styleable.RotatingRingView_backgroundColor,
                backgroundPaint.color
            )
            typedArray.recycle()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val centerX = width / 2f
        val centerY = height / 2f
        val radius = minOf(centerX, centerY) - ringPaint.strokeWidth / 2f

        rectF.set(
            centerX - radius,
            centerY - radius,
            centerX + radius,
            centerY + radius
        )

        // 绘制背景圆环
        canvas.drawArc(rectF, 0f, 360f, false, backgroundPaint)

        // 绘制旋转圆环 (绘制270度的弧，留出空白)
        canvas.drawArc(rectF, startAngle, 180f, false, ringPaint)
    }

    fun startAnimation() {
        if (rotationAnimator?.isRunning == true) return

        rotationAnimator = ValueAnimator.ofFloat(0f, 360f).apply {
            duration = 800
            interpolator = LinearInterpolator()
            repeatCount = ValueAnimator.INFINITE
            addUpdateListener {
                startAngle = it.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    fun stopAnimation() {
        rotationAnimator?.cancel()
    }

    fun setRingColor(color: Int) {
        ringPaint.color = color
        invalidate()
    }

    fun setMyBackgroundColor(color: Int) {
        backgroundPaint.color = color
        invalidate()
    }

    fun setRingWidth(width: Float) {
        ringPaint.strokeWidth = width
        backgroundPaint.strokeWidth = width
        invalidate()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        startAnimation()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopAnimation()
    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        if (visibility == View.VISIBLE) {
            startAnimation()
        } else {
            stopAnimation()
        }
    }
}