package cn.izis.boardmonitor.core;

import cn.izis.boardmonitor.listener.BoardConnectListener;
import cn.izis.boardmonitor.listener.BoardDataListener;
import tw.com.prolific.pl2303multilib.PL2303MultiLib;

public interface BoardConnectorBuilder<B extends BoardConnectorBuilder,R extends BoardConnector> {

    /**
     * 是否调试模式
     */
    B debug(boolean debug);

    /**
     * 设置向底层发送数据的发送间隔 单位ms  默认80
     */
    B delayTime(int time);

    /**
     * 设置波特率 默认B115200
     */
    B baudRate(PL2303MultiLib.BaudRate baudRate);

    /**
     * 设置数据位 默认D8
     */
    B dataBits(PL2303MultiLib.DataBits dataBits);

    /**
     *   默认NONE
     */
    B parity(PL2303MultiLib.Parity parity);

    /**
     * 停止位  默认S1
     */
    B stopBits(PL2303MultiLib.StopBits stopBits);

    /**
     * 流量控制  默认XONXOFF
     */
    B flowControl(PL2303MultiLib.FlowControl flowControl);

    /**
     * 棋盘连接监听器
     */
    B receiveType(BoardConnector.ReceiveType receiveType);

    /**
     * 棋盘连接监听器
     */
    B connectListener(BoardConnectListener listener);

    /**
     * 棋盘数据监听器
     */
    B dataListener(BoardDataListener listener);

    R build();
}
