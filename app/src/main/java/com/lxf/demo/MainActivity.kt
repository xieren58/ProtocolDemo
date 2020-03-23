package com.lxf.demo

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import cn.izis.boardmonitor.core.BoardConnector
import cn.izis.boardmonitor.listener.BoardConnectListener
import cn.izis.boardmonitor.listener.BoardDataListener
import cn.izis.boardmonitor.protocol.BoardProtocol
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), BoardConnectListener, BoardDataListener {
    private lateinit var boardConnector: BoardConnector
    private val boardSize = 19

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnConnect.setOnClickListener {
            btnConnect.isEnabled = false
            connectBoard()
        }
        btnDisconnect.setOnClickListener {
            boardConnector.write(BoardProtocol.closeAllLamp())
            boardConnector.write(BoardProtocol.disConnect())
            boardConnector.callDestroy()

            clearHint()
            state(false)
            btnConnect.isEnabled = true
        }

        btnAllChess.setOnClickListener {
            clearHint()
            boardConnector.write(BoardProtocol.allChess())
        }

        btnBlackLamp.setOnClickListener {
            clearHint()
            boardConnector.write(BoardProtocol.lamp(1))
        }

        btnWhiteLamp.setOnClickListener {
            clearHint()
            boardConnector.write(BoardProtocol.lamp(2))
        }

        btnWarning.setOnClickListener {
            clearHint()
            boardConnector.write(BoardProtocol.warning())
        }

        switchAutoGetData.setOnCheckedChangeListener { _, isChecked ->
            clearHint()
            boardConnector.write(BoardProtocol.autoSendAllChess(isChecked))
        }
        btnLamp.setOnClickListener {
            clearHint()
            var position = etPosition.text.toString().toIntOrNull() ?: 181
            if (position < 1)
                position = 1
            if (position > 361)
                position = 361
            val r = tvColor.text.substring(1..3).toInt()
            val g = tvColor.text.substring(5..7).toInt()
            val b = tvColor.text.substring(9..11).toInt()
            boardConnector.write(BoardProtocol.lampPosition(position, r, g, b))
        }
        tvColor.setOnClickListener {
            selectColor { r, g, b ->
                tvColor.text =
                    "r${changeIntToString(r)}g${changeIntToString(g)}b${changeIntToString(b)},1#"
                tvColor.setTextColor(Color.rgb(r, g, b))
            }
        }
        cbUIShow.setOnCheckedChangeListener { _, isChecked ->
            tileView.visibility = if (isChecked) View.VISIBLE else View.INVISIBLE
        }
    }

    private fun selectColor(listener: (r: Int, g: Int, b: Int) -> Unit) {
        MaterialDialog(this)
            .apply {
                cancelOnTouchOutside(false)
            }
            .noAutoDismiss()
            .customView(
                view = RGBPicker(this).apply {
                    r(tvColor.text.substring(1..3).toInt())
                    g(tvColor.text.substring(5..7).toInt())
                    b(tvColor.text.substring(9..11).toInt())
                }
            )
            .show {
                title(text = "选取颜色")
                positiveButton(text = "确定") {
                    if (it.getCustomView() is RGBPicker) {
                        val r = (it.getCustomView() as RGBPicker).r()
                        val g = (it.getCustomView() as RGBPicker).g()
                        val b = (it.getCustomView() as RGBPicker).b()
                        it.dismiss()

                        listener.invoke(r, g, b)
                    }
                }
            }
    }

    private fun changeIntToString(i: Int) = when {
        i < 10 -> "00$i"
        i < 100 -> "0$i"
        else -> "$i"
    }

    private fun clearHint() {
        tvHint.text = ""
    }

    private fun connectBoard(boardSize: Int = 19) {
        boardConnector = BoardConnector.Builder(this)
            .debug(true)
            .connectListener(this)
            .dataListener(this)
            .build()
        boardConnector.connect(boardSize)
    }

    private fun state(isConnect: Boolean) {
        btnDisconnect.isEnabled = isConnect
        btnAllChess.isEnabled = isConnect
        btnBlackLamp.isEnabled = isConnect
        btnWhiteLamp.isEnabled = isConnect
        btnLamp.isEnabled = isConnect
        btnWarning.isEnabled = isConnect
        switchAutoGetData.isEnabled = isConnect
        cbUIShow.isEnabled = isConnect
    }

    override fun onBoardDisConnect() {
        runOnUiThread {
            toast("电子棋盘断开连接")
            btnConnect.isEnabled = true
            state(false)
        }
    }

    override fun onConnectBoardFail() {
        runOnUiThread {
            toast("电子棋盘连接失败")
            btnConnect.isEnabled = true
            state(false)
        }
    }

    override fun onConnectBoardSuccess() {
        runOnUiThread {
            toast("电子棋盘连接成功")
            boardConnector.write(BoardProtocol.autoSendAllChess(false))
            btnConnect.isEnabled = false
            state(true)
        }
    }

    override fun onReadData(data: String) {
        runOnUiThread {
            tvHint.text = "返回信息：\n $data"

            var command = ""
            var cmdData = ""
            if (data.endsWith("#") && data.lastIndexOf("~") <= 0) {
                for (i in commandhead.indices) {
                    if (data.startsWith(commandhead[i])) {
                        command = data.substring(0, START_LENGTH)
                        cmdData = data.substring(START_LENGTH, data.length - END_LENGTH)
                        break
                    } else if (data.startsWith(defaultcommandhead[i])) {
                        command = data.substring(0, DEF_START_LENGTH)
                        cmdData = data.substring(DEF_START_LENGTH, data.length - END_LENGTH)
                        break
                    }
                }

                when (command) {
                    "~$BKY", BKY -> lampBlack.light()
                    "~$WKY", WKY -> lampWhite.light()
                    "~$SDA", SDA -> {
                        tileView.data = exChange(cmdData)
                    }
                }
            }
        }
    }

    //适应棋盘和横屏屏幕方向
    private fun exChange(cmdData: String): String {
        val a = Array<CharArray>(boardSize) {
            CharArray(boardSize)
        }

        for (i in cmdData.indices) {//0..len -1
            a[i / boardSize][boardSize - i % boardSize - 1] = cmdData[i]
        }

        var data = ""
        for (i in 0 until boardSize) {
            for (j in 0 until boardSize) {
                data += a[i][j]
            }
        }
        return data
    }
}
