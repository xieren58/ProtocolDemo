package com.lxf.demo

import android.content.Context
import android.view.View
import android.widget.Toast

const val GET_ALL_DATA: String = "~REQ#"
const val BLACK_LAMP: String = "~LED11#"
const val WHITE_LAMP: String = "~LED21#"
const val ALL_OPEN: String = "~FLL#"
const val ALL_CLOSE: String = "~RGC#"
const val FENG_MING_QI: String = "~AWO#"
const val ADJ: String = "~ADJ#"
const val RETURN_SDA: String = "~CTS1#"
const val NO_RETURN_SDA: String = "~CTS0#"
const val REQUEST_ALL_DATA: String = "~STA#"
const val STA: String = "STA"
const val SDA: String = "SDA"
const val DAT: String = "DAT"
const val BKY: String = "BKY"
const val WKY: String = "WKY"
/**
 * 电子棋盘更新程序向下发指令的间隔
 */
const val BOARD_DELAY: Long = 200

val commandhead = arrayOf("~$STA", // 命令：主动请求全盘数据，返回~STAstasucceed# 和
    "~$SDA", // 命令：收到下位机发送的 全盘棋子信息
    "~$DAT", // 命令：
    "~$BKY", // 命令：黑方拍钟
    "~$WKY" // 命令：白方拍钟
)
val defaultcommandhead = arrayOf(STA, SDA, DAT, BKY, WKY)
/**
 * 数据头长度
 */
const val START_LENGTH = 4
/**
 * 丢失"~"后数据头长度
 */
const val DEF_START_LENGTH = 3
/**
 * 数据尾长度
 */
const val END_LENGTH = 1



fun Context.toast(msg: String, time: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, msg, time).show()
}

fun View.postDelayed(delay: Long, action: () -> Unit) = postDelayed(action, delay)
