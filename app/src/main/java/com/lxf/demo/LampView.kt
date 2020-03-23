package com.lxf.demo

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import kotlin.math.min


class LampView(context: Context, attr: AttributeSet?) : View(context, attr) {

    private val paintBg = Paint()

    init {
        paintBg.color = Color.BLACK
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        canvas?.drawCircle(
            (width / 2).toFloat(),
            (height / 2).toFloat(),
            (min(width, height) / 2).toFloat(),
            paintBg
        )
    }

    fun light() {
        paintBg.color = Color.GREEN
        invalidate()

        postDelayed(500) {
            paintBg.color = Color.BLACK
            invalidate()
        }
    }
}