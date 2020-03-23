package com.lxf.demo

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class TileView(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {

    var size = 19
    var data: String = ""
        set(value) {
            field = value
            invalidate()
        }

    private val paint = Paint().apply {
        style = Paint.Style.FILL
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        val r = width.toFloat() / 19 / 2

        for (i in data.indices) {
            when (data[i]) {
                '0' -> {
                    paint.color = Color.YELLOW
                }
                '1' -> {
                    paint.color = Color.RED
                }
                '2' -> {
                    paint.color = Color.GREEN
                }
            }

            val x = if ((i + 1) % size == 0) size else ((i + 1) % size)  //1..19
            val y = if ((i + 1) % size == 0) ((i + 1) / size) else ((i + 1) / size + 1)  //1..19
            canvas?.drawCircle((x - 1) * 2 * r + r, (y - 1) * 2 * r + r, r, paint)
        }
    }
}