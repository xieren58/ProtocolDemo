package com.lxf.demo

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SeekBar
import androidx.appcompat.widget.AppCompatSeekBar
import com.afollestad.materialdialogs.color.view.ObservableSeekBar

class RGBPicker(context: Context) : LinearLayout(context) {
    private val imageView = ImageView(context).apply {
        layoutParams = LayoutParams(50, MATCH_PARENT)
    }

    private val seekR = AppCompatSeekBar(context).apply {
        max = 255
//        progressDrawable = ColorDrawable(Color.RED)
        layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
    }
    private val seekG = AppCompatSeekBar(context).apply {
        max = 255
//        progressDrawable = ColorDrawable(Color.GREEN)
        gravity = Gravity.CENTER_VERTICAL
        layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply {
            topMargin = 20
        }
    }
    private val seekB = AppCompatSeekBar(context).apply {
        max = 255
//        progressDrawable = ColorDrawable(Color.BLUE)
        layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply {
            topMargin = 20
        }
    }

    init {

        seekR.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                imageView.setBackgroundColor(Color.rgb(progress, seekG.progress, seekB.progress))
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })

        seekG.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                imageView.setBackgroundColor(Color.rgb(seekR.progress, progress, seekB.progress))
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
        seekB.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                imageView.setBackgroundColor(Color.rgb(seekR.progress, seekG.progress, progress))
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })

        seekR.progress = 255
        seekG.progress = 0
        seekB.progress = 255


        val linearLayout = LinearLayout(context).apply {
            orientation = VERTICAL
            gravity = Gravity.CENTER
            layoutParams = LayoutParams(MATCH_PARENT, MATCH_PARENT).apply {
                marginStart = 20
            }

            addView(seekR)
            addView(seekG)
            addView(seekB)
        }
        addView(imageView)
        addView(linearLayout)

        gravity = Gravity.CENTER
        layoutParams = LayoutParams(MATCH_PARENT, MATCH_PARENT)
        setPadding(50, 0, 50, 0)
    }

    fun r() = seekR.progress
    fun g() = seekG.progress
    fun b() = seekB.progress

    fun r(r:Int){
        seekR.progress = r
    }
    fun g(g:Int){
        seekG.progress = g
    }
    fun b(b:Int){
        seekB.progress = b
    }
}